package tk.ivybits.xwing;

import javax.swing.*;
import java.awt.*;

public class XWidgets {
    public static class Panel extends JPanel {
        public Panel() {
            super(new BorderLayout());
        }
    }

    public static class Frame extends JFrame {
        public Frame() {
            setLayout(new BorderLayout());
        }
    }

    public static class TabbedPane extends JTabbedPane {
        @Override
        public Component add(Component comp) {
            return super.add(comp.getName(), comp);
        }
    }

    public static class HBox extends JPanel {
        public HBox() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        }
    }

    public static class VBox extends JPanel {
        public VBox() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        }
    }

    public static class ButtonGroup extends JPanel {
        private final javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();

        @Override
        public Component add(Component comp) {
            if (comp instanceof AbstractButton)
                group.add((AbstractButton) comp);
            return super.add(comp);
        }

        @Override
        public void remove(Component comp) {
            super.remove(comp);
            if (comp instanceof AbstractButton)
                group.remove((AbstractButton) comp);
        }
    }
}
