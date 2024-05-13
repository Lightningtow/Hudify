package lightningtow.hudify.util;

public class SpotifyData {
    /** the single source of truth for current Spotify state **/
    public static int sp_status_code = 123456;
    public static String sp_track = ""; // track or episode name
    public static String sp_fancy_track = ""; // track with 'bonus track, remastered' etc scrubbed out

    public static String sp_artists = ""; // all artists as one string
    public static String sp_first_artist = ""; // first artist listed. if one artist or podcast, identical to `artists`
    public static String sp_album = "";
    public static String sp_context_type = ""; // "artist", "playlist", "album", "show".
    public static String sp_context_name = ""; // name of artist, playlist etc
    public static String sp_prev_context = "";
    public static String sp_prev_context_uri = "";
    public static String sp_media_type = ""; // "track" or "episode"
    public static String sp_repeat_state = "";


    public static int msg_time_rem = 0;
    private static String sp_message = "";


    public static String get_sp_message() { return sp_message; }
    public static void set_sp_message(String msg) { sp_message = msg; }

    public static Boolean sp_shuffle_state = false;
    public static int sp_progress;
    public static int sp_duration;
    /** the single source of truth for current Spotify state **/
}
