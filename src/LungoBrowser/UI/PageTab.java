package LungoBrowser.UI;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import LungoBrowser.App;
import LungoBrowser.Debug;
import LungoBrowser.HandlePage;
import LungoBrowser.Window;

public class PageTab extends JPanel {

    private JLabel label;
    private JAdvancedButton closeButton;

    private static Image closeImage = App.GetImage("Images/Icons/reloading.png");

    public void setText(String text) {
        label.setText(text);
    }

    public void setIcon(Icon icon) {
        label.setIcon(icon);
    }

    public PageTab(Window self, HandlePage page) {
        super(new FlowLayout());
        setBackground(self.currentColorProfile.bgColorDef);

        var ButtonSize = Math.round(self.UISize) - 3;
        var BevelBorder = BorderFactory.createRaisedBevelBorder();
        closeButton = self.createUiButton(
                closeImage.getScaledInstance(ButtonSize, ButtonSize,
                        java.awt.Image.SCALE_SMOOTH),
                self.urlField.getFont(), BevelBorder, (ActionEvent e) -> {
                    self.removePageHandler(page);
                }, false);
        closeButton.setBackground(new Color(0, 0, 0, 0));

        label = new JLabel("");
        label.setForeground(self.currentColorProfile.TextColor);
        add(label);
        add(closeButton);
    }
}
