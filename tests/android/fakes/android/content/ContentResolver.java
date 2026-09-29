package android.content;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
public class ContentResolver {
    public byte[] content = "hello".getBytes();
    // PLU-92: OpenableColumns.DISPLAY_NAME / SIZE row a real picker would return.
    public String[] cursorColumns = {"_display_name", "_size"};
    public Object[] cursorValues = {"report.pdf", (long) content.length};
    public boolean queryReturnsNull = false;
    public boolean queryThrows = false;
    public boolean openInputStreamReturnsNull = false;

    public AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode) throws FileNotFoundException { return new AssetFileDescriptor(content.length); }
    public InputStream openInputStream(Uri uri) throws FileNotFoundException {
        if (openInputStreamReturnsNull) return null;
        return new ByteArrayInputStream(content);
    }
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (queryThrows) { throw new RuntimeException("simulated query failure"); }
        if (queryReturnsNull) { return null; }
        return new Cursor(cursorColumns, cursorValues);
    }
}
