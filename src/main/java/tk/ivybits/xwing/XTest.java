package tk.ivybits.xwing;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;

public class XTest {
    public static class Login extends XForm {
        public Login() {
            super(new File("login.xw"));
        }

        public void formSubmitted() {
            System.out.println("Form submitted!");
        }
    }

    public static void main(String[] argv) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        Login login = new Login();
    }
}
