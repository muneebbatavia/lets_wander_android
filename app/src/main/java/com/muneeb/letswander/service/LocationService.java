package com.muneeb.letswander.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.location.LocationRequest;

import com.fxn.stash.Stash;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.muneeb.letswander.Constants;
import com.muneeb.letswander.R;
import com.muneeb.letswander.activities.MapsActivity;
import com.muneeb.letswander.helper.NotificationHelper;
import com.muneeb.letswander.models.MarkerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service implements LocationListener, TextToSpeech.OnInitListener {
    private LocationManager locationManager;
    private GeofenceHelper helper;
    Context context;
    String desc, descriptionToSpeak = "";
    private TextToSpeech textToSpeech;
    private static final String TAG = "LocationDETE";

    @Override
    public void onCreate() {
        super.onCreate();

        helper = new GeofenceHelper(this);

        context = this;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        helper = new GeofenceHelper(this);

        textToSpeech = new TextToSpeech(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(2, new Notification());

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
                .setContentTitle("Service running Location")
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Request location updates here using the locationRequest you defined earlier

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        }
        if (intent != null){
            Toast.makeText(this, "Intent is NOT NULL", Toast.LENGTH_SHORT).show();
            geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent != null) {
                int transitionType = geofencingEvent.getGeofenceTransition();
                Toast.makeText(this, "transitionType   " + transitionType, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Intent is null", Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }

    GeofencingEvent geofencingEvent;

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location", Toast.LENGTH_SHORT).show();
        // Handle location updates here
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double lat = (double) ((long) (latitude * 1e6)) / 1e6;
        double lon = (double) ((long) (longitude * 1e6)) / 1e6;

        Toast.makeText(this, "Location : " + lat + ",\t" + lon, Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(this);

        ArrayList<MarkerData> list = Stash.getArrayList(Constants.STASH_Markers, MarkerData.class);
        List<Geofence> geofenceList = new ArrayList<>();
        for (MarkerData markerData : list) {
            LatLng latLng = new LatLng(markerData.getLatitude(), markerData.getLongitude());
            Geofence geofence = helper.getGeofence(String.valueOf(latLng), latLng, MapsActivity.GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
            geofenceList.add(geofence);
        }

        if (geofencingEvent != null) {
            int transitionType = geofencingEvent.getGeofenceTransition();
            Toast.makeText(this, "transitionType 2.0 ---  " + transitionType, Toast.LENGTH_SHORT).show();
            if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL || transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Toast.makeText(this, "Entered", Toast.LENGTH_SHORT).show();
                //        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
                ArrayList<MarkerData> markerDataList = Stash.getArrayList(Constants.STASH_Markers, MarkerData.class);
                for (Geofence geofence : geofenceList) {
                    String geofenceRequestId = geofence.getRequestId();
                    LatLng latLng = new LatLng(lat, lon);
                    String loc = String.valueOf(latLng);
                    if (geofenceRequestId.equals(loc)) {
                        for (MarkerData markerData : markerDataList) {
                            LatLng ll = new LatLng(markerData.getLatitude(), markerData.getLongitude());
                            Toast.makeText(this, ll.toString() + "\n" + loc, Toast.LENGTH_SHORT).show();
                            if (String.valueOf(ll).equals(loc)) {
                                Toast.makeText(this, "Location Match", Toast.LENGTH_SHORT).show();
                                descriptionToSpeak = markerData.getDescription();
                                onInit(TextToSpeech.SUCCESS);
                                notificationHelper.sendHighPriorityNotification(markerData.getTitle(), descriptionToSpeak, MapsActivity.class);
                                break;
                            }
                        }
                    }
                }
                Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
            }
        } else {
            Toast.makeText(this, "GeofencingEvent is NULL", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "GeofencingEvent is null");
        }

    }

    // Implement other LocationListener methods (onProviderEnabled, onProviderDisabled, onStatusChanged) as needed

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
