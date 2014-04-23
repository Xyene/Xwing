package tk.ivybits.xwing;

import tk.ivybits.xwing.prototype.ToCenterPrototype;

import javax.swing.*;

public class Demo {
    public static void main(String[] argv) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

        XFrame calculator = XRenderer.render("calc.xw");

        new ToCenterPrototype().run(calculator, null);
        calculator.setVisible(true);
    }
}