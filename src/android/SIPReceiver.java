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

import android.app.AlarmManager;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
public class SIPReceiver extends BroadcastReceiver {
  
  @Override
  public void onReceive(Context context, Intent intent) {
    
      SIP.context = context;
      SIP.intent  = intent;

      Log.d("SIP","SIP PLUGIN: RECEBENDO LIGACAO");
     
      Bundle extras = intent.getExtras();

      ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      State wifi = conMan.getNetworkInfo(1).getState();

      if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
            Log.d("SIP","SIP PLUGIN: ENVIA EVENTO CORDOVA sendJavascript");
            Log.d("SIP","SIP PLUGIN: CONECTADO A WIFI E RECEBENDO CHAMADA");
            Log.d("SIP","SIP PLUGIN: SIP.pluginWebView "+SIP.pluginWebView);

            NotificationCompat.Builder b = new NotificationCompat.Builder(context);
            PendingIntent contentIntent  = null;

            if(SIP.pluginWebView != null){              
              
              if(SIP.isActivityVisible()){
                SIP.recebeChamada(context,intent);
              }else{
                
              
                Notification notification = new Notification(context.getApplicationInfo().icon, 
                                                            "Recebendo chamada!!",
                                                            System.currentTimeMillis());

                notification.setLatestEventInfo(context, 
                                "Recebendo chamada", 
                                "Recebendo chamada!",
                PendingIntent.getActivity(context, 0, new Intent(),android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
                
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);


              }
              
            }else{

              PackageManager pm   = context.getPackageManager();
              Intent launchIntent = pm.getLaunchIntentForPackage("com.racionaltec");
              
              contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

              b.setAutoCancel(true)
             .setDefaults(Notification.DEFAULT_ALL)
             .setWhen(System.currentTimeMillis())          
             .setSmallIcon(context.getApplicationInfo().icon)
             .setTicker("Hearty365")            
             .setContentTitle("Recebendo chamada!")
             .setContentText("Você está recebendo uma chamada.")
             .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
             .setContentIntent(contentIntent)
             .setContentInfo("Info");
              
             NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
             notificationManager.notify(1, b.build());
            }
            
      }else {
        Log.d("SIP PLUGIN:", "SEM INTERNET !!!!!");
      }
  }
 
}
