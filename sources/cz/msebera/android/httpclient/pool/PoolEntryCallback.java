package cz.msebera.android.httpclient.pool;

/* JADX INFO: loaded from: classes.dex */
public interface PoolEntryCallback<T, C> {
    void process(PoolEntry<T, C> poolEntry);
}
