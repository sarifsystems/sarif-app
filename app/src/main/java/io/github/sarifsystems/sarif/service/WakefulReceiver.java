package io.github.sarifsystems.sarif.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class WakefulReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SarifService.class);
        Log.d("SarifService", "Receiver starting service");
        startWakefulService(context, service);
    }
}
