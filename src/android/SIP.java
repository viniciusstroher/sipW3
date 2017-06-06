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
 
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;

public class SIP extends CordovaPlugin {
    private static final String LOG_TAG = "SIPW3";

    public static CallbackContext callbackContext;
    private       JSONObject      params;
    private       int             orientation;

    public static SipManager mSipManager = null;
    public        SipProfile mSipProfile = null;
    public        SIPReceiver callReceiver;

    public static SipAudioCall sipAudioCall;
    public static SipAudioCall makeAudioCall;

    public String sip       = "";
    public String password  = "";
    public String user      = "";
    public static Boolean inBackground = false;


    public static View pluginWebView;
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
        activityPaused();
    }

    @Override
    public void onResume(boolean multitasking) {
        activityResumed();

    }

    @Override
    public void onDestroy() {
        Log.d("SIP", "SIP PLUGIN: Destruindo Sip profile ");
        encerraChamada();
        closeLocalProfile();
    }


    //COMANDO EXECUTE
    //AQUI FICAO AS ACOES
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        SIP.callbackContext = callbackContext;

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
                            SIP.inChamadaFalse();
                            SIP.callbackContext.success("true");
                            
                        }

                        public void onRegistrationFailed(String localProfileUri, int errorCode,
                            String errorMessage) {
                            Log.d("SIP","SIP PLUGIN: Registration failed.  Please check settings. - ("+errorCode+")"+errorMessage);
                            SIP.callbackContext.success("false");

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

        if(action.equals("chamar")){
            JSONObject params = args.getJSONObject(0);
            String address  = params.getString("address");
            
            try{
                fazChamada(mSipManager ,mSipProfile , address);
            }catch(Exception e){
                Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
            }
        }

        if (action.equals("desconectarSip")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    SIP.inChamadaFalse();
                    closeLocalProfile();
                }
            });

        }

        if(action.equals("emChamada")){
            JSONObject obj = new JSONObject();
            obj.put("emChamada", SIP.isInChamada());

            SIP.callbackContext.success(obj);
        }

        if(action.equals("encerraChamada")){
            
            encerraChamada();
            SIP.callbackContext.success("true");
        }

        if(action.equals("toogleSpeakerEnviaLigacao")){
            JSONObject obj = new JSONObject();
            obj.put("speaker", false);
            
            try{
                if(!SIP.speaker){
                    SIP.makeAudioCall.setSpeakerMode(true);
                }else{
                    SIP.makeAudioCall.setSpeakerMode(false);
                }

                obj.put("speaker", SIP.speaker);
                SIP.callbackContext.success(obj);

            }catch(Exception e){
                 obj.put("exception", e.getMessage());
                 obj.put("speaker", false);
                 SIP.callbackContext.success(obj);
            }
        }

        if(action.equals("toogleSpeakerRecebeLigacao")){
            JSONObject obj = new JSONObject();
            obj.put("speaker", false);
            
            try{
                if(!SIP.speaker){
                    SIP.sipAudioCall.setSpeakerMode(true);
                }else{
                    SIP.sipAudioCall.setSpeakerMode(false);
                }

                obj.put("speaker", SIP.speaker);
                SIP.callbackContext.success(obj);

            }catch(Exception e){
                 obj.put("exception", e.getMessage());
                 obj.put("speaker", false);
                 SIP.callbackContext.success(obj);
            }
        }

        return true;
    }

    public static void sendUpdate(String type) {
        if (SIP.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, type);
            result.setKeepCallback(true);
            SIP.callbackContext.sendPluginResult(result);
        }
        SIP.pluginWebView.postMessage("networkconnection", type);
    }

    //ENVIO DE MENSAGEMS A CLASSE
    @Override
    public Object onMessage(String id, Object data) {
       
        return null;
    }

    // Don't add @Override so that plugin still compiles on 3.x.x for a while
    public void onConfigurationChanged(Configuration newConfig) {
       SIP.pluginWebView = getView();
    }
    
    public static void encerraChamada(){
        try{
                
            SIP.makeAudioCall.endCall();
            SIP.makeAudioCall.close();
            Log.d("SIP","SIP PLUGIN: ligacao encerrada.");
        }catch(SipException e){
            Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
        }

        try{
            
            SIP.sipAudioCall.endCall();
            SIP.sipAudioCall.close();
            Log.d("SIP","SIP PLUGIN: ligacao encerrada.");
        }catch(SipException e){
            Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
        }

    }
    public static void aceitaChamada(Context context, Intent intent){
        try {
            if(SIP.isActivityVisible()){
                if(!SIP.isInChamada()){
                    SIP.inChamadaTrue();
                    Log.d("SIP","SIP PLUGIN: Chamada recebida e ativa.");

                    SipAudioCall.Listener listener = new SipAudioCall.Listener() {

                       @Override
                       public void onCallEstablished(SipAudioCall call) {
                          call.startAudio();
                          //call.setSpeakerMode(true);
                          call.toggleMute();
                          
                          SIP.inChamadaTrue();
                          SIP.callbackContext.success("chamada_em_andamento");
                          Log.d("SIP","SIP PLUGIN: aceitaChamada Chamada iniciada." + SIP.isInChamada());
                       }

                       @Override
                       public void onCallEnded(SipAudioCall call) {
                          SIP.inChamadaFalse();
                          SIP.callbackContext.success("chamada_terminada");
                          Log.d("SIP","SIP PLUGIN: aceitaChamada Chamada encerrada." +SIP.isInChamada());
                       }
                     
                      @Override
                       public void onError(SipAudioCall call, int errorCode, String errorMessage){
                          SIP.inChamadaFalse();
                          SIP.callbackContext.success("chamada_terminada");
                          Log.d("SIP","SIP PLUGIN: onError Chamada encerrada. "+SIP.isInChamada()+"  "+errorCode+" - "+errorMessage);
                       }

                    };

                    SIP.sipAudioCall = SipManager.newInstance(context) 
                                  .takeAudioCall(intent, null);
                    SIP.sipAudioCall.setListener(listener);

                    SIP.sipAudioCall.answerCall(30);
                    SIP.sipAudioCall.startAudio();
                    //SIP.sipAudioCall.setSpeakerMode(true);

                    SIP.callbackContext.success("chamada_em_andamento");
                }else{
                    Log.d("SIP","SIP PLUGIN: else aceitaChamada ja_tem_alguma_chamada_em_andamento."+SIP.isInChamada());
                    SIP.callbackContext.success("ja_tem_alguma_chamada_em_andamento");
                }
            }else{
                Log.d("SIP","SIP PLUGIN: App em background."+SIP.isInChamada());
            }
        }catch(Exception e){
          Log.d("SIP","SIP PLUGIN ERR: "+e.getMessage());
        }
    }

    public static void fazChamada(SipManager m ,SipProfile sp , String address){
        if(!SIP.isInChamada()){
            
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {

               @Override
               public void onCallEstablished(SipAudioCall call) {
                  call.startAudio();
                  //call.setSpeakerMode(true);
                  call.toggleMute();
                  
                  SIP.inChamadaTrue();
                  SIP.callbackContext.success("chamada_em_andamento");
                  Log.d("SIP","SIP PLUGIN:  fazChamada chamada_em_andamento.");
               }

               @Override
               public void onCallEnded(SipAudioCall call) {
                  SIP.inChamadaFalse();
                  SIP.callbackContext.success("chamada_terminada");
                  Log.d("SIP","SIP PLUGIN:  fazChamada chamada_terminada.");
               }


            };

            try{
                SIP.makeAudioCall = m.makeAudioCall(sp.getUriString(), address, listener, 30);  
            }catch(SipException e){
                SIP.inChamadaFalse();
                Log.d("SIP","SIP PLUGIN ERR: "+e.getMessage());
            }

        }else{
            SIP.callbackContext.success("ja_tem_alguma_chamada_em_andamento");
            Log.d("SIP","SIP PLUGIN:  fazChamada else ja_tem_alguma_chamada_em_andamento.");
        }
    }

    //ADICIONAR CANCELAR CHAMADA
    //ATTIBUTO PARA VER SE ESTA EM LIGACAO
     public static boolean isInChamada() {
        return inChamada;
    }  

    public static void inChamadaTrue() {
        inChamada = true;
    }

    public static void inChamadaFalse() {
        inChamada = false;
    }

    private static boolean inChamada;


    public static boolean isActivityVisible() {
        return activityVisible;
    }  

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
     activityVisible = false;
    }

    private static boolean activityVisible;
    private static boolean speaker = false;
}
