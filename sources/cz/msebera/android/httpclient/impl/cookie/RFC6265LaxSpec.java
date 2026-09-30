package cz.msebera.android.httpclient.impl.cookie;

import cz.msebera.android.httpclient.cookie.CommonCookieAttributeHandler;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class RFC6265LaxSpec extends RFC6265CookieSpecBase {
    @Override // cz.msebera.android.httpclient.impl.cookie.RFC6265CookieSpecBase, cz.msebera.android.httpclient.cookie.CookieSpec
    public /* bridge */ /* synthetic */ List formatCookies(List list) {
        return super.formatCookies(list);
    }

    public RFC6265LaxSpec() {
        super(new BasicPathHandler(), new BasicDomainHandler(), new LaxMaxAgeHandler(), new BasicSecureHandler(), new LaxExpiresHandler());
    }

    RFC6265LaxSpec(CommonCookieAttributeHandler... commonCookieAttributeHandlerArr) {
        super(commonCookieAttributeHandlerArr);
    }

    public String toString() {
        return "rfc6265-lax";
    }
}
