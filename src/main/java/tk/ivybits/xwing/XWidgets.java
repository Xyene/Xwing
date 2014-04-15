package tk.ivybits.xwing;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.synth.SynthButtonUI;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;
import java.awt.*;
import java.lang.reflect.Field;

import static javax.swing.WindowConstants.*;

public class XWidgets {


    public static class Button extends JButton {
        {
//            setBackground(Color.RED);
//            setOpaque(true);
//            setBorderPainted(false);
//            setFocusPainted(false);
//            setFocusable(false);
//            setForeground(Color.GREEN);
//            SynthButtonUI ui = (SynthButtonUI) this.getUI();
//            SynthContext s = ui.getContext(this);
//           // this.setContentAreaFilled(false);
//            System.out.println(s.getStyle().);
        }
    }

    public static class Panel extends JPanel {
        public Panel() {
            super(new BorderLayout());
        }
    }

    public static class HRuler extends JSeparator {
        public HRuler() {
            super(HORIZONTAL);
        }
    }

    public static class VRuler extends JSeparator {
        public VRuler() {
            super(VERTICAL);
        }
    }

    public static class Frame extends JFrame {
        public Frame() {
            setLayout(new BorderLayout());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
    }

    public static class Dialog extends JDialog {
        public Dialog() {
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    public static class TabbedPane extends JTabbedPane {
        @Override
        public Component add(Component comp) {
            return super.add(comp.getName(), comp);
        }
    }

    private static abstract class Box extends JPanel {
        public Box() {
            setLayout(new BoxLayout(this, getAlignment()));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        @Override
        public Component add(Component other) {
            // Nasty hack to make components fill all space
            if (other instanceof JPanel || other instanceof JSeparator) return super.add(other);
            JPanel container = new JPanel(new BorderLayout());
            container.add(other, BorderLayout.CENTER);
            return super.add(container);
        }

        protected abstract int getAlignment();
    }

    public static class HBox extends Box {
        @Override
        protected int getAlignment() {
            return BoxLayout.X_AXIS;
        }
    }

    public static class VBox extends Box {
        @Override
        protected int getAlignment() {
            return BoxLayout.Y_AXIS;
        }
    }

    public static class ButtonGroup extends Panel {
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

    public static class BorderPanel extends Panel {
        public static class North extends Panel {
        }

        public static class East extends Panel {
        }

        public static class South extends Panel {
        }

        public static class West extends Panel {
        }

        public static class Center extends Panel {
        }

        @Override
        public Component add(Component comp) {
            if (comp instanceof North)
                super.add(comp, BorderLayout.NORTH);
            else if (comp instanceof East)
                super.add(comp, BorderLayout.EAST);
            else if (comp instanceof South)
                super.add(comp, BorderLayout.SOUTH);
            else if (comp instanceof West)
                super.add(comp, BorderLayout.WEST);
            else if (comp instanceof Center)
                super.add(comp, BorderLayout.CENTER);
            else return super.add(comp);
            return comp;
        }
    }
}
