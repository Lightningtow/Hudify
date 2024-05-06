package lightningtow.hudify;

import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class HudifyMain implements ClientModInitializer
{
	public static final String MOD_ID = "Hudify";
	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	private static Thread requestThread;

	public static int progress;
	public static int duration;
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	// see this link for unofficial estimates of ratelimits
	// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931

	// api response code descriptions
	// https://developer.spotify.com/documentation/web-api/concepts/api-calls
	final public static boolean db = true; // toggle debug messages. very spammy!
	public static int status_code = 123456;
	public static String track = "";
	public static String artists = ""; // all artists
	public static String first_artist = ""; // first artist listed. if one artist or podcast, identical to `artists`
	public static String album = "";

	public static String context_type = " "; // "artist", "playlist", "album", "show".
	public static String context_name = " ";

	public static void dump (String source) {
		if (db) LOGGER.info(String.join(", ",
				source + " dump - Status Code " + status_code, "(" + progress + " / " + duration + ")",
				track, first_artist, artists, context_type)
		);
	}
	@Override
	public void onInitializeClient()
	{

		//LOGGER.info("running HudifyMain.onInitialize()"); //info
		LOGGER.info("initializing main loop"); //info

		//	HudifyConfig.init("Hudify", lightningtow.hudify.util.HudifyConfig.class);
		requestThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(800);
                    if (MinecraftClient.getInstance().world != null) {
//						if (duration < progress) {
//                        Thread.sleep(1000);
						SpotifyUtil.updatePlaybackInfo();

// 204 when app is closed, doesnt immediately go away when app opened
                        if (status_code == 204) {
							// No Content - The request has succeeded but returns no message body.
                            SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
//                        } else if (data[0] != null && data[0].equals("Status Code: 429")) { // rate limited
						} else if (status_code == 429) { // rate limited
							// approximately 180 calls per minute without throwing 429, ~3 calls per second
							LOGGER.error("RATE LIMITED============================================================");
                            Thread.sleep(3000);
//                        } else if (data[0] != null && data[0].equals("Reset")) {
							// getPlaybackInfo returns this if it manually hits an error
//                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was level info and from blockiy
                        }

//							}
                    } else { //when world is null
						Thread.sleep(3000);
						progress = 0;
						duration = -1;
//						LOGGER.error("world null, progress + duration updated in refreshActiveSession"); // spammy af

                    }
                } catch (InterruptedException e) {
                    LOGGER.error("error in main loop: " + e);
                    e.printStackTrace();

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
					LOGGER.info("Authorized!"); //info
					SpotifyUtil.playPause();
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
			LOGGER.info("Next Key Pressed");
			SpotifyUtil.nextSong();
		}
			nextKeyPrevState = nextKey.wasPressed();
		});
	}
	private static void registerPrevKey() {
		KeyBinding prevKey = new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify");
		KeyBindingHelper.registerKeyBinding(prevKey);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (prevKey.wasPressed() && !prevKeyPrevState) {
				LOGGER.info("Prev Key Pressed");
				SpotifyUtil.prevSong();
			}
			prevKeyPrevState = prevKey.wasPressed();
		});
	}
	//</editor-fold>



}
