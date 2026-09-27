package org.apache.cordova;
import org.json.JSONArray;
import org.json.JSONObject;
public class PluginResult {
    public enum Status { NO_RESULT, OK, ERROR }
    public final Status status; public final Object message; private boolean keep;
    public PluginResult(Status s) { this(s, (Object) null); }
    private PluginResult(Status s, Object m) { status = s; message = m; }
    public PluginResult(Status s, String m) { this(s, (Object) m); }
    public PluginResult(Status s, JSONObject m) { this(s, (Object) m); }
    public PluginResult(Status s, JSONArray m) { this(s, (Object) m); }
    public void setKeepCallback(boolean b) { keep = b; }
    @Override public String toString() { return status + ":" + message; }
}
