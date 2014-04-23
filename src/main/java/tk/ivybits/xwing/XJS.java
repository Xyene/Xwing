package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import tk.ivybits.xwing.prototype.Prototype;
import tk.ivybits.xwing.prototype.PrototypedContainer;
import tk.ivybits.xwing.prototype.ToCenterPrototype;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import static tk.ivybits.xwing.prototype.DynamicPrototype.*;

public class XJS {
    public static final Map<String, Prototype> PROTOTYPES = new HashMap<String, Prototype>() {{
        put("toCenter", new ToCenterPrototype());
    }};

    static {
        mapAll(ActionListener.class);
        mapAll(KeyListener.class);
        mapAll(MouseListener.class);
        mapAll(MouseMotionListener.class);
        mapAll(MouseWheelListener.class);
        mapAll(ItemListener.class);
        mapAll(ChangeListener.class);
        mapAll(AncestorListener.class);
        mapAll(WindowListener.class);
        mapAll(WindowFocusListener.class);
        mapAll(WindowStateListener.class);
        mapAll(TreeExpansionListener.class);
        mapAll(TreeWillExpandListener.class);
        mapAll(TreeModelListener.class);
        mapAll(TableColumnModelListener.class);
        mapAll(TableModelListener.class);
    }

    private XContainer self;
    private Context context;
    private ScriptableObject scope;

    public XJS(XContainer self, Object controller) {
        this.self = self;
        context = Context.enter();
        scope = context.initStandardObjects();
        // Add all methods of our form to global scope
        ScriptableObject.putProperty(scope, "$js", this);
        ScriptableObject.putProperty(scope, "$self", new PrototypedContainer<>(this, self));
        if (controller != null)
            ScriptableObject.putProperty(scope, "$controller", controller);
        context.evaluateString(scope,
                "this['$'] = (function() {" +
                "   var method = $js.find;" +
                "   return function() {" +
                "       return method.apply($js, arguments);" +
                "   };" +
                "})();" +
                "this['print'] = function (s) {" +
                "    Packages.java.lang.S*ystem['out'].println(s);" +
                "};",
                "<cmd>", 1, null
        );
    }

    public <T extends Component> PrototypedContainer<T> find(String id) {
        T t = (T) self.byId(id);
        if (t == null)
            return null;
        return new PrototypedContainer<>(this, t);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ScriptableObject getScope() {
        return scope;
    }

    public void setScope(ScriptableObject scope) {
        this.scope = scope;
    }
}
