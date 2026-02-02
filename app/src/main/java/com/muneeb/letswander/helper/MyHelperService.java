package com.muneeb.letswander.helper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MyHelperService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Perform the operation that requires binding to the service.

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
