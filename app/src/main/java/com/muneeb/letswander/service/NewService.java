package com.muneeb.letswander.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.fxn.stash.Stash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.muneeb.letswander.Constants;
import com.muneeb.letswander.GeofenceBroadcastReceiver;
import com.muneeb.letswander.R;
import com.muneeb.letswander.activities.MapsActivity;
import com.muneeb.letswander.helper.Restarter;
import com.muneeb.letswander.models.MarkerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewService extends Service {

    GeofencingEvent geofencingEvent;

    String TAG = "SERVICEHHH";
    private GeofencingClient geofencingClient;
    Context context;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    LocationCallback locationCallback;
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(2, new Notification());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = this;

        Log.d(TAG, "onStartCommand");

        if (intent != null) {

            /*
            runnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Running", Toast.LENGTH_SHORT).show();
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //   return TODO;
                    }
                    Task<Location> task = fusedLocationProviderClient.getLastLocation();
                    task.addOnSuccessListener(location -> {});
                    handler.postDelayed(this, 2000); // Schedule the next toast in 2 seconds
                }
            };
            handler.postDelayed(runnable, 0);
            */


            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Log.d(TAG, locationResult.getLastLocation().getLatitude() + " " + locationResult.getLastLocation().getLongitude());
                }
            };

            Looper myLooper = Looper.myLooper();
            if (myLooper != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return TODO;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, myLooper);
            }

            ArrayList<MarkerData> list = new ArrayList<>();
            Constants.databaseReference().child(Constants.Markers).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MarkerData markerData = snapshot.getValue(MarkerData.class);
                            list.add(markerData);
                        }

                        Log.d(TAG, "list   " + list.size());

                        Stash.put(Constants.STASH_Markers, list);

                        geofencingClient = LocationServices.getGeofencingClient(context);

                        List<Geofence> geofenceList = new ArrayList<>();
                        for (MarkerData markerData : list) {
                            LatLng latLng = new LatLng(markerData.getLatitude(), markerData.getLongitude());
                            Geofence geofence = GeofenceHelper.getGeofence(String.valueOf(latLng), latLng, MapsActivity.GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);

                            Intent i = new Intent(context, GeofenceBroadcastReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                                    .addGeofence(geofence)
                                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                                    .build();

                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                //    return ;
                            }
                            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Geofence Added..."))
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "onFailure: " + e);
                                    });

                            geofenceList.add(geofence);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            Toast.makeText(this, "Intent is null", Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("onTaskRemoved: ", "called.");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(),
                1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacks(runnable);

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }
}
