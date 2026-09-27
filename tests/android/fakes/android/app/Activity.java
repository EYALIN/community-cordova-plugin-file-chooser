package android.app;
import android.content.ContentResolver;
public class Activity {
    public static final int RESULT_OK = -1, RESULT_CANCELED = 0;
    public final ContentResolver resolver = new ContentResolver();
    public ContentResolver getContentResolver() { return resolver; }
}
