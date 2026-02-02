package com.muneeb.letswander.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.fxn.stash.Stash;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.muneeb.letswander.Constants;
import com.muneeb.letswander.R;
import com.muneeb.letswander.helper.NotificationHelper;
import com.muneeb.letswander.models.MarkerData;
import com.muneeb.letswander.service.GeofenceHelper;
import com.muneeb.letswander.service.LocationService;
import com.muneeb.letswander.service.NewService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    public static float GEOFENCE_RADIUS = 30;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private int POST_NOTIFICATION_CODE = 10003;
    private String descriptionToSpeak;
    private TextToSpeech textToSpeech;
    public static ArrayList<MarkerData> markerDataList;
    ProgressDialog progressDialog;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    Map<String, String> geo = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build().connect();

        textToSpeech = new TextToSpeech(this, this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        showMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMap();
    }

    private void showMap() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (!checkGPSStatus()) {
            progressDialog.show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(googleMap -> {
                            mMap = googleMap;
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                return;
                            }
                            googleMap.setMyLocationEnabled(true);
                            Constants.databaseReference().child(Constants.Markers).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        markerDataList = new ArrayList<>();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            MarkerData markerData = snapshot.getValue(MarkerData.class);
                                            markerDataList.add(markerData);

                                            LatLng location = new LatLng(markerData.getLatitude(), markerData.getLongitude());
                                            Boolean star = markerData.getStar();
                                            int width = 48;
                                            int height = 48;
                                            BitmapDescriptor markerIcon = vectorToBitmap(R.drawable.baseline_circle_24, width, height);
                                            if (star != null && star) {
                                                markerIcon = vectorToBitmap(R.drawable.baseline_star_rate_24, width, height);
                                            }
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .title(markerData.getTitle())
                                                    .snippet(markerData.getDescription())
                                                    .icon(markerIcon);

//                                            mMap.clear();
                                            addCircle(location, GEOFENCE_RADIUS, markerOptions.getSnippet());
                                            googleMap.addMarker(markerOptions);
//                                            addGeofence(location, GEOFENCE_RADIUS, markerOptions.getSnippet());
                                            Stash.put(Constants.STASH_Markers, markerDataList);
                                        }
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                            googleMap.setMyLocationEnabled(true);
                            LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                            googleMap.setOnMarkerClickListener(marker -> {
                                showCustomDialog(marker.getTitle(), marker.getPosition(), marker.getSnippet());
                                descriptionToSpeak = marker.getSnippet(); // Set the description to speak
                                onInit(TextToSpeech.SUCCESS); // Initialize TTS
                                return false;
                            });

                            googleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                                @Override
                                public void onMyLocationClick(@NonNull Location location) {
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    Toast.makeText(MapsActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            startInitService();

                        });
                    }
                }
            });
        }
    }

    Intent mServiceIntent;
    private NewService mYourService;

    private void startInitService() {
        mYourService = new NewService();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(mServiceIntent);
            } else {
                startService(mServiceIntent);
            }
        }
    }

    private void askToDisableDozeMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "askToDisableDozeMode: ");
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } else {
                    Toast.makeText(MapsActivity.this, "Doze mode is active", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int vectorResourceId, int width, int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResourceId);
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showCustomDialog(String title, LatLng location, String description) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogLocation = dialog.findViewById(R.id.dialog_location);
        TextView dialogDescription = dialog.findViewById(R.id.dialog_description);

        dialogTitle.setText(title);
        dialogLocation.setText(location.latitude + ", " + location.longitude);
        dialogDescription.setText(description);

        Button okButton = dialog.findViewById(R.id.dialog_ok_button);
        okButton.setOnClickListener(view -> {
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            dialog.dismiss();
        });

        dialog.setOnCancelListener(dialog1 -> {
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        });

    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text-to-speech language not supported.", Toast.LENGTH_SHORT).show();
            } else {
                if (descriptionToSpeak != null && !descriptionToSpeak.isEmpty()) {
                    textToSpeech.speak(descriptionToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        } else {
            Toast.makeText(this, "Text-to-speech initialization failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addGeofence(LatLng latLng, float radius, String snippet) {
//        GEOFENCE_ID = UUID.randomUUID().toString();
        Geofence geofence = geofenceHelper.getGeofence(String.valueOf(latLng), latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geo.put(geofence.getRequestId(), snippet);
        Stash.put(Constants.GEOFENCE, geo);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Geofence Added..."))
                .addOnFailureListener(e -> {
                    String errorMessage = geofenceHelper.getErrorString(e);
                    Log.d(TAG, "onFailure: " + errorMessage);
                });
    }

    private void addCircle(LatLng latLng, float radius, String snippet) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
        mMap.setOnMyLocationChangeListener(location -> {
            float[] distance = new float[2];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance);
            if (distance[0] < circleOptions.getRadius()) {
                descriptionToSpeak = snippet;
                //  onInit(TextToSpeech.SUCCESS);
                // new NotificationHelper(MapsActivity.this).sendHighPriorityNotification("Let's Wander", descriptionToSpeak, MapsActivity.class);
            }

        });
    }

    private boolean checkGPSStatus() {
        LocationManager locationManager;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this).setTitle("Open Settings")
                    .setMessage("GPS not enabled")
                    .setCancelable(true)
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }).show();
        }
        return !gps_enabled && !network_enabled;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
