package com.muneeb.letswander.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.GeofencingEvent;
import com.muneeb.letswander.R;
import com.muneeb.letswander.helper.MyHelperService;

import java.util.Locale;

public class TtsService extends Service implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    String TAG = "SERVICEHHH";
    String desc = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) {
                // Handle error
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(2, new Notification());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String description = intent.getStringExtra("description");
            if (description != null && !description.isEmpty()) {
                Log.d(TAG, "description TTS :   " + description);
                desc = description;
                onInit(TextToSpeech.SUCCESS);
            } else {
                Log.d(TAG, "description is null");
            }
        } else {
            Log.d(TAG, "Intent is null in TTS");
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Service running")
                .setContentText("Detecting location in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Text-to-speech language not supported.");
            } else {
                textToSpeech.speak(desc, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            Log.e(TAG, "Text-to-speech initialization failed.");
        }
    }
}
