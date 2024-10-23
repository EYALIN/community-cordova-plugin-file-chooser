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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
        boolean multiple = options != null && options.optBoolean("multiple", false);
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        cordova.startActivityForResult(this, Intent.createChooser(intent, "Select File(s)"), FILE_SELECT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    List<JSONObject> fileList = new ArrayList<>();
                    boolean includeBase64 = options != null && options.optBoolean("includeBase64", false);

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            fileList.add(getFileDetails(uri, includeBase64));
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        fileList.add(getFileDetails(uri, includeBase64));
                    }

                    JSONArray resultArray = new JSONArray(fileList);
                    callbackContext.success(resultArray);
                } catch (Exception e) {
                    callbackContext.error("Failed to read the file(s): " + e.getMessage());
                }
            }
        } else {
            callbackContext.error("File selection canceled");
        }
    }

    private JSONObject getFileDetails(Uri uri, boolean includeBase64) throws Exception {
        JSONObject fileDetails = new JSONObject();
        String path = uri.toString();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        long fileSize = cordova.getActivity().getContentResolver().openAssetFileDescriptor(uri, "r").getLength();

        fileDetails.put("fileName", fileName);
        fileDetails.put("path", path);
        fileDetails.put("extension", extension);
        fileDetails.put("fileSize", fileSize);

        if (includeBase64) {
            InputStream inputStream = cordova.getActivity().getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String encoded = Base64.getEncoder().encodeToString(buffer);
            fileDetails.put("base64", encoded);
        }

        return fileDetails;
    }
}
