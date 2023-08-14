package LungoBrowser.webrenderer;

import java.awt.Color;
import java.awt.Dimension;

import LungoBrowser.Drawer;
import LungoBrowser.UI.GraphicsPlus;

public class ErrorPage {
    public void draw(Dimension size, GraphicsPlus g, Drawer.ContentType typ) {
        // Debug.Log("help!");
        g.drawString("ERROR: " + typ.toString(), 5, 15, Color.white);
    }
}
