package cz.msebera.android.httpclient.impl.execchain;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.params.HttpParams;
import java.io.IOException;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
class HttpResponseProxy implements CloseableHttpResponse {
    private final ConnectionHolder connHolder;
    private final HttpResponse original;

    public HttpResponseProxy(HttpResponse httpResponse, ConnectionHolder connectionHolder) {
        this.original = httpResponse;
        this.connHolder = connectionHolder;
        ResponseEntityProxy.enchance(httpResponse, connectionHolder);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        ConnectionHolder connectionHolder = this.connHolder;
        if (connectionHolder != null) {
            connectionHolder.abortConnection();
        }
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public StatusLine getStatusLine() {
        return this.original.getStatusLine();
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setStatusLine(StatusLine statusLine) {
        this.original.setStatusLine(statusLine);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setStatusLine(ProtocolVersion protocolVersion, int i) {
        this.original.setStatusLine(protocolVersion, i);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setStatusLine(ProtocolVersion protocolVersion, int i, String str) {
        this.original.setStatusLine(protocolVersion, i, str);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setStatusCode(int i) throws IllegalStateException {
        this.original.setStatusCode(i);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setReasonPhrase(String str) throws IllegalStateException {
        this.original.setReasonPhrase(str);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public HttpEntity getEntity() {
        return this.original.getEntity();
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setEntity(HttpEntity httpEntity) {
        this.original.setEntity(httpEntity);
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public Locale getLocale() {
        return this.original.getLocale();
    }

    @Override // cz.msebera.android.httpclient.HttpResponse
    public void setLocale(Locale locale) {
        this.original.setLocale(locale);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public ProtocolVersion getProtocolVersion() {
        return this.original.getProtocolVersion();
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public boolean containsHeader(String str) {
        return this.original.containsHeader(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public Header[] getHeaders(String str) {
        return this.original.getHeaders(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public Header getFirstHeader(String str) {
        return this.original.getFirstHeader(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public Header getLastHeader(String str) {
        return this.original.getLastHeader(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public Header[] getAllHeaders() {
        return this.original.getAllHeaders();
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void addHeader(Header header) {
        this.original.addHeader(header);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void addHeader(String str, String str2) {
        this.original.addHeader(str, str2);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void setHeader(Header header) {
        this.original.setHeader(header);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void setHeader(String str, String str2) {
        this.original.setHeader(str, str2);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void setHeaders(Header[] headerArr) {
        this.original.setHeaders(headerArr);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void removeHeader(Header header) {
        this.original.removeHeader(header);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public void removeHeaders(String str) {
        this.original.removeHeaders(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public HeaderIterator headerIterator() {
        return this.original.headerIterator();
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    public HeaderIterator headerIterator(String str) {
        return this.original.headerIterator(str);
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    @Deprecated
    public HttpParams getParams() {
        return this.original.getParams();
    }

    @Override // cz.msebera.android.httpclient.HttpMessage
    @Deprecated
    public void setParams(HttpParams httpParams) {
        this.original.setParams(httpParams);
    }

    public String toString() {
        return "HttpResponseProxy{" + this.original + '}';
    }
}
