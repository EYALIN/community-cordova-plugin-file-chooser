package android.content.res;
import java.io.Closeable;
public class AssetFileDescriptor implements Closeable {
    private final long length;
    public AssetFileDescriptor(long l) { length = l; }
    public long getLength() { return length; }
    @Override public void close() {}
}
