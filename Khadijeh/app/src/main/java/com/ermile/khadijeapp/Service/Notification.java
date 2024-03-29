package com.ermile.khadijeapp.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ermile.khadijeapp.Activity.Splash;
import com.ermile.khadijeapp.R;
import com.ermile.khadijeapp.Static.tag;
import com.ermile.khadijeapp.utility.Network;
import com.ermile.khadijeapp.utility.SaveManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.ermile.khadijeapp.utility.Firebase.Attribuites.FCM_ACTION_CLICK_NOTIFICATION;

public class Notification extends Service {

    /*Handler 60sec*/
    boolean powerServic = false;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (powerServic){
                post_smile();
                Log.d(tag.service_notification, "------------------------------------ runnable");
                handler.postDelayed(runnable, 30000);
            }
        }
    };



    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    NotificationCompat.Builder builder ;
    NotificationManagerCompat notificationManager ;


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(tag.service_notification, "onBind"+intent);
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag.service_notification, "onStartCommand BEFORE \n powerServic = "+powerServic);
        createNotificationChannel();
        if (!powerServic){
            Log.d(tag.service_notification, "------------------------------------ ------------------------------------onStartCommand  powerServic = "+powerServic);
            powerServic = true;
            handler.postDelayed(runnable, 0);
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.d(tag.service_notification, "onDestroy");
        super.onDestroy();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    // Notification send header and user Token > new Notif for me?
    public void post_smile(){
        final String apikey = SaveManager.get(getApplicationContext()).getstring_appINFO().get(SaveManager.apiKey);
        // Json <Post Smile> Method
        StringRequest PostSmile_Request = new StringRequest(Request.Method.POST, this.getString(R.string.smile), new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject get_postRequest = new JSONObject(response);

                    // Check New Notif
                    boolean ok_notif = get_postRequest.getBoolean("ok");
                    if (ok_notif){
                        JSONObject result = get_postRequest.getJSONObject("result");
                        boolean new_notif = result.getBoolean("notif_new");
                        if (new_notif){
                            Notif_is(apikey);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
                // Send Header
        {
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers_postSmile = new HashMap<>();
                headers_postSmile.put("apikey", apikey);
                return headers_postSmile;
            }
        }
                ;
        Network.getInstance().addToRequestQueue(PostSmile_Request);
    }

    // get Notification and run for user > Yes Notif is ..
    public void Notif_is(final String apikey){
        // Post Method
        StringRequest Notif_is_Request = new StringRequest(Request.Method.POST, getString(R.string.url_notif) , new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject mainObject = new JSONObject(response);
                    boolean ok_getnotif = mainObject.getBoolean("ok");
                    if (ok_getnotif) {
                        JSONArray result = mainObject.getJSONArray("result");
                        for (int j = 0; j < result.length(); j++) {
                            JSONObject jsonObject = result.getJSONObject(j);
                            String title = null;
                            String excerpt = null;
                            String desc = null;
                            String info = getApplicationContext().getString(R.string.app_name);

                            if (!jsonObject.isNull("title")){
                                title = jsonObject.getString("title");
                            }
                            if (!jsonObject.isNull("excerpt")){
                                excerpt = jsonObject.getString("excerpt");
                            }

                            if (!jsonObject.isNull("text")){
                                desc = jsonObject.getString("text");
                            }
                            else if (!jsonObject.isNull("excerpt")){
                                desc = excerpt;
                            }

                            send_Notif(title,excerpt,desc,info,j);
                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        })
                // Send Headers
        {
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers_postSmile = new HashMap<>();
                headers_postSmile.put("apikey",apikey );
                return headers_postSmile;
            }
        };
        Network.getInstance().addToRequestQueue(Notif_is_Request);
    }

    private void send_Notif(String title,String excerpt,String desc,String info,int id){

        Intent intent = new Intent(this, Splash.class); //Open activity if clicked on notification
        PendingIntent pendingIntent;

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(FCM_ACTION_CLICK_NOTIFICATION);
        pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        int randomNumber = new Random().nextInt(976431 ) + 20;
        builder = new NotificationCompat.Builder(this,CHANNEL_ID);

        long[] pattern = {500,500,500,500,500}; //Notification vibration pattern

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); //Notification alert sound


        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        builder
                .setContentTitle(title)
                .setContentText(excerpt)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(desc))
                .setAutoCancel(true)
                .setVibrate(pattern)
                .setLights(Color.BLUE,1,1)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setContentInfo(info)
                .setSmallIcon(R.drawable.logo_xml)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        notificationManager.notify(100+randomNumber+id, builder.build());
    }
}
