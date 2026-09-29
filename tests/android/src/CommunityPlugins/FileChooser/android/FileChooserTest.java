package CommunityPlugins.FileChooser.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PLU-232: FileChooserPlugin.java on a plain JVM. "Don't keep activities" / process death: the
 * picker result is delivered to a NEW plugin instance whose callbackContext was never set (Crashlytics
 * NPE in onActivityResult). Cordova hands the new instance a fresh CallbackContext through
 * onRestoreStateForActivityResult(state saved by onSaveInstanceState) before onActivityResult.
 */
public class FileChooserTest {
    static int passed, failed;
    static final ExecutorService POOL = new AbstractExecutorService() {
        public void execute(Runnable r) { r.run(); }
        public void shutdown() {}
        public List<Runnable> shutdownNow() { return Collections.emptyList(); }
        public boolean isShutdown() { return false; }
        public boolean isTerminated() { return false; }
        public boolean awaitTermination(long l, TimeUnit u) { return true; }
    };

    static FileChooserPlugin newPlugin(Activity activity) {
        FileChooserPlugin p = new FileChooserPlugin();
        p.privateInitialize(new CordovaInterface() {
            public Activity getActivity() { return activity; }
            public ExecutorService getThreadPool() { return POOL; }
            public void startActivityForResult(CordovaPlugin c, Intent i, int code) {}
        });
        return p;
    }

    static Intent picked(String uri) { Intent i = new Intent(); i.setData(Uri.parse(uri)); return i; }

    interface Body { void run() throws Exception; }
    static void test(String name, Body b) {
        String why = null;
        try { b.run(); } catch (AssertionError a) { why = a.getMessage(); } catch (Throwable t) { why = "APP CRASH: " + t; }
        if (why == null) { passed++; System.out.println("PASS  " + name); } else { failed++; System.out.println("FAIL  " + name + "\n        -> " + why); }
    }
    static void check(boolean ok, String msg) { if (!ok) { throw new AssertionError(msg); } }

    public static void main(String[] args) {
        test("[PLU-232] normal pick: the file details come back", () -> {
            Activity a = new Activity();
            FileChooserPlugin p = newPlugin(a);
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray().put(new JSONObject()), cb);
            p.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/report.pdf"));
            check(cb.results.size() == 1 && cb.results.get(0).status == PluginResult.Status.OK, "got " + cb.results);
        });
        test("[PLU-232] result delivered to a new plugin instance after process death (no restore call) does not crash", () -> {
            FileChooserPlugin fresh = newPlugin(new Activity());
            fresh.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/report.pdf"));
            fresh.onActivityResult(0, Activity.RESULT_CANCELED, null);
        });
        test("[PLU-232] after process death the restored callback receives the picked file, with the saved options", () -> {
            Activity a = new Activity();
            FileChooserPlugin before = newPlugin(a);
            before.execute("chooseFile", new JSONArray().put(new JSONObject().put("includeBase64", true)), new CallbackContext());
            Bundle saved = before.onSaveInstanceState();
            FileChooserPlugin after = newPlugin(a);                 // activity recreated
            CallbackContext restored = new CallbackContext();
            after.onRestoreStateForActivityResult(saved, restored);
            after.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/report.pdf"));
            check(restored.results.size() == 1 && restored.results.get(0).status == PluginResult.Status.OK, "got " + restored.results);
            JSONObject file = ((JSONArray) restored.results.get(0).message).getJSONObject(0);
            check(file.has("base64"), "includeBase64 option lost across process death: " + file);
        });
        test("[PLU-232] RESULT_OK without data answers the call instead of leaving it pending", () -> {
            FileChooserPlugin p = newPlugin(new Activity());
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray(), cb);
            p.onActivityResult(0, Activity.RESULT_OK, null);
            check(cb.results.size() == 1 && cb.results.get(0).status == PluginResult.Status.ERROR, "got " + cb.results);
        });
        test("[PLU-91] includeBase64 decodes to the exact stream size, not just available()", () -> {
            Activity a = new Activity();
            byte[] big = new byte[50000];
            for (int i = 0; i < big.length; i++) { big[i] = (byte) (i % 251); }
            a.resolver.content = big;
            a.resolver.cursorValues = new Object[]{"backup.db", (long) big.length};
            FileChooserPlugin p = newPlugin(a);
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray().put(new JSONObject().put("includeBase64", true)), cb);
            p.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/backup.db"));
            check(cb.results.size() == 1 && cb.results.get(0).status == PluginResult.Status.OK, "got " + cb.results);
            JSONObject file = ((JSONArray) cb.results.get(0).message).getJSONObject(0);
            byte[] decoded = Base64.getDecoder().decode(file.getString("base64"));
            check(decoded.length == big.length, "decoded " + decoded.length + " bytes, expected " + big.length);
            check(file.getLong("fileSize") == big.length, "fileSize=" + file.getLong("fileSize"));
        });
        test("[PLU-91] a 0-byte stream errors instead of returning an empty base64", () -> {
            Activity a = new Activity();
            a.resolver.content = new byte[0];
            FileChooserPlugin p = newPlugin(a);
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray().put(new JSONObject().put("includeBase64", true)), cb);
            p.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/empty.db"));
            check(cb.results.size() == 1 && cb.results.get(0).status == PluginResult.Status.ERROR, "got " + cb.results);
        });
        test("[PLU-92] fileName/extension/fileSize come from OpenableColumns, not the URI segment", () -> {
            Activity a = new Activity();
            a.resolver.cursorColumns = new String[]{"_display_name", "_size"};
            a.resolver.cursorValues = new Object[]{"backup.db", 12345L};
            a.resolver.content = new byte[10];
            FileChooserPlugin p = newPlugin(a);
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray().put(new JSONObject()), cb);
            p.onActivityResult(0, Activity.RESULT_OK, picked("content://com.android.providers.downloads.documents/document/1234"));
            JSONObject file = ((JSONArray) cb.results.get(0).message).getJSONObject(0);
            check(file.getString("fileName").equals("backup.db"), "fileName=" + file.getString("fileName"));
            check(file.getString("extension").equals("db"), "extension=" + file.getString("extension"));
            check(file.getLong("fileSize") == 12345L, "fileSize=" + file.getLong("fileSize"));
        });
        test("[PLU-92] falls back to the URI segment when the OpenableColumns query returns nothing", () -> {
            Activity a = new Activity();
            a.resolver.queryReturnsNull = true;
            a.resolver.content = new byte[7];
            FileChooserPlugin p = newPlugin(a);
            CallbackContext cb = new CallbackContext();
            p.execute("chooseFile", new JSONArray().put(new JSONObject()), cb);
            p.onActivityResult(0, Activity.RESULT_OK, picked("content://docs/fallback.db"));
            JSONObject file = ((JSONArray) cb.results.get(0).message).getJSONObject(0);
            check(file.getString("fileName").equals("fallback.db"), "fileName=" + file.getString("fileName"));
            check(file.getString("extension").equals("db"), "extension=" + file.getString("extension"));
            check(file.getLong("fileSize") == 7L, "fileSize=" + file.getLong("fileSize") + " (should fall back to AssetFileDescriptor length)");
        });
        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) { System.exit(1); }
    }
}
