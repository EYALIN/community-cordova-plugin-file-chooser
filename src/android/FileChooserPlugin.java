package CommunityPlugins.FileChooser.android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.annotation.Nullable;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileChooserPlugin extends CordovaPlugin {
    private static final String TAG = "FileChooserPlugin";
    private static final String STATE_OPTIONS = "options";
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

    // Android may kill the app process while the system picker is open ("Don't keep activities",
    // low memory). cordova-android then recreates the plugin and calls
    // onRestoreStateForActivityResult with a new CallbackContext before onActivityResult, so the
    // options must be saved and the callback taken from the restore call.
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = new Bundle();
        if (options != null) {
            state.putString(STATE_OPTIONS, options.toString());
        }
        return state;
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        String saved = state != null ? state.getString(STATE_OPTIONS) : null;
        if (saved != null) {
            try {
                this.options = new JSONObject(saved);
            } catch (JSONException e) {
                this.options = null;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILE_SELECT_CODE) {
            return;
        }
        CallbackContext callbackContext = this.callbackContext;
        this.callbackContext = null;
        if (callbackContext == null) {
            // no JS call is waiting (e.g. the app was restarted without a restore): nothing to answer
            Log.w(TAG, "File chooser result without a pending callback, ignoring");
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                callbackContext.error("No file selected");
            } else {
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

    // PLU-92: the DISPLAY_NAME/SIZE columns ContentResolver.query() returns for a picked document
    // (e.g. a Downloads/Recents backup) are the correct name and size; the trailing URI segment
    // is only a fallback for providers that answer the query with nothing.
    private JSONObject getFileDetails(Uri uri, boolean includeBase64) throws Exception {
        ContentResolver resolver = cordova.getActivity().getContentResolver();
        JSONObject fileDetails = new JSONObject();
        String path = uri.toString();

        String fileName = null;
        long fileSize = -1;
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                    fileName = cursor.getString(nameIndex);
                }
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    fileSize = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "OpenableColumns query failed, falling back to the URI segment: " + e.getMessage());
        }

        if (fileName == null || fileName.isEmpty()) {
            fileName = path.substring(path.lastIndexOf('/') + 1);
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName.contains(".") ? fileName : path);

        if (fileSize < 0) {
            try (AssetFileDescriptor afd = resolver.openAssetFileDescriptor(uri, "r")) {
                fileSize = afd != null ? afd.getLength() : -1;
            }
        }

        fileDetails.put("fileName", fileName);
        fileDetails.put("path", path);
        fileDetails.put("extension", extension);
        fileDetails.put("fileSize", fileSize);

        // PLU-91: a single available()-sized read stops short on providers (e.g. Drive-backed
        // documents) that report available() as 0 or as less than the real stream length, which
        // silently truncated or emptied the base64 payload. Read the whole stream in a loop instead.
        if (includeBase64) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (InputStream inputStream = resolver.openInputStream(uri)) {
                if (inputStream == null) {
                    throw new IOException("Unable to open an input stream for " + uri);
                }
                byte[] chunk = new byte[8192];
                int read;
                while ((read = inputStream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
            }
            if (buffer.size() == 0) {
                throw new IOException("Read 0 bytes from " + uri);
            }
            String encoded = Base64.getEncoder().encodeToString(buffer.toByteArray());
            fileDetails.put("base64", encoded);
        }

        return fileDetails;
    }
}
