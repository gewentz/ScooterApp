package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpMessage;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.RequestLine;
import cz.msebera.android.httpclient.client.cache.CacheResponseStatus;
import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import cz.msebera.android.httpclient.client.cache.HttpCacheContext;
import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.cache.HttpCacheStorage;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpExecutionAware;
import cz.msebera.android.httpclient.client.methods.HttpRequestWrapper;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.client.utils.DateUtils;
import cz.msebera.android.httpclient.client.utils.URIUtils;
import cz.msebera.android.httpclient.conn.routing.HttpRoute;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.impl.execchain.ClientExecChain;
import cz.msebera.android.httpclient.message.BasicHttpResponse;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Args;
import cz.msebera.android.httpclient.util.VersionInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/* JADX INFO: loaded from: classes.dex */
public class CachingExec implements ClientExecChain {
    private static final boolean SUPPORTS_RANGE_AND_CONTENT_RANGE_HEADERS = false;
    private final AsynchronousValidator asynchRevalidator;
    private final ClientExecChain backend;
    private final CacheConfig cacheConfig;
    private final AtomicLong cacheHits;
    private final AtomicLong cacheMisses;
    private final AtomicLong cacheUpdates;
    private final CacheableRequestPolicy cacheableRequestPolicy;
    private final ConditionalRequestBuilder conditionalRequestBuilder;
    public HttpClientAndroidLog log;
    private final RequestProtocolCompliance requestCompliance;
    private final HttpCache responseCache;
    private final ResponseCachingPolicy responseCachingPolicy;
    private final ResponseProtocolCompliance responseCompliance;
    private final CachedHttpResponseGenerator responseGenerator;
    private final CachedResponseSuitabilityChecker suitabilityChecker;
    private final CacheValidityPolicy validityPolicy;
    private final Map<ProtocolVersion, String> viaHeaders;

    private boolean staleIfErrorAppliesTo(int i) {
        return i == 500 || i == 502 || i == 503 || i == 504;
    }

    public boolean supportsRangeAndContentRangeHeaders() {
        return false;
    }

    public CachingExec(ClientExecChain clientExecChain, HttpCache httpCache, CacheConfig cacheConfig) {
        this(clientExecChain, httpCache, cacheConfig, (AsynchronousValidator) null);
    }

    public CachingExec(ClientExecChain clientExecChain, HttpCache httpCache, CacheConfig cacheConfig, AsynchronousValidator asynchronousValidator) {
        this.cacheHits = new AtomicLong();
        this.cacheMisses = new AtomicLong();
        this.cacheUpdates = new AtomicLong();
        this.viaHeaders = new HashMap(4);
        this.log = new HttpClientAndroidLog(getClass());
        Args.notNull(clientExecChain, "HTTP backend");
        Args.notNull(httpCache, "HttpCache");
        cacheConfig = cacheConfig == null ? CacheConfig.DEFAULT : cacheConfig;
        this.cacheConfig = cacheConfig;
        this.backend = clientExecChain;
        this.responseCache = httpCache;
        CacheValidityPolicy cacheValidityPolicy = new CacheValidityPolicy();
        this.validityPolicy = cacheValidityPolicy;
        this.responseGenerator = new CachedHttpResponseGenerator(cacheValidityPolicy);
        this.cacheableRequestPolicy = new CacheableRequestPolicy();
        this.suitabilityChecker = new CachedResponseSuitabilityChecker(cacheValidityPolicy, cacheConfig);
        this.conditionalRequestBuilder = new ConditionalRequestBuilder();
        this.responseCompliance = new ResponseProtocolCompliance();
        this.requestCompliance = new RequestProtocolCompliance(cacheConfig.isWeakETagOnPutDeleteAllowed());
        this.responseCachingPolicy = new ResponseCachingPolicy(cacheConfig.getMaxObjectSize(), cacheConfig.isSharedCache(), cacheConfig.isNeverCacheHTTP10ResponsesWithQuery(), cacheConfig.is303CachingEnabled());
        this.asynchRevalidator = asynchronousValidator;
    }

    public CachingExec(ClientExecChain clientExecChain, ResourceFactory resourceFactory, HttpCacheStorage httpCacheStorage, CacheConfig cacheConfig) {
        this(clientExecChain, new BasicHttpCache(resourceFactory, httpCacheStorage, cacheConfig), cacheConfig);
    }

    public CachingExec(ClientExecChain clientExecChain) {
        this(clientExecChain, new BasicHttpCache(), CacheConfig.DEFAULT);
    }

    CachingExec(ClientExecChain clientExecChain, HttpCache httpCache, CacheValidityPolicy cacheValidityPolicy, ResponseCachingPolicy responseCachingPolicy, CachedHttpResponseGenerator cachedHttpResponseGenerator, CacheableRequestPolicy cacheableRequestPolicy, CachedResponseSuitabilityChecker cachedResponseSuitabilityChecker, ConditionalRequestBuilder conditionalRequestBuilder, ResponseProtocolCompliance responseProtocolCompliance, RequestProtocolCompliance requestProtocolCompliance, CacheConfig cacheConfig, AsynchronousValidator asynchronousValidator) {
        this.cacheHits = new AtomicLong();
        this.cacheMisses = new AtomicLong();
        this.cacheUpdates = new AtomicLong();
        this.viaHeaders = new HashMap(4);
        this.log = new HttpClientAndroidLog(getClass());
        this.cacheConfig = cacheConfig == null ? CacheConfig.DEFAULT : cacheConfig;
        this.backend = clientExecChain;
        this.responseCache = httpCache;
        this.validityPolicy = cacheValidityPolicy;
        this.responseCachingPolicy = responseCachingPolicy;
        this.responseGenerator = cachedHttpResponseGenerator;
        this.cacheableRequestPolicy = cacheableRequestPolicy;
        this.suitabilityChecker = cachedResponseSuitabilityChecker;
        this.conditionalRequestBuilder = conditionalRequestBuilder;
        this.responseCompliance = responseProtocolCompliance;
        this.requestCompliance = requestProtocolCompliance;
        this.asynchRevalidator = asynchronousValidator;
    }

    public long getCacheHits() {
        return this.cacheHits.get();
    }

    public long getCacheMisses() {
        return this.cacheMisses.get();
    }

    public long getCacheUpdates() {
        return this.cacheUpdates.get();
    }

    public CloseableHttpResponse execute(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper) throws IOException, HttpException {
        return execute(httpRoute, httpRequestWrapper, HttpClientContext.create(), null);
    }

    public CloseableHttpResponse execute(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext) throws IOException, HttpException {
        return execute(httpRoute, httpRequestWrapper, httpClientContext, null);
    }

    @Override // cz.msebera.android.httpclient.impl.execchain.ClientExecChain
    public CloseableHttpResponse execute(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware) throws IOException, HttpException {
        HttpHost targetHost = httpClientContext.getTargetHost();
        String strGenerateViaHeader = generateViaHeader(httpRequestWrapper.getOriginal());
        setResponseStatus(httpClientContext, CacheResponseStatus.CACHE_MISS);
        if (clientRequestsOurOptions(httpRequestWrapper)) {
            setResponseStatus(httpClientContext, CacheResponseStatus.CACHE_MODULE_RESPONSE);
            return Proxies.enhanceResponse(new OptionsHttp11Response());
        }
        HttpResponse fatallyNoncompliantResponse = getFatallyNoncompliantResponse(httpRequestWrapper, httpClientContext);
        if (fatallyNoncompliantResponse != null) {
            return Proxies.enhanceResponse(fatallyNoncompliantResponse);
        }
        this.requestCompliance.makeRequestCompliant(httpRequestWrapper);
        httpRequestWrapper.addHeader("Via", strGenerateViaHeader);
        flushEntriesInvalidatedByRequest(httpClientContext.getTargetHost(), httpRequestWrapper);
        if (!this.cacheableRequestPolicy.isServableFromCache(httpRequestWrapper)) {
            this.log.debug("Request is not servable from cache");
            return callBackend(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
        }
        HttpCacheEntry httpCacheEntrySatisfyFromCache = satisfyFromCache(targetHost, httpRequestWrapper);
        if (httpCacheEntrySatisfyFromCache == null) {
            this.log.debug("Cache miss");
            return handleCacheMiss(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
        }
        return handleCacheHit(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, httpCacheEntrySatisfyFromCache);
    }

    private CloseableHttpResponse handleCacheHit(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware, HttpCacheEntry httpCacheEntry) throws IOException, HttpException {
        CloseableHttpResponse closeableHttpResponseGenerateGatewayTimeout;
        HttpHost targetHost = httpClientContext.getTargetHost();
        recordCacheHit(targetHost, httpRequestWrapper);
        Date currentDate = getCurrentDate();
        if (this.suitabilityChecker.canCachedResponseBeUsed(targetHost, httpRequestWrapper, httpCacheEntry, currentDate)) {
            this.log.debug("Cache hit");
            closeableHttpResponseGenerateGatewayTimeout = generateCachedResponse(httpRequestWrapper, httpClientContext, httpCacheEntry, currentDate);
        } else if (!mayCallBackend(httpRequestWrapper)) {
            this.log.debug("Cache entry not suitable but only-if-cached requested");
            closeableHttpResponseGenerateGatewayTimeout = generateGatewayTimeout(httpClientContext);
        } else {
            if (httpCacheEntry.getStatusCode() != 304 || this.suitabilityChecker.isConditional(httpRequestWrapper)) {
                this.log.debug("Revalidating cache entry");
                return revalidateCacheEntry(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, httpCacheEntry, currentDate);
            }
            this.log.debug("Cache entry not usable; calling backend");
            return callBackend(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
        }
        httpClientContext.setAttribute("http.route", httpRoute);
        httpClientContext.setAttribute("http.target_host", targetHost);
        httpClientContext.setAttribute("http.request", httpRequestWrapper);
        httpClientContext.setAttribute("http.response", closeableHttpResponseGenerateGatewayTimeout);
        httpClientContext.setAttribute("http.request_sent", Boolean.TRUE);
        return closeableHttpResponseGenerateGatewayTimeout;
    }

    private CloseableHttpResponse revalidateCacheEntry(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware, HttpCacheEntry httpCacheEntry, Date date) throws HttpException {
        try {
            if (this.asynchRevalidator != null && !staleResponseNotAllowed(httpRequestWrapper, httpCacheEntry, date) && this.validityPolicy.mayReturnStaleWhileRevalidating(httpCacheEntry, date)) {
                this.log.trace("Serving stale with asynchronous revalidation");
                CloseableHttpResponse closeableHttpResponseGenerateCachedResponse = generateCachedResponse(httpRequestWrapper, httpClientContext, httpCacheEntry, date);
                try {
                    this.asynchRevalidator.revalidateCacheEntry(this, httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, httpCacheEntry);
                    return closeableHttpResponseGenerateCachedResponse;
                } catch (IOException unused) {
                    httpRequestWrapper = httpRequestWrapper;
                    httpClientContext = httpClientContext;
                    httpCacheEntry = httpCacheEntry;
                    return handleRevalidationFailure(httpRequestWrapper, httpClientContext, httpCacheEntry, date);
                }
            }
            return revalidateCacheEntry(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, httpCacheEntry);
        } catch (IOException unused2) {
        }
    }

    private CloseableHttpResponse handleCacheMiss(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware) throws IOException, HttpException {
        HttpHost targetHost = httpClientContext.getTargetHost();
        recordCacheMiss(targetHost, httpRequestWrapper);
        if (!mayCallBackend(httpRequestWrapper)) {
            return Proxies.enhanceResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_GATEWAY_TIMEOUT, "Gateway Timeout"));
        }
        Map<String, Variant> existingCacheVariants = getExistingCacheVariants(targetHost, httpRequestWrapper);
        if (existingCacheVariants != null && !existingCacheVariants.isEmpty()) {
            return negotiateResponseFromVariants(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, existingCacheVariants);
        }
        return callBackend(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
    }

    private HttpCacheEntry satisfyFromCache(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper) {
        try {
            return this.responseCache.getCacheEntry(httpHost, httpRequestWrapper);
        } catch (IOException e) {
            this.log.warn("Unable to retrieve entries from cache", e);
            return null;
        }
    }

    private HttpResponse getFatallyNoncompliantResponse(HttpRequestWrapper httpRequestWrapper, HttpContext httpContext) {
        HttpResponse errorForRequest = null;
        for (RequestProtocolError requestProtocolError : this.requestCompliance.requestIsFatallyNonCompliant(httpRequestWrapper)) {
            setResponseStatus(httpContext, CacheResponseStatus.CACHE_MODULE_RESPONSE);
            errorForRequest = this.requestCompliance.getErrorForRequest(requestProtocolError);
        }
        return errorForRequest;
    }

    private Map<String, Variant> getExistingCacheVariants(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper) {
        try {
            return this.responseCache.getVariantCacheEntriesWithEtags(httpHost, httpRequestWrapper);
        } catch (IOException e) {
            this.log.warn("Unable to retrieve variant entries from cache", e);
            return null;
        }
    }

    private void recordCacheMiss(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper) {
        this.cacheMisses.getAndIncrement();
        if (this.log.isTraceEnabled()) {
            RequestLine requestLine = httpRequestWrapper.getRequestLine();
            this.log.trace("Cache miss [host: " + httpHost + "; uri: " + requestLine.getUri() + "]");
        }
    }

    private void recordCacheHit(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper) {
        this.cacheHits.getAndIncrement();
        if (this.log.isTraceEnabled()) {
            RequestLine requestLine = httpRequestWrapper.getRequestLine();
            this.log.trace("Cache hit [host: " + httpHost + "; uri: " + requestLine.getUri() + "]");
        }
    }

    private void recordCacheUpdate(HttpContext httpContext) {
        this.cacheUpdates.getAndIncrement();
        setResponseStatus(httpContext, CacheResponseStatus.VALIDATED);
    }

    private void flushEntriesInvalidatedByRequest(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper) {
        try {
            this.responseCache.flushInvalidatedCacheEntriesFor(httpHost, httpRequestWrapper);
        } catch (IOException e) {
            this.log.warn("Unable to flush invalidated entries from cache", e);
        }
    }

    private CloseableHttpResponse generateCachedResponse(HttpRequestWrapper httpRequestWrapper, HttpContext httpContext, HttpCacheEntry httpCacheEntry, Date date) {
        CloseableHttpResponse closeableHttpResponseGenerateNotModifiedResponse;
        if (httpRequestWrapper.containsHeader("If-None-Match") || httpRequestWrapper.containsHeader("If-Modified-Since")) {
            closeableHttpResponseGenerateNotModifiedResponse = this.responseGenerator.generateNotModifiedResponse(httpCacheEntry);
        } else {
            closeableHttpResponseGenerateNotModifiedResponse = this.responseGenerator.generateResponse(httpRequestWrapper, httpCacheEntry);
        }
        setResponseStatus(httpContext, CacheResponseStatus.CACHE_HIT);
        if (this.validityPolicy.getStalenessSecs(httpCacheEntry, date) > 0) {
            closeableHttpResponseGenerateNotModifiedResponse.addHeader("Warning", "110 localhost \"Response is stale\"");
        }
        return closeableHttpResponseGenerateNotModifiedResponse;
    }

    private CloseableHttpResponse handleRevalidationFailure(HttpRequestWrapper httpRequestWrapper, HttpContext httpContext, HttpCacheEntry httpCacheEntry, Date date) {
        if (staleResponseNotAllowed(httpRequestWrapper, httpCacheEntry, date)) {
            return generateGatewayTimeout(httpContext);
        }
        return unvalidatedCacheHit(httpRequestWrapper, httpContext, httpCacheEntry);
    }

    private CloseableHttpResponse generateGatewayTimeout(HttpContext httpContext) {
        setResponseStatus(httpContext, CacheResponseStatus.CACHE_MODULE_RESPONSE);
        return Proxies.enhanceResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_GATEWAY_TIMEOUT, "Gateway Timeout"));
    }

    private CloseableHttpResponse unvalidatedCacheHit(HttpRequestWrapper httpRequestWrapper, HttpContext httpContext, HttpCacheEntry httpCacheEntry) {
        CloseableHttpResponse closeableHttpResponseGenerateResponse = this.responseGenerator.generateResponse(httpRequestWrapper, httpCacheEntry);
        setResponseStatus(httpContext, CacheResponseStatus.CACHE_HIT);
        closeableHttpResponseGenerateResponse.addHeader("Warning", "111 localhost \"Revalidation failed\"");
        return closeableHttpResponseGenerateResponse;
    }

    private boolean staleResponseNotAllowed(HttpRequestWrapper httpRequestWrapper, HttpCacheEntry httpCacheEntry, Date date) {
        if (this.validityPolicy.mustRevalidate(httpCacheEntry)) {
            return true;
        }
        return (this.cacheConfig.isSharedCache() && this.validityPolicy.proxyRevalidate(httpCacheEntry)) || explicitFreshnessRequest(httpRequestWrapper, httpCacheEntry, date);
    }

    private boolean mayCallBackend(HttpRequestWrapper httpRequestWrapper) {
        for (Header header : httpRequestWrapper.getHeaders("Cache-Control")) {
            for (HeaderElement headerElement : header.getElements()) {
                if ("only-if-cached".equals(headerElement.getName())) {
                    this.log.trace("Request marked only-if-cached");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean explicitFreshnessRequest(HttpRequestWrapper httpRequestWrapper, HttpCacheEntry httpCacheEntry, Date date) {
        for (Header header : httpRequestWrapper.getHeaders("Cache-Control")) {
            for (HeaderElement headerElement : header.getElements()) {
                if (HeaderConstants.CACHE_CONTROL_MAX_STALE.equals(headerElement.getName())) {
                    try {
                        if (this.validityPolicy.getCurrentAgeSecs(httpCacheEntry, date) - this.validityPolicy.getFreshnessLifetimeSecs(httpCacheEntry) > Integer.parseInt(headerElement.getValue())) {
                            return true;
                        }
                    } catch (NumberFormatException unused) {
                    }
                } else if (HeaderConstants.CACHE_CONTROL_MIN_FRESH.equals(headerElement.getName()) || "max-age".equals(headerElement.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String generateViaHeader(HttpMessage httpMessage) {
        String str;
        ProtocolVersion protocolVersion = httpMessage.getProtocolVersion();
        String str2 = this.viaHeaders.get(protocolVersion);
        if (str2 != null) {
            return str2;
        }
        VersionInfo versionInfoLoadVersionInfo = VersionInfo.loadVersionInfo("cz.msebera.android.httpclient.client", getClass().getClassLoader());
        String release = versionInfoLoadVersionInfo != null ? versionInfoLoadVersionInfo.getRelease() : VersionInfo.UNAVAILABLE;
        int major = protocolVersion.getMajor();
        int minor = protocolVersion.getMinor();
        if (HttpHost.DEFAULT_SCHEME_NAME.equalsIgnoreCase(protocolVersion.getProtocol())) {
            str = String.format("%d.%d localhost (Apache-HttpClient/%s (cache))", Integer.valueOf(major), Integer.valueOf(minor), release);
        } else {
            str = String.format("%s/%d.%d localhost (Apache-HttpClient/%s (cache))", protocolVersion.getProtocol(), Integer.valueOf(major), Integer.valueOf(minor), release);
        }
        this.viaHeaders.put(protocolVersion, str);
        return str;
    }

    private void setResponseStatus(HttpContext httpContext, CacheResponseStatus cacheResponseStatus) {
        if (httpContext != null) {
            httpContext.setAttribute(HttpCacheContext.CACHE_RESPONSE_STATUS, cacheResponseStatus);
        }
    }

    Date getCurrentDate() {
        return new Date();
    }

    boolean clientRequestsOurOptions(HttpRequest httpRequest) {
        RequestLine requestLine = httpRequest.getRequestLine();
        return "OPTIONS".equals(requestLine.getMethod()) && "*".equals(requestLine.getUri()) && "0".equals(httpRequest.getFirstHeader("Max-Forwards").getValue());
    }

    CloseableHttpResponse callBackend(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware) throws IOException, HttpException {
        Date currentDate = getCurrentDate();
        this.log.trace("Calling the backend");
        CloseableHttpResponse closeableHttpResponseExecute = this.backend.execute(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
        try {
            closeableHttpResponseExecute.addHeader("Via", generateViaHeader(closeableHttpResponseExecute));
            return handleBackendResponse(httpRequestWrapper, httpClientContext, currentDate, getCurrentDate(), closeableHttpResponseExecute);
        } catch (IOException e) {
            closeableHttpResponseExecute.close();
            throw e;
        } catch (RuntimeException e2) {
            closeableHttpResponseExecute.close();
            throw e2;
        }
    }

    private boolean revalidationResponseIsTooOld(HttpResponse httpResponse, HttpCacheEntry httpCacheEntry) {
        Header firstHeader = httpCacheEntry.getFirstHeader("Date");
        Header firstHeader2 = httpResponse.getFirstHeader("Date");
        if (firstHeader != null && firstHeader2 != null) {
            Date date = DateUtils.parseDate(firstHeader.getValue());
            Date date2 = DateUtils.parseDate(firstHeader2.getValue());
            if (date != null && date2 != null && date2.before(date)) {
                return true;
            }
        }
        return false;
    }

    CloseableHttpResponse negotiateResponseFromVariants(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware, Map<String, Variant> map) throws Throwable {
        CloseableHttpResponse closeableHttpResponse;
        HttpRequestWrapper httpRequestWrapperBuildConditionalRequestFromVariants = this.conditionalRequestBuilder.buildConditionalRequestFromVariants(httpRequestWrapper, map);
        Date currentDate = getCurrentDate();
        CloseableHttpResponse closeableHttpResponseExecute = this.backend.execute(httpRoute, httpRequestWrapperBuildConditionalRequestFromVariants, httpClientContext, httpExecutionAware);
        try {
            Date currentDate2 = getCurrentDate();
            closeableHttpResponseExecute.addHeader("Via", generateViaHeader(closeableHttpResponseExecute));
            if (closeableHttpResponseExecute.getStatusLine().getStatusCode() != 304) {
                closeableHttpResponse = closeableHttpResponseExecute;
                try {
                    return handleBackendResponse(httpRequestWrapper, httpClientContext, currentDate, currentDate2, closeableHttpResponse);
                } catch (IOException e) {
                    e = e;
                    IOException iOException = e;
                    closeableHttpResponse.close();
                    throw iOException;
                } catch (RuntimeException e2) {
                    e = e2;
                    RuntimeException runtimeException = e;
                    closeableHttpResponse.close();
                    throw runtimeException;
                }
            }
            closeableHttpResponse = closeableHttpResponseExecute;
            try {
                Header firstHeader = closeableHttpResponse.getFirstHeader("ETag");
                if (firstHeader == null) {
                    this.log.warn("304 response did not contain ETag");
                    IOUtils.consume(closeableHttpResponse.getEntity());
                    closeableHttpResponse.close();
                    return callBackend(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
                }
                Variant variant = map.get(firstHeader.getValue());
                if (variant == null) {
                    this.log.debug("304 response did not contain ETag matching one sent in If-None-Match");
                    IOUtils.consume(closeableHttpResponse.getEntity());
                    closeableHttpResponse.close();
                    return callBackend(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
                }
                HttpCacheEntry entry = variant.getEntry();
                if (revalidationResponseIsTooOld(closeableHttpResponse, entry)) {
                    IOUtils.consume(closeableHttpResponse.getEntity());
                    closeableHttpResponse.close();
                    return retryRequestUnconditionally(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware, entry);
                }
                recordCacheUpdate(httpClientContext);
                closeableHttpResponseExecute = closeableHttpResponse;
                try {
                    HttpCacheEntry updatedVariantEntry = getUpdatedVariantEntry(httpClientContext.getTargetHost(), httpRequestWrapperBuildConditionalRequestFromVariants, currentDate, currentDate2, closeableHttpResponseExecute, variant, entry);
                    closeableHttpResponseExecute.close();
                    CloseableHttpResponse closeableHttpResponseGenerateResponse = this.responseGenerator.generateResponse(httpRequestWrapper, updatedVariantEntry);
                    tryToUpdateVariantMap(httpClientContext.getTargetHost(), httpRequestWrapper, variant);
                    return shouldSendNotModifiedResponse(httpRequestWrapper, updatedVariantEntry) ? this.responseGenerator.generateNotModifiedResponse(updatedVariantEntry) : closeableHttpResponseGenerateResponse;
                } catch (IOException e3) {
                    e = e3;
                    closeableHttpResponse = closeableHttpResponseExecute;
                    IOException iOException2 = e;
                    closeableHttpResponse.close();
                    throw iOException2;
                } catch (RuntimeException e4) {
                    e = e4;
                    closeableHttpResponse = closeableHttpResponseExecute;
                    RuntimeException runtimeException2 = e;
                    closeableHttpResponse.close();
                    throw runtimeException2;
                }
            } catch (IOException e5) {
                e = e5;
                IOException iOException3 = e;
                closeableHttpResponse.close();
                throw iOException3;
            } catch (RuntimeException e6) {
                e = e6;
                RuntimeException runtimeException3 = e;
                closeableHttpResponse.close();
                throw runtimeException3;
            }
        } catch (IOException e7) {
            e = e7;
        } catch (RuntimeException e8) {
            e = e8;
        }
    }

    private CloseableHttpResponse retryRequestUnconditionally(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware, HttpCacheEntry httpCacheEntry) throws IOException, HttpException {
        return callBackend(httpRoute, this.conditionalRequestBuilder.buildUnconditionalRequest(httpRequestWrapper, httpCacheEntry), httpClientContext, httpExecutionAware);
    }

    private HttpCacheEntry getUpdatedVariantEntry(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper, Date date, Date date2, CloseableHttpResponse closeableHttpResponse, Variant variant, HttpCacheEntry httpCacheEntry) throws Throwable {
        CloseableHttpResponse closeableHttpResponse2;
        HttpCacheEntry httpCacheEntry2;
        try {
            closeableHttpResponse2 = closeableHttpResponse;
            httpCacheEntry2 = httpCacheEntry;
            try {
                try {
                    HttpCacheEntry httpCacheEntryUpdateVariantCacheEntry = this.responseCache.updateVariantCacheEntry(httpHost, httpRequestWrapper, httpCacheEntry2, closeableHttpResponse2, date, date2, variant.getCacheKey());
                    closeableHttpResponse2.close();
                    return httpCacheEntryUpdateVariantCacheEntry;
                } catch (Throwable th) {
                    th = th;
                    Throwable th2 = th;
                    closeableHttpResponse2.close();
                    throw th2;
                }
            } catch (IOException e) {
                e = e;
                this.log.warn("Could not update cache entry", e);
                closeableHttpResponse2.close();
                return httpCacheEntry2;
            }
        } catch (IOException e2) {
            e = e2;
            closeableHttpResponse2 = closeableHttpResponse;
            httpCacheEntry2 = httpCacheEntry;
        } catch (Throwable th3) {
            th = th3;
            closeableHttpResponse2 = closeableHttpResponse;
            Throwable th4 = th;
            closeableHttpResponse2.close();
            throw th4;
        }
    }

    private void tryToUpdateVariantMap(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper, Variant variant) {
        try {
            this.responseCache.reuseVariantEntryFor(httpHost, httpRequestWrapper, variant);
        } catch (IOException e) {
            this.log.warn("Could not update cache entry to reuse variant", e);
        }
    }

    private boolean shouldSendNotModifiedResponse(HttpRequestWrapper httpRequestWrapper, HttpCacheEntry httpCacheEntry) {
        return this.suitabilityChecker.isConditional(httpRequestWrapper) && this.suitabilityChecker.allConditionalsMatch(httpRequestWrapper, httpCacheEntry, new Date());
    }

    CloseableHttpResponse revalidateCacheEntry(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware, HttpCacheEntry httpCacheEntry) throws IOException, HttpException {
        CloseableHttpResponse closeableHttpResponseExecute;
        Date currentDate;
        Date date;
        HttpRequestWrapper httpRequestWrapperBuildConditionalRequest = this.conditionalRequestBuilder.buildConditionalRequest(httpRequestWrapper, httpCacheEntry);
        URI uri = httpRequestWrapperBuildConditionalRequest.getURI();
        if (uri != null) {
            try {
                httpRequestWrapperBuildConditionalRequest.setURI(URIUtils.rewriteURIForRoute(uri, httpRoute));
            } catch (URISyntaxException e) {
                throw new ProtocolException("Invalid URI: " + uri, e);
            }
        }
        Date currentDate2 = getCurrentDate();
        CloseableHttpResponse closeableHttpResponseExecute2 = this.backend.execute(httpRoute, httpRequestWrapperBuildConditionalRequest, httpClientContext, httpExecutionAware);
        Date currentDate3 = getCurrentDate();
        if (revalidationResponseIsTooOld(closeableHttpResponseExecute2, httpCacheEntry)) {
            closeableHttpResponseExecute2.close();
            HttpRequestWrapper httpRequestWrapperBuildUnconditionalRequest = this.conditionalRequestBuilder.buildUnconditionalRequest(httpRequestWrapper, httpCacheEntry);
            Date currentDate4 = getCurrentDate();
            closeableHttpResponseExecute = this.backend.execute(httpRoute, httpRequestWrapperBuildUnconditionalRequest, httpClientContext, httpExecutionAware);
            currentDate = getCurrentDate();
            date = currentDate4;
        } else {
            closeableHttpResponseExecute = closeableHttpResponseExecute2;
            currentDate = currentDate3;
            date = currentDate2;
        }
        closeableHttpResponseExecute.addHeader("Via", generateViaHeader(closeableHttpResponseExecute));
        int statusCode = closeableHttpResponseExecute.getStatusLine().getStatusCode();
        if (statusCode == 304 || statusCode == 200) {
            recordCacheUpdate(httpClientContext);
        }
        if (statusCode == 304) {
            HttpCacheEntry httpCacheEntryUpdateCacheEntry = this.responseCache.updateCacheEntry(httpClientContext.getTargetHost(), httpRequestWrapper, httpCacheEntry, closeableHttpResponseExecute, date, currentDate);
            if (this.suitabilityChecker.isConditional(httpRequestWrapper) && this.suitabilityChecker.allConditionalsMatch(httpRequestWrapper, httpCacheEntryUpdateCacheEntry, new Date())) {
                return this.responseGenerator.generateNotModifiedResponse(httpCacheEntryUpdateCacheEntry);
            }
            return this.responseGenerator.generateResponse(httpRequestWrapper, httpCacheEntryUpdateCacheEntry);
        }
        Date date2 = currentDate;
        Date date3 = date;
        CloseableHttpResponse closeableHttpResponse = closeableHttpResponseExecute;
        if (staleIfErrorAppliesTo(statusCode) && !staleResponseNotAllowed(httpRequestWrapper, httpCacheEntry, getCurrentDate()) && this.validityPolicy.mayReturnStaleIfError(httpRequestWrapper, httpCacheEntry, date2)) {
            try {
                CloseableHttpResponse closeableHttpResponseGenerateResponse = this.responseGenerator.generateResponse(httpRequestWrapper, httpCacheEntry);
                closeableHttpResponseGenerateResponse.addHeader("Warning", "110 localhost \"Response is stale\"");
                return closeableHttpResponseGenerateResponse;
            } finally {
                closeableHttpResponse.close();
            }
        }
        return handleBackendResponse(httpRequestWrapperBuildConditionalRequest, httpClientContext, date3, date2, closeableHttpResponse);
    }

    CloseableHttpResponse handleBackendResponse(HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, Date date, Date date2, CloseableHttpResponse closeableHttpResponse) throws IOException {
        this.log.trace("Handling Backend response");
        this.responseCompliance.ensureProtocolCompliance(httpRequestWrapper, closeableHttpResponse);
        HttpHost targetHost = httpClientContext.getTargetHost();
        boolean zIsResponseCacheable = this.responseCachingPolicy.isResponseCacheable(httpRequestWrapper, closeableHttpResponse);
        this.responseCache.flushInvalidatedCacheEntriesFor(targetHost, httpRequestWrapper, closeableHttpResponse);
        if (zIsResponseCacheable && !alreadyHaveNewerCacheEntry(targetHost, httpRequestWrapper, closeableHttpResponse)) {
            storeRequestIfModifiedSinceFor304Response(httpRequestWrapper, closeableHttpResponse);
            return this.responseCache.cacheAndReturnResponse(targetHost, (HttpRequest) httpRequestWrapper, closeableHttpResponse, date, date2);
        }
        if (!zIsResponseCacheable) {
            try {
                this.responseCache.flushCacheEntriesFor(targetHost, httpRequestWrapper);
                return closeableHttpResponse;
            } catch (IOException e) {
                this.log.warn("Unable to flush invalid cache entries", e);
            }
        }
        return closeableHttpResponse;
    }

    private void storeRequestIfModifiedSinceFor304Response(HttpRequest httpRequest, HttpResponse httpResponse) {
        Header firstHeader;
        if (httpResponse.getStatusLine().getStatusCode() != 304 || (firstHeader = httpRequest.getFirstHeader("If-Modified-Since")) == null) {
            return;
        }
        httpResponse.addHeader("Last-Modified", firstHeader.getValue());
    }

    private boolean alreadyHaveNewerCacheEntry(HttpHost httpHost, HttpRequestWrapper httpRequestWrapper, HttpResponse httpResponse) {
        HttpCacheEntry cacheEntry;
        Header firstHeader;
        Header firstHeader2;
        try {
            cacheEntry = this.responseCache.getCacheEntry(httpHost, httpRequestWrapper);
        } catch (IOException unused) {
            cacheEntry = null;
        }
        if (cacheEntry == null || (firstHeader = cacheEntry.getFirstHeader("Date")) == null || (firstHeader2 = httpResponse.getFirstHeader("Date")) == null) {
            return false;
        }
        Date date = DateUtils.parseDate(firstHeader.getValue());
        Date date2 = DateUtils.parseDate(firstHeader2.getValue());
        if (date == null || date2 == null) {
            return false;
        }
        return date2.before(date);
    }
}
