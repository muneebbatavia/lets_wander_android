package com.muneeb.letswander.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.muneeb.letswander.service.NewService;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            Log.i("onReceive: ", "Context is null");
            return;
        }
//        context.getApplicationContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("onReceive: ", "        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {");
            context.startForegroundService(new Intent(context, NewService.class));
        } else {
            Log.i("onReceive: ", "} else {");
            context.startService(new Intent(context, NewService.class));
        }
    }
}
