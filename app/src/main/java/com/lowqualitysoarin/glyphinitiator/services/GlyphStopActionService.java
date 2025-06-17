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
        GlyphPlayComposition.stopComposition(false);
        GlyphPlayComposition.release();

        stopSelf(startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}