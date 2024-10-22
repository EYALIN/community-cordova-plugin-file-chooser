package CommunityPlugins.FileChooser.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import androidx.annotation.Nullable;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Base64;

public class FileChooserPlugin extends CordovaPlugin {
    private static final int FILE_SELECT_CODE = 0;
    private CallbackContext callbackContext;
    private JSONObject options;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("chooseFile")) {
            this.callbackContext = callbackContext;
            this.options = args.optJSONObject(0);
            showFileChooser(options);
            return true;
        }
        return false;
    }

    private void showFileChooser(JSONObject options) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String mimeType = "*/*";
        if (options != null && options.has("mimeType")) {
            mimeType = options.optString("mimeType", "*/*");
        }
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        cordova.startActivityForResult(this, Intent.createChooser(intent, "Select a File"), FILE_SELECT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        String responseType = options != null ? options.optString("responseType", "path") : "path";
                        if ("base64".equals(responseType)) {
                            InputStream inputStream = cordova.getActivity().getContentResolver().openInputStream(uri);
                            byte[] buffer = new byte[inputStream.available()];
                            inputStream.read(buffer);
                            String encoded = Base64.getEncoder().encodeToString(buffer);
                            callbackContext.success(encoded);
                        } else {
                            String path = uri.toString();
                            callbackContext.success(path);
                        }
                    } catch (Exception e) {
                        callbackContext.error("Failed to read the file: " + e.getMessage());
                    }
                } else {
                    callbackContext.error("File URI is null");
                }
            }
        } else {
            callbackContext.error("File selection canceled");
        }
    }
}

