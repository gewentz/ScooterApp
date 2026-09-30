package org.cocos2dx.lib;

import android.util.Log;
import com.loopj.android.http.BinaryHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

/* JADX INFO: loaded from: classes.dex */
class DataTaskHandler extends BinaryHttpResponseHandler {
    private Cocos2dxDownloader _downloader;
    int _id;
    private long _lastBytesWritten;

    void LogD(String str) {
        Log.d("Cocos2dxDownloader", str);
    }

    public DataTaskHandler(Cocos2dxDownloader cocos2dxDownloader, int i) {
        super(new String[]{".*"});
        this._downloader = cocos2dxDownloader;
        this._id = i;
        this._lastBytesWritten = 0L;
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onProgress(long j, long j2) {
        this._downloader.onProgress(this._id, j - this._lastBytesWritten, j, j2);
        this._lastBytesWritten = j;
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onStart() {
        this._downloader.onStart(this._id);
    }

    @Override // com.loopj.android.http.BinaryHttpResponseHandler, com.loopj.android.http.AsyncHttpResponseHandler
    public void onFailure(int i, Header[] headerArr, byte[] bArr, Throwable th) {
        String string;
        LogD("onFailure(i:" + i + " headers:" + headerArr + " throwable:" + th);
        if (th == null) {
            string = "";
        } else {
            string = th.toString();
        }
        this._downloader.onFinish(this._id, i, string, null);
    }

    @Override // com.loopj.android.http.BinaryHttpResponseHandler, com.loopj.android.http.AsyncHttpResponseHandler
    public void onSuccess(int i, Header[] headerArr, byte[] bArr) {
        LogD("onSuccess(i:" + i + " headers:" + headerArr);
        this._downloader.onFinish(this._id, 0, null, bArr);
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onFinish() {
        this._downloader.runNextTaskIfExists();
    }
}
