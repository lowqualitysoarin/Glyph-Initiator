package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;

public class GlyphStopSessionService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (GlyphControl.isGlyphStarted()) {
            GlyphControl.endGlyphSession();
            Log.d("GlyphStopSessionService", "Glyph session stopped.");
        } else {
            Log.d("GlyphStopSessionService", "Glyph session is not running.");
        }
        return START_NOT_STICKY;
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
