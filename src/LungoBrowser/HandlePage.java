package LungoBrowser;

import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;

import static LungoBrowser.Debug.Log;
import LungoBrowser.UI.PageTab;
// import static LungoBrowser.Debug.Warn;
// import static LungoBrowser.Debug.Error;

public class HandlePage {
    public Drawer drawer;
    public URI Url;
    public Window self;
    public ArrayList<String> UrlHistory = new ArrayList<String>();
    public int UrlHistoryIndx = 0;

    private static final String DefTabName = "Lungo Homepage";

    public PageTab PageTab;

    public void UrlUpdated(String url) {
        Log("updated url: " + url);
        Url = UrlParser.toURI(url);
        drawer.UrlUpdated(Url);
        self.frame.repaint();

        if (url == "") {
            PageTab.setText(DefTabName);
        }
        if (Url == null || url == "")
            PageTab.setText(DefTabName);
        else {
            if (Url.getHost() != null)
                PageTab.setText(Url.getHost());
            else
                PageTab.setText(DefTabName);
        }
    }

    HandlePage handler;

    HandlePage(Window self, String url) {
        drawer = new Drawer(self, this);
        Url = UrlParser.toURI(url);
        this.self = self;
        handler = this;

        PageTab = new PageTab(self, this);

        PageTab.addMouseListener(new MouseDown());

        PageTab.setText(url);
        if (url == "") {
            PageTab.setText("Lungo Homepage");
        }
    }

    private class MouseDown implements MouseListener {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            self.setCurrentPage(handler);
            Debug.Log(self.getCurrentPage());
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
        }

    }
}
