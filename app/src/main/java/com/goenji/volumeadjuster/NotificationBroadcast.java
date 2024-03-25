package com.goenji.volumeadjuster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class NotificationBroadcast extends BroadcastReceiver {

    private final AudioManager audioManager;
    private boolean isMute = true;

    public NotificationBroadcast(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("servicePrefs", Context.MODE_PRIVATE);
                boolean activated = sharedPreferences.getBoolean("serviceActivated", false);
                if (activated) {
                    Intent sIntent  = new Intent(context, VolumeService.class);
                    context.startService(sIntent);
                }
            }

            if (action.equals("ACTION_MUTE")) {
                if (isMute) {
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                    isMute = false;
                } else {
                    audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
                    isMute = true;
                }
            }

            if (action.equals("ACTION_VOLUME_UP")) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }

            if (action.equals("ACTION_VOLUME_DOWN")) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        }
    }
}
