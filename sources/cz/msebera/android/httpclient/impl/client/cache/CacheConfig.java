package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.util.Args;

/* JADX INFO: loaded from: classes.dex */
public class CacheConfig implements Cloneable {
    public static final CacheConfig DEFAULT = new Builder().build();
    public static final boolean DEFAULT_303_CACHING_ENABLED = false;
    public static final int DEFAULT_ASYNCHRONOUS_WORKERS_CORE = 1;
    public static final int DEFAULT_ASYNCHRONOUS_WORKERS_MAX = 1;
    public static final int DEFAULT_ASYNCHRONOUS_WORKER_IDLE_LIFETIME_SECS = 60;
    public static final boolean DEFAULT_HEURISTIC_CACHING_ENABLED = false;
    public static final float DEFAULT_HEURISTIC_COEFFICIENT = 0.1f;
    public static final long DEFAULT_HEURISTIC_LIFETIME = 0;
    public static final int DEFAULT_MAX_CACHE_ENTRIES = 1000;
    public static final int DEFAULT_MAX_OBJECT_SIZE_BYTES = 8192;
    public static final int DEFAULT_MAX_UPDATE_RETRIES = 1;
    public static final int DEFAULT_REVALIDATION_QUEUE_SIZE = 100;
    public static final boolean DEFAULT_WEAK_ETAG_ON_PUTDELETE_ALLOWED = false;
    private boolean allow303Caching;
    private int asynchronousWorkerIdleLifetimeSecs;
    private int asynchronousWorkersCore;
    private int asynchronousWorkersMax;
    private boolean heuristicCachingEnabled;
    private float heuristicCoefficient;
    private long heuristicDefaultLifetime;
    private boolean isSharedCache;
    private int maxCacheEntries;
    private long maxObjectSize;
    private int maxUpdateRetries;
    private boolean neverCacheHTTP10ResponsesWithQuery;
    private int revalidationQueueSize;
    private boolean weakETagOnPutDeleteAllowed;

    @Deprecated
    public CacheConfig() {
        this.maxObjectSize = 8192L;
        this.maxCacheEntries = 1000;
        this.maxUpdateRetries = 1;
        this.allow303Caching = false;
        this.weakETagOnPutDeleteAllowed = false;
        this.heuristicCachingEnabled = false;
        this.heuristicCoefficient = 0.1f;
        this.heuristicDefaultLifetime = 0L;
        this.isSharedCache = true;
        this.asynchronousWorkersMax = 1;
        this.asynchronousWorkersCore = 1;
        this.asynchronousWorkerIdleLifetimeSecs = 60;
        this.revalidationQueueSize = 100;
    }

    CacheConfig(long j, int i, int i2, boolean z, boolean z2, boolean z3, float f, long j2, boolean z4, int i3, int i4, int i5, int i6, boolean z5) {
        this.maxObjectSize = j;
        this.maxCacheEntries = i;
        this.maxUpdateRetries = i2;
        this.allow303Caching = z;
        this.weakETagOnPutDeleteAllowed = z2;
        this.heuristicCachingEnabled = z3;
        this.heuristicCoefficient = f;
        this.heuristicDefaultLifetime = j2;
        this.isSharedCache = z4;
        this.asynchronousWorkersMax = i3;
        this.asynchronousWorkersCore = i4;
        this.asynchronousWorkerIdleLifetimeSecs = i5;
        this.revalidationQueueSize = i6;
    }

    @Deprecated
    public int getMaxObjectSizeBytes() {
        long j = this.maxObjectSize;
        if (j > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int) j;
    }

    @Deprecated
    public void setMaxObjectSizeBytes(int i) {
        if (i > Integer.MAX_VALUE) {
            this.maxObjectSize = 2147483647L;
        } else {
            this.maxObjectSize = i;
        }
    }

    public long getMaxObjectSize() {
        return this.maxObjectSize;
    }

    @Deprecated
    public void setMaxObjectSize(long j) {
        this.maxObjectSize = j;
    }

    public boolean isNeverCacheHTTP10ResponsesWithQuery() {
        return this.neverCacheHTTP10ResponsesWithQuery;
    }

    public int getMaxCacheEntries() {
        return this.maxCacheEntries;
    }

    @Deprecated
    public void setMaxCacheEntries(int i) {
        this.maxCacheEntries = i;
    }

    public int getMaxUpdateRetries() {
        return this.maxUpdateRetries;
    }

    @Deprecated
    public void setMaxUpdateRetries(int i) {
        this.maxUpdateRetries = i;
    }

    public boolean is303CachingEnabled() {
        return this.allow303Caching;
    }

    public boolean isWeakETagOnPutDeleteAllowed() {
        return this.weakETagOnPutDeleteAllowed;
    }

    public boolean isHeuristicCachingEnabled() {
        return this.heuristicCachingEnabled;
    }

    @Deprecated
    public void setHeuristicCachingEnabled(boolean z) {
        this.heuristicCachingEnabled = z;
    }

    public float getHeuristicCoefficient() {
        return this.heuristicCoefficient;
    }

    @Deprecated
    public void setHeuristicCoefficient(float f) {
        this.heuristicCoefficient = f;
    }

    public long getHeuristicDefaultLifetime() {
        return this.heuristicDefaultLifetime;
    }

    @Deprecated
    public void setHeuristicDefaultLifetime(long j) {
        this.heuristicDefaultLifetime = j;
    }

    public boolean isSharedCache() {
        return this.isSharedCache;
    }

    @Deprecated
    public void setSharedCache(boolean z) {
        this.isSharedCache = z;
    }

    public int getAsynchronousWorkersMax() {
        return this.asynchronousWorkersMax;
    }

    @Deprecated
    public void setAsynchronousWorkersMax(int i) {
        this.asynchronousWorkersMax = i;
    }

    public int getAsynchronousWorkersCore() {
        return this.asynchronousWorkersCore;
    }

    @Deprecated
    public void setAsynchronousWorkersCore(int i) {
        this.asynchronousWorkersCore = i;
    }

    public int getAsynchronousWorkerIdleLifetimeSecs() {
        return this.asynchronousWorkerIdleLifetimeSecs;
    }

    @Deprecated
    public void setAsynchronousWorkerIdleLifetimeSecs(int i) {
        this.asynchronousWorkerIdleLifetimeSecs = i;
    }

    public int getRevalidationQueueSize() {
        return this.revalidationQueueSize;
    }

    @Deprecated
    public void setRevalidationQueueSize(int i) {
        this.revalidationQueueSize = i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
    public CacheConfig m82clone() throws CloneNotSupportedException {
        return (CacheConfig) super.clone();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(CacheConfig cacheConfig) {
        Args.notNull(cacheConfig, "Cache config");
        return new Builder().setMaxObjectSize(cacheConfig.getMaxObjectSize()).setMaxCacheEntries(cacheConfig.getMaxCacheEntries()).setMaxUpdateRetries(cacheConfig.getMaxUpdateRetries()).setHeuristicCachingEnabled(cacheConfig.isHeuristicCachingEnabled()).setHeuristicCoefficient(cacheConfig.getHeuristicCoefficient()).setHeuristicDefaultLifetime(cacheConfig.getHeuristicDefaultLifetime()).setSharedCache(cacheConfig.isSharedCache()).setAsynchronousWorkersMax(cacheConfig.getAsynchronousWorkersMax()).setAsynchronousWorkersCore(cacheConfig.getAsynchronousWorkersCore()).setAsynchronousWorkerIdleLifetimeSecs(cacheConfig.getAsynchronousWorkerIdleLifetimeSecs()).setRevalidationQueueSize(cacheConfig.getRevalidationQueueSize()).setNeverCacheHTTP10ResponsesWithQueryString(cacheConfig.isNeverCacheHTTP10ResponsesWithQuery());
    }

    public static class Builder {
        private boolean neverCacheHTTP10ResponsesWithQuery;
        private long maxObjectSize = 8192;
        private int maxCacheEntries = 1000;
        private int maxUpdateRetries = 1;
        private boolean allow303Caching = false;
        private boolean weakETagOnPutDeleteAllowed = false;
        private boolean heuristicCachingEnabled = false;
        private float heuristicCoefficient = 0.1f;
        private long heuristicDefaultLifetime = 0;
        private boolean isSharedCache = true;
        private int asynchronousWorkersMax = 1;
        private int asynchronousWorkersCore = 1;
        private int asynchronousWorkerIdleLifetimeSecs = 60;
        private int revalidationQueueSize = 100;

        Builder() {
        }

        public Builder setMaxObjectSize(long j) {
            this.maxObjectSize = j;
            return this;
        }

        public Builder setMaxCacheEntries(int i) {
            this.maxCacheEntries = i;
            return this;
        }

        public Builder setMaxUpdateRetries(int i) {
            this.maxUpdateRetries = i;
            return this;
        }

        public Builder setAllow303Caching(boolean z) {
            this.allow303Caching = z;
            return this;
        }

        public Builder setWeakETagOnPutDeleteAllowed(boolean z) {
            this.weakETagOnPutDeleteAllowed = z;
            return this;
        }

        public Builder setHeuristicCachingEnabled(boolean z) {
            this.heuristicCachingEnabled = z;
            return this;
        }

        public Builder setHeuristicCoefficient(float f) {
            this.heuristicCoefficient = f;
            return this;
        }

        public Builder setHeuristicDefaultLifetime(long j) {
            this.heuristicDefaultLifetime = j;
            return this;
        }

        public Builder setSharedCache(boolean z) {
            this.isSharedCache = z;
            return this;
        }

        public Builder setAsynchronousWorkersMax(int i) {
            this.asynchronousWorkersMax = i;
            return this;
        }

        public Builder setAsynchronousWorkersCore(int i) {
            this.asynchronousWorkersCore = i;
            return this;
        }

        public Builder setAsynchronousWorkerIdleLifetimeSecs(int i) {
            this.asynchronousWorkerIdleLifetimeSecs = i;
            return this;
        }

        public Builder setRevalidationQueueSize(int i) {
            this.revalidationQueueSize = i;
            return this;
        }

        public Builder setNeverCacheHTTP10ResponsesWithQueryString(boolean z) {
            this.neverCacheHTTP10ResponsesWithQuery = z;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(this.maxObjectSize, this.maxCacheEntries, this.maxUpdateRetries, this.allow303Caching, this.weakETagOnPutDeleteAllowed, this.heuristicCachingEnabled, this.heuristicCoefficient, this.heuristicDefaultLifetime, this.isSharedCache, this.asynchronousWorkersMax, this.asynchronousWorkersCore, this.asynchronousWorkerIdleLifetimeSecs, this.revalidationQueueSize, this.neverCacheHTTP10ResponsesWithQuery);
        }
    }

    public String toString() {
        return "[maxObjectSize=" + this.maxObjectSize + ", maxCacheEntries=" + this.maxCacheEntries + ", maxUpdateRetries=" + this.maxUpdateRetries + ", 303CachingEnabled=" + this.allow303Caching + ", weakETagOnPutDeleteAllowed=" + this.weakETagOnPutDeleteAllowed + ", heuristicCachingEnabled=" + this.heuristicCachingEnabled + ", heuristicCoefficient=" + this.heuristicCoefficient + ", heuristicDefaultLifetime=" + this.heuristicDefaultLifetime + ", isSharedCache=" + this.isSharedCache + ", asynchronousWorkersMax=" + this.asynchronousWorkersMax + ", asynchronousWorkersCore=" + this.asynchronousWorkersCore + ", asynchronousWorkerIdleLifetimeSecs=" + this.asynchronousWorkerIdleLifetimeSecs + ", revalidationQueueSize=" + this.revalidationQueueSize + ", neverCacheHTTP10ResponsesWithQuery=" + this.neverCacheHTTP10ResponsesWithQuery + "]";
    }
}
