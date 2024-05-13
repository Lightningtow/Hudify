package lightningtow.hudify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import lightningtow.hudify.HudifyMain;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import static lightningtow.hudify.util.SpotifyData.*;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static lightningtow.hudify.HudifyMain.*;

public class SpotifyUtil
{
    private static int ContextCount = 0;
    private static final String client_id = "2f8c634ba8cc43a8be450ff3f745886f";
    private static String verifier;
    private static String authCode;
    private static String accessToken;
    private static String refreshToken;
    private static final String tokenAddress = "https://accounts.spotify.com/api/token";
    private static HttpClient client;
    private static HttpServer authServer;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static HttpRequest playbackRequest;
    private static File authFile;
    private static boolean isAuthorized = false;
    private static boolean isPlaying = false; // var from spotify. true if music is playing, false if paused, app closed, anything

    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    //<editor-fold desc="auth utils">
    public static void initialize()
    {
        //LOGGER.info("running SpotifyUtil.initialize()");
        LOGGER.info("initializing with Spotify");

        authFile = new File(System.getProperty("user.dir") + File.separator +
                "config" + File.separator + "HudifyTokens.json");
        try
        {
            if (!authFile.exists())
            {
//                authFile.createNewFile();
                boolean fileCreated = authFile.createNewFile();
                // Validate that file actually got created
                if (fileCreated) {
                    LOGGER.info("Created new token file at: " + authFile.getAbsolutePath());
                }
                accessToken = "";
                refreshToken = "";
                isAuthorized = false;
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
                    isAuthorized = true;
                }
                else
                {
                    accessToken = "";
                    refreshToken = "";
                    isAuthorized = false;
                }
                scan.close();
            }
        } catch (IOException e)
        {
            LOGGER.error("exception caught in initialize():" + e.getMessage());
        }
        client = HttpClient.newHttpClient();
        updatePlaybackRequest();
    }

    public static String authorize()
    {
        if (db) LOGGER.error("running SpotifyUtil.authorize()");
        StringBuilder authURI = null;
        String[] scope_list = {
                "user-read-playback-state",
                "user-read-currently-playing",
                "user-modify-playback-state",
                "playlist-read-private"  };
        // https://developer.spotify.com/documentation/web-api/concepts/scopes
        try
        {
            authURI = new StringBuilder();
            authURI.append("https://accounts.spotify.com/authorize");
            authURI.append("?client_id=" + client_id);
            authURI.append("&response_type=code"); // http://localhost:8888/callback
            authURI.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            authURI.append("&scope=");
//            authURI.append("&scope=user-read-playback-state%20user-read-currently-playing");
//            authURI.append("%20user-modify-playback-state");
//            authURI.append("%20playlist-read-private");
            for (String scope : scope_list) {
                authURI.append("%20").append(scope);
            }
            authURI.append("&code_challenge_method=S256");
            verifier = PKCEUtil.generateCodeVerifier();
            String challenge = PKCEUtil.generateCodeChallenge(verifier);
            authURI.append("&code_challenge=").append(challenge);
            authServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0);
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            authServer.setExecutor(threadPoolExecutor);
            authServer.createContext("/callback", new AuthServerHandler());
            authServer.start();
        } catch (Exception e)
        {
            LOGGER.error("exception caught in SpotifyUtil.authorize():" + e.getMessage());
        }
        if (authURI == null)
        {
            return "https://www.google.com";
        }

        return authURI.toString();
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
        LOGGER.error("running SpotifyUtil.requestAccessToken");

        try
        {
            StringBuilder accessBody = new StringBuilder();
            accessBody.append("grant_type=authorization_code");
            accessBody.append("&code=").append(authCode);
            accessBody.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            accessBody.append("&client_id=" + client_id);
            accessBody.append("&code_verifier=").append(verifier);
            HttpRequest accessRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(accessBody.toString()))
                    .build();
            LOGGER.info("url request" + accessBody);
            HttpResponse<String> accessResponse = client.send(accessRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject accessJson = JsonParser.parseString(accessResponse.body()).getAsJsonObject();
            accessToken = accessJson.get("access_token").getAsString();
            refreshToken = accessJson.get("refresh_token").getAsString();
            updatePlaybackRequest();
            writeAuthFile();
            isAuthorized = true;
        } catch (Exception e)
        {
            LOGGER.error("exception caught in requestAccessToken():" + e.getMessage());
        }
    }

    public static boolean refreshAccessToken()
    {
        // returns true if refreshed successfully, false if could not refresh
        if (db) LOGGER.error("running SpotifyUtil.refreshAccessToken");

        try
        {
            String refreshRequestBody = "grant_type=refresh_token" +
                    "&refresh_token=" + refreshToken +
                    "&client_id=" + client_id;

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
            if (db) LOGGER.error("exception caught in refreshAccessToken():" + e.getMessage());
        }
        return false;
    }

    public static void refreshActiveSession()
    {
    // todo this gets devices? is this only needed for the volume thing?
    // https://developer.spotify.com/documentation/web-api/reference/get-a-users-available-devices
        //        if (db) LOGGER.info("running SpotifyUtil.refreshActiveSession");

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
                if (db) LOGGER.error("SpotifyUtil.refreshActiveSession: no active device");

                isPlaying = false;
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
            if (db) LOGGER.error("RefreshActiveSession - API responded with status code: "
                    + client.send(setActive, HttpResponse.BodyHandlers.ofString()).statusCode());

        } catch (Exception e)
        {
            if (db) LOGGER.error("exception caught in refreshActiveSession():" + e.getMessage());
        }
//        LOGGER.info("Successfully refreshed active session"); // lol this runs even with 404s
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
            LOGGER.error(e.getMessage());
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

//    public boolean



    public static String getContext(String uri) /* get name from uri */ {
        // gets the name of a playlist/artist/album from their uri
        // get - Retrieves resources
        try
        {
            String[] splitString = uri.split(":");
            String link = "https://api.spotify.com/v1/" + splitString[1] + "s/" + splitString[2];
            LOGGER.info("context link: " + link);

            HttpRequest getReq = HttpRequest.newBuilder(new URI(link))
                    .GET()
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
//            LOGGER.info("GET Request (" + getReq + "): " + getRes + " " + getRes.statusCode());
            if (getRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                LOGGER.info("Retrying get request...");
                getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
//                LOGGER.info("GET Request (" + uri + "): " + getRes.statusCode());
            }
            else if (getRes.statusCode() == 403) /* forbidden */ {
//               HudifyMain.send_message();
            }
//            else if (getRes.statusCode() == 401) /* unauthorized */ {
////                if (refreshAccessToken()) getRequest(uri);
////                else isAuthorized = false;
//            }
            else if (getRes.statusCode() == 429) { // rate limited
                // approximately 180 calls per minute without throwing 429, ~3 calls per second
                LOGGER.error("RATE LIMITED============================================================");
                Thread.sleep(3000);
                ContextCount = -10;
                return "rate limited!";
    //                        } else if (data[0].equals("Reset")) {
                // getPlaybackInfo returns this if connection reset
    //                            LOGGER.error("Reset condition, maintaining HUD until reset"); // was level info and from blockiy
            }
//            LOGGER.info("get entire request json " + getRes.body()); // prints entire block of returned json
            JsonObject json = (JsonObject) JsonParser.parseString(getRes.body());
            return (String.valueOf(json.get("name")).replaceAll("\"", ""));
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry get request...");
                getContext(uri);
                LOGGER.info("Successfully sent get request");
            }
            else LOGGER.error("exception caught in getRequest():" + e.getMessage());
        }
        return "error in getRequest()";
    }



    //<editor-fold desc="playback functions">
    public static void nextSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("next");
            sp_duration = -2;
            LOGGER.info("Skipping to next song");
            // LOGGER.error("duration set to -2000 from nextSong");
        });
    }
    public static void prevSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("previous");
            sp_duration = -2; // was -2000
            LOGGER.info("Skipping to previous song");
            //LOGGER.error("duration set to -2000 from prevSong");
        });
    }

    public static void togglePlayPause() {
        if (isPlaying) {
            LOGGER.info("Pausing playback");
            EXECUTOR_SERVICE.execute(() -> putRequest("pause"));
        }
        else {
            LOGGER.info("Resuming playback");
            EXECUTOR_SERVICE.execute(() -> putRequest("play"));
        }

//        isPlaying = !isPlaying; // this should update automatically
    }
    //</editor-fold> playback functions

    public static void updatePlaybackInfo() // rename to updatePlaybackInfo?
    {
        // todo can I just mash this whole thing into main loop?
        // probably not a good idea
        String dump_msg = "getPlaybackInfo";
        try
        {
            HttpResponse<String> playbackResponse = client.send(playbackRequest, HttpResponse.BodyHandlers.ofString());
            // https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback

            sp_status_code = playbackResponse.statusCode();
//            LOGGER.info("getPlaybackInfo - status code: " + playbackResponse.statusCode());
            // app closed returns 204

            if (playbackResponse.statusCode() == 429) return; // rate limited
            if (playbackResponse.statusCode() == 200) // OK - The request has succeeded
            {
                JsonObject json = (JsonObject) JsonParser.parseString(playbackResponse.body());
                // the `json.get("progress_ms")` is incorrect after pausing then resuming

                dump_msg += " " + json.get("progress_ms") + " / " + json.get("item").getAsJsonObject().get("duration_ms");

                sp_media_type = (json.get("currently_playing_type").getAsString().equals("episode")) ? "episode" : "track";

                sp_progress = (json.get("progress_ms").getAsInt() / 1000);
                sp_duration = (json.get("item").getAsJsonObject().get("duration_ms").getAsInt() / 1000);

                sp_shuffle_state = json.get("shuffle_state").getAsBoolean();
                sp_repeat_state = json.get("repeat_state").getAsString(); // if repeat is "context" change to "all"
                /* repeat */  if (Objects.equals(sp_repeat_state, "context")) sp_repeat_state = "all"; // else leave it


                sp_track = json.get("item").getAsJsonObject().get("name").getAsString();
                isPlaying = json.get("is_playing").getAsBoolean();


                if (Objects.equals(sp_media_type, "episode")) /* for podcasts */ {
                    String show = json.get("item").getAsJsonObject().get("show").getAsJsonObject().get("name").getAsString();
                    sp_artists = show;
                    sp_first_artist = show;
                    sp_album = "";

                    HudifyMain.dump(dump_msg);
                    return;
                }


                /** sp_artists + sp_first_artist **/
                    JsonArray artistArray = json.get("item").getAsJsonObject().get("artists").getAsJsonArray();
                    StringBuilder artistString = new StringBuilder(artistArray.get(0).getAsJsonObject().get("name").getAsString());
                    sp_first_artist = artistString.toString();
                    for (int i = 1; i < artistArray.size(); i++) /* skip the first artist */ {
                        artistString.append(", ").append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
                    }
                    sp_artists = artistString.toString();
                /** sp_artists + sp_first_artist **/


                sp_album = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();

                /* context */ JsonObject context = json.get("context").getAsJsonObject();
                /* context type */ sp_context_type = context.get("type").getAsString();
                /* context name */ sp_prev_context = sp_context_name;
                if (!sp_prev_context_uri.equals(context.get("uri").getAsString())) { // if DOESNT match
//                    LOGGER.info("contexts do NOT match, updating context");
                    LOGGER.info("type: " + sp_context_type + ", uris " + sp_prev_context_uri + " / " + context.get("uri").getAsString());
                    sp_prev_context_uri = context.get("uri").getAsString();
                    switch (sp_context_type) {
                        case "album":
                            sp_context_name = sp_album;  break;
                        case "show":
                            sp_context_name = sp_artists;  break;
                        case "artist":
                        case "playlist":
                            EXECUTOR_SERVICE.execute(() -> sp_context_name = getContext(context.get("uri").getAsString()));
                    }

                }

            } // if response successful
            else if (playbackResponse.statusCode() == 401) /* unauthorized */ {
                if (!refreshAccessToken()) isAuthorized = false;
            }

        } catch (Exception e)
        {
//            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
//            {
//                LOGGER.info("Resetting connection and retrying info get...");
////                results[0] = "Reset";
//
//            }
//            else
                LOGGER.error("exception caught in getPlaybackInfo(): " + e.getMessage());
        }
        HudifyMain.dump(dump_msg);
//        return;

    }

    //<editor-fold desc="put/post calls">

    // https://developer.spotify.com/documentation/web-api/concepts/api-calls

    public static void putRequest(String type) /* play, pause */ {
        // PUT - Changes and/or replaces resources or collections
        try
        {
            HttpRequest putReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.error("PUT Request (" + type + "): " + putRes.statusCode());

            if (putRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                LOGGER.info("Retrying put request...");
                putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("PUT Request (" + type + "): " + putRes.statusCode());
            }
            else if (putRes.statusCode() == 403) /* forbidden */ {
//                MutableText msg = Text.translatable("hudify.messages.premium_required");
                HudifyMain.send_message("why does spotify say you're forbidden from pausing", 5);
            }
            else if (putRes.statusCode() == 401) /* unauthorized */ {
                if (refreshAccessToken()) putRequest(type);
                else isAuthorized = false;
            }
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry put request...");
                putRequest(type);
                LOGGER.info("Successfully sent put request");
            }
            else LOGGER.error("exception caught in putRequest():" + e.getMessage());
        }
    }

    public static void postRequest(String type) /* skip forward, back */ {
        // POST - Creates resources
        try
        {
            HttpRequest postReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.error("POST Request (" + type + "): " + postRes.statusCode());
            if (postRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                LOGGER.info("Retrying post request...");
                postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("POST Request (" + type + "): " + postRes.statusCode());
            }
            else if (postRes.statusCode() == 403) /* forbidden */ {
//                HudifyMain.send_message();
            }
            else if (postRes.statusCode() == 401) /* unauthorized */ {
                if (refreshAccessToken()) postRequest(type);
                else isAuthorized = false;
            }

        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry post request...");
                postRequest(type);
                LOGGER.info("Successfully sent post request");
            }
            else LOGGER.error("exception caught in postRequest():" + e.getMessage());
        }
    }

    //</editor-fold> put/post calls

    public static boolean isAuthorized() { return isAuthorized; }

//    public static boolean isPlaying() { return isPlaying; } // getter, makes it so outer classes can't screw it up


//JsonArray imageArray = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray();
//if (imageArray.size() > 1)
//{ results[4] = imageArray.get(1).getAsJsonObject().get("url").getAsString();
//} else { results[4] = null; }


}
