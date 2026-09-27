package android.content;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
public class ContentResolver {
    public byte[] content = "hello".getBytes();
    public AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode) throws FileNotFoundException { return new AssetFileDescriptor(content.length); }
    public InputStream openInputStream(Uri uri) throws FileNotFoundException { return new ByteArrayInputStream(content); }
}
