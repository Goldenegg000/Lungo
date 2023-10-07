package LungoBrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
// import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.awt.event.WindowAdapter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import LungoBrowser.UI.*;

import static LungoBrowser.Debug.Log;
// import static LungoBrowser.Debug.Warn;
// import static LungoBrowser.Debug.Error;

public class Window extends Thread {

    JFrame frame;
    private int targetFPS = 30;

    public static class ColorProfile {
        public Color bgColorDef = new Color(0x202020);
        public Color navColor = new Color(0x151515);
        public Color buttonColor = new Color(0x303030);
        public Color TextColor = new Color(0xdddddd);
        public Color FretColor = TextColor;
        public Color HighlightColor = new Color(0xf06220);
    }

    public static class KeyBind {
        public boolean ControlDown;
        public boolean AltDown;
        public int KeyCode;

        public KeyBind(boolean controlDown, boolean altDown, int keyCode) {
            ControlDown = controlDown;
            AltDown = altDown;
            KeyCode = keyCode;
        }

        public boolean pressed(KeyEvent e) {
            return (e.isControlDown() == ControlDown)
                    && (e.isAltDown() == AltDown)
                    && (e.getKeyCode() == KeyCode);
        }
    }

    public static class KeyBindProfile {
        public static KeyBind FullScreen = new KeyBind(false, false, KeyEvent.VK_F11);
        public static KeyBind NewWindow = new KeyBind(true, false, KeyEvent.VK_N);
        public static KeyBind CloseWindow = new KeyBind(true, false, KeyEvent.VK_BACK_SPACE);
    }

    public String LoadedUrl = "";
    public ColorProfile currentColorProfile = new ColorProfile();
    public Dimension DefaultSize = new Dimension(1920 / 2, 1080 / 2);
    public boolean isFullScreen = false;

    public Font currentFont;

    @Override
    public void run() {

        frame = new JFrame();
        frame.pack(); // trick the JFrame to calculate the insets
        frame.setMinimumSize(new Dimension(600, 600 + frame.getInsets().top + NavBarHeight));
        frame.setSize(DefaultSize);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                CloseThisWindow();
            }
        });

        frame.setIconImage(App.GetImage("Images/Lungo_Icon_Bg.png"));
        try {
            currentFont = Font.createFonts(App.getFileInputStream("Fonts/Ubuntu-Regular.ttf"))[0];
        } catch (FontFormatException | IOException | URISyntaxException e1) {
            Debug.Error("could not load font: \"Fonts/Ubuntu-Regular.ttf\" in jar Resource");
            e1.printStackTrace();
            System.exit(1);
        }

        SetupBrowserUi(LoadedUrl);
        // Debug.Log(frame.getInsets().top);

        // var handler = PageHandlers.get(currentPageIndx);
        // handler.UrlHistory.add(LoadedUrl);

        if (isFullScreen)
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(new KeyWindowListener(""));
        frame.addMouseListener(new MouseWindowListener());
        urlField.addKeyListener(new KeyWindowListener("urlField"));
        searchButton.addKeyListener(new KeyWindowListener(""));
        HomeButton.addKeyListener(new KeyWindowListener(""));
        previousPage.addKeyListener(new KeyWindowListener(""));
        nextPage.addKeyListener(new KeyWindowListener(""));
        Reload.addKeyListener(new KeyWindowListener(""));

        // urlField.setFont(currentFont.deriveFont(Font.PLAIN, 20));

        frame.addMouseMotionListener(new MouseMove());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        }, 0, 1000 / targetFPS);
    }

    public JTextField urlField;

    private JAdvancedButton searchButton;
    private JAdvancedButton HomeButton;
    private JAdvancedButton previousPage;
    private JAdvancedButton nextPage;
    private JAdvancedButton Reload;

    private JPanel mainPanel;
    private JPanel menuPanel;
    private JPanel tabbar;
    private JPanel navbar;
    private ArrayList<HandlePage> PageHandlers;
    private int currentPageIndx = 0;

    private static Image SearchLense;
    private static Image home;
    private static Image before;
    private static Image next;
    private static Image reload;
    private static Image reloading;
    private static Image newTab;

    private void LoadUiImages(float uISize) {
        int ButtonSize = Math.round(uISize) + 3;
        if (SearchLense == null)
            SearchLense = App.GetImage("Images/Icons/search.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (home == null)
            home = App.GetImage("Images/Icons/home-page-filled.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (before == null)
            before = App.GetImage("Images/Icons/left.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (next == null)
            next = App.GetImage("Images/Icons/right.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (reload == null)
            reload = App.GetImage("Images/Icons/reload.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (reloading == null)
            reloading = App.GetImage("Images/Icons/reloading.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
        if (newTab == null)
            newTab = App.GetImage("Images/Icons/newTab.png")
                    .getScaledInstance(ButtonSize, ButtonSize, java.awt.Image.SCALE_SMOOTH);
    }

    public final float UISize = 18f * 1.2f;
    public final int NavBarHeight = (int) (UISize + 15) * 2;

    private void SetupBrowserUi(String url) {
        urlField = new JTextField(url); // quick and dirty fix by creating the urlField first.
        // urlField.setFont(new Font("Arial", Font.PLAIN, 20));

        PageHandlers = new ArrayList<>();
        addPageHandler(new HandlePage(this, url)); // add a page
        Update(); //

        LoadUiImages(UISize);

        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 2, 2);
        BorderLayout menuLayout = new BorderLayout();
        BorderLayout bl = new BorderLayout();

        mainPanel = new JPanel();
        menuPanel = new JPanel(menuLayout);
        navbar = new JPanel(fl);
        tabbar = new JPanel(fl);
        var BevelBorder = BorderFactory.createRaisedBevelBorder();
        var NoBorder = BorderFactory.createEmptyBorder();
        menuPanel.setBorder(BevelBorder);
        menuPanel.setSize(new Dimension(Integer.MAX_VALUE, NavBarHeight));
        navbar.setBorder(NoBorder);
        navbar.setSize(new Dimension(Integer.MAX_VALUE, NavBarHeight / 2));
        tabbar.setBorder(NoBorder);
        tabbar.setSize(new Dimension(Integer.MAX_VALUE, NavBarHeight / 2));

        menuPanel.setBackground(currentColorProfile.navColor);
        navbar.setBackground(currentColorProfile.navColor);
        tabbar.setBackground(currentColorProfile.navColor);
        // navbar.setLayout(new FlowLayout());

        urlField.setBorder(BevelBorder);
        urlField.setFont(currentFont.deriveFont(UISize - 1));
        // urlField.setMaximumSize(new Dimension(10000, NavBarHeight));

        urlField.setBackground(currentColorProfile.buttonColor);
        urlField.setForeground(currentColorProfile.TextColor);
        urlField.setCaretColor(currentColorProfile.FretColor);
        urlField.setSelectionColor(currentColorProfile.HighlightColor);

        searchButton = createUiButton(SearchLense, currentFont, BevelBorder, (ActionEvent e) -> {
            PaintLoading(true);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UpdateUrl(null, true);
                    PaintLoading(false);
                    Log("pressed search button");
                }
            });
        }, true);

        HomeButton = createUiButton(home, currentFont, BevelBorder, (ActionEvent e) -> {
            var handler = PageHandlers.get(currentPageIndx);
            handler.UrlHistoryIndx = 0;
            handler.UrlHistory = new ArrayList<>();
            PaintLoading(true);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UpdateUrl(null, false);
                    PaintLoading(false);
                    Log("pressed home button");
                }
            });
        }, true);

        previousPage = createUiButton(before, currentFont, BevelBorder, (ActionEvent e) -> {
            previousPage();
            Log("pressed previous button");
        }, true);

        nextPage = createUiButton(next, currentFont, BevelBorder, (ActionEvent e) -> {
            nextPage();
            Log("pressed next button");
        }, true);

        Reload = createUiButton(reload, currentFont, BevelBorder, (ActionEvent e) -> {
            Reload.setIcon(new ImageIcon(reloading));
            PaintLoading(true);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UpdateUrl(null, false);
                    // Update();
                    PaintLoading(false);
                    Reload.setIcon(new ImageIcon(reload));
                    Log("reloaded webpage");
                }
            });
        }, true);

        AddNewTabButton = createUiButton(newTab, currentFont, BevelBorder, (ActionEvent e) -> {
            addPageHandler(new HandlePage(this, ""));
            Update();
        }, false);
        AddNewTabButton.setBackground(new Color(0, 0, 0, 0));

        menuPanel.add(tabbar, BorderLayout.NORTH);
        menuPanel.add(navbar, BorderLayout.SOUTH);

        mainPanel.add(menuPanel);

        navbar.add(HomeButton);
        navbar.add(previousPage); // add all ui elements to the navbar
        navbar.add(nextPage);
        navbar.add(Reload);
        navbar.add(urlField);
        navbar.add(searchButton);

        mainPanel.setLayout(bl);

        mainPanel.add(pageDrawer);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.getContentPane().setBackground(currentColorProfile.bgColorDef);

        frame.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent arg0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10); // adding a fucking delay so it resizes it correctly <-<
                    urlField.setColumns(getUrlFieldColumnsAmount()); // need to set the colum amount because we cant set
                                                                     // the width o.o
                } catch (InterruptedException e) {
                    e.printStackTrace(); // just don't care tbh, like How does a Interrupt error even exist?
                }
            }
        });

        frame.pack();
        urlField.setColumns(getUrlFieldColumnsAmount());

        UpdateUrl(url, true);
    }

    private PageDrawer pageDrawer = new PageDrawer();

    public class PageDrawer extends JPanel {

        private boolean isLoading = false;
        private BufferedImage img;

        public void PaintLoading(boolean loading) {
            isLoading = loading;
        }

        @Override
        public void paintComponent(Graphics g) {

            img = new BufferedImage((int) getSize().getWidth(),
                    (int) getSize().getHeight() - NavBarHeight, BufferedImage.TYPE_INT_ARGB);
            var drawer = PageHandlers.get(currentPageIndx).drawer;

            if (isLoading)
                drawer.PaintLoading(
                        new Dimension((int) getSize().getWidth(), (int) getSize().getHeight() - NavBarHeight),
                        img.getGraphics());
            else
                drawer.Paint(new Dimension((int) getSize().getWidth(), (int) getSize().getHeight() - NavBarHeight),
                        img.getGraphics());

            g.drawImage(img, 0, 0 + NavBarHeight, (int) getSize().getWidth(),
                    (int) getSize().getHeight() - NavBarHeight, null);
        }
    }

    public void setCurrentPage(int page) {
        currentPageIndx = page;
        UpdateUrl(null, false, true);
        Update();
        frame.repaint();
        // Reload.setIcon(new ImageIcon(reload));
        Log("switched tab");
    }

    public int getCurrentPage() {
        return currentPageIndx;
    }

    public void setCurrentPage(HandlePage page) {
        var i = 0;
        for (var currpage : PageHandlers) {
            if (currpage.equals(page)) {
                setCurrentPage(i);
                return;
            }
            i++;
        }
    }

    public JAdvancedButton createUiButton(Image imgIcon, Font font, Border border,
            Consumer<ActionEvent> calB, boolean Highlight) {
        var button = new JAdvancedButton();
        button.setIcon(new ImageIcon(imgIcon));
        button.setFont(font.deriveFont(UISize));
        button.setBorder(border);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                calB.accept(e);
            }

        });

        button.setBackground(currentColorProfile.buttonColor);
        if (Highlight) {
            button.setHoverBackgroundColor(currentColorProfile.HighlightColor.darker().darker());
            button.setPressedBackgroundColor(currentColorProfile.HighlightColor);
        }

        return button;
    }

    public int getUrlFieldColumnsAmount() {
        var result = Math.min(Math.floorDiv((int) ((frame.getWidth() - 300) / urlField.getFont().getSize()), 1), 60);
        if (result < 10) {
            result = 10;
        }
        return result;
    }

    private JAdvancedButton AddNewTabButton;

    public void Update() {
        // frame.setTitle(App.AppName);
        frame.setTitle(App.AppName + " " + App.ver + " - ID:" + App.getWindowID(this));
        if (!updated)
            updateTabBar();
    }

    public void addPageHandler(HandlePage pageHandler) {
        PaintLoading(true);
        PageHandlers.add(pageHandler);
        currentPageIndx = PageHandlers.size() - 1;
        updateTabBar();
        frame.repaint();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                UpdateUrl("", false);
                PaintLoading(false);
            }
        });
    }

    public void removePageHandler(HandlePage pageHandler) {
        PageHandlers.remove(pageHandler);
        if (PageHandlers.size() == 0)
            CloseThisWindow();
        if (currentPageIndx > PageHandlers.size() - 1)
            currentPageIndx = PageHandlers.size() - 1;
        else if (currentPageIndx > 0)
            currentPageIndx--;
        updateTabBar();
        frame.repaint();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                UpdateUrl("", false, true);
            }
        });
    }

    boolean updated = false;

    private void updateTabBar() {
        // Debug.Log(tabbar);
        if (tabbar != null) {
            updated = true;
            List<PageTab> pageTabs = new ArrayList<>();

            for (HandlePage handlePage : PageHandlers) {
                pageTabs.add(handlePage.PageTab);
            }

            tabbar.removeAll();
            for (PageTab pageTab : pageTabs) {
                tabbar.add(pageTab);
            }

            tabbar.add(AddNewTabButton);

            frame.revalidate();
        } else {
            updated = false;
        }
    }

    public void UpdateUrl(String url, boolean New) {
        UpdateUrl(url, New, false);
    }

    public void UpdateUrl(String url, boolean New, boolean noRefresh) {
        if (url == null) {
            url = urlField.getText();
        }
        url = UrlParser.MakeValidUrl(url);

        var handler = PageHandlers.get(currentPageIndx);

        // Debug.Warn(url.equals(""));

        if (New && !url.equals("")) {
            while (handler.UrlHistory.size() > handler.UrlHistoryIndx) {
                handler.UrlHistory.remove(handler.UrlHistory.size() - 1);
            }
            if (handler.UrlHistoryIndx == 0 || !handler.UrlHistory.get(handler.UrlHistoryIndx - 1).equals(url)) {
                handler.UrlHistory.add(url);
                handler.UrlHistoryIndx++;
            }
        }

        if (handler.UrlHistoryIndx < 0) {
            // handler.UrlHistory.add("");
            handler.UrlHistoryIndx++;
        }

        if (noRefresh) {
            if (handler.UrlHistoryIndx == 0)
                urlField.setText("");
            else
                urlField.setText(UrlParser.UrlDecode(handler.UrlHistory.get(handler.UrlHistoryIndx - 1)));
        } else if (handler.UrlHistoryIndx == 0) {
            urlField.setText("");
            handler.UrlUpdated("");
        } else {
            urlField.setText(UrlParser.UrlDecode(handler.UrlHistory.get(handler.UrlHistoryIndx - 1)));
            handler.UrlUpdated(handler.UrlHistory.get(handler.UrlHistoryIndx - 1));
        }

        // Log(handler.UrlHistory, handler.UrlHistoryIndx);
    }

    public void previousPage() {
        var handler = PageHandlers.get(currentPageIndx);
        // handler.UrlHistory.remove(handler.UrlHistory.size() - 1);
        if (handler.UrlHistoryIndx > 0) {
            handler.UrlHistoryIndx--;
            PaintLoading(true);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UpdateUrl("", false);
                    PaintLoading(false);
                }
            });
        }
        // if (handler.UrlHistoryIndx > 0)
        // UpdateUrl(handler.UrlHistory.get(handler.UrlHistory.size() - 1), false);
        // else {
        // UpdateUrl("", false);
        // handler.UrlHistoryIndx = 0;
        // }
    }

    public void nextPage() {
        var handler = PageHandlers.get(currentPageIndx);
        if (handler.UrlHistory.size() > handler.UrlHistoryIndx) {
            handler.UrlHistoryIndx++;
            PaintLoading(true);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    UpdateUrl("", false);
                    PaintLoading(false);
                }
            });
        }
    }

    public void CloseThisWindow() { // dispose of this window
        frame.dispose();
        App.RemoveWindow(this);
    }

    @SuppressWarnings("deprecation")
    private void PaintLoading(boolean is) {
        if (is) {
            frame.setCursor(Cursor.WAIT_CURSOR);
        } else {
            frame.setCursor(Cursor.DEFAULT_CURSOR);
        }
        pageDrawer.PaintLoading(is);
        frame.repaint();
    }

    private class MouseWindowListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            frame.setFocusable(true);
            frame.requestFocusInWindow();
            frame.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }

    private class MouseMove implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

    }

    private class KeyWindowListener implements KeyListener {
        String key;

        public KeyWindowListener(String key) {
            this.key = key;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // urlField.setText(PageHandlers.get(currentPageIndx).Url);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
            Integer keyCode = e.getKeyCode();

            if (key == "urlField") {
                if (keyCode.equals(KeyEvent.VK_ESCAPE)) {
                    urlField.setText(PageHandlers.get(currentPageIndx).Url.toString());
                    frame.requestFocusInWindow();
                }
                if (keyCode.equals(KeyEvent.VK_ENTER)) {
                    PaintLoading(true);
                    if (urlField.getText().isEmpty()) {
                        var handler = PageHandlers.get(currentPageIndx);
                        handler.UrlHistoryIndx = 0;
                        handler.UrlHistory = new ArrayList<>();
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            UpdateUrl(null, true);
                            PaintLoading(false);
                        }
                    });
                    frame.requestFocusInWindow();
                }
            }

            if (KeyBindProfile.FullScreen.pressed(e)) {
                if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                } else
                    frame.setExtendedState(JFrame.NORMAL);
                e.consume();
                return;
            }

            if (KeyBindProfile.NewWindow.pressed(e)) {
                var window = App.CreateWindow("");
                window.start();
                Log("created new window");
                e.consume();
                return;
            }
            if (KeyBindProfile.CloseWindow.pressed(e) && key != "urlField") {
                CloseThisWindow();
                Log("closed window");
                e.consume();
                return;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

    }

}
