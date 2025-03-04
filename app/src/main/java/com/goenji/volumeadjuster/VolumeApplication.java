package com.goenji.volumeadjuster;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class VolumeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        notificationChannel();
    }
    private void notificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("NOTIFICATION_CHANNEL",
                    "Volume Service",
                    NotificationManager.IMPORTANCE_NONE);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.createNotificationChannel(notificationChannel);
            }
        }
    }
}
