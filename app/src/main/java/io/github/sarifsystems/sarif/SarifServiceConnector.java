package io.github.sarifsystems.sarif;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.sarifsystems.sarif.client.SarifClient;
import io.github.sarifsystems.sarif.client.SarifClientListener;
import io.github.sarifsystems.sarif.service.SarifService;

/**
 * Created by me on 12/5/2017.
 */

public class SarifServiceConnector {

    private Context ctx;
    private SarifService sarif;
    private List<Task> taskQueue = new ArrayList<>();

    public SarifServiceConnector(Context ctx) {
        this.ctx = ctx;
    }

    public void bind() {
        if (this.sarif != null) {
            return;
        }

        Intent i = new Intent(this.ctx, SarifService.class);
        this.ctx.startService(i);
        this.ctx.bindService(i, new SarifServiceConnector.SarifServiceConnection(), 0);
    }

    public void runTask(Task task) {
        if (this.sarif != null) {
            task.run(this.sarif);
            return;
        }

        this.taskQueue.add(task);
        this.bind();
    }

    public SarifService getSarif() {
        return this.sarif;
    }

    public interface Task {
        void run(SarifService service);
    }

    public void addListener(final SarifClientListener listener) {
        this.runTask(new Task() {
            @Override
            public void run(SarifService service) {
                service.addListener(listener);
            }
        });
    }

    public void removeListener(SarifClientListener listener) {
        if (sarif != null) {
            sarif.removeListener(listener);
        } else {
            Log.d("SarifServiceConnector", "listener remove failed: no longer bound");
        }
    }

    public class SarifServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sarif = ((SarifService.SarifServiceBinder) service).getService();

            for (Task task : taskQueue) {
                task.run(sarif);
            }
            taskQueue.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sarif = null;
        }
    }
}
