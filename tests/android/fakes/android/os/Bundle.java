package android.os;
import java.util.HashMap;
import java.util.Map;
public class Bundle {
    private final Map<String, Object> m = new HashMap<>();
    public void putString(String k, String v) { m.put(k, v); }
    public String getString(String k) { return (String) m.get(k); }
    public boolean containsKey(String k) { return m.containsKey(k); }
}
