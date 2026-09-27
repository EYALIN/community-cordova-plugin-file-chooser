package android.content;
import android.net.Uri;
public class Intent {
    public static final String ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT";
    public static final String CATEGORY_OPENABLE = "android.intent.category.OPENABLE";
    public static final String EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE";
    private Uri data; private ClipData clip; public String action;
    public Intent() {}
    public Intent(String action) { this.action = action; }
    public static Intent createChooser(Intent target, CharSequence title) { return target; }
    public Intent setType(String t) { return this; }
    public Intent addCategory(String c) { return this; }
    public Intent putExtra(String k, boolean v) { return this; }
    public Intent setData(Uri u) { data = u; return this; }
    public Uri getData() { return data; }
    public void setClipData(ClipData c) { clip = c; }
    public ClipData getClipData() { return clip; }
}
