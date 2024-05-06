package lightningtow.hudify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import lightningtow.hudify.HudifyMain;
import net.minecraft.client.MinecraftClient;
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

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static lightningtow.hudify.HudifyMain.db;

public class SpotifyUtil
{
    private static String client_id = "d34978659f8940e9bfce52d124539feb";
    private static String challenge;
    private static String verifier;
    private static String authCode;
    private static String accessToken;
    private static String refreshToken;
    private static String tokenAddress = "https://accounts.spotify.com/api/token";
    private static String playerAddress = "https://api.spotify.com/v1/me/player/";
    private static HttpClient client;
    private static HttpServer authServer;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static HttpRequest playbackRequest;
    private static HttpResponse<String> playbackResponse;
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
                authFile.createNewFile();
                LOGGER.info("Created new token file at: " + authFile.getAbsolutePath());
                accessToken = "";
                refreshToken = "";
                isAuthorized = false;
            }
            else
            {
                Scanner scan = new Scanner(authFile);
                JsonObject authJson = null;
                if (scan.hasNextLine())
                {
                    authJson = new JsonParser().parse(scan.nextLine()).getAsJsonObject();
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
        LOGGER.error("running SpotifyUtil.authorize()");

        StringBuilder authURI = null;
        try
        {
            authURI = new StringBuilder();
            authURI.append("https://accounts.spotify.com/authorize");
            authURI.append("?client_id=" + client_id);
            authURI.append("&response_type=code");
            authURI.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            authURI.append("&scope=user-read-playback-state%20user-read-currently-playing");
            authURI.append("%20user-modify-playback-state");
            authURI.append("&code_challenge_method=S256");
            verifier = PKCEUtil.generateCodeVerifier();
            challenge = PKCEUtil.generateCodeChallenge(verifier);
            authURI.append("&code_challenge=" + challenge);
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
            accessBody.append("&code=" + authCode);
            accessBody.append("&redirect_uri=http%3A%2F%2Flocalhost%3A8001%2Fcallback");
            accessBody.append("&client_id=" + client_id);
            accessBody.append("&code_verifier=" + verifier);
            HttpRequest accessRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(accessBody.toString()))
                    .build();
            HttpResponse<String> accessResponse = client.send(accessRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject accessJson = new JsonParser().parse(accessResponse.body()).getAsJsonObject();
            accessToken = accessJson.get("access_token").getAsString();
            refreshToken = accessJson.get("refresh_token").getAsString();
            updatePlaybackRequest();
            updateJson();
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
            StringBuilder refreshRequestBody = new StringBuilder();
            refreshRequestBody.append("grant_type=refresh_token");
            refreshRequestBody.append("&refresh_token=" + refreshToken);
            refreshRequestBody.append("&client_id=" + client_id);

            HttpRequest refreshRequest = HttpRequest.newBuilder(
                    new URI(tokenAddress))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(refreshRequestBody.toString()))
                    .build();

            HttpResponse<String> refreshResponse = client.send(refreshRequest, HttpResponse.BodyHandlers.ofString());
            if (refreshResponse.statusCode() == 200) // successful
            {
                JsonObject refreshJson = new JsonParser().parse(refreshResponse.body()).getAsJsonObject();
                accessToken = refreshJson.get("access_token").getAsString();
                refreshToken = refreshJson.get("refresh_token").getAsString();
                updateJson();
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
        if (db) LOGGER.info("running SpotifyUtil.refreshActiveSession");

        try
        {
            HttpRequest getDevices = HttpRequest.newBuilder(
                    new URI(playerAddress + "devices"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> devices = client.send(getDevices, HttpResponse.BodyHandlers.ofString());
            JsonArray devicesJson = new JsonParser().parse(devices.body()).getAsJsonObject().get("devices").getAsJsonArray();
            JsonObject currDevice;
            String computerName = InetAddress.getLocalHost().getHostName();
            String thisDeviceID = "";
            if (devicesJson.isEmpty())
            {
                HudifyMain.duration = 1;
                HudifyMain.progress = 0;
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

    public static void updateJson()
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
    public static void putRequest(String type)
    {
        // PUT - Changes and/or replaces resources or collections
        // dont mess with this, getPlaybackInfo() is what you're looking for
        //LOGGER.error("running SpotifyUtil.putRequest");

        try
        {
            HttpRequest putReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.error("PUT Request (" + type + "): " + putRes.statusCode());
            if (putRes.statusCode() == 404)
            {
                refreshActiveSession();
                LOGGER.info("Retrying put request...");
                putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("PUT Request (" + type + "): " + putRes.statusCode());
            }
            else if (putRes.statusCode() == 403)
            {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Spotify Premium is required for this feature."));
            }
            else if (putRes.statusCode() == 401)
            {
                if (refreshAccessToken())
                {
                    putRequest(type);
                }
                else
                {
                    isAuthorized = false;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry put request...");
                putRequest(type);
                LOGGER.info("Successfully sent put request");
            }
            else
            {
                LOGGER.error("exception caught in putRequest():" + e.getMessage());
            }
        }

    }

    public static void postRequest(String type)
    {
        // POST - Creates resources
        // dont mess with this, getPlaybackInfo() is what you're looking for
        try
        {
            HttpRequest postReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
            LOGGER.error("POST Request (" + type + "): " + postRes.statusCode());
            if (postRes.statusCode() == 404) // requested info could not be found
            {
                refreshActiveSession();
                LOGGER.info("Retrying post request...");
                postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("POST Request (" + type + "): " + postRes.statusCode());
            }
            else if (postRes.statusCode() == 403) // forbidden
            {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.sendMessage(Text.of("Spotify Premium is required for this feature."));
            }
            else if (postRes.statusCode() == 401) // unauthorized
            {
                if (refreshAccessToken())
                {
                    postRequest(type);
                }
                else
                {
                    isAuthorized = false;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Attempting to retry post request...");
                postRequest(type);
                LOGGER.info("Successfully sent post request");
            }
            else
            {
                LOGGER.error("exception caught in postRequest():" + e.getMessage());
            }
        }
    }
    //</editor-fold>


    //<editor-fold desc="playback functions">
    public static void nextSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("next");
            HudifyMain.duration = -2;
            LOGGER.info("Skipping to next song");
            // LOGGER.error("duration set to -2000 from nextSong");
        });
    }

    public static void prevSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("previous");
            HudifyMain.duration = -2; // was -2000
            LOGGER.info("Skipping to previous song");
            //LOGGER.error("duration set to -2000 from prevSong");
        });
    }

    public static void playSong() {
        EXECUTOR_SERVICE.execute(() -> putRequest("play"));
    }

    public static void pauseSong() {
        EXECUTOR_SERVICE.execute(() -> putRequest("pause"));
    }

    public static void playPause() {
        //isPlaying == true : pauseSong() ? playSong();
        LOGGER.info("Toggling playback");
        if (isPlaying) {
            pauseSong();
            LOGGER.info("Pausing playback");
        }
        else {
            playSong();
            LOGGER.info("Resuming playback");
        }

//        isPlaying = !isPlaying; // this should update automatically
    }
    //</editor-fold>

    public static void updatePlaybackInfo() // rename to updatePlaybackInfo?
    {
        String dump_msg = "getPlaybackInfo";
        try
        {
            playbackResponse = client.send(playbackRequest, HttpResponse.BodyHandlers.ofString());
            // https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback
            HudifyMain.status_code = playbackResponse.statusCode();
//            LOGGER.info("getPlaybackInfo - status code: " + playbackResponse.statusCode());
            // app closed returns 204

            if (playbackResponse.statusCode() == 429) // Too Many Requests - Rate limiting has been applied.
            {
                return;
            }
            if (playbackResponse.statusCode() == 200) // OK - The request has succeeded
            {
                JsonObject json = (JsonObject) new JsonParser().parse(playbackResponse.body());
                if (json.get("currently_playing_type").getAsString().equals("episode")) /* for podcasts */ {
//                    results[0] = json.get("item").getAsJsonObject().get("name").getAsString();
//                    results[1] = json.get("item").getAsJsonObject().get("show").getAsJsonObject().get("name").getAsString();
//                    results[2] = json.get("progress_ms").getAsString();
//                    results[3] = json.get("item").getAsJsonObject().get("duration_ms").getAsString();
//                    results[4] = json.get("item").getAsJsonObject().get("images").getAsJsonArray().get(1).getAsJsonObject().get("url").getAsString();
//                    return results; // for podcasts
                    HudifyMain.track = json.get("item").getAsJsonObject().get("name").getAsString();
                    String show = json.get("item").getAsJsonObject().get("show").getAsJsonObject().get("name").getAsString();
                    HudifyMain.artists = show;
                    HudifyMain.first_artist = show;
                    HudifyMain.progress = (json.get("progress_ms").getAsInt() / 1000);
                    HudifyMain.duration = (json.get("item").getAsJsonObject().get("duration_ms").getAsInt() / 1000);
//                    results[4] = json.get("item").getAsJsonObject().get("images").getAsJsonArray().get(1).getAsJsonObject().get("url").getAsString();
                    return; // for podcasts
                }


                // the rest is all for normal music tracks
                HudifyMain.track = json.get("item").getAsJsonObject().get("name").getAsString();

                /** artist variables **/
                JsonArray artistArray = json.get("item").getAsJsonObject().get("artists").getAsJsonArray();
                StringBuilder artistString = new StringBuilder();
                HudifyMain.first_artist = artistArray.get(0).getAsJsonObject().get("name").getAsString();
                artistString.append(artistArray.get(0).getAsJsonObject().get("name").getAsString());
                for (int i = 1; i < artistArray.size(); i++) // skips the first artist
                {
                    artistString.append(", ").append(artistArray.get(i).getAsJsonObject().get("name").getAsString());
                }
                HudifyMain.artists = artistString.toString();
                /** artist variables **/


                // the `json.get("progress_ms")` is incorrect after pausing then resuming
                dump_msg += " " + json.get("progress_ms") + " / " + json.get("item").getAsJsonObject().get("duration_ms");

                HudifyMain.progress = (json.get("progress_ms").getAsInt() / 1000);
                HudifyMain.duration = (json.get("item").getAsJsonObject().get("duration_ms").getAsInt() / 1000);
                HudifyMain.context_type = json.get("context").getAsJsonObject().get("type").getAsString();
                switch (HudifyMain.context_type)
                { // playing directly from search result listings throws an exception but gets causght
                    case "artist":
                        HudifyMain.context_name = HudifyMain.first_artist; break;
                    case "album":
                        HudifyMain.context_name = HudifyMain.album;     break;
                    case "show":
                        HudifyMain.context_name = HudifyMain.artists;   break;
                    case "playlist":
                        HudifyMain.context_name = "playlist title goes here. fetch from URI";   break;
                }

                HudifyMain.album = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("name").getAsString();



                isPlaying = json.get("is_playing").getAsBoolean();


//                if (db) LOGGER.info("getPlaybackInfo - isPlaying: " + isPlaying);
            }
            else if (playbackResponse.statusCode() == 401)
            { // Unauthorized - The request requires user authentication or,
                // if the request included authorization credentials, authorization has been refused for those credentials.
                if (!refreshAccessToken())
                {
                    isAuthorized = false;
                }
            }

        } catch (Exception e)
        {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                LOGGER.info("Resetting connection and retrying info get...");
//                results[0] = "Reset";
            }
            else
            {
                LOGGER.error("exception caught in getPlaybackInfo(): " + e.getMessage());
            }
        }
        HudifyMain.dump(dump_msg);
        return;

    }



    public static boolean isAuthorized()
    {
        return isAuthorized;
    }

    public static boolean isPlaying() { return isPlaying; } // getter, makes it so outer classes can't screw it up


//JsonArray imageArray = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray();
//if (imageArray.size() > 1)
//{ results[4] = imageArray.get(1).getAsJsonObject().get("url").getAsString();
//} else { results[4] = null; }


}
