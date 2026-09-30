package com.loopj.android.http;

import android.os.Build;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public class LogHandler implements LogInterface {
    boolean mLoggingEnabled = true;
    int mLoggingLevel = 2;

    @Override // com.loopj.android.http.LogInterface
    public boolean isLoggingEnabled() {
        return this.mLoggingEnabled;
    }

    @Override // com.loopj.android.http.LogInterface
    public void setLoggingEnabled(boolean z) {
        this.mLoggingEnabled = z;
    }

    @Override // com.loopj.android.http.LogInterface
    public int getLoggingLevel() {
        return this.mLoggingLevel;
    }

    @Override // com.loopj.android.http.LogInterface
    public void setLoggingLevel(int i) {
        this.mLoggingLevel = i;
    }

    @Override // com.loopj.android.http.LogInterface
    public boolean shouldLog(int i) {
        return i >= this.mLoggingLevel;
    }

    public void log(int i, String str, String str2) {
        logWithThrowable(i, str, str2, null);
    }

    public void logWithThrowable(int i, String str, String str2, Throwable th) {
        if (isLoggingEnabled() && shouldLog(i)) {
            if (i == 2) {
                Log.v(str, str2, th);
                return;
            }
            if (i == 3) {
                Log.d(str, str2, th);
                return;
            }
            if (i == 4) {
                Log.i(str, str2, th);
                return;
            }
            if (i == 5) {
                Log.w(str, str2, th);
                return;
            }
            if (i == 6) {
                Log.e(str, str2, th);
            } else {
                if (i != 8) {
                    return;
                }
                if (Integer.valueOf(Build.VERSION.SDK).intValue() > 8) {
                    checkedWtf(str, str2, th);
                } else {
                    Log.e(str, str2, th);
                }
            }
        }
    }

    private void checkedWtf(String str, String str2, Throwable th) {
        Log.wtf(str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void v(String str, String str2) {
        log(2, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void v(String str, String str2, Throwable th) {
        logWithThrowable(2, str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void d(String str, String str2) {
        log(2, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void d(String str, String str2, Throwable th) {
        logWithThrowable(3, str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void i(String str, String str2) {
        log(4, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void i(String str, String str2, Throwable th) {
        logWithThrowable(4, str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void w(String str, String str2) {
        log(5, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void w(String str, String str2, Throwable th) {
        logWithThrowable(5, str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void e(String str, String str2) {
        log(6, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void e(String str, String str2, Throwable th) {
        logWithThrowable(6, str, str2, th);
    }

    @Override // com.loopj.android.http.LogInterface
    public void wtf(String str, String str2) {
        log(8, str, str2);
    }

    @Override // com.loopj.android.http.LogInterface
    public void wtf(String str, String str2, Throwable th) {
        logWithThrowable(8, str, str2, th);
    }
}
