package cz.msebera.android.httpclient;

/* JADX INFO: loaded from: classes.dex */
public interface RequestLine {
    String getMethod();

    ProtocolVersion getProtocolVersion();

    String getUri();
}
