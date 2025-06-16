package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;
import com.lowqualitysoarin.glyphinitiator.notif.AppNotificationManager;

public class GlyphStartSessionService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!GlyphControl.isGlyphStarted()) {
            GlyphControl.startGlyphSession(getApplicationContext());

            Notification foregroundNotification = AppNotificationManager.createForegroundNotification(this, "Glyph Initiator", "Glyph Initiator is running...");
            startForeground(AppNotificationManager.NOTIFICATION_ID, foregroundNotification);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
