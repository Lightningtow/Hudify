package lightningtow.hudify;

import eu.midnightdust.lib.config.MidnightConfig;

public class HudifyConfig extends MidnightConfig {
    @Entry(min=800,max=60000) public static int poll_rate = 850;
    @Entry(min=-1, max=200) public static int truncate_length = -1;
    @Entry public static boolean db = false;
    @Entry public static boolean scrub_name = true;


}
