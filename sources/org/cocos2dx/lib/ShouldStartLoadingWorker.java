package org.cocos2dx.lib;

import java.util.concurrent.CountDownLatch;

/* JADX INFO: loaded from: classes.dex */
class ShouldStartLoadingWorker implements Runnable {
    private CountDownLatch mLatch;
    private boolean[] mResult;
    private final String mUrlString;
    private final int mViewTag;

    ShouldStartLoadingWorker(CountDownLatch countDownLatch, boolean[] zArr, int i, String str) {
        this.mLatch = countDownLatch;
        this.mResult = zArr;
        this.mViewTag = i;
        this.mUrlString = str;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mResult[0] = Cocos2dxWebViewHelper._shouldStartLoading(this.mViewTag, this.mUrlString);
        this.mLatch.countDown();
    }
}
