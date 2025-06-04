package com.lowqualitysoarin.glyphinitiator.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lowqualitysoarin.glyphinitiator.services.ApplicationInitiatorService;
import java.util.Objects;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }

        Log.d("Glyph Initiator", "Starting services...");

        Intent appInitiatorService = new Intent(context, ApplicationInitiatorService.class);
        context.startService(appInitiatorService);

        Log.d("Glyph Initiator", "Services started.");
    }
}
