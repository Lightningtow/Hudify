package lightningtow.hudify.util;

public class SpotifyData {
    /** the single source of truth for current Spotify state **/
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
        String msg = switch (code) {
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
// see this link for unofficial estimates of ratelimits
// https://community.spotify.com/t5/Spotify-for-Developers/Web-API-ratelimit/m-p/5503153/highlight/true#M7931


}
