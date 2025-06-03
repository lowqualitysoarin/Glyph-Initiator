package com.lowqualitysoarin.glyphinitiator.glyphcontrol;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import com.nothing.ketchum.*;
import java.util.Objects;

public class GlyphControl {
    private static final String tag = "GlyphControl";

    private static int npModelIndex = 0;

    @SuppressLint("StaticFieldLeak")
    static GlyphManager mGM = null;
    private static GlyphManager.Callback mCallback = null;
    private static boolean started = false;

    public static void startGlyphSession(Context context) {
        if (isGlyphStarted()) {
            Log.e(tag, "Glyph session already started");
            return;
        }

        initGlyph();
        mGM = GlyphManager.getInstance(context);
        mGM.init(mCallback);
        started = true;
        Log.d(tag, "Glyph session started");
    }

    public static void endGlyphSession() {
        if (!isGlyphStarted()) {
            Log.e(tag, "Glyph session not started");
            return;
        }

        try {
            mGM.closeSession();
        } catch (GlyphException e) {
            throw new RuntimeException(e);
        }
        mGM.unInit();
        started = false;
        Log.d(tag, "Glyph session ended");
    }

    private static void initGlyph() {
        mCallback = new GlyphManager.Callback() {
            @Override
            public void onServiceConnected(ComponentName componentName) {
                if (Common.is20111()) {
                    mGM.register(Glyph.DEVICE_20111);
                    npModelIndex = 1;
                    Log.i(tag, "Device is Phone (1)");
                } else if (Common.is22111()) {
                    mGM.register(Glyph.DEVICE_22111);
                    npModelIndex = 2;
                    Log.i(tag, "Device is Phone (2)");
                } else if (Common.is23111()) {
                    mGM.register(Glyph.DEVICE_23111);
                    npModelIndex = 3;
                    Log.i(tag, "Device is Phone (2a)");
                } else if (Common.is23113()) {
                    mGM.register(Glyph.DEVICE_23113);
                    npModelIndex = 4;
                    Log.i(tag, "Device is Phone (2a) Plus");
                } else if (Common.is24111()) {
                    mGM.register(Glyph.DEVICE_24111);
                    npModelIndex = 5;
                    Log.e(tag, "Device is Phone (3a) / (3a) pro");
                }

                try {
                    mGM.openSession();
                } catch (GlyphException e) {
                    Log.e(tag, Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                try {
                    mGM.closeSession();
                } catch (GlyphException e) {
                    Log.e(tag, Objects.requireNonNull(e.getMessage()));
                }
            }
        };
    }

    public static void buildChannelA(int interval, int cycles, int period) {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelA().buildInterval(interval).buildCycles(cycles).buildPeriod(period).build();
        mGM.animate(frame);
    }

    public static void buildChannelB(int interval, int cycles, int period) {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelB().buildInterval(interval).buildCycles(cycles).buildPeriod(period).build();
        mGM.animate(frame);
    }

    public static void buildChannelC(int interval, int cycles, int period) {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelC().buildInterval(interval).buildCycles(cycles).buildPeriod(period).build();
        mGM.animate(frame);
    }

    public static void buildChannelD(int interval, int cycles, int period) {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelD().buildInterval(interval).buildCycles(cycles).buildPeriod(period).build();
        mGM.animate(frame);
    }

    public static void buildChannelE(int interval, int cycles, int period) {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelE().buildInterval(interval).buildCycles(cycles).buildPeriod(period).build();
        mGM.animate(frame);
    }

    public static void glyphPlaySequence(int[] sequenceArr) {
        try {
            mGM.setFrameColors(sequenceArr);
        } catch (GlyphException e) {
            throw new RuntimeException(e);
        }
    }

    public static void glyphOff() {
        mGM.turnOff();
    }

    public static int getDeviceModelIndex() {
        return npModelIndex;
    }

    public static int getDeviceZoneCount() {
        int deviceModelIndex = getDeviceModelIndex();
        switch (deviceModelIndex) {
            case 1:
                return 15;
            case 2:
                return 33;
            case 3:
            case 4:
                return 26;
            case 5:
                return 36;
        }
        return 0;
    }

    public static boolean isGlyphStarted() {
        return started;
    }
}
