package com.goenji.volumeadjuster;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

public class VolumeRunnable implements VolumeListener {

    private final Context context;
    private Handler handler;
    private Runnable runnable;
    private final AudioManager audioManager;

    public VolumeRunnable(Context context, AudioManager audioManager) {
        this.context = context;
        this.audioManager = audioManager;
    }

    @Override
    public void stopVolumeListener() {
        if(handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public Runnable runVolumeListener(int runId, @Nullable TextView textView,
                                      @Nullable NotificationManagerCompat notificationManagerCompat, @NonNull int streamType) {
        runnable = new Runnable() {
            @Override
            public void run() {
                float currentVolume = 0f;

                if (streamType == AudioManager.STREAM_MUSIC) {
                    currentVolume = audioManager.getStreamVolume(streamType)
                    * 6.667f; //try round to one hundred.
                } else if (streamType == AudioManager.STREAM_ALARM || streamType == AudioManager.STREAM_RING) {
                    currentVolume = audioManager.getStreamVolume(streamType)
                            * 14.295f; //try round to one hundred.
                } else if (streamType == AudioManager.STREAM_VOICE_CALL) {
                    currentVolume = audioManager.getStreamVolume(streamType)
                            * 20f; //try round to one hundred.
                }

                String volumeLabel = "Volume: " + (int) currentVolume + "%";

                if(textView != null) textView.setText(volumeLabel);

                if (notificationManagerCompat != null) {
                    VolumeService.updateNotification(context, (int) currentVolume);
                }

                handler = new Handler();
                handler.postDelayed(this, 1000);
            }
        };

        return runnable;
    }
}
