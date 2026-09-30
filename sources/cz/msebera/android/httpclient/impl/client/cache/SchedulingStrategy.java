package cz.msebera.android.httpclient.impl.client.cache;

import java.io.Closeable;

/* JADX INFO: loaded from: classes.dex */
public interface SchedulingStrategy extends Closeable {
    void schedule(AsynchronousValidationRequest asynchronousValidationRequest);
}
