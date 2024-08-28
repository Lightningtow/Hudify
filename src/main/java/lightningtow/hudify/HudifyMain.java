package lightningtow.hudify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minenash.customhud.CustomHud;
import lightningtow.hudify.integrations.CustomhudIntegration;
import lightningtow.hudify.util.SpotifyData;
import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.PngMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import static lightningtow.hudify.util.SpotifyData.*;
import static lightningtow.hudify.HudifyConfig.db;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HudifyMain implements ClientModInitializer
{
	public static final String MOD_ID = "hudify";
	public static final String MOD_DISPLAY_NAME = "Hudify";

	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
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

	public static String tryTruncate(String victim) {
		int len = HudifyConfig.truncate_length;
		if (len == -1) return victim;

		String thing = victim;
		thing = (victim.length() > len) ? victim.substring(0, len).trim() + "..." : victim;
		return thing;
	}

//	public static void truncate() { // chop the ends off variables after a user-specified number of characters
//		int len = HudifyConfig.truncate_length;
////		if (db) Log(Level.INFO,"truncati	ng to: {}", sp_artists.substring(0, len));
//
//		sp_artists = (sp_artists.length() > len) ? sp_artists.substring(0, len).trim() + "..." : sp_artists;
//		sp_track = (sp_track.length() > len) ? sp_track.substring(0, len).trim() + "..." : sp_track;
//		LogThis(Level.INFO, sp_track);
//		sp_fancy_track = (sp_track.length() > len) ? sp_track.substring(0, len).trim() + "..." : sp_track;
//		sp_album = (sp_album.length() > len) ? sp_album.substring(0, len).trim() + "..." : sp_album;
//		// todo make this only run once. use before/after variables to detect if track URI changed
//		// todo attempt to make the outputted variable cleaner, truncating at word breaks and removing trailing commas
//	}

	public static void LogThis(Level lvl, String msg) {
		msg = "(" + MOD_DISPLAY_NAME + ") " + msg;
		LogManager.getLogger(MOD_DISPLAY_NAME).log(lvl, msg);
	}
	static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

	public static void getAlbumArt() {
		final MinecraftClient client = MinecraftClient.getInstance();



//        if (link == null || link.isEmpty()) link = "https://i.scdn.co/image/ab67616d00001e023ce4f9e5bc032ff88268edba";

		try {

			InputStream in;
			if (sp_album_art_link == null || sp_album_art_link.isEmpty()) in = new URL("https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228").openStream();
			else in = new URL(sp_album_art_link).openStream();
//            InputStream in = new URL("https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228").openStream();
//            InputStream in = new URL(link).openStream();
			byte[] byteArray = in.readAllBytes();

//						byte[] byteArray = byteArrayOutputStream.toByteArray();


			// read a jpeg from a inputFile
//			BufferedImage bufferedImage = ImageIO.read(in);
//			NativeImage img = null;
			NativeImage img = NativeImage.read(in);

			NativeImageBackedTexture image = new NativeImageBackedTexture(NativeImage.read(byteArray));

			// https://stackoverflow.com/questions/2716596/how-to-put-data-from-an-outputstream-into-a-bytebuffer

			ImageIO.write(ImageIO.read(in), "PNG", byteArrayOutputStream);
//			ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
//			ImageIO.write(ImageIO.read(in), "PNG", thing);

//            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
//			image = new NativeImageBackedTexture(NativeImage.read(ImageIO.write));

//			image = new NativeImageBackedTexture(NativeImage.read(byteArrayOutputStream.toByteArray()));
//			img = image.getImage();

//            image = new NativeImageBackedTexture(NativeImage.read(resultingBytes));
//            client.getTextureManager().registerTexture(new Identifier("test"), image);
			// todo wait i got it to display teh bird, just need to figure out what i did there

			Identifier newtexture = new Identifier("textures/hudify/albumart.png");

//			client.getTextureManager().registerTexture(newtexture, new NativeImageBackedTexture(NativeImage.read(byteArrayOutputStream.toByteArray())));
			client.getTextureManager().registerTexture(newtexture, image);


			in.close();
			byteArrayOutputStream.close();
//            Optional<Resource> resource = client.getResourceManager().getResource(Identifier.tryParse("textures/item/albumart"));
//            if (resource.isPresent())
//                img = NativeImage.read(resource.get().getInputStream());
// this^ doesnt cause Java.lang.NullPointerException: Cannot invoke "minecraft.util.Identifier.getNamespace()" because "identifier" is null
		} catch (IOException e) {
			LogThis(Level.ERROR, "error in try(InputStream) in CustomHudExtender: " + e.getMessage());
//            e.printStackTrace();
		}
//		NativeImageBackedTexture finalImage = image;

//		try {
//			Optional<Resource> resource = client.getResourceManager().getResource(texture);
//			if (resource.isPresent())
//				img = NativeImage.read(resource.get().getInputStream());
//		}
//		catch (IOException e) { CustomHud.LOGGER.catching(e); }
/*
        https://i.scdn.co/image/ab67616d00001e023ce4f9e5bc032ff88268edba
        https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228
*/


	}


	@Override
	public void onInitializeClient()
	{

		try {
			CustomhudIntegration.initCustomhud();
			LogThis(Level.INFO,"Successfully integrated with CustomHud");
		}
		catch (Exception e) { LogThis(Level.ERROR,"Error integrating with CustomHud: " + e); }
//		File authFile = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "HudifyTokens.json");
		HudifyConfig.init(MOD_ID, HudifyConfig.class); //todo uncomment me
		SpotifyUtil.initialize();

		SpotifyUtil.authorize(); // nothing happens if you attempt to auth and fail


//		if (SpotifyUtil.get_client_id().isEmpty()) {
//			client_id_empty_on_startup = true;
//		}
//		//info
//		if (!SpotifyData.sp_is_authorized && !(SpotifyUtil.get_client_id().isEmpty())) { // dont init if empty lol
//			LogThis(Level.ERROR, "authorizing from initializeClient");
//
//			LogThis(Level.INFO,"initializing client. Spotify is not authorized, initiating authorization progress ");
////			Util.getOperatingSystem().open(SpotifyUtil.authorize());
//			SpotifyUtil.authorize();
//
//		}
		resetData();

//		else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
		Thread requestThread = new Thread( () -> {

			while (true) {
				try {
					Thread.sleep(HudifyConfig.poll_rate);

//					if (client_id_empty_on_startup && !(SpotifyUtil.get_client_id().isEmpty())) {
//						LogThis(Level.ERROR, "authorizing from main loop");
//						client_id_empty_on_startup = false;
//						SpotifyUtil.authorize();
////						Util.getOperatingSystem().open(SpotifyUtil.authorize());
//					}


					if (MinecraftClient.getInstance().world == null) {
						Thread.sleep(HudifyConfig.inactive_poll_rate * 1000L);
						sp_progress = 0;
						sp_duration = -1;
					}
					else {
//						if (HudifyConfig.refresh_client_auth) {
//							HudifyConfig.refresh_client_auth = false;
//							Util.getOperatingSystem().open(SpotifyUtil.authorize());
//						}

						updatePlaybackInfo();
						tick_message();
						// 204 when app is closed, doesnt immediately go away when app opened
						if (sp_status_code == 204) { // No Content - The request has succeeded but returns no message body.
//							sp_progress = 0; // dont reset progress and duration here, it breaks it when app is paused
							SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
							Thread.sleep(HudifyConfig.inactive_poll_rate * 1000L);

						} else if (sp_status_code == 429) { // rate limited
							// approximately 180 calls per minute without throwing 429, ~3 calls per second
							LogThis(Level.ERROR,"RATE LIMITED============================================================");
							Thread.sleep(3000);
//                        } else if (data[0].equals("Reset")) {
							// getPlaybackInfo returns this if connection reset
//                            Log(Level.ERROR,"Reset condition, maintaining HUD until reset"); // was level info and from blockiy
						}

					}
				} catch (InterruptedException e) {
                    LogThis(Level.ERROR,"error in main loop: " + Arrays.toString(e.getStackTrace()));
				}
			}
		});
		requestThread.setName("Spotify Thread"); //spotify thread
		requestThread.start();
		registerKeyBindings();

	}

	public static void updatePlaybackInfo()
	{
		String dump_msg = "getPlaybackInfo";
		try
		{

			HttpResponse<String> playbackResponse =
					SpotifyUtil.getClient().send(SpotifyUtil.getPlaybackRequest(), HttpResponse.BodyHandlers.ofString());
			// https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback

			sp_status_code = playbackResponse.statusCode();
//            Log(Level.INFO,"getPlaybackInfo - status code: " + playbackResponse.statusCode());
			// app closed returns 204
//			if (playbackResponse.statusCode() == 204) { // no content - returned when app is closed
//			}

			if (playbackResponse.statusCode() == 429) return; // rate limited
			else if (playbackResponse.statusCode() == 401) /* unauthorized */ {
				if (!SpotifyUtil.refreshAccessToken()) sp_is_authorized = false;
				////	 SpotifyUtil.isAuthorized doesn't get used anywhere, just the value set
			}
			else if (playbackResponse.statusCode() == 200) // OK - The request has succeeded
			{
				JsonObject json = (JsonObject) JsonParser.parseString(playbackResponse.body());
				// the `json.get("progress_ms")` is incorrect after pausing then resuming
				boolean truncating = (HudifyConfig.truncate_length != -1);
				int len = HudifyConfig.truncate_length;

                dump_msg += " " + json.get("progress_ms") + " / " + json.get("item").getAsJsonObject().get("duration_ms");

				sp_is_podcast = (json.get("currently_playing_type").getAsString().equals("episode"));

				sp_progress = (json.get("progress_ms").getAsInt() / 1000);
				sp_duration = (json.get("item").getAsJsonObject().get("duration_ms").getAsInt() / 1000);

				sp_shuffle_enabled = json.get("shuffle_state").getAsBoolean();
				sp_repeat_state = json.get("repeat_state").getAsString(); // if repeat is "context" change to "all"
				/* repeat */  if (sp_repeat_state.equals("context")) sp_repeat_state = "all"; // else leave it

				sp_track = tryTruncate(json.get("item").getAsJsonObject().get("name").getAsString());
				sp_fancy_track = tryTruncate(HudifyConfig.scrub_name ? SpotifyUtil.scrub(sp_track) : sp_track);

//				sp_track = json.get("item").getAsJsonObject().get("name").getAsString();
//				sp_fancy_track = HudifyConfig.scrub_name ? SpotifyUtil.scrub(sp_track) : sp_track;

//				sp_icon_link = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").toString();
//[20:24:01] [Spotify Thread/INFO] (Hudify) (Hudify) [{"height":640,"url":"https://i.scdn.co/image/ab67616d0000b273cfc4b1939aba562fc97159c5","width":640},{"height":300,"url":"https://i.scdn.co/image/ab67616d00001e02cfc4b1939aba562fc97159c5","width":300},{"height":64,"url":"https://i.scdn.co/image/ab67616d00004851cfc4b1939aba562fc97159c5","width":64}]
				//sp_icon_link = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray().get(0).toString();
// [20:27:10] [Spotify Thread/INFO] (Hudify) (Hudify) {"height":640,"url":"https://i.scdn.co/image/ab67616d0000b2733b4362147e2b0595d512033e","width":640}
				sp_album_art_link = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray().get(1).getAsJsonObject().get("url").getAsString();

				getAlbumArt();
				LogThis(Level.INFO, "album art link: " + sp_album_art_link);

				sp_device_id = json.get("device").getAsJsonObject().get("id").getAsString();
				sp_device_is_active = json.get("device").getAsJsonObject().get("is_active").getAsBoolean();
				sp_device_name = json.get("device").getAsJsonObject().get("name").getAsString();

				sp_is_playing = json.get("is_playing").getAsBoolean();

				/* get context */ JsonObject contextJson = json.get("context").getAsJsonObject();
				/* context type */ sp_context_type = contextJson.get("type").getAsString();
				/* context name */ sp_prev_context = sp_context_name;
				if (!sp_prev_context_uri.equals(contextJson.get("uri").getAsString()) || sp_prev_context.isEmpty()) {
					// if context changed or is empty
//                    Log(Level.INFO,"contexts do NOT match, updating context");
					if (db) LogThis(Level.INFO,"type: " + sp_context_type + ", uris "
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


				/** sp_artists + sp_first_artist **/
				JsonArray artistArray = json.get("item").getAsJsonObject().get("artists").getAsJsonArray();
				StringBuilder artistString = new StringBuilder(artistArray.get(0).getAsJsonObject().get("name").getAsString());
				sp_first_artist = artistString.toString();
				for (int i = 1; i < artistArray.size(); i++) /* skip the first artist */ {
					artistString.append(", ").append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
				}
				sp_artists = tryTruncate(artistString.toString());
				/** sp_artists + sp_first_artist **/


//		sp_album = sp_is_podcast ? "" : json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();
				sp_album = tryTruncate(json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString());

				UpdateMaps();

			} // if response successful

//			if (HudifyConfig.truncate_length != -1) truncate();

		} catch (Exception e) {
			LogThis(Level.ERROR,"exception caught in getPlaybackInfo(): " + e.getMessage());
//            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
//            { Log(Level.INFO,"Resetting connection and retrying info get...");
////                results[0] = "Reset"; }
		}
		HudifyMain.dump(dump_msg);
//        return;
	}




	//<editor-fold desc="register keybindings">
	public static void registerKeyBindings() {
//		registerRefreshKey();
		registerToggleKey();
		registerNextKey();
		registerPrevKey();
	}
	// aint worth abstracting out
//	private static void registerRefreshKey() {
//		KeyBinding newKey = new KeyBinding("hudify.key.refresh", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MOD_DISPLAY_NAME);
//		KeyBindingHelper.registerKeyBinding(newKey);
//
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			if (newKey.wasPressed() && !refreshKeyPrevState) {
//				if (sp_is_authorized) { // todo this isnt really necessary anymore delete later?
//					updatePlaybackInfo();
//				}
//				else SpotifyUtil.authorize();
//
////				else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
//			}
//			refreshKeyPrevState = newKey.wasPressed();
//		});
//	}
	private static void registerToggleKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MOD_DISPLAY_NAME);
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (newKey.wasPressed() && !toggleKeyPrevState) {
				if (sp_is_authorized) {
					if (db) LogThis(Level.INFO,"Toggle key pressed"); //info
					SpotifyUtil.togglePlayPause();
				}
				else SpotifyUtil.authorize();
			}
			toggleKeyPrevState = newKey.wasPressed();
		});
	}
	private static void registerNextKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MOD_DISPLAY_NAME);
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
		if (newKey.wasPressed() && !nextKeyPrevState) {
			if (sp_is_authorized) {
				if (db) LogThis(Level.INFO,"Next key pressed");
				SpotifyUtil.nextSong();
			}
			else SpotifyUtil.authorize();


		}
			nextKeyPrevState = newKey.wasPressed();
		});
	}
	private static void registerPrevKey() {
		KeyBinding newKey = new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MOD_DISPLAY_NAME);
		KeyBindingHelper.registerKeyBinding(newKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (newKey.wasPressed() && !prevKeyPrevState) {
				if (sp_is_authorized) {
					if (db) LogThis(Level.INFO,"Prev key pressed");
					SpotifyUtil.prevSong();
				}
				else SpotifyUtil.authorize();

			}
			prevKeyPrevState = newKey.wasPressed();
		});
	}
	//</editor-fold> register keybindings

}
