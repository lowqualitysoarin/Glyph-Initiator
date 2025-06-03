package com.lowqualitysoarin.glyphinitiator.notif;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.lowqualitysoarin.glyphinitiator.MainActivity;
import com.lowqualitysoarin.glyphinitiator.R;

public class AppNotificationManager {
    private static final String CHANNEL_ID = "GlyphPlaygroundChannel";
    private static boolean isReady;
    public static final int NOTIFICATION_ID = 1;

    public static void readyNotificationChannel(Context appContext) {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Glyph Playground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        serviceChannel.setSound(null, null);
        serviceChannel.enableLights(false);
        serviceChannel.enableVibration(false);
        serviceChannel.setShowBadge(false);

        NotificationManager manager = getSystemService(appContext, NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
            isReady = true;
        }
    }

    public static Notification createForegroundNotification(Context context, String contentTitle, String contentText) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    public static boolean isNotificationChannelReady() {
        return isReady;
    }
}
