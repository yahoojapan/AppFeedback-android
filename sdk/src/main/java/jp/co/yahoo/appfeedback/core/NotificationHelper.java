package jp.co.yahoo.appfeedback.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {
    private static final String CHANNEL_ID = "appfeedback";
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        }

        if (isAboveOreo()) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "AppFeedback",
                    NotificationManager.IMPORTANCE_LOW
            );

            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setVibrationPattern(null);

            notificationManager.createNotificationChannel(notificationChannel);

        }
    }

    public Notification createNotification(Context context, String title, String content, int icon, PendingIntent intent) {
        if (isAboveOreo()) {
            return builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(icon)
                    .setContentIntent(intent)
                    .setChannelId(CHANNEL_ID)
                    .build();
        } else {
            return builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(icon)
                    .setContentIntent(intent)
                    .build();
        }
    }

    private Notification.Builder builder(Context context) {
        if (isAboveOreo()) {
            return new Notification.Builder(context, CHANNEL_ID);
        } else {
            return new Notification.Builder(context);
        }
    }

    private boolean isAboveOreo(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
