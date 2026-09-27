package android.content.res;
public class AssetFileDescriptor {
    private final long length;
    public AssetFileDescriptor(long l) { length = l; }
    public long getLength() { return length; }
    public void close() {}
}
