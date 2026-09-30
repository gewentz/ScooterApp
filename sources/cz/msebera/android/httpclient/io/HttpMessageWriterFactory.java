package cz.msebera.android.httpclient.io;

import cz.msebera.android.httpclient.HttpMessage;

/* JADX INFO: loaded from: classes.dex */
public interface HttpMessageWriterFactory<T extends HttpMessage> {
    HttpMessageWriter<T> create(SessionOutputBuffer sessionOutputBuffer);
}
