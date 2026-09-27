package android.webkit;
public class MimeTypeMap {
    public static String getFileExtensionFromUrl(String url) { int i = url.lastIndexOf('.'); return i < 0 ? "" : url.substring(i + 1); }
}
