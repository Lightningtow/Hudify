package lightningtow.hudify;
//
import eu.midnightdust.lib.config.MidnightConfig;

public class HudifyConfig extends MidnightConfig {
    public static final String TEXT = "text";
    public static final String ADVANCED = "advanced";

    @Entry(min = -1, max = 200) public static int truncate_length = -1;
    @Entry public static String CLIENT_ID = "";
//    @Comment(centered = true) public static Comment advanced_options_divider;
    @Entry(category = ADVANCED) public static boolean scrub_name = true;
    @Entry(category = ADVANCED) public static boolean db = false;
    @Entry(category = ADVANCED, min = 800, max = 60000) public static int poll_rate = 850;
    @Entry(category = ADVANCED, min = 1, max = 60) public static int inactive_poll_rate = 3;
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
