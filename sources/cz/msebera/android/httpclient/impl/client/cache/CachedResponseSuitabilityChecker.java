package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import java.util.Date;
import kotlin.jvm.internal.LongCompanionObject;

/* JADX INFO: loaded from: classes.dex */
class CachedResponseSuitabilityChecker {
    private final float heuristicCoefficient;
    private final long heuristicDefaultLifetime;
    public HttpClientAndroidLog log;
    private final boolean sharedCache;
    private final boolean useHeuristicCaching;
    private final CacheValidityPolicy validityStrategy;

    CachedResponseSuitabilityChecker(CacheValidityPolicy cacheValidityPolicy, CacheConfig cacheConfig) {
        this.log = new HttpClientAndroidLog(getClass());
        this.validityStrategy = cacheValidityPolicy;
        this.sharedCache = cacheConfig.isSharedCache();
        this.useHeuristicCaching = cacheConfig.isHeuristicCachingEnabled();
        this.heuristicCoefficient = cacheConfig.getHeuristicCoefficient();
        this.heuristicDefaultLifetime = cacheConfig.getHeuristicDefaultLifetime();
    }

    CachedResponseSuitabilityChecker(CacheConfig cacheConfig) {
        this(new CacheValidityPolicy(), cacheConfig);
    }

    private boolean isFreshEnough(HttpCacheEntry httpCacheEntry, HttpRequest httpRequest, Date date) {
        HttpCacheEntry httpCacheEntry2;
        Date date2;
        if (this.validityStrategy.isResponseFresh(httpCacheEntry, date)) {
            return true;
        }
        if (this.useHeuristicCaching) {
            httpCacheEntry2 = httpCacheEntry;
            date2 = date;
            if (this.validityStrategy.isResponseHeuristicallyFresh(httpCacheEntry2, date2, this.heuristicCoefficient, this.heuristicDefaultLifetime)) {
                return true;
            }
        } else {
            httpCacheEntry2 = httpCacheEntry;
            date2 = date;
        }
        if (originInsistsOnFreshness(httpCacheEntry2)) {
            return false;
        }
        long maxStale = getMaxStale(httpRequest);
        return maxStale != -1 && maxStale > this.validityStrategy.getStalenessSecs(httpCacheEntry2, date2);
    }

    private boolean originInsistsOnFreshness(HttpCacheEntry httpCacheEntry) {
        if (this.validityStrategy.mustRevalidate(httpCacheEntry)) {
            return true;
        }
        if (this.sharedCache) {
            return this.validityStrategy.proxyRevalidate(httpCacheEntry) || this.validityStrategy.hasCacheControlDirective(httpCacheEntry, "s-maxage");
        }
        return false;
    }

    private long getMaxStale(HttpRequest httpRequest) {
        long j = -1;
        for (Header header : httpRequest.getHeaders("Cache-Control")) {
            for (HeaderElement headerElement : header.getElements()) {
                if (HeaderConstants.CACHE_CONTROL_MAX_STALE.equals(headerElement.getName())) {
                    if ((headerElement.getValue() == null || "".equals(headerElement.getValue().trim())) && j == -1) {
                        j = LongCompanionObject.MAX_VALUE;
                    } else {
                        long j2 = 0;
                        try {
                            long j3 = Long.parseLong(headerElement.getValue());
                            j2 = j3 >= 0 ? j3 : 0L;
                            if (j == -1 || j2 < j) {
                                j = j2;
                            }
                        } catch (NumberFormatException unused) {
                        }
                    }
                }
            }
        }
        return j;
    }

    public boolean canCachedResponseBeUsed(HttpHost httpHost, HttpRequest httpRequest, HttpCacheEntry httpCacheEntry, Date date) {
        boolean z;
        boolean z2 = false;
        if (!isFreshEnough(httpCacheEntry, httpRequest, date)) {
            this.log.trace("Cache entry was not fresh enough");
            return false;
        }
        if (isGet(httpRequest) && !this.validityStrategy.contentLengthHeaderMatchesActualLength(httpCacheEntry)) {
            this.log.debug("Cache entry Content-Length and header information do not match");
            return false;
        }
        if (hasUnsupportedConditionalHeaders(httpRequest)) {
            this.log.debug("Request contained conditional headers we don't handle");
            return false;
        }
        if (!isConditional(httpRequest) && httpCacheEntry.getStatusCode() == 304) {
            return false;
        }
        if (isConditional(httpRequest) && !allConditionalsMatch(httpRequest, httpCacheEntry, date)) {
            return false;
        }
        if (hasUnsupportedCacheEntryForGet(httpRequest, httpCacheEntry)) {
            this.log.debug("HEAD response caching enabled but the cache entry does not contain a request method, entity or a 204 response");
            return false;
        }
        Header[] headers = httpRequest.getHeaders("Cache-Control");
        int length = headers.length;
        int i = 0;
        while (i < length) {
            HeaderElement[] elements = headers[i].getElements();
            int length2 = elements.length;
            int i2 = 0;
            while (i2 < length2) {
                HeaderElement headerElement = elements[i2];
                if (HeaderConstants.CACHE_CONTROL_NO_CACHE.equals(headerElement.getName())) {
                    this.log.trace("Response contained NO CACHE directive, cache was not suitable");
                    return z2;
                }
                if (HeaderConstants.CACHE_CONTROL_NO_STORE.equals(headerElement.getName())) {
                    this.log.trace("Response contained NO STORE directive, cache was not suitable");
                    return z2;
                }
                if ("max-age".equals(headerElement.getName())) {
                    try {
                        if (this.validityStrategy.getCurrentAgeSecs(httpCacheEntry, date) > Integer.parseInt(headerElement.getValue())) {
                            this.log.trace("Response from cache was NOT suitable due to max age");
                            return z2;
                        }
                    } catch (NumberFormatException e) {
                        this.log.debug("Response from cache was malformed" + e.getMessage());
                        return z2;
                    }
                }
                if (HeaderConstants.CACHE_CONTROL_MAX_STALE.equals(headerElement.getName())) {
                    try {
                        z = false;
                        if (this.validityStrategy.getFreshnessLifetimeSecs(httpCacheEntry) > Integer.parseInt(headerElement.getValue())) {
                            try {
                                this.log.trace("Response from cache was not suitable due to Max stale freshness");
                                return false;
                            } catch (NumberFormatException e2) {
                                e = e2;
                                this.log.debug("Response from cache was malformed: " + e.getMessage());
                                return z;
                            }
                        }
                    } catch (NumberFormatException e3) {
                        e = e3;
                        z = false;
                    }
                } else {
                    z = false;
                }
                if (HeaderConstants.CACHE_CONTROL_MIN_FRESH.equals(headerElement.getName())) {
                    try {
                        long j = Long.parseLong(headerElement.getValue());
                        if (j < 0) {
                            return z;
                        }
                        if (this.validityStrategy.getFreshnessLifetimeSecs(httpCacheEntry) - this.validityStrategy.getCurrentAgeSecs(httpCacheEntry, date) < j) {
                            this.log.trace("Response from cache was not suitable due to min fresh freshness requirement");
                            return z;
                        }
                    } catch (NumberFormatException e4) {
                        this.log.debug("Response from cache was malformed: " + e4.getMessage());
                        return z;
                    }
                }
                i2++;
                i = i;
                z2 = false;
            }
            i++;
            z2 = false;
        }
        this.log.trace("Response from cache was suitable");
        return true;
    }

    private boolean isGet(HttpRequest httpRequest) {
        return httpRequest.getRequestLine().getMethod().equals("GET");
    }

    private boolean entryIsNotA204Response(HttpCacheEntry httpCacheEntry) {
        return httpCacheEntry.getStatusCode() != 204;
    }

    private boolean cacheEntryDoesNotContainMethodAndEntity(HttpCacheEntry httpCacheEntry) {
        return httpCacheEntry.getRequestMethod() == null && httpCacheEntry.getResource() == null;
    }

    private boolean hasUnsupportedCacheEntryForGet(HttpRequest httpRequest, HttpCacheEntry httpCacheEntry) {
        return isGet(httpRequest) && cacheEntryDoesNotContainMethodAndEntity(httpCacheEntry) && entryIsNotA204Response(httpCacheEntry);
    }

    public boolean isConditional(HttpRequest httpRequest) {
        return hasSupportedEtagValidator(httpRequest) || hasSupportedLastModifiedValidator(httpRequest);
    }

    public boolean allConditionalsMatch(HttpRequest httpRequest, HttpCacheEntry httpCacheEntry, Date date) {
        boolean zHasSupportedEtagValidator = hasSupportedEtagValidator(httpRequest);
        boolean zHasSupportedLastModifiedValidator = hasSupportedLastModifiedValidator(httpRequest);
        boolean z = zHasSupportedEtagValidator && etagValidatorMatches(httpRequest, httpCacheEntry);
        boolean z2 = zHasSupportedLastModifiedValidator && lastModifiedValidatorMatches(httpRequest, httpCacheEntry, date);
        if (zHasSupportedEtagValidator && zHasSupportedLastModifiedValidator && (!z || !z2)) {
            return false;
        }
        if (!zHasSupportedEtagValidator || z) {
            return !zHasSupportedLastModifiedValidator || z2;
        }
        return false;
    }

    private boolean hasUnsupportedConditionalHeaders(HttpRequest httpRequest) {
        return (httpRequest.getFirstHeader("If-Range") == null && httpRequest.getFirstHeader("If-Match") == null && !hasValidDateField(httpRequest, "If-Unmodified-Since")) ? false : true;
    }

    private boolean hasSupportedEtagValidator(HttpRequest httpRequest) {
        return httpRequest.containsHeader("If-None-Match");
    }

    private boolean hasSupportedLastModifiedValidator(HttpRequest httpRequest) {
        return hasValidDateField(httpRequest, "If-Modified-Since");
    }

    private boolean etagValidatorMatches(HttpRequest httpRequest, HttpCacheEntry httpCacheEntry) {
        Header firstHeader = httpCacheEntry.getFirstHeader("ETag");
        String value = firstHeader != null ? firstHeader.getValue() : null;
        Header[] headers = httpRequest.getHeaders("If-None-Match");
        if (headers != null) {
            for (Header header : headers) {
                for (HeaderElement headerElement : header.getElements()) {
                    String string = headerElement.toString();
                    if (("*".equals(string) && value != null) || string.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean lastModifiedValidatorMatches(HttpRequest httpRequest, HttpCacheEntry httpCacheEntry, Date date) {
        Header firstHeader = httpCacheEntry.getFirstHeader("Last-Modified");
        Date date2 = firstHeader != null ? DateUtils.parseDate(firstHeader.getValue()) : null;
        if (date2 == null) {
            return false;
        }
        for (Header header : httpRequest.getHeaders("If-Modified-Since")) {
            Date date3 = DateUtils.parseDate(header.getValue());
            if (date3 != null && (date3.after(date) || date2.after(date3))) {
                return false;
            }
        }
        return true;
    }

    private boolean hasValidDateField(HttpRequest httpRequest, String str) {
        Header[] headers = httpRequest.getHeaders(str);
        return headers.length > 0 && DateUtils.parseDate(headers[0].getValue()) != null;
    }
}
