package io.github.sarifsystems.sarif;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.github.sarifsystems.sarif.client.SarifClientListener;
import io.github.sarifsystems.sarif.service.SarifService;

public class SarifAwareActivity extends AppCompatActivity {

    private SarifService sarif;
    private List<SarifClientListener> listenerQueue = new ArrayList<>();

    protected void onResume() {
        super.onResume();

        Context ctx = getApplicationContext();
        Intent i = new Intent(ctx, SarifService.class);
        ctx.startService(i);
        ctx.bindService(i, new SarifServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    public SarifService getSarif() {
        return sarif;
    }

    public void addSarifListener(SarifClientListener listener) {
        if (sarif != null) {
            sarif.addListener(listener);
        } else {
            listenerQueue.add(listener);
        }
    }

    public void removeListener(SarifClientListener listener) {
        if (sarif != null) {
            sarif.removeListener(listener);
        }
    }

    public class SarifServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sarif = ((SarifService.SarifServiceBinder) service).getService();

            for (SarifClientListener listener : listenerQueue) {
                sarif.addListener(listener);
            }
            listenerQueue.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sarif = null;
        }
    }
}
