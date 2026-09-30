package cz.msebera.android.httpclient.client.entity;

import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public interface InputStreamFactory {
    InputStream create(InputStream inputStream) throws IOException;
}
