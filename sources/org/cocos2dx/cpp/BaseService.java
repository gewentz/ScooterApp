package org.cocos2dx.cpp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/* JADX INFO: loaded from: classes.dex */
public class BaseService extends Service {
    private final IBinder mBinder = new LocalBinder();

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public <T extends BaseService> T getService() {
            return (T) BaseService.this;
        }
    }
}
