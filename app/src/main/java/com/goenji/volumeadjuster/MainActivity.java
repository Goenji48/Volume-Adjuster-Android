package com.goenji.volumeadjuster;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private static VolumeRunnable volumeRunnable;
    private TabLayout tabLayout;
    private ImageView streamTypeIndicator;
    private ImageButton btnMute, btnVolumeUp, btnVolumeDown;
    private Button btnStart, btnStop;
    private TextView volumeLabel;
    private boolean isMute = true;
    private static int streamType = AudioManager.STREAM_MUSIC;

    public static int getCurrentStreamType() {
        return streamType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.main_start_service);
        btnStop = findViewById(R.id.main_stop_service);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
        volumeLabel = findViewById(R.id.main_volume_label);
        streamTypeIndicator = findViewById(R.id.current_stream_type_indicator);
        tabLayout = findViewById(R.id.control_type_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        streamType = AudioManager.STREAM_MUSIC;
                        streamTypeIndicator.setImageResource(R.drawable.ic_music);
                        restartVolumeRunnable(streamType);
                        break;
                    case 1:
                        streamType = AudioManager.STREAM_ALARM;
                        streamTypeIndicator.setImageResource(R.drawable.ic_alarm);
                        restartVolumeRunnable(streamType);
                        break;
                    case 2:
                        streamType = AudioManager.STREAM_RING;
                        streamTypeIndicator.setImageResource(R.drawable.ic_notification);
                        restartVolumeRunnable(streamType);
                        break;
                    case 3:
                        streamType = AudioManager.STREAM_VOICE_CALL;
                        streamTypeIndicator.setImageResource(R.drawable.ic_call);
                        restartVolumeRunnable(streamType);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnMute = findViewById(R.id.btnMute);
        btnVolumeDown = findViewById(R.id.btnVolumeDown);
        btnVolumeUp = findViewById(R.id.btnVolumeUp);
        notificationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setAudioManager();
        restartVolumeRunnable(streamType);
    }

    private void restartVolumeRunnable(int streamType) {
        if(volumeRunnable != null) volumeRunnable.stopVolumeListener();
        volumeRunnable = new VolumeRunnable(getApplicationContext(), audioManager);
        volumeRunnable.runVolumeListener(1, volumeLabel,
                null, streamType).run();
    }

    @Override
    protected void onDestroy() {
        if(volumeRunnable != null) volumeRunnable.stopVolumeListener();
        super.onDestroy();
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
                    .setMessage("Developer: João Luiz (Goenji48) - 2025" + "\n\n" + "Version " + BuildConfig.VERSION_NAME)
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
        ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if(result) {
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
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        });

        btnVolumeDown.setOnClickListener(v -> {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        });

        btnMute.setOnClickListener(v -> {
            if(isMute) {
                btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.red));
                audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                isMute = false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    TypedValue typedValue = new TypedValue();
                    getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                    btnMute.setColorFilter(typedValue.data);
                } else {
                    int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    if(nightMode == Configuration.UI_MODE_NIGHT_NO) {
                        btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.lime));
                    } else {
                        btnMute.setColorFilter(new ContextWrapper(this).getColor(R.color.skyBlue));
                    }
                }
                audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
                isMute = true;
            }});
    }

    private void startService() {
        Intent intent = new Intent(this, VolumeService.class);
        SharedPreferences sharedPreferences = getSharedPreferences("servicePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("serviceActivated", true).apply();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    }

    private void stopService() {
        Intent intent = new Intent(this, VolumeService.class);
        SharedPreferences sharedPreferences = getSharedPreferences("servicePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("serviceActivated", false).apply();
        stopService(intent);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }
}