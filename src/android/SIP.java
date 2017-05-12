package org.apache.cordova.SIP;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;

import  android.util.Log;
import java.util.*;

//SIP
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;

import android.net.sip.SipException;
 
 
public class SIP extends CordovaPlugin {
    private static final String LOG_TAG = "SIPW3";

    private CallbackContext callbackContext;
    private JSONObject params;
    private int orientation;

    public SipManager mSipManager = null;
    public SipProfile mSipProfile = null;
    public SIPReceiver callReceiver;


    private View getView() {
        try {
            return (View)webView.getClass().getMethod("getView").invoke(webView);
        } catch (Exception e) {
            return (View)webView;
        }
    }

    @Override
    protected void pluginInitialize() {
        
        try{
            if (mSipManager == null) {
                mSipManager = SipManager.newInstance(cordova.getActivity());

                
            }

            SipProfile.Builder builder = new SipProfile.Builder("1062", "192.168.0.43");
            builder.setPassword("password");
            mSipProfile = builder.build();
            Log.d("SIP","SIP PLUGIN: SIP PROFILE BUILDED");

       
        }catch(Exception e){
            Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
        }

        try{
            
            Intent intent = new Intent();
            intent.setAction("org.apache.cordova.SIP.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, intent, Intent.FILL_IN_DATA);
            
            
            Log.d("SIP","SIP PLUGIN: isRegistered "+mSipManager.isRegistered(mSipProfile.getUriString()));
            Log.d("SIP","SIP PLUGIN: isOpened "+mSipManager.isOpened(mSipProfile.getUriString()));
            
            mSipProfile.getAutoRegistration();
            mSipManager.open(mSipProfile, pendingIntent, null);

            Log.d("SIP","SIP PLUGIN: PROFILE SIP - "+mSipProfile.getUriString());

            mSipManager.setRegistrationListener(mSipProfile.getUriString(), new SipRegistrationListener() {

                public void onRegistering(String localProfileUri) {
                    Log.d("SIP","SIP PLUGIN: Registering with SIP Server... "+localProfileUri);
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    Log.d("SIP","SIP PLUGIN: Ready "+localProfileUri );
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                    String errorMessage) {
                    Log.d("SIP","SIP PLUGIN: Registration failed.  Please check settings. - ("+errorCode+")"+errorMessage);
                }

            });

            Log.d("SIP","SIP PLUGIN: Listener registrado");
        
            

        }catch(Exception e){
            Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
        }
    }

    public void registerManagerInReceiver(){
        IntentFilter filter = new IntentFilter(mSipManager);
        filter.addAction("org.apache.cordova.SIP.INCOMING_CALL");
        callReceiver = new SIPReceiver();
        cordova.getActivity().registerReceiver(callReceiver, filter);
    }

    public void closeLocalProfile() {
        if (mSipManager == null) {
           return;
        }
        
        try {
           if (mSipProfile != null) {
            
              Log.d("SIP", "SIP DESLOGANDO: "+ mSipProfile.getUriString());
              mSipManager.close(mSipProfile.getUriString());
           }
        } catch (Exception e) {
           Log.d("SIP", "SIP PLUGIN: Failed to close local profile: "+ e.getMessage());
        }
    }

    @Override
    public void onPause(boolean multitasking) {

    }

    @Override
    public void onDestroy() {

    }


    //COMANDO EXECUTE
    //AQUI FICAO AS ACOES
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       
        /*if (action.equals("abrirRtsp")) {
            //pega parametros do js
            this.params = args.getJSONObject(0);
            Intent intent = new Intent(cordova.getActivity(), RtspW3Activity.class);

            //LINK PARA ENVIAR PARA A ACTIVITY
            intent.putExtra("LINK_RTSP", this.params.getString("link"));

            if (this.cordova != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            }
        }*/

        callbackContext.success();
        return true;
    }


    //ENVIO DE MENSAGEMS A CLASSE
    @Override
    public Object onMessage(String id, Object data) {
       
        return null;
    }

    // Don't add @Override so that plugin still compiles on 3.x.x for a while
    public void onConfigurationChanged(Configuration newConfig) {
       
    }

}
