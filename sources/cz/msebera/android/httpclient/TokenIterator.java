package cz.msebera.android.httpclient;

import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public interface TokenIterator extends Iterator<Object> {
    @Override // java.util.Iterator
    boolean hasNext();

    String nextToken();
}
