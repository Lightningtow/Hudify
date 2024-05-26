package lightningtow.hudify;
//
import eu.midnightdust.lib.config.MidnightConfig;

public class HudifyConfig extends MidnightConfig {
    @Entry(min = 800, max = 60000) public static int poll_rate = 850;
    @Entry(min = 1, max = 60) public static int inactive_poll_rate = 3;
    @Entry(min = -1, max = 200) public static int truncate_length = -1;
    @Entry public static boolean db = true;
    @Entry public static boolean scrub_name = false;
    @Entry public static String CLIENT_ID = "";
//    @Entry public static boolean refresh_client_auth = false;


}
//
//public class HudifyConfig {
//    public static int poll_rate = 850;
//    public static int inactive_poll_rate = 3;
//    public static int truncate_length = -1;
//    public static boolean db = true;
//    public static boolean scrub_name = false;
//}
