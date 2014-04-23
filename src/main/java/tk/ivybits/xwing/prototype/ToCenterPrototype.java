package tk.ivybits.xwing.prototype;

import java.awt.*;

public class ToCenterPrototype implements Prototype<Component> {
    @Override
    public Object run(Component component, Object... args) {
        // setLocationRelativeTo(null) doesn't work as expected on Ubuntu 12.04, 13.04 & 14.10 on a multimonitor display
        // this doesn't work well either, but at least window is somewhat centered
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        component.setLocation(new Point((screenSize.width / 2) - (component.getWidth() / 2), screenSize.height / 2 - (component.getHeight() / 2)));
        return null;
    }

    @Override
    public boolean isApplicableOn(Component component) {
        return component instanceof Window;
    }
}
