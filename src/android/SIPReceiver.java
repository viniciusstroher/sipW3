package org.apache.cordova.SIP;

import org.apache.cordova.SIP.SIP;
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
      
      Log.d("SIP","SIP PLUGIN: RECEBENDO LIGACAO");
     
      Bundle extras = intent.getExtras();

      ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      State wifi = conMan.getNetworkInfo(1).getState();

      if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
        Log.d("SIP","SIP PLUGIN: ENVIA EVENTO CORDOVA sendJavascript");
        Log.d("SIP","SIP PLUGIN: CONECTADO A WIFI E RECEBENDO CHAMADA");

        SIP.pluginWebView.loadUrl("javascript:window.recebendoChamadaSip = {status:true};");     
        SIP.aceitaChamada(context,intent);
        
      }else {
        Log.d("SIP PLUGIN:", "SEM INTERNET !!!!!");
      }
  }
 
}
