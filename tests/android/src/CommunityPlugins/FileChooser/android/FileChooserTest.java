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
        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) { System.exit(1); }
    }
}
