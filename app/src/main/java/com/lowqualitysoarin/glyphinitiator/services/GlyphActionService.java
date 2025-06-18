package com.lowqualitysoarin.glyphinitiator.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import com.lowqualitysoarin.glyphinitiator.entry.OggEntry;
import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphPlayComposition;
import com.lowqualitysoarin.glyphinitiator.utils.GlyphAudioDecompressor;
import com.lowqualitysoarin.glyphinitiator.utils.OggEntryAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

public class GlyphActionService extends Service {
    private Context context;
    private int currentStartId = -1;
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final AtomicBoolean allowOverride = new AtomicBoolean(false);
    private final GlyphPlayComposition.GlyphCompositionListener listener = new GlyphPlayComposition.GlyphCompositionListener() {
        @Override
        public void onCompositionStart() {
            Log.d("GlyphActionService", "Composition started.");
        }

        @Override
        public void onCompositionEnd() {
            Log.d("GlyphActionService", "Composition ended.");
            stopAction();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            Log.e("GlyphActionService", "Can't start service without intent extras.");
            stopSelf(startId);
            return START_NOT_STICKY;
        } else if (isRunning.get() ) {
            if (!allowOverride.get()) {
                Log.e("GlyphActionService", "Can't start service while another one is running.");
                stopSelf(startId);
                return START_NOT_STICKY;
            } else {
                Log.d("GlyphActionService", "Overriding current action...");
                stopAction();
            }
        }

        //if (!isRunning.compareAndSet(false, true)) {
        //    Log.e("GlyphActionService", "Service is already ran by another thread.");
        //    stopSelf(startId);
        //    return START_NOT_STICKY;
        //}

        currentStartId = startId;

        OggEntry entry = OggEntryAdapter.pickRandom();
        if (intent.getExtras().containsKey("actionKey")) {
            entry = OggEntryAdapter.getEntry(intent.getExtras().getString("actionKey"));
        }

        boolean noAudio = intent.getBooleanExtra("noAudio", false);
        allowOverride.set(intent.getBooleanExtra("allowOverride", false));

        if (entry == null || entry.getUriString() == null) {
            stopSelf(currentStartId);
            return START_NOT_STICKY;
        }

        Uri fileUri = Uri.parse(entry.getUriString());
        int[][] composition = GlyphAudioDecompressor.getCompositionData(context, fileUri);

        isRunning.set(true);
        GlyphPlayComposition.addEventListener(listener);
        GlyphPlayComposition.playComposition(context, fileUri, composition, noAudio);

        return START_STICKY;
    }

    private void stopAction() {
        isRunning.set(false);
        allowOverride.set(false);

        if (currentStartId != -1) {
            stopSelf(currentStartId);
        } else {
            stopSelf();
        }
        currentStartId = -1;

        GlyphPlayComposition.removeEventListener(listener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}