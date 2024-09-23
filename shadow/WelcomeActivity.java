package com.yahya.shadow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yahya.shadow.services.FirebaseWorker;
import com.yahya.shadow.services.ForegroundService;

import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private static final int PROGRESS_ANIMATION_DELAY = 30; // milliseconds
    private static final int PROGRESS_MAX = 100;

    private MyApp myApp; // Make sure to have a reference to your application class

    private ProgressBar mProgress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private TextView mPercentage;
    private Handler handler;

    private int progress; // Moved progress to class field

    public static final String CHANNEL_ID = "daily_checkup_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initializeComponents();
        configureFirebaseAuth();
        // Schedule the work to repeat every 15 minutes
        createNotificationChannel();
        //startForegroundService();
    }

    private void initializeComponents() {
        mPercentage = findViewById(R.id.percentage);
        mProgress = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        handler = new Handler();
        progress = 0; // Initialize progress here
    }

    private void configureFirebaseAuth() {
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    animateProgressAndStartActivity(MainActivity.class);
                } else {
                    animateProgressAndStartActivity(LoginActivity.class);
                }
            }
        };
    }

    private void animateProgressAndStartActivity(final Class<?> activityClass) {
        new Thread(() -> {
            for (progress = 0; progress <= PROGRESS_MAX; progress++) {
                try {
                    Thread.sleep(PROGRESS_ANIMATION_DELAY);
                    handler.post(() -> {
                        mProgress.setProgress(progress);
                        mPercentage.setText(progress + "%");
                        if (progress == PROGRESS_MAX) {
                            Intent intent = new Intent(WelcomeActivity.this, activityClass);
                            intent.putExtra("current", 1);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e(TAG, "Progress animation interrupted", e);
                }
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Checkup Channel";
            String description = "Channel for daily checkup notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
