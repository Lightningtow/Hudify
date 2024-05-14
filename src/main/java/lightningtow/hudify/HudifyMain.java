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

import static lightningtow.hudify.util.SpotifyData.*;
import java.util.Arrays;

public class HudifyMain implements ClientModInitializer
{
	public static final String MOD_ID = "Hudify";
	private static boolean toggleKeyPrevState = false;
	private static boolean nextKeyPrevState = false;
	private static boolean prevKeyPrevState = false;
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	final public static boolean db = true; // toggle debug messages. very spammy!


	public static void dump (String source) {
		if (db) LOGGER.info(String.join(", ",
				"dump from " + source + " - Status Code " + sp_status_code, "(" + sp_progress + " / " + sp_duration + ")",
				sp_track, sp_first_artist, "(" + sp_artists + ")", sp_context_type)
		);
	}

	private static void tick_message() {
		if (msg_time_rem > 0) {
			msg_time_rem -= 1;
		}
		else
			set_sp_message("");
	}

	public static void send_message (String msg, int msg_dur) {
		msg_time_rem = msg_dur;
		set_sp_message(msg);
	}

	@Override
	public void onInitializeClient()
	{

		try {
			CustomhudIntegrationThree.initCustomhud();
//			CustomhudIntegrationThree
			LOGGER.info("Successfully integrated with CustomHud");

		}
		catch (Exception e) {
			LOGGER.error("Error integrating with CustomHud: {}", e.toString());
		}
//		File authFile = new File(System.getProperty("user.dir") + File.separator +
//				"config" + File.separator + "HudifyTokens.json");
		HudifyConfig.init("hudify", HudifyConfig.class);

//		LOGGER.info("initializing main loop");
		//info
//		if (!SpotifyUtil.isAuthorized()) {
//			LOGGER.info("initializing client. Spotify is not authorized, initiating authorization progress ");
//
//			Util.getOperatingSystem().open(SpotifyUtil.authorize());
//		}
//		else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
		Thread requestThread = new Thread( () -> {

			while (true) {
				try {
					Thread.sleep(850);

					if (MinecraftClient.getInstance().world == null) {
						Thread.sleep(3000);
						sp_progress = 0;
						sp_duration = -1;
					}
					else
					{
//                        Thread.sleep(1000);
						SpotifyUtil.updatePlaybackInfo();
						tick_message();
						// 204 when app is closed, doesnt immediately go away when app opened
						if (sp_status_code == 204) { // No Content - The request has succeeded but returns no message body.
//							sp_progress = 0; // todo this is whats breaking progress when paused
//							sp_duration = -1; // todo but how do i fix progress bug
							SpotifyUtil.refreshActiveSession(); // returns this when app is closed, and refreshActiveSession throws 404s
							Thread.sleep(1000);

						} else if (sp_status_code == 429) { // rate limited
							// approximately 180 calls per minute without throwing 429, ~3 calls per second
							LOGGER.error("RATE LIMITED============================================================");
							Thread.sleep(3000);
//                        } else if (data[0].equals("Reset")) {
							// getPlaybackInfo returns this if connection reset
//                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was level info and from blockiy
						}
//						else {}

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
				HudifyMain.send_message("congrats you hit pause", 5);
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
