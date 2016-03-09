package com.asalfo.movies.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by asalfo on 09/03/16.
 */
public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MovieSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("MovieSyncAdapter", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}