package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.cookie.SetCookie2;
import java.util.Date;

/* JADX INFO: loaded from: classes.dex */
public class BasicClientCookie2 extends BasicClientCookie implements SetCookie2 {
    private static final long serialVersionUID = -7744598295706617057L;
    private String commentURL;
    private boolean discard;
    private int[] ports;

    public BasicClientCookie2(String str, String str2) {
        super(str, str2);
    }

    @Override // cz.msebera.android.httpclient.impl.cookie.BasicClientCookie, cz.msebera.android.httpclient.cookie.Cookie
    public int[] getPorts() {
        return this.ports;
    }

    @Override // cz.msebera.android.httpclient.cookie.SetCookie2
    public void setPorts(int[] iArr) {
        this.ports = iArr;
    }

    @Override // cz.msebera.android.httpclient.impl.cookie.BasicClientCookie, cz.msebera.android.httpclient.cookie.Cookie
    public String getCommentURL() {
        return this.commentURL;
    }

    @Override // cz.msebera.android.httpclient.cookie.SetCookie2
    public void setCommentURL(String str) {
        this.commentURL = str;
    }

    @Override // cz.msebera.android.httpclient.cookie.SetCookie2
    public void setDiscard(boolean z) {
        this.discard = z;
    }

    @Override // cz.msebera.android.httpclient.impl.cookie.BasicClientCookie, cz.msebera.android.httpclient.cookie.Cookie
    public boolean isPersistent() {
        return !this.discard && super.isPersistent();
    }

    @Override // cz.msebera.android.httpclient.impl.cookie.BasicClientCookie, cz.msebera.android.httpclient.cookie.Cookie
    public boolean isExpired(Date date) {
        return this.discard || super.isExpired(date);
    }

    @Override // cz.msebera.android.httpclient.impl.cookie.BasicClientCookie
    public Object clone() throws CloneNotSupportedException {
        BasicClientCookie2 basicClientCookie2 = (BasicClientCookie2) super.clone();
        int[] iArr = this.ports;
        if (iArr != null) {
            basicClientCookie2.ports = (int[]) iArr.clone();
        }
        return basicClientCookie2;
    }
}
