package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import com.lowqualitysoarin.glyphinitiator.entry.OggEntry;
import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphPlayComposition;
import com.lowqualitysoarin.glyphinitiator.notif.AppNotificationManager;
import com.lowqualitysoarin.glyphinitiator.utils.GlyphAudioDecompressor;
import com.lowqualitysoarin.glyphinitiator.utils.OggEntryAdapter;

public class GlyphActionService extends Service {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            Log.e("GlyphActionService", "Can't start service without intent extras.");
            return START_NOT_STICKY;
        }

        OggEntry entry = OggEntryAdapter.pickRandom();
        if (intent.getExtras().containsKey("actionKey")) {
            entry = OggEntryAdapter.getEntry(intent.getExtras().getString("actionKey"));
        }

        boolean noAudio = intent.getBooleanExtra("noAudio", false);;

        if (entry == null || entry.getUriString() == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Uri fileUri = Uri.parse(entry.getUriString());
        int[][] composition = GlyphAudioDecompressor.getCompositionData(context, fileUri);

        Notification foregroundNotification = AppNotificationManager.createForegroundNotification(this, "Glyph Action Running", "Playing glyph composition...");
        startForeground(AppNotificationManager.NOTIFICATION_ID, foregroundNotification);

        GlyphPlayComposition.playComposition(context, fileUri, composition, noAudio);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}