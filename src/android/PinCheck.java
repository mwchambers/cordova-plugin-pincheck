package cordova.plugin.pincheck;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.os.Build;


/**
 * This class returns a string depending on whether the keyguard is detected on Android. This method is called from JavaScript.
 */
public class PinCheck extends CordovaPlugin {
    private static final boolean isDeviceSecureSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private static String TAG = "PinCheck";
    private static final String ARC_DEVICE_PATTERN = ".+_cheets|cheets_.+";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("isPinSetup")) {
            this.pinCheck(callbackContext);
            return true;
        }
        return false;
    }

    private void pinCheck(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        boolean result = isDeviceSecure(context);
        if (result) {
            Log.d(TAG, "device IS secure");
            callbackContext.success("PIN_SETUP");
        } else {
            Log.d(TAG, "device NOT secure");
            callbackContext.error("NO_PIN_SETUP");
        }
    }

    private boolean isDeviceSecure(Context context) {

        boolean isSecure;

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (PinCheck.isDeviceSecureSupported) {
            Log.d(TAG, "calling KeyguardManager.isDeviceSecure()");
            isSecure = keyguardManager.isDeviceSecure();
        } else {
            Log.d(TAG, "calling KeyguardManager.isKeyguardSecure()");
            isSecure = keyguardManager.isKeyguardSecure();
        }

        if(isSecure) {
            return true;
        }

        // https://stackoverflow.com/questions/39784415/how-to-detect-programmatically-if-android-app-is-running-in-chrome-book-or-in
        if( context.getPackageManager().hasSystemFeature("org.chromium.arc.device_management")) {
            Log.d(TAG, "org.chromium.arc.device_management SystemFeature present");
            return true;
        } else {
            Log.d(TAG, "org.chromium.arc.device_management SystemFeature not present");
        }

        // See: https://github.com/google/talkback/blob/e69d4731fce02bb9e69613d0e48c29033cad4a98/utils/src/main/java/FormFactorUtils.java#L51
        if(Build.DEVICE != null) { 
            Log.d(TAG, "Build.DEVICE: " + Build.DEVICE);
            if(Build.DEVICE.matches(ARC_DEVICE_PATTERN)) { 
                Log.d(TAG, "matches pattern: " + ARC_DEVICE_PATTERN);
                return true;
            }
        }

        return false;
    }
}
