package lightningtow.hudify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import lightningtow.hudify.HudifyConfig;
import lightningtow.hudify.HudifyMain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import net.minecraft.util.Util;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static lightningtow.hudify.HudifyConfig.smartbrackets_kill_featuring;
import static lightningtow.hudify.util.SpotifyData.*;
import static lightningtow.hudify.HudifyConfig.db;
import static lightningtow.hudify.HudifyMain.LogThis;
public class SpotifyUtil
{
    /**
     A huge thank you to Erruqie's Blockify for much of this code!
     */


//    private static final String client_id = "2f8c634ba8cc43a8be450ff3f745886f";
//    private static final String client_id = "invalid client id test";

    public static String get_client_id() {
        return HudifyConfig.CLIENT_ID.trim();
    }

    private static String verifier;
    private static String authCode;
    private static String accessToken;
    private static String refreshToken;
    private static final String tokenAddress = "https://accounts.spotify.com/api/token";
    private static HttpClient client;
    public static HttpClient getClient() { return client; }

    private static HttpServer authServer;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static HttpRequest playbackRequest;
    public static HttpRequest getPlaybackRequest() { return playbackRequest; }

    private static File authFile;
    private static final String auth_filename = "hudify_tokens_dont_edit.json";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();


    //<editor-fold desc="auth utils">
    public static void initialize()
    {
        //Log(Level.INFO,"running SpotifyUtil.initialize()");
        LogThis(Level.INFO,"initializing Spotify integration");

        authFile = new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + auth_filename);
//        authFile = new File(System.getProperty("user.dir") + File.separator
//                + "config" + File.separator + MOD_ID+ File.separator + auth_filename);

        try
        {
            if (!authFile.exists())
            {
//                authFile.createNewFile();
                boolean fileCreated = authFile.createNewFile();
                // Validate that file actually got created
                if (fileCreated) {
                    LogThis(Level.INFO,"Created new token file at: " + authFile.getAbsolutePath());
                }
                accessToken = "";
                refreshToken = "";
                sp_is_authorized = false;
                authorize();
            }
            else
            {
                Scanner scan = new Scanner(authFile);
                JsonObject authJson;// = null;
                if (scan.hasNextLine())
                {
                    authJson = JsonParser.parseString(scan.nextLine()).getAsJsonObject();
                    accessToken = authJson.get("access_token").getAsString();
                    refreshToken = authJson.get("refresh_token").getAsString();
                    sp_is_authorized = true;
                }
                else
                {
                    accessToken = "";
                    refreshToken = "";
                    sp_is_authorized = false;
                }
                scan.close();
            }
        } catch (IOException e)
        {
            LogThis(Level.ERROR,"exception caught in initialize():" + e.getMessage());
        }
        client = HttpClient.newHttpClient();
        updatePlaybackRequest();
    }

    public static void authorize() /* this is what opens the web browser to authorize*/
    {

        if (db) LogThis(Level.INFO,"running SpotifyUtil.authorize()");

        if (get_client_id().trim().isEmpty()) {
            LogThis(Level.INFO,"Could not authorize, empty client ID");
            HudifyMain.send_message("Could not authorize, empty client ID", 3);
            return;
        }

        StringBuilder authURI = null;
        String[] scope_list = {
                "user-read-playback-state", // get playback state
//                "user-read-currently-playing", // redundant with user-read-playback-state
                "user-modify-playback-state", // allows play/pause/skip
                "playlist-read-private"  }; // allows you to get the name of context playlist
        // https://developer.spotify.com/documentation/web-api/concepts/scopes
        try
        {
            authURI = new StringBuilder();
            authURI.append("https://accounts.spotify.com/authorize");
            authURI.append("?client_id=").append(get_client_id());
            authURI.append("&response_type=code"); // http://localhost:8001/callback
            authURI.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            authURI.append("&scope=");
            for (String scope : scope_list) {
                authURI.append("%20").append(scope);
            }
            authURI.append("&code_challenge_method=S256");
            verifier = AuthServerHandler.generateCodeVerifier();
            String challenge = AuthServerHandler.generateCodeChallenge(verifier);
            authURI.append("&code_challenge=").append(challenge);
            authServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0);
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            authServer.setExecutor(threadPoolExecutor);
            authServer.createContext("/callback", new AuthServerHandler());
            authServer.start();
        } catch (Exception e)
        {
            LogThis(Level.ERROR,"exception caught in SpotifyUtil.authorize():" + e.getMessage());
        }
        if (authURI == null)
        {
            LogThis(Level.INFO,"Could not authorize, authURI is null");
            HudifyMain.send_message("Could not authorize, authURI is null", 3);

            return;// false;
        }
        LogThis(Level.INFO,"Sucessfully opened authorization prompt");
        Util.getOperatingSystem().open(authURI.toString());

        return;// authURI.toString();
    }

    public static void authorize(String authCode)
    {
        SpotifyUtil.authCode = authCode;
        authServer.stop(0);
        threadPoolExecutor.shutdown();
        requestAccessToken();
    }

    private static void requestAccessToken()
    {
        if (db) LogThis(Level.INFO,"running SpotifyUtil.requestAccessToken");

        try
        {
            StringBuilder accessBody = new StringBuilder();
            accessBody.append("grant_type=authorization_code");
            accessBody.append("&code=").append(authCode);
            accessBody.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            accessBody.append("&client_id=").append(get_client_id());
            accessBody.append("&code_verifier=").append(verifier);
            HttpRequest accessRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(accessBody.toString()))
                    .build();
            if (db) LogThis(Level.INFO,"url request " + accessBody);
            HttpResponse<String> accessResponse = client.send(accessRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject accessJson = JsonParser.parseString(accessResponse.body()).getAsJsonObject();
            accessToken = accessJson.get("access_token").getAsString();
            refreshToken = accessJson.get("refresh_token").getAsString();
            updatePlaybackRequest();
            writeAuthFile();
            sp_is_authorized = true;
        } catch (Exception e)
        {
            LogThis(Level.ERROR,"exception caught in requestAccessToken():" + e.getMessage());
        }
    }

    public static boolean refreshAccessToken()
    {
        // returns true if refreshed successfully, false if could not refresh
        if (db) LogThis(Level.INFO,"running SpotifyUtil.refreshAccessToken");

        try
        {
            String refreshRequestBody = "grant_type=refresh_token" +
                    "&refresh_token=" + refreshToken +
                    "&client_id=" + get_client_id();

            HttpRequest refreshRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(refreshRequestBody))
                    .build();

            HttpResponse<String> refreshResponse = client.send(refreshRequest, HttpResponse.BodyHandlers.ofString());
            if (refreshResponse.statusCode() == 200) // successful
            {
                JsonObject refreshJson = JsonParser.parseString(refreshResponse.body()).getAsJsonObject();
                accessToken = refreshJson.get("access_token").getAsString();
                refreshToken = refreshJson.get("refresh_token").getAsString();
                writeAuthFile();
                updatePlaybackRequest();
                return true;
            }
        } catch (Exception e)
        {
            if (db) LogThis(Level.ERROR,"exception caught in refreshAccessToken():" + e.getMessage());
        }
        return false;
    }

    public static void refreshActiveSession()
    {
    // todo this gets devices? is this only needed for the volume thing?
        // todo this can likely be abstracted into apiRequest
    // https://developer.spotify.com/documentation/web-api/reference/get-a-users-available-devices
        //        if (db) Log(Level.INFO,"running SpotifyUtil.refreshActiveSession");

        try
        {
            String playerAddress = "https://api.spotify.com/v1/me/player/";
            HttpRequest getDevices = HttpRequest.newBuilder(
                    new URI(playerAddress + "devices"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> devices = client.send(getDevices, HttpResponse.BodyHandlers.ofString());
            JsonArray devicesJson = JsonParser.parseString(devices.body()).getAsJsonObject().get("devices").getAsJsonArray();
            JsonObject currDevice;
            String computerName = InetAddress.getLocalHost().getHostName();
            String thisDeviceID = "";
            if (devicesJson.isEmpty())
            {
                sp_duration = 1;
                sp_progress = 0;
                if (db) LogThis(Level.INFO,"SpotifyUtil.refreshActiveSession: no active device");

                sp_is_playing = false;
//                isPlaying = false;
                return;
            }
            for (int i = 0; i < devicesJson.size(); i++)
            {
                currDevice = devicesJson.get(i).getAsJsonObject();
                if (currDevice.get("name").getAsString().equals(computerName))
                {
                    thisDeviceID = currDevice.get("id").getAsString();
                    break;
                }
            }
            String deviceIDBody = "{\"device_ids\" : [\"" + thisDeviceID + "\"]}";
            HttpRequest setActive = HttpRequest.newBuilder(
                    new URI(playerAddress))
                    .header("Authorization", "Bearer " + accessToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(deviceIDBody))
                    .build();
            if (db) LogThis(Level.INFO,"RefreshActiveSession - API responded with status code: "
                    + client.send(setActive, HttpResponse.BodyHandlers.ofString()).statusCode());

        } catch (Exception e)
        {
            if (db) LogThis(Level.ERROR,"exception caught in refreshActiveSession():" + e.getMessage());
        }
//        Log(Level.INFO,"Successfully refreshed active session"); // lol this runs even with 404s
    }

    public static void writeAuthFile()
    {
        try
        {
            FileWriter jsonWriter = new FileWriter(authFile);
            jsonWriter.write("{" + "\"access_token\" : \"" + accessToken + "\", \"refresh_token\" : \"" + refreshToken + "\" }");
            jsonWriter.flush();
            jsonWriter.close();
        } catch (IOException e)
        {
            LogThis(Level.ERROR,e.getMessage());
        }
    }

    public static void updatePlaybackRequest()
    {
        playbackRequest = HttpRequest.newBuilder(
                        URI.create("https://api.spotify.com/v1/me/player?additional_types=episode"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json").build();
    }
    //</editor-fold> auth utils


    public enum reqType {GET, POST, PUT, DELETE}
    /** pass this a URL, like in the "request sample" part of
     *     <a href="https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback">...</a>
     * THIS CAN RETURN NULL
     * and is supposed to return null, if you're doing stuff that doesn't need a response like play/pause, skip etc
     * example call:  EXECUTOR_SERVICE.execute(() -> apiRequest(reqType.PUT,"https:/ /api.spotify.com/v1/me/player/pause"));
     * (remove the whitespace between the slashes in the link above, thats to stop javadocs from throwing warnings)
     * be sure to call this using an executor, not on the main thread, so as to not freeze the main thread
    **/
    public static JsonObject apiRequest(reqType type, String url)  {
        try
        {

            if(db) LogThis(Level.INFO,"link: " + url);

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(new URI(url));
            switch (type) {
                case GET: reqBuilder.GET();  break;
                case PUT: reqBuilder.PUT(HttpRequest.BodyPublishers.ofString(""));  break;
                case POST: reqBuilder.POST(HttpRequest.BodyPublishers.ofString(""));  break;
                case DELETE: reqBuilder.DELETE();  break; // check this call is right if i ever use delete
            }
            reqBuilder.header("Authorization", "Bearer " + accessToken).build();
            HttpRequest request = reqBuilder.build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            Log(Level.INFO,"GET Request (" + getReq + "): " + getRes + " " + getRes.statusCode());
//            sp_status_code = response.statusCode(); // lets keep sp_status_code to just updatePlaybackInfo()

            if (response.statusCode() == 401) /* unauthorized */ {
                if (refreshAccessToken()) apiRequest(type, url);
                else sp_is_authorized = false;
            }
            else if (response.statusCode() == 403) /* forbidden */ {
//               HudifyMain.send_message("");
                if(db) LogThis(Level.INFO,type + " request " + url + " returned 403 forbidden");

            }
            else if (response.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                if(db) LogThis(Level.INFO,"Retrying get request...");
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                Log(Level.INFO,"GET Request (" + uri + "): " + getRes.statusCode());
            }
            else if (response.statusCode() == 429) { // rate limited
                // approximately 180 calls per minute without throwing 429, ~3 calls per second
                LogThis(Level.ERROR,"RATE LIMITED============================================================");
                Thread.sleep(3000);
                return null;
//                return "rate limited!";
                //                        } else if (data[0].equals("Reset")) {
                // getPlaybackInfo returns this if connection reset
                //  Log(Level.ERROR,"Reset condition, maintaining HUD until reset"); // was level info and from blockiy
            }
//            Log(Level.INFO,"get entire request json " + getRes.body()); // prints entire block of returned json
            if (response.body().isEmpty()) {
                return null;
            }
            else {
                try {
                    return (JsonObject) JsonParser.parseString(response.body());
                } catch (Exception e) {
                    LogThis(Level.ERROR, "Error parsing api request:" + type + " request " + url + " returned 403 forbidden");
                    return null;
                }

            }
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LogThis(Level.INFO,"Attempting to retry get request...");
                apiRequest(type, url); // just reruns the method using what was passed the first time, no need to edit this call
                LogThis(Level.INFO,"Successfully sent get request");
            }
            else LogThis(Level.ERROR,"exception caught in getRequest(): " + e.getMessage());
        }
        return null;//"error in getRequest()";
    }

    public static String nobrackets(String input) {
        char[] blacklist = { '{', '(', '-' };
        String output = "";
        char x1 = '(';
        char x2 = '[';
        char x3 = '{'; // hoping itll be faster to have individual variables rather than an array
        char x4 = '-';
        char x5 = '–';

        // todo this is very inefficient, make this only run when track updates
        for(int i = 0, len = input.length() ; i < len ; i++) {
            char c = input.charAt(i);
            if (c == x1 || c == x2 || c == x3 || c == x4 || c == x5) {
                output = input.substring(0, i-1);
                return output;

            }
        }
        return input;
    }

    public static String smartbrackets(String input) {
        // take stuff like 'remastered' etc and scrub it from the end of track name
        String output = input;

        // todo this is very inefficient, make this only run when track updates

        ArrayList<String> blacklist = new ArrayList<>();
        blacklist.add("bonus track");
        blacklist.add("bonus");
        blacklist.add("intro");
        blacklist.add("outro");
        blacklist.add("interlude");
        blacklist.add("cover");
        blacklist.add("remix");
        blacklist.add("david garrett edition");


//        blacklist.add("live with the sfso");
//        if (sp_first_artist.equals("Metallica") && sp_album.equals("S&M2")) blacklist.add("live");
        blacklist.add("original mix");
        blacklist.add("single version");
        blacklist.add("recorded at spotify singles nyc");
        blacklist.add("spotify singles");
        blacklist.add("single mix");
        blacklist.add("remastere?d? [0-9]{4} \\/ remixed");
        blacklist.add("remastere?d? [0-9]{4}$");
        blacklist.add("[0-9]{4} - remastere?d?");
        blacklist.add("[0-9]{4} remastere?d?");
        blacklist.add("remastere?d?");
        blacklist.add("single");
        blacklist.add("music from [\\w\\s]*");
        blacklist.add("from [^\\])]*");

        if (HudifyConfig.smartbrackets_kill_featuring) {
            blacklist.add("with [^\\])]*");
            blacklist.add("featuring [\\w\\s]*"); // no whitespace so it matches feat with and w/o a period, feat.
            blacklist.add("feat. [\\w\\s]*"); // no whitespace so it matches feat with and w/o a period, feat.
//            blacklist.add("feat [^\\])]*"); // no whitespace so it matches feat with and w/o a period, feat.
            blacklist.add("ft[^\\])]*");

        }



        // smart brackets did not edit these:
        // burnout - beat avengers remix
        // piece of your mind feat. iona smith and beat fatigue - grid division remix
        // family reunion - live/1999
        // Ante Up (feat. Busta Rhymes, Teflon & Remi Martin) - Remix
        // shatter me featuring lzzy hale

        // https://regexr.com is the good regex website
        //   \w matches any letter
        //   \s matches any whitespace

        //    [^\])]*  matches any character that isnt ) or ]
        //    [\w\s]*  matches any letter, number, underscore, or whitespace
        //    which is better? ig it depends on whether tracks have needlessly complicated names

        ArrayList<String> real_blacklist = new ArrayList<>();
        // real blacklist is the blacklist with every combo of bracket and dash
        for (String elem : blacklist) {
            real_blacklist.add(" - Spider-Man: Across the Spider-Verse"); // since its exact, no need for every combo
            real_blacklist.add(" - " + elem);
            real_blacklist.add(" - " + elem);
            real_blacklist.add(" – " + elem);

            real_blacklist.add("\\/\\/ " + elem);
            real_blacklist.add("\\(" + elem + "\\)");
            real_blacklist.add("\\[" + elem + "\\]");
//            real_blacklist.add(elem);

            if (HudifyConfig.smartbrackets_kill_featuring) {
//                // because some songs put feat directly in the title with no brackets
                real_blacklist.add("featuring [\\w\\s]*"); // no whitespace so it matches feat with and w/o a period, feat.
                    // this makes it so it still appears in nobrackets. will get feedback later
                real_blacklist.add("feat. [\\w\\s]*");

//                real_blacklist.add("with [^\\])]*");
//                real_blacklist.add("feat[\\w\\s]*");
//                real_blacklist.add("ft[\\w\\s]*");
//
            }
        }


        for (String elem : real_blacklist) {
            String regex = "(?i)" + elem; // ?i makes it case insensitive
            output = output.replaceAll(regex, "");
            output = output.replace("  ", " "); // makes doublespaces single spaces

        }


        return output.trim();
    }

    //<editor-fold desc="playback functions">
    public static void nextSong() {
        EXECUTOR_SERVICE.execute(() -> {
            apiRequest(reqType.POST,"https://api.spotify.com/v1/me/player/next");
            sp_duration = -2;
            LogThis(Level.INFO,"Skipping to next song");
        });
    }
    public static void prevSong() {
        EXECUTOR_SERVICE.execute(() -> {
            apiRequest(reqType.POST,"https://api.spotify.com/v1/me/player/previous");
            sp_duration = -2;
            LogThis(Level.INFO,"Skipping to previous song");
        });
    }

    public static void togglePlayPause() {
        if (sp_is_playing) { // isPlaying
            if (db) HudifyMain.send_message("Paused playback", 3); // todo this runs even if 403 forbidden
            LogThis(Level.INFO,"Pausing playback");
            EXECUTOR_SERVICE.execute(() -> apiRequest(reqType.PUT,"https://api.spotify.com/v1/me/player/pause"));
        }
        else {
            if (db) HudifyMain.send_message("Resumed playback", 3);
            LogThis(Level.INFO,"Resuming playback");
            EXECUTOR_SERVICE.execute(() -> apiRequest(reqType.PUT, "https://api.spotify.com/v1/me/player/play"));
        }

//        isPlaying = !isPlaying; // this should update automatically
    }
    //</editor-fold> playback functions


    // https://developer.spotify.com/documentation/web-api/concepts/api-calls




//JsonArray imageArray = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray();
//if (imageArray.size() > 1)
//{ results[4] = imageArray.get(1).getAsJsonObject().get("url").getAsString();
//} else { results[4] = null; }


}
