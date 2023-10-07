package LungoBrowser.webrenderer;

import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Random;

import LungoBrowser.App;
import LungoBrowser.Drawer;
import LungoBrowser.UI.GraphicsPlus;

public class HomePage {
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

    public HomePage() {
        Logo = App.GetImage("Images/Lungo_Logo.png");

        var randomBgImage = bgImages.get(new Random().nextInt(0, bgImages.size() - 1));
        BackgroundImage = App.GetImage("Images/backgroundImages/" + randomBgImage);
    }

    public void draw(Dimension size, GraphicsPlus g) {
        var img = App.coverImageWithinBounds(size.width, size.height,
                App.toBufferedImage(BackgroundImage));
        g.drawImage(img, 0, 0, size.width, size.height, null);
        g.drawHitbox(1, 1, size.width - 2, size.height - 2);

        Drawer.DrawImageScreen(size, g, Logo, null);
    }
}
