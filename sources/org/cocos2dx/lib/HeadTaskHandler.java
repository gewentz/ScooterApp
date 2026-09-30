package org.cocos2dx.lib;

import android.util.Log;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHeaders;

/* JADX INFO: loaded from: classes.dex */
class HeadTaskHandler extends AsyncHttpResponseHandler {
    private Cocos2dxDownloader _downloader;
    String _host;
    int _id;
    String _path;
    String _url;

    void LogD(String str) {
        Log.d("Cocos2dxDownloader", str);
    }

    public HeadTaskHandler(Cocos2dxDownloader cocos2dxDownloader, int i, String str, String str2, String str3) {
        this._downloader = cocos2dxDownloader;
        this._id = i;
        this._host = str;
        this._url = str2;
        this._path = str3;
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onSuccess(int i, Header[] headerArr, byte[] bArr) {
        Boolean boolValueOf = Boolean.FALSE;
        for (Header header : headerArr) {
            if (header.getName().equals(HttpHeaders.ACCEPT_RANGES)) {
                boolValueOf = Boolean.valueOf(header.getValue().equals("bytes"));
                break;
            }
        }
        Cocos2dxDownloader.setResumingSupport(this._host, boolValueOf);
        Cocos2dxDownloader.createTask(this._downloader, this._id, this._url, this._path);
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onFinish() {
        this._downloader.runNextTaskIfExists();
    }

    @Override // com.loopj.android.http.AsyncHttpResponseHandler
    public void onFailure(int i, Header[] headerArr, byte[] bArr, Throwable th) {
        String string;
        LogD("onFailure(code:" + i + " headers:" + headerArr + " throwable:" + th + " id:" + this._id);
        if (th == null) {
            string = "";
        } else {
            string = th.toString();
        }
        this._downloader.onFinish(this._id, i, string, null);
    }
}
