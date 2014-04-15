package tk.ivybits.xwing;

public class XScript {
    private String script;
    private String id;

    public XScript(String script, String id) {
        this.script = script;
        this.id = id;
    }

    public XScript(String script) {
        this(script, "<cmd>");
    }

    public XScript() {
        this(null, "<cmd>");
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
