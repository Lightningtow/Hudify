package lightningtow.hudify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
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
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static lightningtow.hudify.util.SpotifyData.*;
import static lightningtow.hudify.HudifyConfig.db;
import static lightningtow.hudify.HudifyMain.Log;
public class SpotifyUtil
{
    private static final String client_id = "2f8c634ba8cc43a8be450ff3f745886f";
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

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

//    private static boolean isAuthorized = false;
//    public static boolean isAuthorized() { return isAuthorized; }
//    private static boolean isPlaying = false; // var from spotify. true if music is playing, false if paused, app closed, anything
//    public static boolean isPlaying() { return isPlaying; }

    //<editor-fold desc="auth utils">
    public static void initialize()
    {
        //Log(Level.INFO,"running SpotifyUtil.initialize()");
        Log(Level.INFO,"initializing Spotify integration");

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
                    Log(Level.INFO,"Created new token file at: " + authFile.getAbsolutePath());
                }
                accessToken = "";
                refreshToken = "";
                sp_is_authorized = false;
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
            Log(Level.ERROR,"exception caught in initialize():" + e.getMessage());
        }
        client = HttpClient.newHttpClient();
        updatePlaybackRequest();
    }

    public static String authorize()
    {
        if (db) Log(Level.INFO,"running SpotifyUtil.authorize()");
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
            Log(Level.ERROR,"exception caught in SpotifyUtil.authorize():" + e.getMessage());
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
        if (db) Log(Level.INFO,"running SpotifyUtil.requestAccessToken");

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
            Log(Level.INFO,"url request" + accessBody);
            HttpResponse<String> accessResponse = client.send(accessRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject accessJson = JsonParser.parseString(accessResponse.body()).getAsJsonObject();
            accessToken = accessJson.get("access_token").getAsString();
            refreshToken = accessJson.get("refresh_token").getAsString();
            updatePlaybackRequest();
            writeAuthFile();
            sp_is_authorized = true;
        } catch (Exception e)
        {
            Log(Level.ERROR,"exception caught in requestAccessToken():" + e.getMessage());
        }
    }

    public static boolean refreshAccessToken()
    {
        // returns true if refreshed successfully, false if could not refresh
        if (db) Log(Level.INFO,"running SpotifyUtil.refreshAccessToken");

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
            if (db) Log(Level.ERROR,"exception caught in refreshAccessToken():" + e.getMessage());
        }
        return false;
    }

    public static void refreshActiveSession()
    {
    // todo this gets devices? is this only needed for the volume thing?
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
                if (db) Log(Level.INFO,"SpotifyUtil.refreshActiveSession: no active device");

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
            if (db) Log(Level.INFO,"RefreshActiveSession - API responded with status code: "
                    + client.send(setActive, HttpResponse.BodyHandlers.ofString()).statusCode());

        } catch (Exception e)
        {
            if (db) Log(Level.ERROR,"exception caught in refreshActiveSession():" + e.getMessage());
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
            Log(Level.ERROR,e.getMessage());
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
            if(db) Log(Level.INFO,"context link: " + link);

            HttpRequest getReq = HttpRequest.newBuilder(new URI(link))
                    .GET()
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
//            Log(Level.INFO,"GET Request (" + getReq + "): " + getRes + " " + getRes.statusCode());
            sp_status_code = getRes.statusCode();
            if (getRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                if(db) Log(Level.INFO,"Retrying get request...");
                getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
//                Log(Level.INFO,"GET Request (" + uri + "): " + getRes.statusCode());
            }
//            else if (getRes.statusCode() == 403) /* forbidden */ {
////               HudifyMain.send_message();
//            }
//            else if (getRes.statusCode() == 401) /* unauthorized */ {
////                if (refreshAccessToken()) getRequest(uri);
////                else isAuthorized = false;
//            }
            else if (getRes.statusCode() == 429) { // rate limited
                // approximately 180 calls per minute without throwing 429, ~3 calls per second
                Log(Level.ERROR,"RATE LIMITED============================================================");
                Thread.sleep(3000);
                return "rate limited!";
    //                        } else if (data[0].equals("Reset")) {
                // getPlaybackInfo returns this if connection reset
    //                            Log(Level.ERROR,"Reset condition, maintaining HUD until reset"); // was level info and from blockiy
            }
//            Log(Level.INFO,"get entire request json " + getRes.body()); // prints entire block of returned json
            JsonObject json = (JsonObject) JsonParser.parseString(getRes.body());
            return (String.valueOf(json.get("name")).replaceAll("\"", "")); // name comes wrapped in quotes
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                Log(Level.INFO,"Attempting to retry get request...");
                getContext(uri);
                Log(Level.INFO,"Successfully sent get request");
            }
            else Log(Level.ERROR,"exception caught in getRequest(): " + e.getMessage());
        }
        return "error in getRequest()";
    }

    public static String scrub(String input) {
        String output = "";
        output = input;

//        String[] blacklist = {"bonus track", "bonus", "intro", "outro", "interlude", "original mix", "single version", "recorded at spotify singles nyc",
//                "spotify singles", "remastere?d? [0-9]{4} \\/ remixed",};

        ArrayList<String> blacklist = new ArrayList<>();
        blacklist.add("bonus track");
        blacklist.add("bonus");
        blacklist.add("intro");
        blacklist.add("outro");
        blacklist.add("interlude");
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

        ArrayList<String> real_blacklist = new ArrayList<>();
        for (String elem : blacklist) {
            real_blacklist.add("- " + elem);
            real_blacklist.add("– " + elem);
            real_blacklist.add("\\/\\/ " + elem);
            real_blacklist.add("\\(" + elem + "\\)");
            real_blacklist.add("\\[" + elem + "\\]");
//            real_blacklist.add(elem);
        }


        for (String elem : real_blacklist) {
            String regex = "(?i)" + elem;
            output = output.replaceAll(regex, "");

        }


        return output.trim();
    }

    //<editor-fold desc="playback functions">
    public static void nextSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("next");
            sp_duration = -2;
            Log(Level.INFO,"Skipping to next song");
            // Log(Level.ERROR,"duration set to -2000 from nextSong");
        });
    }
    public static void prevSong() {
        EXECUTOR_SERVICE.execute(() -> {
            postRequest("previous");
            sp_duration = -2; // was -2000
            Log(Level.INFO,"Skipping to previous song");
            //Log(Level.ERROR,"duration set to -2000 from prevSong");
        });
    }

    public static void togglePlayPause() {
        if (sp_is_playing) { // isPlaying
            if (db) HudifyMain.send_message("Paused playback", 3);
            Log(Level.INFO,"Pausing playback");
            EXECUTOR_SERVICE.execute(() -> putRequest("pause"));
        }
        else {
            if (db) HudifyMain.send_message("Resumed playback", 3);
            Log(Level.INFO,"Resuming playback");
            EXECUTOR_SERVICE.execute(() -> putRequest("play"));
        }

//        isPlaying = !isPlaying; // this should update automatically
    }
    //</editor-fold> playback functions

    //<editor-fold desc="put/post calls">

    // https://developer.spotify.com/documentation/web-api/concepts/api-calls

    // todo can i combine the request functions? theyre mostly duplicated

    public static void putRequest(String type) /* play, pause */ {
        // PUT - Changes and/or replaces resources or collections
        try
        {
            HttpRequest putReq = HttpRequest.newBuilder(new URI("https://api.spotify.com/v1/me/player/" + type))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .header("Authorization", "Bearer " + accessToken).build();
            HttpResponse<String> putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
            Log(Level.INFO,"PUT Request ( +" + type + ")" + putRes.statusCode());
            sp_status_code = putRes.statusCode();
            if (putRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                Log(Level.INFO,"Retrying put request...");
                putRes = client.send(putReq, HttpResponse.BodyHandlers.ofString());
                Log(Level.INFO,"PUT Request ( +" + type + ")" + putRes.statusCode());
            }
            else if (putRes.statusCode() == 403) /* forbidden */ {
//                MutableText msg = Text.translatable("hudify.messages.premium_required");
                HudifyMain.send_message("why does spotify say you're forbidden from pausing", 5);
            }
            else if (putRes.statusCode() == 401) /* unauthorized */ {
                if (refreshAccessToken()) putRequest(type);
                else sp_is_authorized = false;
            }
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                Log(Level.INFO,"Attempting to retry put request...");
                putRequest(type);
                Log(Level.INFO,"Successfully sent put request");
            }
            else Log(Level.ERROR,"exception caught in putRequest(): " + e.getMessage());
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
            Log(Level.ERROR,"POST Request (" + type + "): " + postRes.statusCode());
            sp_status_code = postRes.statusCode();

            if (postRes.statusCode() == 404) /* not found */ {
                refreshActiveSession();
                Log(Level.INFO,"Retrying post request...");
                postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                Log(Level.INFO,"POST Request (" + type + "): " + postRes.statusCode());
            }
            else if (postRes.statusCode() == 403) /* forbidden */ {
                HudifyMain.send_message("Spotify premium is required for this", 5);
//                HudifyMain.send_message();
            }
            else if (postRes.statusCode() == 401) /* unauthorized */ {
                if (refreshAccessToken()) postRequest(type);
                else sp_is_authorized = false;
            }

        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            if (e instanceof IOException && e.getMessage().equals("Connection reset"))
            {
                Log(Level.INFO,"Attempting to retry post request...");
                postRequest(type);
                Log(Level.INFO,"Successfully sent post request");
            }
            else Log(Level.ERROR,"exception caught in postRequest():" + e.getMessage());
        }
    }

    //</editor-fold> put/post calls




//JsonArray imageArray = json.get("item").getAsJsonObject().get("album").getAsJsonObject().get("images").getAsJsonArray();
//if (imageArray.size() > 1)
//{ results[4] = imageArray.get(1).getAsJsonObject().get("url").getAsString();
//} else { results[4] = null; }


}
