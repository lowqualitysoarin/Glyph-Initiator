package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;

public class GlyphOffService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GlyphControl.glyphOff();
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
