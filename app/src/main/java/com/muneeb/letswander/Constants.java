package com.muneeb.letswander;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Constants {

    public static final String ARE_ALL_PERMISSIONS_GRANTED = "ARE_ALL_PERMISSIONS_GRANTED";
    public static final String Markers = "Markers";
    public static final String STASH_Markers = "STASH_Markers";
    public static final String STASH_GEOFENCE = "STASH_GEOFENCE";
    public static final String GEOFENCE = "GEOFENCE";

    public enum CURRENT_LAYOUT {LOCATION, GEOLOCATION, NOTIFICATION}

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static final String[] permissions13 = {
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final String[] permissions = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    };

    public static boolean isPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return
                    (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        } else {
            return
                    (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }
    }

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("LetsWander");
        db.keepSynced(true);
        return db;
    }
}
