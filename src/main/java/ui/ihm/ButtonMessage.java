package ui.ihm;

import javax.swing.*;

public class ButtonMessage extends JLabel {

    public ButtonMessage(String msg, String sender) {
        super(sender + " : " + msg);
    }
}
