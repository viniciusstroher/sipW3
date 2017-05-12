package org.apache.cordova.SIP;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import android.net.sip.SipManager;
import android.net.sip.SipSession;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;

import android.util.Log;
import android.app.Activity;

import java.util.Iterator;
import java.util.Set;

public class SIPReceiver extends BroadcastReceiver {
 
  @Override
  public void onReceive(Context context, Intent intent) {
      SipAudioCall incomingCall = null;
      Log.d("SIP","SIP PLUGIN: RECEBENDO LIGACAO");
      //dumpIntent(intent);
      
      Bundle extras = intent.getExtras();

      ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

      //State mobile = conMan.getNetworkInfo(0).getState();

      State wifi = conMan.getNetworkInfo(1).getState();

      if (wifi == State.CONNECTED || wifi == State.CONNECTING) {

        Log.d("SIP","SIP PLUGIN: CONECTADO A WIFI E RECEBENDO CHAMADA");
        try {
          
          SipAudioCall.Listener listener = new SipAudioCall.Listener() {
              @Override
              public void onRinging(SipAudioCall call, SipProfile caller) {
                try {
                  Log.d("SIP","SIP PLUGIN: LIGACAO ");
                  call.answerCall(30);
                }catch (Exception e) {
                  Log.d("SIP","SIP PLUGIN: "+e.getMessage());
                }
              }
          };
          
          Sip sipact = ((Sip)context.getApplicationContext());
          incomingCall = sipact.mSipManager.takeAudioCall(intent, listener);
          incomingCall.answerCall(30);
          incomingCall.startAudio();
          incomingCall.setSpeakerMode(true);
          
          /*if(incomingCall.isMuted()) {
            incomingCall.toggleMute();
          }

          wtActivity.call = incomingCall;
          wtActivity.updateStatus(incomingCall);*/
          

        }catch(Exception e){
          Log.d("SIP","SIP PLUGIN: "+e.getMessage());
        }

      }else {
        Log.d("SIP PLUGIN:", "SEM INTERNET !!!!!");
      }
  }
  /*
  public static void dumpIntent(Intent i){

    Log.d("SIP", i.getAction());
    Log.d("SIP", Integer.toString(i.getFlags()));
    Uri uri = i.getData();
    
    if (uri != null) {
      Log.d("SIP", uri.toString());
    }
    else {
      Log.d("SIP", "data null");
    }

    Bundle bundle = i.getExtras();
    if (bundle != null) {
        Set<String> keys = bundle.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Log.d("SIP","[" + key + "=" + bundle.get(key)+"]");
        }
    }
  }*/
}
