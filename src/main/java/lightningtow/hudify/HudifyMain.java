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

//public class Hudify implements ModInitializer {
//	// This logger is used to write text to the console and the log file.
//	// It is considered best practice to use your mod id as the logger's name.
//	// That way, it's clear which mod wrote info, warnings, and errors.
//    public static final Logger LOGGER = LoggerFactory.getLogger("hudify");
//
//	@Override
//	public void onInitialize() {
//		// This code runs as soon as Minecraft is in a mod-load-ready state.
//		// However, some things (like resources) may still be uninitialized.
//		// Proceed with mild caution.
//
//		LOGGER.info("Hello Fabric world!");
//	}
//}
public class HudifyMain implements ModInitializer
{
	public static final String MOD_ID = "Hudify";

	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	private boolean forceKeyPrevState = false;
//	private boolean hideKeyPrevState = false;
//	private boolean increaseVolumeKeyPrevState = false;
//	private boolean decreaseVolumeKeyPrevState = false;
//	private boolean toggleInGameMusicKeyPrevState = false;
	private static Thread requestThread;
	public static final Logger LOGGER = LogManager.getLogger("Hudify");

	// see this link for unofficial estimates of ratelimits
	// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931

	// api response code descriptions
	// https://developer.spotify.com/documentation/web-api/concepts/api-calls
	final private boolean db = false; // toggle debug messages
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
//		CustomhudIntegration.on
//		prevKey = KeyBindingHelper.registerKeyBinding(
//				new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

//		playKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("hudify.key.play", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

//		nextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("hudify.key.next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

//		forceKey = KeyBindingHelper.registerKeyBinding(
//				new KeyBinding("hudify.key.force", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));



//		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
//			dispatcher.register(
//					ClientCommandManager.literal("sharetrack").executes(context -> {
//						var player = MinecraftClient.getInstance().player;
//						if (player == null) { return 0; }
//						player.sendMessage(Text.of(lightningtow.hudify.HudifyHUD.hudInfo[5]));
//						return 0;
//					})
//			);
//		});
	}
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




	public void prevKeyHandler(boolean currPressState) {
		if (currPressState && !prevKeyPrevState) {
			LOGGER.info("Previous Key Pressed");
			SpotifyUtil.prevSong();
		}
		prevKeyPrevState = currPressState;
	}

	public void forceKeyHandler(boolean currPressState) {
		if (currPressState && !forceKeyPrevState) {
			LOGGER.info("Force Key Pressed");
			//lightningtow.hudify.util.HudifyHUD.setDuration(-2000);
		}
		forceKeyPrevState = currPressState;
	}
//
//	public void hideKeyHandler(boolean currPressState)
//	{
//		if (currPressState && !hideKeyPrevState)
//		{
//			LOGGER.info("Hide Key Pressed");
//			lightningtow.hudify.util.HudifyHUD.isHidden = !lightningtow.hudify.util.HudifyHUD.isHidden;
//		}
//		hideKeyPrevState = currPressState;
//	}
//
//	public void increaseVolumeKeyHandler(boolean currPressState)
//	{
//		if (currPressState && !increaseVolumeKeyPrevState)
//		{
//			LOGGER.info("Increase Volume Key Pressed");
//			lightningtow.hudify.util.HudifyHUD.increaseVolume();
//		}
//		increaseVolumeKeyPrevState = currPressState;
//	}
//
//	public void decreaseVolumeKeyHandler(boolean currPressState)
//	{
//		if (currPressState && !decreaseVolumeKeyPrevState)
//		{
//			LOGGER.info("Decrease Volume Key Pressed");
//			lightningtow.hudify.util.HudifyHUD.decreaseVolume();
//		}
//		decreaseVolumeKeyPrevState = currPressState;
//	}
//
//	public void toggleInGameMusicKeyHandler(boolean currPressState)
//	{
//		if (currPressState && !toggleInGameMusicKeyPrevState)
//		{
//			LOGGER.info("Toggle In Game Music Key Pressed");
//			SpotifyUtil.toggleInGameMusic();
//		}
//		toggleInGameMusicKeyPrevState = currPressState;
//	}
}
