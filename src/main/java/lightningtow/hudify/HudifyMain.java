package lightningtow.hudify;

import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class HudifyMain implements ClientModInitializer
{
	public static final String MOD_ID = "Hudify";
	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	final public static boolean db = true; // toggle debug messages. very spammy!
	// see this link for unofficial estimates of ratelimits
	// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931

	// api response code descriptions
	// https://developer.spotify.com/documentation/web-api/concepts/api-calls


	/** the single source of truth for current Spotify state **/
	public static int sp_status_code = 123456;
	public static String sp_track = ""; // track or episode name
	public static String sp_artists = ""; // all artists as one string
	public static String sp_first_artist = ""; // first artist listed. if one artist or podcast, identical to `artists`
	public static String sp_album = "";
	public static String sp_context_type = ""; // "artist", "playlist", "album", "show".
	public static String sp_context_name = ""; // name of artist, playlist etc
	public static String sp_media_type = ""; // "track" or "episode"
	public static String sp_repeat_state = "";
	public static Boolean sp_shuffle_state = false;
	public static int sp_progress;
	public static int sp_duration;
	/** the single source of truth for current Spotify state **/



	public static void dump (String source) {
		if (db) LOGGER.info(String.join(", ",
				"dump from " + source + " - Status Code " + sp_status_code, "(" + sp_progress + " / " + sp_duration + ")",
				sp_track, sp_first_artist, "(" + sp_artists + ")", sp_context_type)
		);
	}

	public static void send_message (String message, int duration) { // todo probably make this a customhud variable instead/as well
		// be sure to use translatable text! This accepts a string rather than translatableText,
		// cause otherwise it doesn't check en_us for the string
		assert MinecraftClient.getInstance().player != null;
		MinecraftClient.getInstance().player.sendMessage(Text.of(message));
	}

	@Override
	public void onInitializeClient()
	{
		//	HudifyConfig.init("Hudify", lightningtow.hudify.util.HudifyConfig.class);

		LOGGER.info("initializing main loop"); //info

		Thread requestThread = new Thread( () -> {
			while (true) {
				try {
					Thread.sleep(800);
					if (MinecraftClient.getInstance().world != null) {
//                        Thread.sleep(1000);
						SpotifyUtil.updatePlaybackInfo();

// 204 when app is closed, doesnt immediately go away when app opened
						if (sp_status_code == 204) { // No Content - The request has succeeded but returns no message body.
							sp_progress = 0;
							sp_duration = -1;
							SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
						} else if (sp_status_code == 429) { // rate limited
							// approximately 180 calls per minute without throwing 429, ~3 calls per second
							LOGGER.error("RATE LIMITED============================================================");
							Thread.sleep(3000);
//                        } else if (data[0].equals("Reset")) {
							// getPlaybackInfo returns this if connection reset
//                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was level info and from blockiy
						}
					} else { //when world is null
						Thread.sleep(3000);
						sp_progress = 0;
						sp_duration = -1;
//						LOGGER.error("world null, progress + duration updated in refreshActiveSession"); // spammy af

					}
				} catch (InterruptedException e) {
					LOGGER.error("error in main loop: " + Arrays.toString(e.getStackTrace()));
				}
			}
		});
		requestThread.setName("Spotify Thread"); //spotify thread
		requestThread.start();
		SpotifyUtil.initialize();
		registerKeyBindings();

	}
	//<editor-fold desc="register keybindings">
	public static void registerKeyBindings() {
		registerToggleKey();
		registerNextKey();
		registerPrevKey();
	}
	private static void registerToggleKey() {
		KeyBinding toggleKey = new KeyBinding("hudify.key.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(toggleKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleKey.wasPressed() && !toggleKeyPrevState) {
				if (SpotifyUtil.isAuthorized()) {
					if (db) LOGGER.info("Toggle key pressed!"); //info
					SpotifyUtil.togglePlayPause();
				}
				else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
			}
			toggleKeyPrevState = toggleKey.wasPressed();
		});
	}
	private static void registerNextKey() {
		KeyBinding nextKey = new KeyBinding("hudify.key.next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(nextKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
		if (nextKey.wasPressed() && !nextKeyPrevState) {
			if (SpotifyUtil.isAuthorized()) {
				if (db) LOGGER.info("Next key pressed");
				SpotifyUtil.nextSong();
			}
			else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }

		}
			nextKeyPrevState = nextKey.wasPressed();
		});
	}
	private static void registerPrevKey() {
		KeyBinding prevKey = new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(prevKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (prevKey.wasPressed() && !prevKeyPrevState) {
				if (SpotifyUtil.isAuthorized()) {
					if (db) LOGGER.info("Prev key pressed");
					SpotifyUtil.prevSong();
				}
				else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
			}
			prevKeyPrevState = prevKey.wasPressed();
		});
	}
	//</editor-fold> register keybindings

}
