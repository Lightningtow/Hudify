package lightningtow.chyma;

import lightningtow.chyma.integrations.CustomhudIntegration;
import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.Level;

public class ChymaMain implements ClientModInitializer
{
	public static final String MOD_ID = "chyma";
	public static final String MOD_DISPLAY_NAME = "CHYMA";

	public static Boolean minimap_displayed = false;


//	public enum Level { INFO, ERROR, TRACE, DEBUG, LOG, FATAL, WARN, OFF, ALL }
	public static void LogThis(org.apache.logging.log4j.Level lvl, String msg) {
		org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MOD_DISPLAY_NAME);
		msg = MOD_DISPLAY_NAME + " " + msg;

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


	@Override
	public void onInitializeClient() {

		// journeymap initialization happens in JourneymapIntegration. It is not referenced elsewhere to leave it as a soft dependency


		try {
//			LogThis(Level.INFO, "Beginning integration with CustomHud");
			CustomhudIntegration.initCustomhud();
			LogThis(Level.INFO, "Successfully integrated with CustomHud");


		} catch (Exception e) {
			LogThis(Level.ERROR, "Error integrating with CustomHud: " + e);
		}



	}


	//<editor-fold desc="register keybindings">
//	public static void registerKeyBindings() {
//		registerRefreshKey();
//
//	}
//
//	private static boolean refreshKeyPrevState = false;
//	// don't waste time abstracting these, they work perfectly as they are
//	// and the duplicated code is isolated and very easy to collapse and ignore and work around
//
//	private static void registerRefreshKey() {
//		KeyBinding newKey = new KeyBinding("chyma.key.refresh", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "chyma");
//		KeyBindingHelper.registerKeyBinding(newKey);
//
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			if (newKey.wasPressed() && !refreshKeyPrevState) {
////				if (sp_is_authorized) { // no need to check authorization ; apiRequest already does
//				if (db) LogThis(Level.INFO,"Refresh key pressed");
////				} else { Util.getOperatingSystem().open(SpotifyUtil.authorize()); }
//			}
//			refreshKeyPrevState = newKey.wasPressed();
//		});
//	}

	//</editor-fold> register keybindings

}
