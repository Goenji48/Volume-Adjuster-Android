package com.goenji.volumeadjuster;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class VolumeService extends Service {
    private static NotificationManagerCompat notificationManagerCompat;
    private NotificationBroadcast notificationBroadcast;
    private VolumeRunnable volumeRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManagerCompat = NotificationManagerCompat.from(this);
        int streamType = MainActivity.getCurrentStreamType() | AudioManager.STREAM_MUSIC;

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationBroadcast = new NotificationBroadcast();
        notificationBroadcast.setStreamType(streamType);
        notificationBroadcast.setAudioManager(audioManager);

        registerReceiver(notificationBroadcast, new IntentFilter("ACTION_MUTE"));
        registerReceiver(notificationBroadcast, new IntentFilter("ACTION_VOLUME_UP"));
        registerReceiver(notificationBroadcast, new IntentFilter("ACTION_VOLUME_DOWN"));

        startForeground(1, notificationService(this, "Volume: "));

        volumeRunnable = new VolumeRunnable(getApplicationContext(),
                audioManager);
        volumeRunnable.runVolumeListener(2,null, notificationManagerCompat, streamType).run();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(notificationBroadcast != null) unregisterReceiver(notificationBroadcast);
        if(volumeRunnable != null) volumeRunnable.stopVolumeListener();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void updateNotification(Context context, int current) {
        String volumeLabel = "Volume: " + current + "%";
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(1, notificationService(context, volumeLabel));
    }

    private static Notification notificationService(Context context, String currentVolume) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "NOTIFICATION_CHANNEL");

        PendingIntent muteIntent = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_MUTE"), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent volumeUpIntent = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_VOLUME_UP"), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent volumeDownIntent = PendingIntent.getBroadcast(context, 0, new Intent("ACTION_VOLUME_DOWN"), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notification.setSmallIcon(R.drawable.ic_volume);
        notification.setPriority(Notification.PRIORITY_MIN);
        notification.setShowWhen(false);
        notification.setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        remoteViews.setTextViewText(R.id.notification_current_volume, currentVolume);
        remoteViews.setOnClickPendingIntent(R.id.notification_btnMute, muteIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_btnVolumeUp, volumeUpIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_btnVolumeDown, volumeDownIntent);
        notification.setCustomContentView(remoteViews);
        notification.setCustomBigContentView(remoteViews);

        return notification.build();
    }
}
