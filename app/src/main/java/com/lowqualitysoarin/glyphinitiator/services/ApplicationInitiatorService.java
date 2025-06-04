package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;
import com.lowqualitysoarin.glyphinitiator.notif.AppNotificationManager;

public class ApplicationInitiatorService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!AppNotificationManager.isNotificationChannelReady()) {
            AppNotificationManager.readyNotificationChannel(this);
        }

        if (!GlyphControl.isGlyphStarted()) {
            Intent startSessionService = new Intent(this, GlyphStartSessionService.class);
            startForegroundService(startSessionService);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
