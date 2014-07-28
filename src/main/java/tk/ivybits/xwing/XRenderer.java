package tk.ivybits.xwing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XRenderer {
    public static XFrame render(String reader) {
        return render(reader, null);
    }

    public static XFrame render(String reader, final Object controller) {
        return new XFrame(ClassLoader.getSystemResourceAsStream(reader), controller);
    }

    public static XFrame render(File reader, final Object controller) throws FileNotFoundException {
        return new XFrame(new FileInputStream(reader), controller);
    }
}
