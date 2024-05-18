package lightningtow.hudify.util;

import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import oshi.util.tuples.Triplet;

import java.util.HashMap;


public class SpotifyData {
    /** the single source of truth for current Spotify state **/
    //<editor-fold desc="variables">

    public static String sp_track = ""; // track or episode name
    public static String sp_fancy_track = ""; // track with 'bonus track, remastered' etc scrubbed out

    public static String sp_artists = ""; // all artists as one string
    public static String sp_first_artist = ""; // first artist listed. if one artist or podcast, identical to `artists`
    public static String sp_album = "";
    public static String sp_context_type = ""; // "artist", "playlist", "album", "show".
    public static String sp_context_name = ""; // name of artist, playlist etc

    public static Boolean sp_is_podcast = false;
    public static String sp_repeat_state = "";
    public static Boolean sp_is_authorized = false;

    private static String sp_message = "";
    public static int sp_msg_time_rem = 0;

    public static Boolean sp_is_playing = false;

    public static String get_sp_message() { return sp_message; }
    public static void set_sp_message(String msg) { sp_message = msg; } // should only be used from tickMessage() and setMessage()

    public static Boolean sp_shuffle_enabled = false;
    public static int sp_progress = 0;
    public static int sp_duration = 0;

    public static int sp_status_code = 123456;

    public static String sp_device_id = "";
    public static Boolean sp_device_is_active = false;
    public static String sp_device_name = "";

    public static String sp_prev_context = "";
    public static String sp_prev_context_uri = "";

    public static String get_status_string(int code) {
        String msg = "stop telling me to inline this";
        msg = switch (code) {
            case 200 -> "OK - The request has succeeded. The client can read the result of the request in the body and the headers of the response.";
            case 201 -> "Created - The request has been fulfilled and resulted in a new resource being created.";
            case 202 -> "Accepted - The request has been accepted for processing, but the processing has not been completed.";
            case 204 -> "No Content - The request has succeeded but returns no message body.";
            case 304 -> "Not Modified. See Conditional requests.";
            case 400 -> "Bad Request - The request could not be understood by the server due to malformed syntax.";
            case 401 -> "Unauthorized - The request requires user auth or, if the request included auth credentials, authorization has been refused for those creds.";
            case 403 -> "Forbidden - The server understood the request, but is refusing to fulfill it.";
            case 404 -> "Not Found - The requested resource could not be found. This error can be due to a temporary or permanent condition.";
            case 429 -> "Too Many Requests - Rate limiting has been applied.";
            case 500 -> "Internal Server Error - Spotify's fault, there's nothing I can do about this one -Lightningtow";
            case 502 -> "Bad Gateway - The server was acting as a gateway or proxy and received an invalid response from the upstream server.";
            case 503 -> "Service Unavailable - The server is currently unable to handle the request due to a temp condition which will be alleviated after some delay.";
            default -> "";
        };
        return msg;
    }

    public static HashMap<String, String> stringmap = new HashMap<>();
    public static HashMap<String, Boolean> boolmap = new HashMap<>();
    public static HashMap<String, Integer> intmap = new HashMap<>();
    public static HashMap<String, Triplet<String, Integer, Boolean>> specialmap = new HashMap<>();

    //</editor-fold> variables

    public static void UpdateMaps() {

        stringmap.put("sp_song", sp_track);
        stringmap.put("sp_track", sp_track);
        stringmap.put("sp_fancy_track", sp_fancy_track);
        stringmap.put("sp_artist", sp_artists);
        stringmap.put("sp_artists", sp_artists);
        stringmap.put("sp_first_artist", sp_first_artist);
        stringmap.put("sp_context_type", sp_context_type);
        stringmap.put("sp_context_name", sp_context_name);
        stringmap.put("sp_album", sp_album);
        stringmap.put("sp_repeat", sp_repeat_state);
        stringmap.put("sp_repeat_state", sp_repeat_state);
        stringmap.put("sp_device_id", sp_device_id);
        stringmap.put("sp_device_name", sp_device_name);
        stringmap.put("sp_status_string", get_status_string(sp_status_code));

        boolmap.put("sp_device_is_active", sp_device_is_active);
        boolmap.put("sp_shuffle", sp_shuffle_enabled);
        boolmap.put("sp_is_podcast", sp_is_podcast);
        boolmap.put("sp_is_playing", sp_is_playing);
        boolmap.put("sp_is_authorized", sp_is_authorized);

        intmap.put("sp_status_code", sp_status_code);

        Triplet<String, Integer, Boolean> prog = new Triplet<>(((sp_progress / 60) + ":" + String.format("%02d", sp_progress % 60)), sp_progress, sp_progress > 0);
        specialmap.put("sp_progress", prog);
        specialmap.put("sp_prog", prog);

        Triplet<String, Integer, Boolean> dur = new Triplet<>(((sp_duration / 60) + ":" + String.format("%02d", sp_duration % 60)), sp_duration, sp_duration > 0);
        specialmap.put("sp_duration", dur);
        specialmap.put("sp_dur", dur);

        Triplet<String, Integer, Boolean> msg = new Triplet<>(get_sp_message(), sp_msg_time_rem, !get_sp_message().isEmpty());
        specialmap.put("sp_message", msg);
        specialmap.put("sp_msg", msg);

        intmap.put("sp_message_duration", sp_msg_time_rem);
        intmap.put("sp_msg_dur", sp_msg_time_rem);

    }


// see this link for unofficial estimates of ratelimits
// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931


}
