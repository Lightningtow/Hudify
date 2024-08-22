package lightningtow.hudify.util;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lightningtow.hudify.HudifyMain;
import org.apache.logging.log4j.Level;
public class AuthServerHandler implements HttpHandler
{
    /**
    A huge thank you to Erruqie's Blockify for this code!
     */

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String requestParamValue = null;
        if ("GET".equals(httpExchange.getRequestMethod())) { requestParamValue = handleGetRequest(httpExchange); }
        try {
            handleResponse(httpExchange, requestParamValue);
        } catch (URISyntaxException | InterruptedException e) { HudifyMain.LogThis(Level.ERROR, e.getMessage());
        }
    }
    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
    }
    private void handleResponse (HttpExchange httpExchange, String requestParamValue)
            throws IOException, URISyntaxException, InterruptedException
    {
        OutputStream outputStream = httpExchange.getResponseBody();
//htmlBuilder.append("<html>").append("<body>").append("<h1>").append("Success!").append("</h1>").append("</body?").append("</html>");
        String htmlResponse = "<html><body><h1>Success!</h1></body?</html>";
        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        lightningtow.hudify.util.SpotifyUtil.authorize(requestParamValue);
    }
    /**  PKCE UTILS */
    public static String generateCodeVerifier()
            throws UnsupportedEncodingException
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
    public static String generateCodeChallenge(String codeVerifier)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException
    {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}