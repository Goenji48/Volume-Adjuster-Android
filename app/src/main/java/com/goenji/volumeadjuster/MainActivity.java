package com.goenji.volumeadjuster;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private TextView volumeLabel;
    private AudioManager audioManager;
    private static NotificationManagerCompat notificationManagerCompat;
    public static NotificationManagerCompat getNotificationManagerInstance() {
        return notificationManagerCompat;
    }
    private Handler handler;
    private ImageButton btnMute, btnVolumeUp, btnVolumeDown;
    private Button btnStart, btnStop;
    private float currentVolume;
    private boolean isMute = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.main_start_service);
        btnStop = findViewById(R.id.main_stop_service);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        btnMute = findViewById(R.id.btnMute);
        btnVolumeDown = findViewById(R.id.btnVolumeDown);
        btnVolumeUp = findViewById(R.id.btnVolumeUp);

        volumeLabel = findViewById(R.id.main_volume_label);

        notificationPermission();
        setAudioManager();
        currentVolumeRunnable().run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.about) {
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(this, R.style.DefaultAlertDialogStyle))
                    .setTitle("About")
                    .setMessage("Developer: Goenji48 - 2024")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();
            alertDialog.show();
        }
        return true;
    }

    private void notificationPermission() {
        ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if(result) {
                notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                btnStart.setEnabled(true);
                btnStop.setEnabled(true);
                btnStart.setOnClickListener(v -> {
                    startService();
                });
                btnStop.setOnClickListener(v -> {
                    stopService();
                });
            } else {
                Toast.makeText(this, "A notificação não será exibida.", Toast.LENGTH_SHORT).show();
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }
        });
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void setAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        btnVolumeUp.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        });

        btnVolumeDown.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        });

        btnMute.setOnClickListener(v -> {
            if(isMute) {
                btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.red));
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                isMute = false;
            } else {
                int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if(nightMode == Configuration.UI_MODE_NIGHT_NO) {
                    btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.lime));
                } else {
                    btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.skyBlue));
                }
                audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
                isMute = true;
            }});
    }

    private void startService() {
        Intent intent = new Intent(this, VolumeService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    }

    private void stopService() {
        Intent intent = new Intent(this, VolumeService.class);
        stopService(intent);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

    private Runnable currentVolumeRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        * 6.667f;

                volumeLabel.setText(volumeLabel(currentVolume));

                if(VolumeService.getNotificationManagerCompat() != null) {
                    VolumeService.updateNotification(getApplicationContext(), getResources(), (int) currentVolume);
                }

                handler = new Handler();
                handler.postDelayed(this, 1000);
            }
        };
    }

    private String volumeLabel(float currentVolume) {
        int castCurrentVolume = (int) currentVolume;
        return "Volume: " + castCurrentVolume + " %";
    }
}