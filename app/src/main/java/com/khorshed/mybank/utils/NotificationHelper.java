package com.khorshed.mybank.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.khorshed.mybank.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "my_bank_channel";
    private static final String CHANNEL_NAME = "MY BANK Notifications";
    private static final String CHANNEL_DESC = "Notifications for MY BANK app";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(
        Context context,
        String title,
        String message,
        int notificationId,
        Intent intent
    ) {
        createNotificationChannel(context);

        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    public static void showTransactionNotification(Context context, String type, double amount) {
        String title = "Transaction " + type;
        String message = String.format("Amount: ৳%.2f", amount);
        showNotification(context, title, message, (int) System.currentTimeMillis(), null);
    }
}
