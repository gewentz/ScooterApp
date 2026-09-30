package cz.msebera.android.httpclient.conn.scheme;

import java.io.IOException;
import java.net.Socket;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public interface LayeredSocketFactory extends SocketFactory {
    Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException;
}
