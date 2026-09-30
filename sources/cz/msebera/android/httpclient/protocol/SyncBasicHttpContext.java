package cz.msebera.android.httpclient.protocol;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class SyncBasicHttpContext extends BasicHttpContext {
    public SyncBasicHttpContext(HttpContext httpContext) {
        super(httpContext);
    }

    public SyncBasicHttpContext() {
    }

    @Override // cz.msebera.android.httpclient.protocol.BasicHttpContext, cz.msebera.android.httpclient.protocol.HttpContext
    public synchronized Object getAttribute(String str) {
        return super.getAttribute(str);
    }

    @Override // cz.msebera.android.httpclient.protocol.BasicHttpContext, cz.msebera.android.httpclient.protocol.HttpContext
    public synchronized void setAttribute(String str, Object obj) {
        super.setAttribute(str, obj);
    }

    @Override // cz.msebera.android.httpclient.protocol.BasicHttpContext, cz.msebera.android.httpclient.protocol.HttpContext
    public synchronized Object removeAttribute(String str) {
        return super.removeAttribute(str);
    }

    @Override // cz.msebera.android.httpclient.protocol.BasicHttpContext
    public synchronized void clear() {
        super.clear();
    }
}
