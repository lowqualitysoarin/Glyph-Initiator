package com.lowqualitysoarin.glyphinitiator.glyphcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.lowqualitysoarin.glyphinitiator.services.GlyphStartSessionService;
import com.lowqualitysoarin.glyphinitiator.services.GlyphStopSessionService;

import java.io.IOException;

public class GlyphPlayComposition {
    private static int[][] currentComposition;
    private static int[] lastCompositionRow;

    private static int currentIteration = 0;
    private static int totalIterations = 0;
    private static final long delayMillis = 16;

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static HandlerThread glyphHandlerThread;
    private static Handler glyphSequenceHandler;

    private static MediaPlayer mediaPlayer;

    @SuppressLint("StaticFieldLeak")
    private static Context currentContext;
    private static boolean isMediaPlayerPrepared = false;
    private static boolean shouldPlayImmediatelyAfterPrepare = false;

    private static class TimedLoopRunnable implements Runnable {
        private boolean playedInternal = false;

        public void resetPlayedFlag() {
            playedInternal = false;
            Log.d("GlyphLoopThread", "TimedLoopRunnable: playedInternal flag reset.");
        }

        @Override
        public void run() {
            if (glyphSequenceHandler == null || glyphHandlerThread == null || !glyphHandlerThread.isAlive()) {
                Log.w("GlyphPlayComposition", "timedLoopRunnable: Handler or thread is not active. Exiting loop.");
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                mainHandler.post(GlyphControl::glyphOff);
                return;
            }

            if (currentIteration < totalIterations) {
                long startTime = System.nanoTime();
                if (currentComposition != null && currentIteration < currentComposition.length) {
                    if (currentComposition[currentIteration] != lastCompositionRow) {
                        GlyphControl.glyphPlaySequence(currentComposition[currentIteration]);
                    }
                    lastCompositionRow = currentComposition[currentIteration];
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;
                currentIteration++;

                long actualDelay = Math.max(0, delayMillis - duration);
                if (glyphSequenceHandler != null) {
                    glyphSequenceHandler.postDelayed(this, actualDelay);
                }

            } else {
                Log.d("GlyphLoopThread", "timedLoopRunnable: Playback iterations complete.");
                if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                    mainHandler.post(GlyphControl::glyphOff);
                    //stopComposition();
                }
            }

            if (isMediaPlayerPrepared && !playedInternal && mediaPlayer != null && currentContext != null) {
                if (!mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.start();
                        Log.d("MediaPlayerDebug", "MediaPlayer started by TimedLoopRunnable.");
                    } catch (IllegalStateException e) {
                        Log.e("MediaPlayerDebug", "Failed to start MediaPlayer in runnable: " + e.getMessage());
                        stopComposition();
                        return;
                    }
                }
                playedInternal = true;
            }
        }
    }
    private static final TimedLoopRunnable timedLoopRunnable = new TimedLoopRunnable();

    private static boolean isPlaying;

    private static synchronized void ensureThreadStarted() {
        if (glyphHandlerThread == null || !glyphHandlerThread.isAlive()) {
            glyphHandlerThread = new HandlerThread("GlyphSequenceThread", Process.THREAD_PRIORITY_BACKGROUND);
            glyphHandlerThread.start();
            Looper looper = glyphHandlerThread.getLooper();
            if (looper != null) {
                glyphSequenceHandler = new Handler(looper);
            } else {
                Log.e("GlyphPlayComposition", "Failed to get looper from HandlerThread");
                if (glyphHandlerThread != null) {
                    glyphHandlerThread.quitSafely();
                    glyphHandlerThread = null;
                }
                return;
            }
            Log.d("GlyphLoopThread", "GlyphSequenceThread started.");
        }
    }

    public static void playComposition(Context appContext, Uri fileUri, int[][] composition) {
        if (composition == null || composition.length == 0) {
            Log.w("GlyphPlayComposition", "Composition is null or empty. Cannot play.");
            return;
        }

        // Stop any existing playback first.
        stopComposition();

        lastCompositionRow = null;
        currentComposition = composition;
        totalIterations = composition.length;
        currentIteration = 0;

        mediaPlayer = new MediaPlayer();
        currentContext = appContext;
        if (currentContext == null) {
            return;
        }

        try {
            mediaPlayer.setDataSource(appContext, fileUri);
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d("MediaPlayerDebug", "MediaPlayer prepared. Starting playback.");
                isMediaPlayerPrepared = true;
                if (shouldPlayImmediatelyAfterPrepare) {
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopComposition();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("MediaPlayerDebug", "MediaPlayer Error - What: " + what + ", Extra: " + extra);
                String errorMsg = "MediaPlayer Error: ";
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        errorMsg += "MEDIA_ERROR_UNKNOWN";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        errorMsg += "MEDIA_ERROR_SERVER_DIED";
                        break;
                    default:
                        errorMsg += "Other error (" + what + ")";
                }
                if (extra != 0) {
                    switch (extra) {
                        case MediaPlayer.MEDIA_ERROR_IO:
                            errorMsg += ", MEDIA_ERROR_IO";
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            errorMsg += ", MEDIA_ERROR_MALFORMED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            errorMsg += ", MEDIA_ERROR_UNSUPPORTED";
                            break;
                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                            errorMsg += ", MEDIA_ERROR_TIMED_OUT";
                            break;
                        default:
                            errorMsg += ", Other extra error (" + extra + ")";
                    }
                }
                stopComposition();
                return true;
            });
            mediaPlayer.prepareAsync();
            Log.d("MediaPlayerDebug", "mediaPlayer.prepareAsync() called.");

        } catch (IOException e) {
            Log.e("MediaPlayerDebug", "IOException setting data source or preparing: " + e.getMessage(), e);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return;
        } catch (IllegalArgumentException e) {
            Log.e("MediaPlayerDebug", "IllegalArgumentException setting data source: " + e.getMessage(), e);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return;
        } catch (SecurityException e) {
            Log.e("MediaPlayerDebug", "SecurityException setting data source (Permissions?): " + e.getMessage(), e);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return;
        }

        ensureThreadStarted();

        if (glyphSequenceHandler != null) {
            isMediaPlayerPrepared = false;
            shouldPlayImmediatelyAfterPrepare = true;

            glyphSequenceHandler.removeCallbacks(timedLoopRunnable);
            timedLoopRunnable.resetPlayedFlag();
            glyphSequenceHandler.post(timedLoopRunnable);
            Log.d("GlyphLoopThread", "Composition playback sequence posted.");

            GlyphControl.startGlyphSession(currentContext);
            isPlaying = true;
        } else {
            Log.e("GlyphPlayComposition", "glyphSequenceHandler is null. Cannot start playback sequence.");
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public static void stopComposition() {
        Log.d("GlyphLoopThread", "stopComposition called.");
        if (glyphSequenceHandler != null && glyphHandlerThread != null && glyphHandlerThread.isAlive()) {
            glyphSequenceHandler.removeCallbacks(timedLoopRunnable);

            glyphSequenceHandler.post(() -> {
                Log.d("GlyphLoopThread", "Executing stopCompositionInternal on GlyphSequenceThread.");
                if (glyphHandlerThread != null) {
                    glyphHandlerThread.quitSafely();
                    glyphHandlerThread = null;
                    glyphSequenceHandler = null;
                    Log.d("GlyphLoopThread", "GlyphSequenceThread cleanup initiated from itself.");
                }
            });
        } else {
            Log.d("GlyphLoopThread", "GlyphSequenceThread or handler was already null or not alive during stopComposition.");
            if (glyphHandlerThread != null) {
                if (!glyphHandlerThread.isAlive()) {
                    try {
                        glyphHandlerThread.join(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    glyphHandlerThread = null;
                } else {
                    glyphHandlerThread.quitSafely();
                    try {
                        glyphHandlerThread.join(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    glyphHandlerThread = null;
                }
            }
            glyphSequenceHandler = null;
        }

        GlyphControl.endGlyphSession();
        isPlaying = false;

        currentContext = null;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mainHandler.post(() -> {
            GlyphControl.glyphOff();
            Log.d("GlyphLoopThread", "GlyphOff called on main thread due to stopComposition.");
        });
    }

    public static boolean isGlyphCompositionPlaying() {
        return isPlaying;
    }

    public static synchronized void release() {
        Log.d("GlyphLoopThread", "release called.");
        if (glyphHandlerThread != null) {
            if (glyphSequenceHandler != null) {
                glyphSequenceHandler.removeCallbacksAndMessages(null);
            }
            glyphHandlerThread.quitSafely();
            try {
                glyphHandlerThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("GlyphLoopThread", "Interrupted while releasing GlyphSequenceThread", e);
            }
            glyphHandlerThread = null;
            glyphSequenceHandler = null;
            currentContext = null;

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Log.d("GlyphLoopThread", "GlyphSequenceThread released.");
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            GlyphControl.glyphOff();
        } else {
            mainHandler.post(GlyphControl::glyphOff);
        }
    }
}