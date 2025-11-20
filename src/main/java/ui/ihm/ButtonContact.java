package ui.ihm;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ButtonContact extends JButton {

    public ButtonContact(String contactName) {
        super(contactName);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new LineBorder(Color.GRAY, 1));
    }
}
