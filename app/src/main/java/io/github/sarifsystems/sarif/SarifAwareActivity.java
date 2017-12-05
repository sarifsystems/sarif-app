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

    private SarifServiceConnector sarif;

    protected void onResume() {
        super.onResume();

        sarif = new SarifServiceConnector(getApplicationContext());
    }

    public SarifService getSarif() {
        return this.sarif.getSarif();
    }

    public void addSarifListener(SarifClientListener listener) {
        sarif.addListener(listener);
    }

    public void removeListener(SarifClientListener listener) {
        this.sarif.removeListener(listener);
    }
}
