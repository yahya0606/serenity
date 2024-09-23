package com.yahya.shadow.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yahya.shadow.MainActivity;
import com.yahya.shadow.R;
import com.yahya.shadow.WelcomeActivity;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WelcomeActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.account) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
