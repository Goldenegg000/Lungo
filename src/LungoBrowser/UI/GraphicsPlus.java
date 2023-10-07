package LungoBrowser.UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import LungoBrowser.Debug;

public class GraphicsPlus {

    public Graphics G;
    public Font currentFont;

    public GraphicsPlus(Graphics g) {
        G = g;
    }

    public void drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        G.drawImage(img, x, y, width, height, observer);
        drawHitbox(x, y, width, height);
    }

    public void drawHitbox(int x, int y, int width, int height, Color c) { // draws a hitbox
        if (!Debug.ifFlag("box")) // don't draw hitbox if the -box flag is disabled
            return;
        G.setColor(c);
        G.drawRect(x, y, width, height);
    }

    public void drawHitbox(int x, int y, int width, int height) { // draws a hitbox
        if (!Debug.ifFlag("box")) // don't draw hitbox if the -box flag is disabled
            return;
        G.setColor(Color.green);
        G.drawRect(x, y, width, height);
    }

    public void fillRect(int x, int y, int width, int height, Color c) {
        G.setColor(c);
        G.fillRect(x, y, width, height);
    }

    public void setFont(String fontName, Integer fontStyle, Integer size) {
        if (fontName == null)
            fontName = "Arial";
        if (fontStyle == null)
            fontStyle = Font.PLAIN;
        if (size == null)
            size = 10;
        setFont(new Font(fontName, fontStyle, size));
    }

    public void setFont(Font font) {
        currentFont = font;
    }

    public void drawString(String text, int x, int y, Color c) {
        G.setColor(c);
        G.setFont(currentFont);
        G.drawString(text, x, y);
        // drawHitbox(x, y, );
    }
}
