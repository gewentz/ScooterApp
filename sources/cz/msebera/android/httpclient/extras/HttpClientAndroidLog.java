package cz.msebera.android.httpclient.extras;

import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public class HttpClientAndroidLog {
    private String logTag;
    private boolean debugEnabled = false;
    private boolean errorEnabled = false;
    private boolean traceEnabled = false;
    private boolean warnEnabled = false;
    private boolean infoEnabled = false;

    public HttpClientAndroidLog(Object obj) {
        this.logTag = obj.toString();
    }

    public void enableDebug(boolean z) {
        this.debugEnabled = z;
    }

    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    public void debug(Object obj) {
        if (isDebugEnabled()) {
            Log.d(this.logTag, obj.toString());
        }
    }

    public void debug(Object obj, Throwable th) {
        if (isDebugEnabled()) {
            Log.d(this.logTag, obj.toString(), th);
        }
    }

    public void enableError(boolean z) {
        this.errorEnabled = z;
    }

    public boolean isErrorEnabled() {
        return this.errorEnabled;
    }

    public void error(Object obj) {
        if (isErrorEnabled()) {
            Log.e(this.logTag, obj.toString());
        }
    }

    public void error(Object obj, Throwable th) {
        if (isErrorEnabled()) {
            Log.e(this.logTag, obj.toString(), th);
        }
    }

    public void enableWarn(boolean z) {
        this.warnEnabled = z;
    }

    public boolean isWarnEnabled() {
        return this.warnEnabled;
    }

    public void warn(Object obj) {
        if (isWarnEnabled()) {
            Log.w(this.logTag, obj.toString());
        }
    }

    public void warn(Object obj, Throwable th) {
        if (isWarnEnabled()) {
            Log.w(this.logTag, obj.toString(), th);
        }
    }

    public void enableInfo(boolean z) {
        this.infoEnabled = z;
    }

    public boolean isInfoEnabled() {
        return this.infoEnabled;
    }

    public void info(Object obj) {
        if (isInfoEnabled()) {
            Log.i(this.logTag, obj.toString());
        }
    }

    public void info(Object obj, Throwable th) {
        if (isInfoEnabled()) {
            Log.i(this.logTag, obj.toString(), th);
        }
    }

    public void enableTrace(boolean z) {
        this.traceEnabled = z;
    }

    public boolean isTraceEnabled() {
        return this.traceEnabled;
    }

    public void trace(Object obj) {
        if (isTraceEnabled()) {
            Log.i(this.logTag, obj.toString());
        }
    }

    public void trace(Object obj, Throwable th) {
        if (isTraceEnabled()) {
            Log.i(this.logTag, obj.toString(), th);
        }
    }
}
