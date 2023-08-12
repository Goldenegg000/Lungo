package LungoBrowser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import static LungoBrowser.Debug.Log;
// import static LungoBrowser.Debug.Warn;
import static LungoBrowser.Debug.Error;

// {domain}:{input}

public class UrlParser {

    public static URI toURI(String url) {
        try {
            URI uri = new URI(MakeValidUrl(url));
            return uri;
        } catch (URISyntaxException e) {
            Error("couldn't parse url:", url);
        }
        return null;
    }

    public static String MakeValidUrl(String url) {
        if (url.isEmpty())
            return "";
        url = url.trim();
        url = url.replaceAll("\\\\", "/");
        url = url.replaceAll("file:/*", "file:///");

        // Extract the path using regex
        String regex = "(file:[^?#]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String path = matcher.group(1);
            // Encode the path
            String encodedPath = path.replaceAll(" ", "%20");
            // Replace the original path with the encoded path
            // Log(url.replace(path, encodedPath));
            return url.replace(path, encodedPath);
        }

        return url;
    }

    public static String UrlDecode(String url) {
        final String regex = "%[0-9][0-9]";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(url);

        var newUrl = url;
        while (matcher.find()) {
            var finding = matcher.group(0);
            // System.out.println("Full match: " + finding);
            byte[] charByte = { ((byte) Integer.parseInt(finding.replace("%", ""), 16)) };
            newUrl = newUrl.replace(finding, new String(charByte, StandardCharsets.UTF_8));
        }
        // url = url.replace("%20", " ");
        return newUrl;
    }

    public static Pair<byte[], HttpURLConnection> downloadFromUri(URI link) {
        BufferedInputStream in;
        try {
            var url = new URL(link.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.addRequestProperty("Browser", "Lungo");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 - Lungo/" + App.ver.toString()); // add the user
            // agent to trick any server that
            // we are a browser #HackerMan
            InputStream input;
            if (connection.getResponseCode() == 200) { // this must be called before 'getErrorStream()' works
                input = connection.getInputStream();
            } else {
                Debug.Error("Invalid Response!: " + connection.getResponseCode());
                return new Pair<>(null, connection);
            }
            in = new BufferedInputStream(input);
            return new Pair<>(in.readAllBytes(), connection);
        } catch (IOException e) {
        }
        return null;
    }

    // public static void main(String[] args) {
    // Debug.Log(new
    // String(downloadFromUri(toURI("https://www.youtube.com/watch?v=IEiPL9PUVYw")),
    // StandardCharsets.UTF_8));
    // }
}
