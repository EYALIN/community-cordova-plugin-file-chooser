package android.content;
import android.net.Uri;
public class ClipData {
    public static class Item { private final Uri uri; public Item(Uri u) { uri = u; } public Uri getUri() { return uri; } }
    private final Item[] items;
    public ClipData(Item... items) { this.items = items; }
    public int getItemCount() { return items.length; }
    public Item getItemAt(int i) { return items[i]; }
}
