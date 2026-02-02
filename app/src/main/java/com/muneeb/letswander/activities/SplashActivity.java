package com.muneeb.letswander.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.fxn.stash.Stash;
import com.muneeb.letswander.Constants;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            if (Constants.isPermissionGranted(this)) {
                //not granted
                startActivity(new Intent(this, MapsActivity.class));
            } else {
                startActivity(new Intent(this, PermissionsActivity.class));
            }
        }, 2000);

    }
}