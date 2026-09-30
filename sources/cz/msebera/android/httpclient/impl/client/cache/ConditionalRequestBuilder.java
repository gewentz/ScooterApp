package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.methods.HttpRequestWrapper;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
class ConditionalRequestBuilder {
    ConditionalRequestBuilder() {
    }

    public HttpRequestWrapper buildConditionalRequest(HttpRequestWrapper httpRequestWrapper, HttpCacheEntry httpCacheEntry) throws ProtocolException {
        HttpRequestWrapper httpRequestWrapperWrap = HttpRequestWrapper.wrap(httpRequestWrapper.getOriginal());
        httpRequestWrapperWrap.setHeaders(httpRequestWrapper.getAllHeaders());
        Header firstHeader = httpCacheEntry.getFirstHeader("ETag");
        if (firstHeader != null) {
            httpRequestWrapperWrap.setHeader("If-None-Match", firstHeader.getValue());
        }
        Header firstHeader2 = httpCacheEntry.getFirstHeader("Last-Modified");
        if (firstHeader2 != null) {
            httpRequestWrapperWrap.setHeader("If-Modified-Since", firstHeader2.getValue());
        }
        boolean z = false;
        for (Header header : httpCacheEntry.getHeaders("Cache-Control")) {
            for (HeaderElement headerElement : header.getElements()) {
                if (HeaderConstants.CACHE_CONTROL_MUST_REVALIDATE.equalsIgnoreCase(headerElement.getName()) || HeaderConstants.CACHE_CONTROL_PROXY_REVALIDATE.equalsIgnoreCase(headerElement.getName())) {
                    z = true;
                    break;
                }
            }
        }
        if (z) {
            httpRequestWrapperWrap.addHeader("Cache-Control", "max-age=0");
        }
        return httpRequestWrapperWrap;
    }

    public HttpRequestWrapper buildConditionalRequestFromVariants(HttpRequestWrapper httpRequestWrapper, Map<String, Variant> map) {
        HttpRequestWrapper httpRequestWrapperWrap = HttpRequestWrapper.wrap(httpRequestWrapper.getOriginal());
        httpRequestWrapperWrap.setHeaders(httpRequestWrapper.getAllHeaders());
        StringBuilder sb = new StringBuilder();
        boolean z = true;
        for (String str : map.keySet()) {
            if (!z) {
                sb.append(",");
            }
            sb.append(str);
            z = false;
        }
        httpRequestWrapperWrap.setHeader("If-None-Match", sb.toString());
        return httpRequestWrapperWrap;
    }

    public HttpRequestWrapper buildUnconditionalRequest(HttpRequestWrapper httpRequestWrapper, HttpCacheEntry httpCacheEntry) {
        HttpRequestWrapper httpRequestWrapperWrap = HttpRequestWrapper.wrap(httpRequestWrapper.getOriginal());
        httpRequestWrapperWrap.setHeaders(httpRequestWrapper.getAllHeaders());
        httpRequestWrapperWrap.addHeader("Cache-Control", HeaderConstants.CACHE_CONTROL_NO_CACHE);
        httpRequestWrapperWrap.addHeader("Pragma", HeaderConstants.CACHE_CONTROL_NO_CACHE);
        httpRequestWrapperWrap.removeHeaders("If-Range");
        httpRequestWrapperWrap.removeHeaders("If-Match");
        httpRequestWrapperWrap.removeHeaders("If-None-Match");
        httpRequestWrapperWrap.removeHeaders("If-Unmodified-Since");
        httpRequestWrapperWrap.removeHeaders("If-Modified-Since");
        return httpRequestWrapperWrap;
    }
}
