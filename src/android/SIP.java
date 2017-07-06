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
import org.apache.cordova.CordovaInterface;

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
import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.media.AudioManager;
import android.net.sip.SipSession;

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


    public static CordovaWebView pluginWebView;

    public static Context context; 
    public static Intent  intent;

    public static Boolean chamandoPonto = false;
    public static Boolean watcherProntoParaChamadas = false;
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        SIP.pluginWebView = webView; 
    }

    private View getView() {
        try{
            return (View)webView.getClass().getMethod("getView").invoke(webView);
        }catch(Exception e) {
            return (View)webView;
        }
    }

    @Override
    protected void pluginInitialize() {
        SIP.pluginWebView = webView; 
        watchChamdasSIP();

        //verifica se ja tem alguma ligação
       
    }

    public void fechaProfileSIP() {

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
        fechaProfileSIP();
    }

    //COMANDO EXECUTE
    //AQUI FICAO AS ACOES
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        SIP.callbackContext = callbackContext;

        if (action.equals("conectarSip")) {
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
                //builder.setAuthUserName(this.user);
                //builder.setOutboundProxy(this.sip);
                
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
                            SIP.watcherProntoParaChamadas = true;
                            
                        }

                        public void onRegistrationFailed(String localProfileUri, int errorCode,
                            String errorMessage) {
                            Log.d("SIP","SIP PLUGIN: Registration failed.  Please check settings. - ("+errorCode+")"+errorMessage);
                        }

                    });
                    SIP.callbackContext.success("true");
                    Log.d("SIP","SIP PLUGIN: Listener registrado");

                }else{
                    SIP.callbackContext.success("false");
                    Log.d("SIP","SIP PLUGIN: NÃO SUPORTADO.");
                }
            }catch(Exception e){
                SIP.callbackContext.success("false");
                Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
            }  
        }

        if(action.equals("chamar")){
            JSONObject params = args.getJSONObject(0);
            String address    = params.getString("address");
            
            try{
                SIP.fazChamada(mSipManager ,mSipProfile , address);
                SIP.callbackContext.success("true");
            }catch(Exception e){
                Log.d("SIP","SIP PLUGIN ERROR: "+e.getMessage());
                SIP.callbackContext.success("false");
            }
        }

        if(action.equals("aceitarChamada")){
            try{
                SIP.aceitaChamada();
                SIP.callbackContext.success("true");
            }catch(Exception e){
                SIP.callbackContext.success("false");
                Log.d("SIP","SIP PLUGIN ERROR: aceitaChamada "+e.getMessage());

            }
        }

        if (action.equals("desconectarSip")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    fechaProfileSIP();
                    SIP.callbackContext.success("true");
                }
            });
        }

        if(action.equals("emChamada")){
            JSONObject obj = new JSONObject();
            obj.put("emChamada",SIP.isInChamada());
            SIP.callbackContext.success(obj);
        }

        if(action.equals("encerraChamada")){
            encerraChamada();
            SIP.chamandoPonto = false;
            SIP.callbackContext.success("true");
        }

        if(action.equals("toogleWatcher")){
            if(SIP.watcherProntoParaChamadas){
                encerraChamada();
                SIP.watcherProntoParaChamadas = false;
                SIP.callbackContext.success("false");
            }

            if(!SIP.watcherProntoParaChamadas){
                SIP.watcherProntoParaChamadas = true;
                SIP.callbackContext.success("true");
            }
            
        }

        if(action.equals("toogleSpeakerEnviaLigacao")){
            JSONObject obj = new JSONObject();
            obj.put("speaker", false);
            
            try{
                if(SIP.makeAudioCall != null){
                    if(!SIP.speaker){
                        SIP.makeAudioCall.setSpeakerMode(true);
                        SIP.speaker = true;
                    }else{
                        SIP.makeAudioCall.setSpeakerMode(false);
                        SIP.speaker = false;
                    }
                }
                obj.put("speaker", SIP.speaker);
                SIP.callbackContext.success(obj);

            }catch(Exception e){
                 SIP.speaker = false;
                 obj.put("exception", e.getMessage());
                 obj.put("speaker", false);
                 SIP.callbackContext.success(obj);
            }
        }

        if(action.equals("toogleSpeakerRecebeLigacao")){
            JSONObject obj = new JSONObject();
            obj.put("speaker", false);
            
            try{
                if(SIP.sipAudioCall != null){
                    if(!SIP.speaker){
                        SIP.sipAudioCall.setSpeakerMode(true);
                        SIP.speaker = true;
                    }else{
                        SIP.sipAudioCall.setSpeakerMode(false);
                        SIP.speaker = false;
                    }
                }
                obj.put("speaker", SIP.speaker);
                SIP.callbackContext.success(obj);

            }catch(Exception e){
                 SIP.speaker = false;
                 obj.put("exception", e.getMessage());
                 obj.put("speaker", false);
                 SIP.callbackContext.success(obj);
            }
        }

        return true;
    }

    @Override
    public Object onMessage(String id, Object data) {return null;}
    public void onConfigurationChanged(Configuration newConfig) {}
    
    //ADICIONAR CANCELAR CHAMADA
    //ATTIBUTO PARA VER SE ESTA EM LIGACAO
    public static boolean isInChamada() {
        if(SIP.sipAudioCall != null){
            return SIP.sipAudioCall.isInCall();
        }

        if(SIP.makeAudioCall != null){
            return SIP.makeAudioCall.isInCall();
        }
        
        return false;
    }  

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

  

    public static void recebeChamada(Context context, Intent intent){
        SIP.context = context;
        SIP.intent  = intent;
        try {
            if(!SIP.isInChamada()){
                Log.d("SIP","SIP PLUGIN: Chamada recebida e ativa.");

                SipAudioCall.Listener listener = new SipAudioCall.Listener() {

                   @Override
                   public void onCallEstablished(SipAudioCall call) {
                      Log.d("SIP","SIP PLUGIN: recebeChamada Chamada iniciada.");
                   }

                   @Override
                   public void onCallEnded(SipAudioCall call) {
                      Log.d("SIP","SIP PLUGIN: recebeChamada Chamada encerrada.");
                      SIP.encerraChamada();
                   }

                   @Override
                   public void onRinging(SipAudioCall call, SipProfile caller){
                      Log.d("SIP","SIP PLUGIN: recebeChamada onRinging . "); 
                   }

                   @Override
                   public void onError(SipAudioCall call, int errorCode, String errorMessage){
                      Log.d("SIP","SIP PLUGIN: recebeChamada onError Chamada encerrada. ("+errorCode+") - "+errorMessage);
                      SIP.encerraChamada();
                   }

                }; 

                SIP.sipAudioCall = SipManager.newInstance(context) 
                                   .takeAudioCall(intent, null);
                SIP.sipAudioCall.setSpeakerMode(false);
                SIP.sipAudioCall.setListener(listener);

                Log.d("SIP","SIP PLUGIN: Ligação recebido pelo broadcard receiver");
            
            }else{
                Log.d("SIP","SIP PLUGIN: else aceitaChamada ja_tem_alguma_chamada_em_andamento."+SIP.isInChamada());
            }
            
        }catch(Exception e){
          SIP.sipAudioCall = null;
          Log.d("SIP","SIP PLUGIN ERR: "+e.getMessage());
        }
    }

    public static void fazChamada(SipManager m ,SipProfile sp , String address){
        if(!SIP.isInChamada()){
            
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {

               @Override
               public void onCallEstablished(SipAudioCall call) {
                  call.setSpeakerMode(false);
                  call.startAudio();
                  //call.toggleMute();
                  Log.d("SIP","SIP PLUGIN:  fazChamada chamada_em_andamento.");
               }

               @Override
               public void onCallEnded(SipAudioCall call) {
                  Log.d("SIP","SIP PLUGIN:  fazChamada chamada_terminada.");
                  SIP.chamandoPonto = false;
               }

            };

            try{
                SIP.chamandoPonto = true;
                SIP.makeAudioCall = m.makeAudioCall(sp.getUriString(), address, listener, 30);  
                SIP.makeAudioCall.setSpeakerMode(false);
            }catch(SipException e){
                SIP.chamandoPonto = false;
                SIP.makeAudioCall =  null;
                Log.d("SIP","SIP PLUGIN ERR: "+e.getMessage());
            }

        }else{
            SIP.callbackContext.success("ja_tem_alguma_chamada_em_andamento");
            Log.d("SIP","SIP PLUGIN:  fazChamada else ja_tem_alguma_chamada_em_andamento.");
        }
    }



    public static void encerraChamada(){


        try{
            if(SIP.sipAudioCall != null){
                SIP.sipAudioCall.endCall();
                if(SIP.sipAudioCall.getPeerProfile() != null){
                    SIP.sipAudioCall.endCall();  
                }
                SIP.sipAudioCall = null;
            }
           
        }catch(SipException e){
            SIP.sipAudioCall = null;
            Log.d("SIP","SIP PLUGIN ERROR sipAudioCall: "+e.getMessage());
        }

        try{    
           
            if(SIP.makeAudioCall != null){
                SIP.makeAudioCall.stopCall();
                SIP.makeAudioCall.endCall();

                if(SIP.makeAudioCall.getPeerProfile() != null){
                    SIP.makeAudioCall.endCall();
                }
                SIP.makeAudioCall = null;
            }
        }catch(SipException e){
            SIP.makeAudioCall = null;
            Log.d("SIP","SIP PLUGIN ERROR makeAudioCall: "+e.getMessage());
        }
        Log.d("SIP","SIP PLUGIN: ligacao encerrada.");
    }

    public static void aceitaChamada(){

        if(SIP.sipAudioCall != null){
            try{
                SIP.sipAudioCall.answerCall(30);
                SIP.sipAudioCall.startAudio();
            }catch(SipException e){
                Log.d("SIP","SIP PLUGIN ERROR aceitaChamada: "+e.getMessage());
            }
        }
    }

    public static void eventoRecebendoChamadaSIP(){
        if(SIP.pluginWebView != null){
            String sipComming = "";
            String sipMe      = "";

            if(SIP.sipAudioCall != null){
                if(SIP.sipAudioCall.getPeerProfile() != null){
                    sipComming = SIP.sipAudioCall.getPeerProfile().getUriString();
                }

                if(SIP.sipAudioCall.getLocalProfile() != null){
                    sipMe = SIP.sipAudioCall.getLocalProfile().getUriString();
                }
            }
            Log.d("SIP","SIP evt javascript:window.statusSIP = {status:'recebendoChamada', sipComming:'"+sipComming+"' , sipMe:'"+sipMe+"'};");
            SIP.pluginWebView.loadUrl("javascript:window.statusSIP = {status:'recebendoChamada', sipComming:'"+sipComming+"' , sipMe:'"+sipMe+"'};");                    
          
        }
    }

    public static void eventoChamadaEmAndamentoSIP(){
        if(SIP.pluginWebView != null){
            Log.d("SIP","SIP evt javascript:window.statusSIP = {status:'chamadaEmAndamento'};");
            SIP.pluginWebView.loadUrl("javascript:window.statusSIP = {status:'chamadaEmAndamento'};");                    
            
        }
    }

    public static void eventoSemChamadaSIP(){
        if(SIP.pluginWebView != null){
            Log.d("SIP","SIP evt javascript:window.statusSIP = {status:'semChamada'};");
            SIP.pluginWebView.loadUrl("javascript:window.statusSIP = {status:'semChamada'};");                    
          
        }
    }

    public void resolveStatusChamdaSIP(){
        if(SIP.sipAudioCall != null){
            if(SIP.sipAudioCall.isInCall()){
                SIP.eventoChamadaEmAndamentoSIP();
            }else{
                if(SIP.sipAudioCall.getState() == SipSession.State.INCOMING_CALL){
                    SIP.eventoRecebendoChamadaSIP();
                }else{
                    SIP.eventoSemChamadaSIP();
                }
                
            }

        }else{
            SIP.eventoSemChamadaSIP();
        }
    }

    public void resolveStatusFazChamdaSIP(){
        if(SIP.makeAudioCall != null){
            if(SIP.makeAudioCall.isInCall()){
                Log.d("SIP","SIP faz evt javascript:window.statusSIP = {status:'chamadaEmAndamento'};");
                SIP.pluginWebView.loadUrl("javascript:window.statusSIP = {status:'chamadaEmAndamento'};");                    
            }else{
                Log.d("SIP","SIP faz evt javascript:window.statusSIP = {status:'semChamada'};");
                SIP.pluginWebView.loadUrl("javascript:window.statusSIP = {status:'semChamada'};");                    
                
            }

        }else{
            SIP.eventoSemChamadaSIP();
        }
    }

    public void watchChamdasSIP(){
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                boolean looping = true;
                while(looping){
                    if(SIP.watcherProntoParaChamadas){
                        try{
                            Log.d("SIP","SIP chamandoPonto: "+SIP.chamandoPonto);
                            if(!SIP.chamandoPonto){
                                resolveStatusChamdaSIP();
                            }else{
                                resolveStatusFazChamdaSIP();
                            }
                            Thread.sleep(500);
                        }catch(Exception e){
                            Log.d("SIP","SIP PLUGIN ERROR watchChamdasSIP: "+e.getMessage());
                        }
                    }
                }
            }
        });

    }

}
