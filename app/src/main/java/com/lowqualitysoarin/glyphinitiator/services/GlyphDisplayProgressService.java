package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;
import com.lowqualitysoarin.glyphinitiator.notif.AppNotificationManager;

public class GlyphDisplayProgressService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            Log.e("GlyphDisplayProgressService", "Can't start this service without extra data or without intent.");
            return START_NOT_STICKY;
        }

        String channel = "";
        if (intent.getExtras().containsKey("channel")) {
            channel = intent.getStringExtra("channel");
        }

        if (channel != null && channel.isBlank()) {
            Log.e("GlyphDisplayProgressService", "Can't start this service without a channel.");
            return START_NOT_STICKY;
        }

        int progress = intent.getIntExtra("progress", 0);
        boolean reversed = intent.getBooleanExtra("reversed", false);

        Notification foregroundNotification = AppNotificationManager.createForegroundNotification(getApplicationContext(), "Glyph Progress Running", "Trying to show progress on the glyph.");
        startForeground(AppNotificationManager.NOTIFICATION_ID, foregroundNotification);

        GlyphControl.glyphDisplayProgress(channel, progress, reversed);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
