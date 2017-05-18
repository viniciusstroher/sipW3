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
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
 
 
public class SIP extends CordovaPlugin {
    private static final String LOG_TAG = "SIPW3";

    private CallbackContext callbackContext;
    private JSONObject params;
    private int orientation;

    public static SipManager mSipManager = null;
    public SipProfile mSipProfile = null;
    public SIPReceiver callReceiver;

    public String sip       = "";
    public String password  = "";
    public String user      = "";
    public static Boolean inBackground = false;

    private View getView() {
        try {
            return (View)webView.getClass().getMethod("getView").invoke(webView);
        } catch (Exception e) {
            return (View)webView;
        }
    }

    @Override
    protected void pluginInitialize() {
        
        
    }

    public void closeLocalProfile() {

        if (mSipManager == null) {
           return;
        }
        
        try {
           if (mSipProfile != null) {
            
              Log.d("SIP", "SIP DESLOGANDO: "+ mSipProfile.getUriString());
              mSipManager.close(mSipProfile.getUriString());
              
              if (mSipManager.isRegistered(mSipProfile.getProfileName())){
                mSipManager.unregister(mSipProfile, null);
              }
           }
        } catch (Exception e) {
           Log.d("SIP", "SIP PLUGIN: Failed to close local profile: "+ e.getMessage());
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        
    }

    @Override
    public void onResume(boolean multitasking) {

    }

    @Override
    public void onDestroy() {
        Log.d("SIP", "SIP PLUGIN: Destruindo Sip profile ");
        closeLocalProfile();
    }


    //COMANDO EXECUTE
    //AQUI FICAO AS ACOES
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       
        if (action.equals("conectarSip")) {
            //pega parametros do js
            //this.params = args.getJSONObject(0);
            JSONObject params = args.getJSONObject(0);

            this.sip        = params.getString("sip");
            this.password   = params.getString("password");
            this.user       = params.getString("user");

            try{
                if (mSipManager == null) {
                    mSipManager = SipManager.newInstance(cordova.getActivity());
                }

                SipProfile.Builder builder = new SipProfile.Builder(this.user, this.sip);
                builder.setPassword(this.password);
                mSipProfile = builder.build();

                Log.d("SIP","SIP PLUGIN: SIP PROFILE BUILDED");

            }catch(Exception e){
                Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
            }

            try{
                
                Intent intent = new Intent();
                intent.setAction("org.apache.cordova.SIP.INCOMING_CALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, intent, Intent.FILL_IN_DATA);
                
                Boolean isRegistered    = mSipManager.isRegistered(mSipProfile.getUriString());
                Boolean isOpened        = mSipManager.isOpened(mSipProfile.getUriString());
                Boolean isVoipSupported = mSipManager.isVoipSupported(cordova.getActivity());
                Boolean isApiSupported  = mSipManager.isApiSupported(cordova.getActivity());
                
                Log.d("SIP","SIP PLUGIN: isRegistered "    + isRegistered);
                Log.d("SIP","SIP PLUGIN: isOpened "        + isOpened);
                Log.d("SIP","SIP PLUGIN: isVoipSupported " + isVoipSupported);
                Log.d("SIP","SIP PLUGIN: isApiSupported "  + isApiSupported);
               
                mSipProfile.getAutoRegistration();
                
                if(isVoipSupported && isApiSupported){
                    
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
                }else{
                    
                    Log.d("SIP","SIP PLUGIN: NÃO SUPORTADO.");
                }
            }catch(Exception e){
                Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
            }  

        }

        if (action.equals("desconectarSip")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    closeLocalProfile();
                }
            });

        }

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

    public static void aceitaChamada(Context context, Intent intent){
        if(isVisible()){
            Log.d("SIP","SIP PLUGIN: Chamada recebida e ativa.");

            SipAudioCall sipAudioCall = SipManager.newInstance(context) 
                          .takeAudioCall(intent, null);

            sipAudioCall.answerCall(30);
            sipAudioCall.startAudio();
            sipAudioCall.setSpeakerMode(true);

        }else{
            Log.d("SIP","SIP PLUGIN: App em background.");
        }
    }

}
