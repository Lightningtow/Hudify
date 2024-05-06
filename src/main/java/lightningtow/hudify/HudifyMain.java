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
//	private boolean forceKeyPrevState = false;
	private static Thread requestThread;
//	private static int progressMS;
//	private static int durationMS;
	public static int progress;
	public static int duration;
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	// see this link for unofficial estimates of ratelimits
	// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931

	// api response code descriptions
	// https://developer.spotify.com/documentation/web-api/concepts/api-calls
//	final public boolean db = true; // toggle debug messages. very spammy!
	final public static boolean db = true; // toggle debug messages. very spammy!
//	public static String[] hudInfo;
	public static int status_code = 123456;
	public static String track = " ";
	public static String artists = " ";
	public static String first_artist = " ";
	public static String context_type = " ";

	public static void dump (String source) {
		if (db) LOGGER.info(String.join(", ",
				source + " dump - Status Code " + status_code, "(" + progress + " / " + duration + ")",
				track, first_artist, artists, context_type )
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
//							if (lightningtow.hudify.HudifyHUD.getDuration() < lightningtow.hudify.HudifyHUD.getProgress()) {
                        //Thread.sleep(1000);
//                        String[] data = SpotifyUtil.getPlaybackInfo();
						SpotifyUtil.updatePlaybackInfo();
//						if(dbs) LOGGER.info("main loop - data[]: " + Arrays.toString(data));
//                        if(dbs) LOGGER.info("main loop - data[0]: " + data[0]);
// 204 when app is closed, doesnt immediately go away when app opened
//						if (data[0] != null && data[0].equals("Status Code: 204")) {

                        if (status_code == 204) {
							// No Content - The request has succeeded but returns no message body.
                            SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
//                        } else if (data[0] != null && data[0].equals("Status Code: 429")) { // rate limited
						} else if (status_code == 429) { // rate limited
							LOGGER.error("RATE LIMITED============================================================");
                            Thread.sleep(3000);
//                        } else if (data[0] != null && data[0].equals("Reset")) {
							// getPlaybackInfo returns this if it manually hits an error
//                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was level info and from blockiy
                        } else { // else it went thru successfully...?
//                            if(db) LOGGER.info("main loop: updating data"); // redundant with the logger call in updateData
//                            HudifyMain.updateData(data); // make this redundant?
                        }
//							} else if (SpotifyUtil.isPlaying()) {
//                        HudifyHUD.setProgress(HudifyHUD.getProgress() + 1000);
//						progressMS = progressMS + 1000;
						// this runs regardless of whether it errored out?
//						progress += 1;
                            //LOGGER.info("progress updated in main loop to: " + lightningtow.hudify.HudifyHUD.getProgress() + 1000); // spammy but very helpful
//                        if(db) LOGGER.info("progress updated in main loop to: " +
//                                (HudifyHUD.getProgress() / (1000 * 60)) + ":" + String.format("%02d", HudifyHUD.getProgress() / 1000 % 60)); // spammy but very helpful

//							}
                    } else {
                        //when world is null
//						progressMS = 0;
//						durationMS = -1;
						progress = 0;
						duration = -1;
                        // LOGGER.error("progress + duration updated in refreshActiveSession"); // spammy af

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
//	public static void updateData(String[] data)
//	{
//		hudInfo = data;
////		progressMS = hudInfo[2] == null ? 0 : (Integer.parseInt(hudInfo[2]) - 1000);
////		durationMS = hudInfo[3] == null ? -1 : Integer.parseInt(hudInfo[3]);
//		progress = hudInfo[2] == null ?  0 : Integer.parseInt(hudInfo[2]);
//		duration = hudInfo[3] == null ? -1 : Integer.parseInt(hudInfo[3]);
//
////		progress = hudInfo[2] == null ?  0 : (int) (Math.floor(((double) Integer.parseInt(hudInfo[2]) / 1000)) - 1);
////		duration = hudInfo[3] == null ? -1 : (int) (Math.floor((double) Integer.parseInt(hudInfo[3]) / 1000));
//		if(db) { LOGGER.info("HudifyMain.updateData: (" + progress + " / " + duration + ") - " + Arrays.toString(data)); }
//	}
//	public static int getProgressSec() { return (progressMS/1000); }
//	public static int getDurationSec() { return (durationMS/1000); }
//	public static int getProgress() { return (progress); }
//	public static int getDuration() { return (duration); }
//
//	public static void setProgress(int progress_arg) {
//		progress = progress_arg;
////		progressMS = progress;
//	}
//
//	public static void setDuration(int duration_arg) {
//		duration = duration_arg;
////		durationMS = duration;
//	}



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
