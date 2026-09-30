package com.huawei.android.hwgamesdk;

/* JADX INFO: loaded from: classes.dex */
public class NoExtAPIException extends RuntimeException {
    private static final long serialVersionUID = -5365630128856068164L;

    public NoExtAPIException() {
    }

    public NoExtAPIException(String str) {
        super(str);
    }

    public NoExtAPIException(String str, Throwable th) {
        super(str, th);
    }

    public NoExtAPIException(Throwable th) {
        super(th == null ? null : th.toString(), th);
    }
}
