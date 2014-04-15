package tk.ivybits.xwing;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.synth.SynthButtonUI;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import static javax.swing.WindowConstants.*;

public interface XWidget {

    Component add(Component component, Map<String, String> attributes);

    public static class ComboBox extends JComboBox {
        public static class Choice extends Container {
            private String name;
            List<ActionListener> listenerList = new LinkedList<>();

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void addActionListener(ActionListener listener) {
                listenerList.add(listener);
            }

            @Override
            public void addNotify() {
                throw new IllegalStateException();
            }
        }

        {
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    return value instanceof Component ? (Component) value : super.getListCellRendererComponent(list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);
                }
            });
            this.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Choice value = choices.get(e.getItem());
                        if (value != null) {
                            ActionEvent evt = new ActionEvent(ComboBox.this, ActionEvent.ACTION_FIRST, null);
                            for (ActionListener listener : value.listenerList) {
                                listener.actionPerformed(evt);
                            }
                        }
                    }

                }
            });
        }

        Map<String, Choice> choices = new HashMap<>();

        @Override
        public Component add(Component comp) {
            if (comp instanceof Choice) {
                addItem(comp.getName());
                choices.put(comp.getName(), (Choice) comp);
                return comp;
            }
            return super.add(comp);
        }
    }

    public static class Panel extends JPanel implements XWidget {
        public Panel() {
            super(new BorderLayout());
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            String position = attributes.get("position");
            if (position != null) {
                position = Character.toTitleCase(position.charAt(0)) + position.substring(1);
                add(component, position);
                return component;
            }
            return add(component);
        }
    }

    public static class HRuler extends JSeparator implements XWidget {
        public HRuler() {
            super(HORIZONTAL);
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return super.add(component);
        }
    }

    public static class VRuler extends JSeparator implements XWidget {
        public VRuler() {
            super(VERTICAL);
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return super.add(component);
        }
    }

    public static class Frame extends JFrame implements XWidget {
        public Frame() {
            setLayout(new BorderLayout());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return super.add(component);
        }
    }

    public static class Dialog extends JDialog implements XWidget {
        public Dialog() {
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return super.add(component);
        }
    }

    public static class TabbedPane extends JTabbedPane implements XWidget {
        @Override
        public Component add(Component comp) {
            return super.add(comp.getName(), comp);
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return super.add(component);
        }
    }

    static class Box extends JPanel implements XWidget {
        public Box() {
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        public void setOrient(String orient) {
            // Do not allow <hbox orient="vertical/>
            if (getClass() == Box.class) {
                switch (orient) {
                    case "horizontal":
                        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                        return;
                    case "vertical":
                        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                        return;
                }
            }
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return add(component);
        }

        @Override
        public Component add(Component other) {
            // Nasty hack to make components fill all space
            if (other instanceof JPanel || other instanceof JSeparator) return super.add(other);
            JPanel container = new JPanel(new BorderLayout());
            container.add(other, BorderLayout.CENTER);
            return super.add(container);
        }
    }

    public static class HBox extends Box {
        {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }
    }

    public static class VBox extends Box {
        {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
    }

    public static class ButtonGroup extends Panel {
        private final javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            return add(component);
        }

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

    public static class SplitPane extends JSplitPane implements XWidget {
        public void setMode(String mode) {
            switch (mode) {
                case "horizontal":
                    setOrientation(HORIZONTAL_SPLIT);
                    return;
                case "vertical":
                    setOrientation(VERTICAL_SPLIT);
                    return;
            }
        }

        @Override
        public Component add(Component component, Map<String, String> attributes) {
            String position = attributes.get("split");
            if (position != null) {
                switch (position) {
                    case "right":
                    case "top":
                        this.setRightComponent(component);
                        return component;
                    case "left":
                    case "down":
                        this.setLeftComponent(component);
                        return component;
                }
            }
            return add(component);
        }
    }

    public static class ProgressBar extends JProgressBar {
        {
            setStringPainted(true);
        }

        public void setOrientation(String mode) {
            switch (mode) {
                case "horizontal":
                    setOrientation(HORIZONTAL);
                    return;
                case "vertical":
                    setOrientation(VERTICAL);
                    return;
            }
        }
    }
}
