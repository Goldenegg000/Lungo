package LungoBrowser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;

import LungoBrowser.UI.GraphicsPlus;
import LungoBrowser.webrenderer.*;

import static LungoBrowser.Debug.Log;
// import static LungoBrowser.Debug.Warn;
// import static LungoBrowser.Debug.Error;

public class Drawer {

    private Window main;
    private HandlePage PageHandler;

    private HomePage homePage = new HomePage();
    private ErrorPage errorPage = new ErrorPage();

    public Drawer(Window self, HandlePage pageH) {
        super();

        if (Debug.ifFlag("box"))
            Debug.Warn("hitbox's Enabled");

        main = self;
        PageHandler = pageH;
    }

    String contentType;
    ContentType contentTypeResult;

    public void UrlUpdated(URI url) {
        Pair<byte[], HttpURLConnection> content = null;

        StaticImage = null;

        if (url == null) {
            Log("failed to load uri");
            return;
        }

        if (App.isLocalFileURI(url)) {
            Log("The URI represents a local file.");
            content = new Pair<>(null, null);
            content.Value1 = App.convertToByteArray(App.getFileContents(Paths.get(url).toAbsolutePath().toString()));
        } else if (App.isValidUrl(url)) {
            Log("The URI represent a url.");
            if (PageHandler != null) {
                Log("Downloading content...");
                content = UrlParser.downloadFromUri(PageHandler.Url);
            } else {
                Debug.Warn("PageHandler is null!");
                Log("Downloading content...");
                content = UrlParser.downloadFromUri(url);
            }
        } else if (url.getPath().equals("")) {
            contentTypeResult = ContentType.home;
            Log("loading homepage");
            return;
        }

        if (content == null) {
            contentTypeResult = ContentType.invalid;
            Log("failed getting data from uri");
            return;
        }

        Log("checking for data type");
        contentType = getContentType(content);
        contentTypeResult = getContentSimpleType(contentType);
        Debug.Log(contentType);

        BufferedImage loadImg = null;
        if (contentTypeResult == ContentType.image) {
            Log("matched image, converting to image...");
            loadImg = App.GetImageFromData(content.Value1);
            if (loadImg != null) {
                StaticImage = loadImg;
                Log("image was loaded :D");
                return;
            }
            Debug.Error("could not load image! type: " + contentType);
            contentTypeResult = ContentType.invalid;
            // Debug.Write(content.Value1, "debug.jpg");
            return;
        }

        Log("no types matched");
        contentTypeResult = ContentType.home;
    }

    Image StaticImage = null;

    private String getContentType(Pair<byte[], HttpURLConnection> content) {
        var contentType = "";
        if (content.Value2 != null)
            contentType = content.Value2.getHeaderField("content-type");
        return contentType;
    }

    public enum ContentType {
        image,
        invalid, home;
    }

    private ContentType getContentSimpleType(String conString) {
        if (conString.equals("image/jpg")
                || conString.equals("image/jpeg")
                || conString.equals("image/png")
                || conString.equals("image/webp"))
            return ContentType.image;
        return ContentType.invalid;
    }

    public void Paint(Dimension size, Graphics graph) {

        GraphicsPlus g = new GraphicsPlus(graph);
        g.setFont(null, null, 14);

        if (main != null)
            main.urlField.setColumns(main.getUrlFieldColumnsAmount());

        g.fillRect(0, 0, size.width, size.height, new Color(0x20, 0x20, 0x20));

        if (contentTypeResult == null)
            return;
        switch (contentTypeResult) {
            case image:
                DrawImageScreen(size, g, StaticImage, " Typ:" + contentType);
                break;
            case invalid:
                errorPage.draw(size, g, contentTypeResult);
                break;
            case home:
                homePage.draw(size, g);
        }
    }

    public void PaintLoading(Dimension size, Graphics graph) {
        GraphicsPlus g = new GraphicsPlus(graph);
        g.fillRect(0, 0, size.width, size.height, new Color(0x20, 0x20, 0x20));
        g.setFont(null, null, 50);
        var tWidth = g.G.getFontMetrics(g.currentFont).stringWidth("LOADING");
        g.drawString("LOADING", size.width / 2 - tWidth / 2, size.height / 2, Color.WHITE);
    }

    public static void DrawImageScreen(Dimension size, GraphicsPlus g, Image img, String extra) {
        int padding = 10;

        Dimension ImageSize = App.getDimensionOfImage(img);
        Image Ratioed = App.fitImageWithinBounds(size.width - padding * 3, size.height - padding * 3,
                App.toBufferedImage(img));

        g.drawImage(Ratioed, padding, padding, size.width - padding * 2, size.height - padding * 2, null);

        if (extra == null)
            return;
        g.drawString("w:" + ImageSize.width + " h:" + ImageSize.height + extra, 10, size.height - padding, Color.WHITE);
    }
}
