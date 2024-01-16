package com.goenji.volumeadjuster;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

public interface VolumeListener  {
    void stopVolumeListener();
    Runnable runVolumeListener(int runId, @Nullable TextView textView,
                               @Nullable NotificationManagerCompat notificationManagerCompat);
}

