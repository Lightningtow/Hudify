package lightningtow.hudify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lightningtow.hudify.integrations.CustomhudIntegration;
import lightningtow.hudify.util.SpotifyData;
import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import static lightningtow.hudify.util.SpotifyData.*;
//import static lightningtow.hudify.HudifyConfig.db;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HudifyMain implements ClientModInitializer
{

	public static final Boolean db = true;
	//<editor-fold desc="data and util funcs">

	public static final String MOD_ID = "hudify";
	public static final String MOD_DISPLAY_NAME = "Hudify";

	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	public static void dump (String source) {
		if (db) LogThis(Level.INFO,String.join(", ",
				"dump from " + source + " - Status Code " + sp_status_code, "(" + sp_progress + " / " + sp_duration + ")",
				sp_track, sp_first_artist, "(" + sp_artists + ")", sp_context_type)
		);
	}
	private static void tick_message() {
		if (sp_msg_time_rem > 0)
			sp_msg_time_rem -= 1;

		else
			set_sp_message("");
	}
	public static void send_message (String msg, int msg_dur) {
		sp_msg_time_rem = msg_dur;
		set_sp_message(msg);
	}
//	public static void truncate() { // chop the ends off variables after a user-specified number of characters
//		int len = HudifyConfig.truncate_length;
////		if (db) Log(Level.INFO,"truncating to: {}", sp_artists.substring(0, len));
//
//		sp_artists = (sp_artists.length() > len) ? sp_artists.substring(0, len).trim() + "..." : sp_artists;
//		sp_track = (sp_track.length() > len) ? sp_track.substring(0, len).trim() + "..." : sp_track;
//		sp_fancy_track = (sp_track.length() > len) ? sp_track.substring(0, len).trim() + "..." : sp_track;
//		sp_album = (sp_album.length() > len) ? sp_album.substring(0, len).trim() + "..." : sp_album;
//		// make this only run once. use before/after variables to detect if track URI changed
//		// attempt to make the outputted variable cleaner, truncating at word breaks and removing trailing commas
//	}

//	public enum Level { INFO, ERROR, TRACE, DEBUG, LOG, FATAL, WARN, OFF, ALL }
	public static void LogThis(org.apache.logging.log4j.Level lvl, String msg) {
		org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MOD_DISPLAY_NAME);
		msg = "(Hudify) " + msg;

		switch (lvl.toString()) {
			case "INFO": LOGGER.info(msg);   break;
			case "ERROR": LOGGER.error(msg); break;

			case "FATAL": LOGGER.fatal(msg); break;
			case "WARN": LOGGER.warn(msg);   break;
			case "DEBUG": LOGGER.debug(msg); break;
			case "TRACE": LOGGER.trace(msg); break;

			default: msg += "(invalid Level for log message? hit default switchcase"; LOGGER.info(msg);  break;
		}
	}

	//</editor-fold> data and util funcs

	@Override
	public void onInitializeClient()
	{

		try {
			LogThis(Level.INFO,"Beginning integration with CustomHud");
			CustomhudIntegration.initCustomhud();
			LogThis(Level.INFO,"Successfully integrated with CustomHud");
		}
		catch (Exception e) {
			LogThis(Level.ERROR,"Error integrating with CustomHud: " + e);
		}
//		File authFile = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "HudifyTokens.json");
//		HudifyConfig.init(MOD_ID, HudifyConfig.class);
		SpotifyUtil.initialize();
		registerKeyBindings();
		updatePlaybackInfo();
		SpotifyData.resetData(true);

		Thread requestThread = new Thread( () -> {
		boolean worldNullAlreadyReset = false;
			while (true) {
				try {
					Thread.sleep(1000);
					if (db) HudifyMain.dump("Main loop");
					UpdateMaps();
//					if (sp_needs_refresh) {
//						updatePlaybackInfo();
//						sp_needs_refresh = false;
//					}

					if (MinecraftClient.getInstance().world == null) {
						Thread.sleep(3000); // you can just leave this as one second cause it doesn't poll anything
//						sp_progress = 0;
//						sp_duration = -1;
						if (!worldNullAlreadyReset) { // only
							SpotifyData.resetData(true);
							worldNullAlreadyReset = true;
						}
					}
					else  {
						worldNullAlreadyReset = false;
						tick_message();
						if (sp_status_code == 204) { // No Content - The request has succeeded but returns no message body.
//							sp_progress = 0; // dont reset progress and duration here, it breaks it when app is paused
							SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
							SpotifyData.resetData();
						}
						if (sp_progress > sp_duration) { // 204 when app is closed, doesnt immediately go away when app opened
							sp_progress = 0;
							updatePlaybackInfo();
							if (sp_status_code == 429) { // rate limited
								// approximately 180 calls per minute without throwing 429, ~3 calls per second
								LogThis(Level.ERROR, "RATE LIMITED============================================================");
								Thread.sleep(3000);
							}
//                        } else if (data[0].equals("Reset")) {
								// getPlaybackInfo returns this if connection reset
//                            Log(Level.ERROR,"Reset condition, maintaining HUD until reset"); // was level info and from blockiy

						} else if (sp_is_playing) {
							sp_progress += 1;
						}
					}

				} catch (InterruptedException e) {
                    LogThis(Level.ERROR,"error in main loop: " + Arrays.toString(e.getStackTrace()));
				}
			}
		});
		requestThread.setName("Spotify Thread");
		requestThread.start();


	}

	public static void updatePlaybackInfo()
	{
		// make the api call to get playback info
		String dump_msg = "getPlaybackInfo";
		try
		{
			HttpResponse<String> playbackResponse =
					SpotifyUtil.getClient().send(SpotifyUtil.getPlaybackRequest(), HttpResponse.BodyHandlers.ofString());
			// https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback

			sp_status_code = playbackResponse.statusCode();

//            Log(Level.INFO,"getPlaybackInfo - status code: " + playbackResponse.statusCode());
			// app closed returns 204
//			if (playbackResponse.statusCode() == 204) { }  // no content - returned when app is closed

			if (playbackResponse.statusCode() == 429) return; // rate limited
			else if (playbackResponse.statusCode() == 401) /* unauthorized */ {
				if (!SpotifyUtil.refreshAccessToken()) sp_is_authorized = false;
				////	 SpotifyUtil.isAuthorized doesn't get used anywhere, just the value set
			}
			else if (playbackResponse.statusCode() == 200) // OK - The request has succeeded
			{
				JsonObject json = (JsonObject) JsonParser.parseString(playbackResponse.body());
				// the `json.get("progress_ms")` is incorrect after pausing then resuming


				//<editor-fold desc="update individual variables">

				dump_msg += " " + json.get("progress_ms") + " / " + json.get("item").getAsJsonObject().get("duration_ms");

				sp_is_podcast = (json.get("currently_playing_type").getAsString().equals("episode"));

				sp_progress = (json.get("progress_ms").getAsInt() / 1000);
				sp_duration = (json.get("item").getAsJsonObject().get("duration_ms").getAsInt() / 1000);

				sp_shuffle_enabled = json.get("shuffle_state").getAsBoolean();
				sp_repeat_state = json.get("repeat_state").getAsString(); // if repeat is "context" change to "all"
				/* repeat */  if (sp_repeat_state.equals("context")) sp_repeat_state = "all"; // else leave it

				sp_track = json.get("item").getAsJsonObject().get("name").getAsString();
//				if (HudifyConfig.scrub_name) sp_track = SpotifyUtil.scrub(sp_track);
				sp_fancy_track = SpotifyUtil.scrub(sp_track);

				sp_device_id = json.get("device").getAsJsonObject().get("id").getAsString();
				sp_device_is_active = json.get("device").getAsJsonObject().get("is_active").getAsBoolean();
				sp_device_name = json.get("device").getAsJsonObject().get("name").getAsString();

				sp_is_playing = json.get("is_playing").getAsBoolean();

				/* get context */ JsonObject contextJson = json.get("context").getAsJsonObject();
				/* context type */ sp_context_type = contextJson.get("type").getAsString();
				/* context name */ sp_prev_context = sp_context_name;
				if (!sp_prev_context_uri.equals(contextJson.get("uri").getAsString())) { // if context changed
//                    Log(Level.INFO,"contexts do NOT match, updating context");
					LogThis(Level.INFO,"type: " + sp_context_type + ", uris "
							+ sp_prev_context_uri + " / " + contextJson.get("uri").getAsString());
					sp_prev_context_uri = contextJson.get("uri").getAsString();
					switch (sp_context_type) {
						case "album":
							sp_context_name = sp_album;  break;
						case "show":
							sp_context_name = sp_artists;  break;
						case "artist":
						case "playlist":
							EXECUTOR_SERVICE.execute(() -> { // this does this single func async
								// href is the exact url needed to get the string like
								// "https://api.spotify.com/v1/playlists/3PXFZxy8QdBmvFHCYyErw3"
								JsonObject fullContextJson =
										SpotifyUtil.apiRequest(SpotifyUtil.reqType.GET, contextJson.get("href").getAsString());
								if (fullContextJson == null) sp_context_name = "";
                                else sp_context_name = fullContextJson.get("name").getAsString().replaceAll("\"", "");
					});
					}
				}

				if (sp_is_podcast) {
					String show = json.get("item").getAsJsonObject().get("show").getAsJsonObject().get("name").getAsString();
					sp_artists = show;
					sp_first_artist = show;
					sp_album = "";

					HudifyMain.dump(dump_msg);
					return;
				}


				// artist info
				JsonArray artistArray = json.get("item").getAsJsonObject().get("artists").getAsJsonArray();
				StringBuilder artistString = new StringBuilder(artistArray.get(0).getAsJsonObject().get("name").getAsString());
				sp_first_artist = artistString.toString();
				for (int i = 1; i < artistArray.size(); i++) /* skip the first artist */ {
					artistString.append(", ").append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
				}
				sp_artists = artistString.toString();
				// artist info


//		sp_album = sp_is_podcast ? "" : json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
				sp_album = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();

				//</editor-fold> update individual variables


				SpotifyData.UpdateMaps();

			} // if response successful

//			if (HudifyConfig.truncate_length != -1) truncate();

		} catch (Exception e) {
			LogThis(Level.ERROR,"exception caught in updatePlaybackInfo(): " + e.getMessage());
//            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
//            {
//                Log(Level.INFO,"Resetting connection and retrying info get...");
////                results[0] = "Reset";
//            }
//            else
		}
		HudifyMain.dump(dump_msg);
//        return;
	}





	//<editor-fold desc="register keybindings">
	public static void registerKeyBindings() {
		registerRefreshKey();
		registerToggleKey();
		registerNextKey();
		registerPrevKey();
	}
	private static boolean refreshKeyPrevState = false;
	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	// todo can i abstract out the below into a single function with arguments
//	private static void registerKeyBinding(String name, Boolean prevStateVar, () -> test ) {	}

	private static void registerRefreshKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.refresh", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (newKey.wasPressed() && !refreshKeyPrevState) {
//				if (sp_is_authorized) { // no need to check authorization ; apiRequest already does
				if (db) LogThis(Level.INFO,"Refresh key pressed");
				updatePlaybackInfo();
//				} else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
			}
			refreshKeyPrevState = newKey.wasPressed();
		});
	}
	private static void registerToggleKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (newKey.wasPressed() && !toggleKeyPrevState) {
//				if (sp_is_authorized) {
					if (db) LogThis(Level.INFO,"Toggle key pressed");
					SpotifyUtil.togglePlayPause();
//				} else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
			}
			toggleKeyPrevState = newKey.wasPressed();
		});
	}
	private static void registerNextKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
		if (newKey.wasPressed() && !nextKeyPrevState) {
//			if (sp_is_authorized) {
				if (db) LogThis(Level.INFO,"Next key pressed");
				SpotifyUtil.nextSong();
//			} else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }

		}
			nextKeyPrevState = newKey.wasPressed();
		});
	}
	private static void registerPrevKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (newKey.wasPressed() && !prevKeyPrevState) {
//				if (sp_is_authorized) {
					if (db) LogThis(Level.INFO,"Prev key pressed");
					SpotifyUtil.prevSong();
//				} else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
			}
			prevKeyPrevState = newKey.wasPressed();
		});
	}
	//</editor-fold> register keybindings

}
