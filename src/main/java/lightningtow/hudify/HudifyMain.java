package lightningtow.hudify;

import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
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
	private static KeyBinding playKey;
	private static KeyBinding nextKey;
	private static KeyBinding prevKey;
	private static KeyBinding forceKey;
	private static KeyBinding hideKey;
	private static KeyBinding increaseVolumeKey;
	private static KeyBinding decreaseVolumeKey;
	private static KeyBinding toggleInGameMusicKey;
	private boolean playKeyPrevState = false;
	private boolean nextKeyPrevState = false;
	private boolean prevKeyPrevState = false;
	private boolean forceKeyPrevState = false;
	private boolean hideKeyPrevState = false;
	private boolean increaseVolumeKeyPrevState = false;
	private boolean decreaseVolumeKeyPrevState = false;
	private boolean toggleInGameMusicKeyPrevState = false;
	private static Thread requestThread;

	public static final Logger LOGGER = LogManager.getLogger("Hudify");

	@Override
	public void onInitialize()
	{

		LOGGER.error("Hudify successfully loaded"); //info
	//	HudifyConfig.init("Hudify", lightningtow.hudify.util.HudifyConfig.class);
		requestThread = new Thread()
		{
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						if (MinecraftClient.getInstance().world != null) {
							if (lightningtow.hudify.HudifyHUD.getDuration() < lightningtow.hudify.HudifyHUD.getProgress()) {
								Thread.sleep(1000);
								String[] data = SpotifyUtil.getPlaybackInfo();
								LOGGER.error("main loop - data[0]: " + data[0]);

								if (data[0] != null && data[0].equals("Status Code: 204")) {
									SpotifyUtil.refreshActiveSession();
								} else if (data[0] != null && data[0].equals("Status Code: 429")) {
									Thread.sleep(3000);
								} else if (data[0] != null && data[0].equals("Reset")) {
									LOGGER.info("Reset condition, maintaining HUD until reset");
								} else {
									lightningtow.hudify.HudifyHUD.updateData(data);
								}
							} else if (SpotifyUtil.isPlaying()) {
								lightningtow.hudify.HudifyHUD.setProgress(lightningtow.hudify.HudifyHUD.getProgress() + 1000);
								//LOGGER.error("progress updated in main loop to: " + lightningtow.hudify.HudifyHUD.getProgress() + 1000); // spammy but very helpful

							}
						} else {
							lightningtow.hudify.HudifyHUD.setProgress(0);
							lightningtow.hudify.HudifyHUD.setDuration(-1);
							// LOGGER.error("progress + duration updated in refreshActiveSession"); // spammy af

						}
					} catch (InterruptedException e) {
						LOGGER.error("error in main loop: " + e);
						e.printStackTrace();

					}
				}
			}

		};
		requestThread.setName("Spotify Thread");
		requestThread.start();
		SpotifyUtil.initialize();
//		CustomhudIntegration.on
		prevKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		playKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.play", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "hudify"));

		nextKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		forceKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.force", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "hudify"));

		hideKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.hide", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		increaseVolumeKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.increaseVolume", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		decreaseVolumeKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.decreaseVolume", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		toggleInGameMusicKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("hudify.key.toggleInGameMusic", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "hudify"));

		ClientTickEvents.END_CLIENT_TICK.register(
				client ->
				{
					try
					{
						playKeyHandler(playKey.isPressed());
						nextKeyHandler(nextKey.isPressed());
						prevKeyHandler(prevKey.isPressed());
						forceKeyHandler(forceKey.isPressed());
//						hideKeyHandler(hideKey.isPressed());
//						increaseVolumeKeyHandler(increaseVolumeKey.isPressed());
//						decreaseVolumeKeyHandler(decreaseVolumeKey.isPressed());
//						toggleInGameMusicKeyHandler(toggleInGameMusicKey.isPressed());

					} catch (Exception e)
					{
						LOGGER.error("exception caught in ClientTickEvents.END_CLIENT_TICK.register(): " + e.getMessage());
					}
				}
		);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("sharetrack").executes(context -> {
						var player = MinecraftClient.getInstance().player;
						if (player == null) { return 0; }
						player.sendMessage(Text.of(lightningtow.hudify.HudifyHUD.hudInfo[5]));
						return 0;
					})
			);
		});
	}

	public void playKeyHandler(boolean currPressState)
	{
		// LOGGER.error("running HudifyMain.playKeyHandler"); quite spammy lol
		try
		{
			if (currPressState && !playKeyPrevState)
			{
				if (SpotifyUtil.isAuthorized())
				{
					LOGGER.error("Authorized!"); //info
					SpotifyUtil.playPause();
				}
				else
				{
					Util.getOperatingSystem().open(SpotifyUtil.authorize());
				}
			}
			playKeyPrevState = currPressState;
		} catch (Exception e) {
			LOGGER.error("exception caught in playKeyHandler(): " + e.getMessage());
		}
	}

	public void nextKeyHandler(boolean currPressState) {
		if (currPressState && !nextKeyPrevState) {
			LOGGER.info("Next Key Pressed");
			SpotifyUtil.nextSong();
		}
		nextKeyPrevState = currPressState;
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
