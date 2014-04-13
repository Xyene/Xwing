package tk.ivybits.xwing;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.ScriptableObject;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProxiedContainer<T extends Component> {
    private final XForm form;
    private final Component component;

    public ProxiedContainer(XForm form, T component) {
        this.form = form;
        this.component = component;
    }

    public void onClick(final Object call) {
        System.out.println(call + ": " + component);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (call instanceof Function) {
                    Function func = (Function) call;
                    Context context = Context.enter();
                    ScriptableObject scope = context.initStandardObjects();
                    func.call(context, scope, scope, new Object[] {mouseEvent});
                }
            }
        });
    }

    public <T> T get() {
        return (T) component;
    }
}
