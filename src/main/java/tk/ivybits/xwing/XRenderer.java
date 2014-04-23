package tk.ivybits.xwing;

import java.io.File;

public class XRenderer {
    public static XFrame render(String reader) {
        return render(reader, null);
    }

    public static XFrame render(String reader, final Object controller) {
        return new XFrame(new File(reader), controller);
    }
}
