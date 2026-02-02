package com.muneeb.letswander.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.muneeb.letswander.Constants;
import com.muneeb.letswander.R;
import com.muneeb.letswander.databinding.ActivityPermissionsBinding;

public class PermissionsActivity extends AppCompatActivity {

    private ActivityPermissionsBinding b;

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private int POST_NOTIFICATION_CODE = 10003;


    Constants.CURRENT_LAYOUT currentLayout = Constants.CURRENT_LAYOUT.LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPermissionsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.original_background));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            currentLayout = Constants.CURRENT_LAYOUT.LOCATION;
            updateUI();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            currentLayout = Constants.CURRENT_LAYOUT.GEOLOCATION;
            updateUI();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            currentLayout = Constants.CURRENT_LAYOUT.NOTIFICATION;
            updateUI();
        } else {
            moveToNextScreen();
        }

        b.allowBtn.setOnClickListener(v -> {

            switch (currentLayout) {
                case LOCATION:
                    askForLocation();
                    break;
                case GEOLOCATION:
                    askForGeo();
                    break;
                case NOTIFICATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        askForNotification();
                    } else {
                        moveToNextScreen();
                    }
                    break;
            }
        });


/*        b.backgroundBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 29) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Bg is granted", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                    }
                }

            }
        });

        b.locationBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "location granted", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        });*/

    }

    private void moveToNextScreen() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    private void askForNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATION_CODE);
        } else {
            moveToNextScreen();
        }
    }

    private void askForGeo() {
        Toast.makeText(this, "Please Select All the time for better app working", Toast.LENGTH_LONG).show();
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
        }
    }

    private void askForLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }
    }

    private void updateUI(){
        switch (currentLayout) {
            case LOCATION:
                b.locationImage.setImageResource(R.drawable.ic_location);
                b.locationHeading.setText("Enable Location");
                b.locationDesc.setText("Please provide us access to your location, which is required to check if you are passing by a place");
                break;
            case GEOLOCATION:
                b.locationImage.setImageResource(R.drawable.ic_geolocation);
                b.locationHeading.setText("Enable Geo Location");
                b.locationDesc.setText("Please provide us access to your location, which is required to check if you are passing by a place");
                break;
            case NOTIFICATION:
                b.locationImage.setImageResource(R.drawable.ic_notifications);
                b.locationHeading.setText("Enable Notification");
                b.locationDesc.setText("Please provide us access to your Notification, which is required to notify you if you are passing by a place");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                currentLayout = Constants.CURRENT_LAYOUT.GEOLOCATION;
                updateUI();
            } else {
                currentLayout = Constants.CURRENT_LAYOUT.LOCATION;
                updateUI();
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                currentLayout = Constants.CURRENT_LAYOUT.NOTIFICATION;
                updateUI();
            } else {
                currentLayout = Constants.CURRENT_LAYOUT.GEOLOCATION;
                updateUI();
                //We do not have the permission..
                Toast.makeText(this, "Geo Location permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == POST_NOTIFICATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                moveToNextScreen();
            } else {
                currentLayout = Constants.CURRENT_LAYOUT.NOTIFICATION;
                updateUI();
                //We do not have the permission..
                Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
