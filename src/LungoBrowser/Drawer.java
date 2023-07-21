package LungoBrowser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import LungoBrowser.UI.GraphicsPlus;

import static LungoBrowser.Debug.Log;
// import static LungoBrowser.Debug.Warn;
// import static LungoBrowser.Debug.Error;

public class Drawer {

    private Window main;
    private HandlePage PageHandler;
    private Image Logo;
    private Image BackgroundImage;
    private static ArrayList<String> bgImages = new ArrayList<>();
    {
        bgImages.add("Beach.jpg");
        bgImages.add("BeachSunset.jpg");
        bgImages.add("BlueHills.jpg");
        bgImages.add("BlueWaterSea.jpg");
        bgImages.add("CliffCave.jpg");
        bgImages.add("ForestRiver.jpg");
        bgImages.add("NatureHills.jpg");
        bgImages.add("NeoMountain.jpg");
        bgImages.add("Wave.jpg");
    }

    public Drawer(Window self, HandlePage pageH) {
        super();

        if (Debug.ifFlag("box"))
            Debug.Warn("hitbox's Enabled");

        main = self;
        PageHandler = pageH;

        Logo = App.GetImage("Images/Lungo_Logo.png");
        changeRandomBackgroundImage();
    }

    public void changeRandomBackgroundImage() {
        var randomBgImage = bgImages.get(new Random().nextInt(0, bgImages.size() - 1));
        BackgroundImage = App.GetImage("Images/backgroundImages/" + randomBgImage);
    }

    public void UrlUpdated(URI url) {
        Pair<byte[], HttpURLConnection> content = null;

        if (url == null) {
            Log("failed to load uri");
            return;
        }

        if (App.isLocalFileURI(url)) {
            Log("The URI represents a local file.");
            content = new Pair<>(null, null);
            content.Value1 = App.convertToByteArray(App.getFileContents(Paths.get(url).toAbsolutePath().toString()));
        } else {
            Log("The URI represent a url.");
            if (PageHandler != null) {
                Log("Downloading content...");
                content = UrlParser.downloadFromUri(PageHandler.Url);
            } else {
                Debug.Warn("PageHandler is null!");
                Log("Downloading content...");
                content = UrlParser.downloadFromUri(url);
            }
        }
        StaticImage = null;
        if (content == null) {
            Log("failed getting data from uri");
            return;
        }

        Log("checking for data type");
        var contentType = "file";
        if (content.Value2 != null)
            contentType = content.Value2.getHeaderField("content-type");
        Debug.Log(contentType);
        BufferedImage loadImg = null;
        if (contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/png")
                || contentType.equals("image/webp")
                || contentType.equals("file")) {
            Log("matched image, converting to image...");
            loadImg = App.GetImageFromData(content.Value1);
            if (loadImg != null) {
                StaticImage = loadImg;
                Log("image was loaded :D");
                return;
            }
            Debug.Error("could not load image! type: " + contentType);
            Debug.Write(content.Value1, "debug.jpg");
            return;
        }

        Log("no types matched");
    }

    Image StaticImage = null;

    public void Paint(Dimension size, Graphics graph) {

        GraphicsPlus g = new GraphicsPlus(graph);
        g.setFont(null, null, 14);

        if (main != null)
            main.urlField.setColumns(main.getUrlFieldColumnsAmount());

        g.fillRect(0, 0, size.width, size.height, new Color(0x20, 0x20, 0x20));

        // Debug.Log(size);

        if (StaticImage != null) {
            DrawImageScreen(size, g, StaticImage);
        } else {
            var img = App.coverImageWithinBounds(size.width, size.height,
                    App.toBufferedImage(BackgroundImage));
            g.drawImage(img, 0, 0, size.width, size.height, null);
            g.drawHitbox(1, 1, size.width - 2, size.height - 2);

            DrawImageScreen(size, g, Logo);
        }

        // Debug.Log(size);

    }

    public void PaintLoading(Dimension size, Graphics graph) {
        GraphicsPlus g = new GraphicsPlus(graph);
        g.fillRect(0, 0, size.width, size.height, new Color(0x20, 0x20, 0x20));
        g.setFont(null, null, 50);
        var tWidth = g.G.getFontMetrics(g.currentFont).stringWidth("LOADING");
        // Debug.Log(tWidth);
        g.drawString("LOADING", size.width / 2 - tWidth / 2, size.height / 2, Color.WHITE);
    }

    public void DrawImageScreen(Dimension size, GraphicsPlus g, Image img) {
        int padding = 10;

        Dimension ImageSize = App.getDimensionOfImage(img);
        Image Ratioed = App.fitImageWithinBounds(size.width - padding * 3, size.height - padding * 3,
                App.toBufferedImage(img));

        g.drawImage(Ratioed, padding, padding, size.width - padding * 2, size.height - padding * 2, null);

        g.drawString("W:" + ImageSize.width + " H:" + ImageSize.height, 10, size.height - padding, Color.WHITE);
    }
}
