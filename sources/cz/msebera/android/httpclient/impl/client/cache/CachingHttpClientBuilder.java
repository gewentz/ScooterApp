package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.client.cache.HttpCacheInvalidator;
import cz.msebera.android.httpclient.client.cache.HttpCacheStorage;
import cz.msebera.android.httpclient.client.cache.ResourceFactory;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.execchain.ClientExecChain;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class CachingHttpClientBuilder extends HttpClientBuilder {
    private CacheConfig cacheConfig;
    private File cacheDir;
    private boolean deleteCache = true;
    private HttpCacheInvalidator httpCacheInvalidator;
    private ResourceFactory resourceFactory;
    private SchedulingStrategy schedulingStrategy;
    private HttpCacheStorage storage;

    public static CachingHttpClientBuilder create() {
        return new CachingHttpClientBuilder();
    }

    protected CachingHttpClientBuilder() {
    }

    public final CachingHttpClientBuilder setResourceFactory(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheStorage(HttpCacheStorage httpCacheStorage) {
        this.storage = httpCacheStorage;
        return this;
    }

    public final CachingHttpClientBuilder setCacheDir(File file) {
        this.cacheDir = file;
        return this;
    }

    public final CachingHttpClientBuilder setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    public final CachingHttpClientBuilder setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheInvalidator(HttpCacheInvalidator httpCacheInvalidator) {
        this.httpCacheInvalidator = httpCacheInvalidator;
        return this;
    }

    public CachingHttpClientBuilder setDeleteCache(boolean z) {
        this.deleteCache = z;
        return this;
    }

    @Override // cz.msebera.android.httpclient.impl.client.HttpClientBuilder
    protected ClientExecChain decorateMainExec(ClientExecChain clientExecChain) {
        CacheConfig cacheConfig = this.cacheConfig;
        if (cacheConfig == null) {
            cacheConfig = CacheConfig.DEFAULT;
        }
        CacheConfig cacheConfig2 = cacheConfig;
        ResourceFactory fileResourceFactory = this.resourceFactory;
        if (fileResourceFactory == null) {
            if (this.cacheDir == null) {
                fileResourceFactory = new HeapResourceFactory();
            } else {
                fileResourceFactory = new FileResourceFactory(this.cacheDir);
            }
        }
        ResourceFactory resourceFactory = fileResourceFactory;
        HttpCacheStorage httpCacheStorage = this.storage;
        HttpCacheStorage basicHttpCacheStorage = httpCacheStorage;
        if (httpCacheStorage == null) {
            if (this.cacheDir == null) {
                basicHttpCacheStorage = new BasicHttpCacheStorage(cacheConfig2);
            } else {
                final ManagedHttpCacheStorage managedHttpCacheStorage = new ManagedHttpCacheStorage(cacheConfig2);
                if (this.deleteCache) {
                    addCloseable(new Closeable() { // from class: cz.msebera.android.httpclient.impl.client.cache.CachingHttpClientBuilder.1
                        @Override // java.io.Closeable, java.lang.AutoCloseable
                        public void close() throws IOException {
                            managedHttpCacheStorage.shutdown();
                        }
                    });
                    basicHttpCacheStorage = managedHttpCacheStorage;
                } else {
                    addCloseable(managedHttpCacheStorage);
                    basicHttpCacheStorage = managedHttpCacheStorage;
                }
            }
        }
        HttpCacheStorage httpCacheStorage2 = basicHttpCacheStorage;
        AsynchronousValidator asynchronousValidatorCreateAsynchronousRevalidator = createAsynchronousRevalidator(cacheConfig2);
        CacheKeyGenerator cacheKeyGenerator = new CacheKeyGenerator();
        HttpCacheInvalidator cacheInvalidator = this.httpCacheInvalidator;
        if (cacheInvalidator == null) {
            cacheInvalidator = new CacheInvalidator(cacheKeyGenerator, httpCacheStorage2);
        }
        return new CachingExec(clientExecChain, new BasicHttpCache(resourceFactory, httpCacheStorage2, cacheConfig2, cacheKeyGenerator, cacheInvalidator), cacheConfig2, asynchronousValidatorCreateAsynchronousRevalidator);
    }

    private AsynchronousValidator createAsynchronousRevalidator(CacheConfig cacheConfig) {
        if (cacheConfig.getAsynchronousWorkersMax() <= 0) {
            return null;
        }
        AsynchronousValidator asynchronousValidator = new AsynchronousValidator(createSchedulingStrategy(cacheConfig));
        addCloseable(asynchronousValidator);
        return asynchronousValidator;
    }

    private SchedulingStrategy createSchedulingStrategy(CacheConfig cacheConfig) {
        SchedulingStrategy schedulingStrategy = this.schedulingStrategy;
        return schedulingStrategy != null ? schedulingStrategy : new ImmediateSchedulingStrategy(cacheConfig);
    }
}
