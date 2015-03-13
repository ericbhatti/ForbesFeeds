package com.bezy_apps.forbesfeeds.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Eric on 3/9/2015.
 */
public class ForbesAuthenticatorService extends Service {

    private ForbesAuthenticator forbesAuthenticator;

    @Override
    public void onCreate() {
        forbesAuthenticator = new ForbesAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return forbesAuthenticator.getIBinder();
    }
}
