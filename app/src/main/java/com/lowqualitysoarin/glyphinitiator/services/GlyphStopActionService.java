package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import com.lowqualitysoarin.glyphinitiator.MainActivity;
import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphPlayComposition;
import com.lowqualitysoarin.glyphinitiator.notif.AppNotificationManager;

public class GlyphStopActionService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // --- Foreground Service Notification ---
        Intent notificationIntent = new Intent(this, MainActivity.class); // Replace YourMainActivity if necessary
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification foregroundNotification = AppNotificationManager.createForegroundNotification(this, "Glyph Playground", "Stopped playing composition...");
        startForeground(AppNotificationManager.NOTIFICATION_ID, foregroundNotification);

        GlyphPlayComposition.stopComposition();
        GlyphPlayComposition.release();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}