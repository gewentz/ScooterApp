package cz.msebera.android.httpclient.protocol;

/* JADX INFO: loaded from: classes.dex */
public interface HttpContext {
    public static final String RESERVED_PREFIX = "http.";

    Object getAttribute(String str);

    Object removeAttribute(String str);

    void setAttribute(String str, Object obj);
}
