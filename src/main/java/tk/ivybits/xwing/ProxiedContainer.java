package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

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
                    Context.enter();
                    func.call(form.context, form.scope, form.scope, new Object[] {mouseEvent});
                    Context.exit();
                }
            }
        });
    }

    public <T> T get() {
        return (T) component;
    }
}
