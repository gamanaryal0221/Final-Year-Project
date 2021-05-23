package com.gaman_aryal.easyform.notification;

import android.app.Application;
import android.app.NotificationChannel;
import android.os.Build;

public class NotificationHelper extends Application {
    public static final String CHANNEL_1_ID = "Admin Notification";
    public static final String CHANNEL_2_ID = "Staff Notification";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Admin Notification",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This allows to show new request notifications on admin panel");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Staff Notification",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("This allows to show organization's action notifications on user panel");

            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
