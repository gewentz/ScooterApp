package cz.msebera.android.httpclient.conn.scheme;

import java.io.IOException;
import java.net.InetAddress;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public interface HostNameResolver {
    InetAddress resolve(String str) throws IOException;
}
