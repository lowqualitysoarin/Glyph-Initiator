package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphBuildParams;
import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;

public class GlyphBuildChannelService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            Log.e("GlyphBuildChannelService", "Can't start this service without extra data or without intent.");
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        String channel = "";
        if (intent.getExtras().containsKey("channel")) {
            channel = intent.getStringExtra("channel");
        }

        if (channel != null && channel.isBlank()) {
            Log.e("GlyphBuildChannelService", "Can't start this service without a channel.");
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        boolean noAnimate = intent.getBooleanExtra("noAnimate", false);

        GlyphBuildParams buildParams = new GlyphBuildParams();
        buildParams.interval = intent.getIntExtra("interval", 10);
        buildParams.cycles = intent.getIntExtra("cycles", 1);
        buildParams.period = intent.getIntExtra("period", 3000);

        GlyphControl.glyphBuildChannel(channel, noAnimate, buildParams);

        stopSelf(startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
