package lightningtow.hudify;

import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;


public class HudifyMain implements ModInitializer
{
	public static final String MOD_ID = "Hudify";

	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	private boolean forceKeyPrevState = false;
	private static Thread requestThread;
	public static final Logger LOGGER = LogManager.getLogger("Hudify");

	// see this link for unofficial estimates of ratelimits
	// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931

	// api response code descriptions
	// https://developer.spotify.com/documentation/web-api/concepts/api-calls
	final public boolean db = false; // toggle debug messages
	final public static boolean dbs = false; // toggle debug messages

	@Override
	public void onInitialize()
	{

		//LOGGER.info("running HudifyMain.onInitialize()"); //info
		LOGGER.info("initializing main loop"); //info

		//	HudifyConfig.init("Hudify", lightningtow.hudify.util.HudifyConfig.class);
		requestThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (MinecraftClient.getInstance().world != null) {
//							if (lightningtow.hudify.HudifyHUD.getDuration() < lightningtow.hudify.HudifyHUD.getProgress()) {
                        //Thread.sleep(1000);
                        String[] data = SpotifyUtil.getPlaybackInfo();
                        //if(db) LOGGER.info("main loop - data[0]: " + data[0]);

                        if (data[0] != null && data[0].equals("Status Code: 204")) {
                            SpotifyUtil.refreshActiveSession();
                        } else if (data[0] != null && data[0].equals("Status Code: 429")) { // rate limited
                            LOGGER.error("RATE LIMITED============================================================");
                            Thread.sleep(3000);
                        } else if (data[0] != null && data[0].equals("Reset")) {
                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was info and from blockiy
                        } else {
                            if(db) LOGGER.info("main loop: updating data");
                            HudifyHUD.updateData(data);
                        }
//							} else if (SpotifyUtil.isPlaying()) {
                        HudifyHUD.setProgress(HudifyHUD.getProgress() + 1000);
                            //LOGGER.info("progress updated in main loop to: " + lightningtow.hudify.HudifyHUD.getProgress() + 1000); // spammy but very helpful
                        if(db) LOGGER.info("progress updated in main loop to: " +
                                (HudifyHUD.getProgress() / (1000 * 60)) + ":" + String.format("%02d", HudifyHUD.getProgress() / 1000 % 60)); // spammy but very helpful

//							}
                    } else {
                        //when world is null
                        HudifyHUD.setProgress(0);
                        HudifyHUD.setDuration(-1);
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
		registerBindings();


	}
	//<editor-fold desc="register keybindings">
	public static void registerBindings() {
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
