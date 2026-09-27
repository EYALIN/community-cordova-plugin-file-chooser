package org.apache.cordova;
import android.content.Intent;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
/** Mirrors cordova-android: after process death a NEW plugin instance gets onRestoreStateForActivityResult, then onActivityResult. */
public class CordovaPlugin {
    public CordovaInterface cordova;
    public final void privateInitialize(CordovaInterface c) { cordova = c; }
    public boolean execute(String action, JSONArray args, CallbackContext cb) throws JSONException { return false; }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {}
    public Bundle onSaveInstanceState() { return null; }
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {}
}
