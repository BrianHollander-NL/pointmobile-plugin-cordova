package nl.valuga.pointmobileplugin;

import org.apache.cordova.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.Cordova;
import org.json.*;

import java.lang.*;

import android.content.Context;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import device.sdk.ScanManager;

import device.common.DecodeResult;
import device.common.ScanConst;

public class PointMobile extends CordovaPlugin {
    // Reference to application context for construction and resource purposes
    private Context context;
    /* read state */
    public static final int READ_SUCCESS=0;
    public static final int READ_FAIL=1;
    public static final int READ_READY=2;

    private static ScanManager mScan = null;
    private static DecodeResult mDecodeResult;
    private final ScanResultReceiver scanResultReceiver = new ScanResultReceiver();
    private int origScanResultType = 0;

    private String mResult = null;

    private boolean readerActivated = false;
    private boolean scannerActivated = true;

    /***************************************************
     * LIFECYCLE
     ***************************************************/

    /**
     * Called after plugin construction and fields have been initialized.
     */
    @Override
    public void initialize(CordovaInterface cordova,CordovaWebView webView){
        super.initialize(cordova,webView);
        mDetectResult = new MsrResult();
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     * The reader is killed and MSR is unregistered.
     *
     * @param multitasking
     *      Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);

        if (scannerActivated) {
            deactivateScanner(null);
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     * The reader is reinitialized as if the app was just opened.
     *
     * @param multitasking
     *      Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (scannerActivated) {
            activateScanner(null);
        }
    }

    /***************************************************
     * JAVASCRIPT INTERFACE IMPLEMENTATION
     ***************************************************/

    /**
     * Executes the request sent from JavaScript.
     *
     * @param action
     *      The action to execute.
     * @param args
     *      The exec() arguments in JSON form.
     * @param command
     *      The callback context used when calling back into JavaScript.
     * @return
     *      Whether the action was valid.
     */
    @Override
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if("SCAN_activateScanner".equals(action)){
            activateScanner(callbackContext);
        } else if("SCAN_deactivateScanner".equals(action)){
            deactivateScanner(callbackContext);
        } else {
            // Method not found.
            return false;
        }
        return true;
    }

    /**
     * @param callbackContext
     *        Used when calling back into JavaScript
     */
    private void activateScanner(final CallbackContext callbackContext){
        String callbackContextMsg = null;
        if (context == null) context = this.cordova.getActivity().getApplicationContext();
        try{

            mScan = new ScanManager();
            mDecodeResult = new DecodeResult();
            if(mScan != null){
                mScan.aDecodeAPIInit();
                origScanResultType = mScan.aDecodeGetResultType();
                mScan.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
                IntentFilter intentFilter = new IntentFilter(ScanConst.INTENT_USERMSG);
                try {
                    context.registerReceiver(scanResultReceiver, intentFilter);
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                    // If we're not going to be able to detect via hardware whether
                    // the swipe is plugged in, we can't continue.
                    callbackContextMsg = "Unable to register ScanReceiver.";
                }
            }

            if(callbackContext != null){
                scannerActivated = true;
            } else {
                fireEvent("scanner_reactivated");
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
            callbackContextMsg = "Failed to activate scanner";
        }
        sendCallback(callbackContext,callbackContextMsg);
    }
    private void deactivateScanner(final CallbackContext callbackContext){
        try{
            context.unregisterReceiver(scanResultReceiver);
            mScan.aDecodeSetResultType(origScanResultType);
            mScan.aDecodeAPIDeinit();
            mScan = null;
            if(callbackContext != null){
                scannerActivated = false;
            }
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }

        sendCallback(callbackContext,null);
    }
    /***************************************************
     * SDK CALLBACKS
     ***************************************************/

    private class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mScan != null) {
                mScan.aDecodeGetResult(mDecodeResult.recycle());
                String message;
                if( mDecodeResult.symName.equals("READ_FAIL") || mDecodeResult.symName.equals("")) {
                    message = "{\"Success\": false }";
                }
                else{
                    message = "{\"Success\": true,"
                            +"\"Type\":\"" + mDecodeResult.symName + "\","
                            + "\"Data\":\"" + mDecodeResult.toString() + "\"}";
                }
                fireEvent("scan_result", message);
            }
        }
    }

    /**
     * Perform either a success or error callback on given CallbackContext
     * depending on state of msg.
     * @param callbackContext
     *        Used when calling back into JavaScript
     * @param msg
     *        Error message, or null if success
     */

    private void sendCallback(CallbackContext callbackContext, String msg) {
        // callbackContext will only be null when caller called from
        // lifecycle methods (i.e., never from containing app).
        if (callbackContext != null) {
            if (msg == null) {
                callbackContext.success();
            } else callbackContext.error(msg);
        }
    }

    /**
     * Pass event to method overload.
     *
     * @param event
     *        The event name
     */
    private void fireEvent(String event) {
        fireEvent(event, null);
    }

    /**
     * Format and send event to JavaScript side.
     *
     * @param event
     *        The event name
     * @param data
     *        Details about the event
     */
    private void fireEvent(String event, String data) {
        if(data != null) {
            data = data.replaceAll("\\s","");
        }
        String dataArg = data != null ? "','" + data + "" : "";

        String js = "cordova.plugins.PointMobile.fireEvent('" +
                event + dataArg + "');";

        webView.sendJavascript(js);
    }
}
