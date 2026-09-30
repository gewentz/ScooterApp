package cz.msebera.android.httpclient.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.message.HeaderGroup;
import cz.msebera.android.httpclient.util.Args;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class HttpCacheEntry implements Serializable {
    private static final String REQUEST_METHOD_HEADER_NAME = "Hc-Request-Method";
    private static final long serialVersionUID = -6300496422359477413L;
    private final Date date;
    private final Date requestDate;
    private final Resource resource;
    private final Date responseDate;
    private final HeaderGroup responseHeaders;
    private final StatusLine statusLine;
    private final Map<String, String> variantMap;

    public HttpCacheEntry(Date date, Date date2, StatusLine statusLine, Header[] headerArr, Resource resource, Map<String, String> map, String str) {
        Args.notNull(date, "Request date");
        Args.notNull(date2, "Response date");
        Args.notNull(statusLine, "Status line");
        Args.notNull(headerArr, "Response headers");
        this.requestDate = date;
        this.responseDate = date2;
        this.statusLine = statusLine;
        HeaderGroup headerGroup = new HeaderGroup();
        this.responseHeaders = headerGroup;
        headerGroup.setHeaders(headerArr);
        this.resource = resource;
        this.variantMap = map != null ? new HashMap(map) : null;
        this.date = parseDate();
    }

    public HttpCacheEntry(Date date, Date date2, StatusLine statusLine, Header[] headerArr, Resource resource, Map<String, String> map) {
        this(date, date2, statusLine, headerArr, resource, map, null);
    }

    public HttpCacheEntry(Date date, Date date2, StatusLine statusLine, Header[] headerArr, Resource resource) {
        this(date, date2, statusLine, headerArr, resource, new HashMap());
    }

    public HttpCacheEntry(Date date, Date date2, StatusLine statusLine, Header[] headerArr, Resource resource, String str) {
        this(date, date2, statusLine, headerArr, resource, new HashMap(), str);
    }

    private Date parseDate() {
        Header firstHeader = getFirstHeader("Date");
        if (firstHeader == null) {
            return null;
        }
        return DateUtils.parseDate(firstHeader.getValue());
    }

    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.statusLine.getProtocolVersion();
    }

    public String getReasonPhrase() {
        return this.statusLine.getReasonPhrase();
    }

    public int getStatusCode() {
        return this.statusLine.getStatusCode();
    }

    public Date getRequestDate() {
        return this.requestDate;
    }

    public Date getResponseDate() {
        return this.responseDate;
    }

    public Header[] getAllHeaders() {
        HeaderGroup headerGroup = new HeaderGroup();
        HeaderIterator it = this.responseHeaders.iterator();
        while (it.hasNext()) {
            Header header = (Header) it.next();
            if (!REQUEST_METHOD_HEADER_NAME.equals(header.getName())) {
                headerGroup.addHeader(header);
            }
        }
        return headerGroup.getAllHeaders();
    }

    public Header getFirstHeader(String str) {
        if (REQUEST_METHOD_HEADER_NAME.equalsIgnoreCase(str)) {
            return null;
        }
        return this.responseHeaders.getFirstHeader(str);
    }

    public Header[] getHeaders(String str) {
        if (REQUEST_METHOD_HEADER_NAME.equalsIgnoreCase(str)) {
            return new Header[0];
        }
        return this.responseHeaders.getHeaders(str);
    }

    public Date getDate() {
        return this.date;
    }

    public Resource getResource() {
        return this.resource;
    }

    public boolean hasVariants() {
        return getFirstHeader("Vary") != null;
    }

    public Map<String, String> getVariantMap() {
        return Collections.unmodifiableMap(this.variantMap);
    }

    public String getRequestMethod() {
        Header firstHeader = this.responseHeaders.getFirstHeader(REQUEST_METHOD_HEADER_NAME);
        if (firstHeader != null) {
            return firstHeader.getValue();
        }
        return "GET";
    }

    public String toString() {
        return "[request date=" + this.requestDate + "; response date=" + this.responseDate + "; statusLine=" + this.statusLine + "]";
    }
}
