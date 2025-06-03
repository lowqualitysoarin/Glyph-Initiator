package com.lowqualitysoarin.glyphinitiator.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.lowqualitysoarin.glyphinitiator.glyphcontrol.GlyphControl;

public class GlyphAudioDecompressor {
    private static final String tag = "GlyphAudioDecompressor";

    private static byte[] zDecompress(byte[] compressedData) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);

        try (outputStream) {
            byte[] buffer = new byte[1024]; // Adjust buffer size as needed
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) {
                    // Should not happen if all compressed data is provided at once
                    throw new DataFormatException("Inflater needs input, but all data was provided.");
                }
                if (count == 0 && inflater.finished()) {
                    break; // Successfully finished
                }
                outputStream.write(buffer, 0, count);
            }
        } finally {
            inflater.end(); // Release the decompressor's resources
        }

        return outputStream.toByteArray();
    }

    private static byte[] decodeBase64ToBytes(String encodedString) {
        if (encodedString == null) {
            return null;
        }
        try {
            return Base64.decode(encodedString, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string to bytes: " + e.getMessage());
            return null;
        }
    }

    public static int[][] getCompositionData(Context context, Uri fileUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, fileUri);

            String author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            if (author == null) {
                Log.e(tag, "Failed to decompress, author is null, this is where the glyph composition can be found...");
                return new int[0][0];
            }

            byte[] decodedMeta = decodeBase64ToBytes(author);
            byte[] decompressedData = zDecompress(decodedMeta);

            try {
                String readableData = new String(decompressedData, StandardCharsets.UTF_8);

                int zoneCount = GlyphControl.getDeviceZoneCount();
                if (zoneCount == 0) {
                    Log.e(tag, "Failed to decompress, zone count is 0");
                    return new int[0][0];
                }

                String[] unoptimizedArr = readableData.split(",");
                int loopCount = unoptimizedArr.length / zoneCount;
                int[][] compositionData = new int[loopCount][zoneCount];

                int frameSkips = 0;
                for (int i = 0; i < loopCount; i++) {
                    for (int cI = 0; cI < zoneCount; cI++) {
                        String str = unoptimizedArr[frameSkips + cI].trim();

                        if (isNumberOnly(str)) {
                            compositionData[i][cI] = Integer.parseInt(str);
                        }
                    }
                    frameSkips += zoneCount;
                }
                return compositionData;
            } catch (Exception e) {
                Log.e(tag, "Failed to decompress data", e);
            }
            return new int[0][0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isNumberOnly(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
