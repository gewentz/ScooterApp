package cz.msebera.android.httpclient.params;

import cz.msebera.android.httpclient.util.Args;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public abstract class HttpAbstractParamBean {
    protected final HttpParams params;

    public HttpAbstractParamBean(HttpParams httpParams) {
        this.params = (HttpParams) Args.notNull(httpParams, "HTTP parameters");
    }
}
