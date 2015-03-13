package com.bezy_apps.forbesfeeds.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Eric on 3/9/2015.
 */
public class ForbesSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ForbesSyncAdapter sforbesSyncAdapter = null;


    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sforbesSyncAdapter == null) {
                sforbesSyncAdapter = new ForbesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sforbesSyncAdapter.getSyncAdapterBinder();
    }
}
