package cz.msebera.android.httpclient.params;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class SyncBasicHttpParams extends BasicHttpParams {
    private static final long serialVersionUID = 5387834869062660642L;

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams, cz.msebera.android.httpclient.params.HttpParams
    public synchronized boolean removeParameter(String str) {
        return super.removeParameter(str);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams, cz.msebera.android.httpclient.params.HttpParams
    public synchronized HttpParams setParameter(String str, Object obj) {
        return super.setParameter(str, obj);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams, cz.msebera.android.httpclient.params.HttpParams
    public synchronized Object getParameter(String str) {
        return super.getParameter(str);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams
    public synchronized boolean isParameterSet(String str) {
        return super.isParameterSet(str);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams
    public synchronized boolean isParameterSetLocally(String str) {
        return super.isParameterSetLocally(str);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams
    public synchronized void setParameters(String[] strArr, Object obj) {
        super.setParameters(strArr, obj);
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams
    public synchronized void clear() {
        super.clear();
    }

    @Override // cz.msebera.android.httpclient.params.BasicHttpParams
    public synchronized Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
