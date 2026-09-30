package cz.msebera.android.httpclient;

import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public interface HeaderIterator extends Iterator<Object> {
    @Override // java.util.Iterator
    boolean hasNext();

    Header nextHeader();
}
