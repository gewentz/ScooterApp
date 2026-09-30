package cz.msebera.android.httpclient.impl.client.cache;

import cz.msebera.android.httpclient.client.cache.HttpCacheEntry;
import cz.msebera.android.httpclient.client.cache.HttpCacheStorage;
import cz.msebera.android.httpclient.client.cache.HttpCacheUpdateCallback;
import cz.msebera.android.httpclient.util.Args;
import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: loaded from: classes.dex */
public class ManagedHttpCacheStorage implements HttpCacheStorage, Closeable {
    private final CacheMap entries;
    private final ReferenceQueue<HttpCacheEntry> morque = new ReferenceQueue<>();
    private final Set<ResourceReference> resources = new HashSet();
    private final AtomicBoolean active = new AtomicBoolean(true);

    public ManagedHttpCacheStorage(CacheConfig cacheConfig) {
        this.entries = new CacheMap(cacheConfig.getMaxCacheEntries());
    }

    private void ensureValidState() throws IllegalStateException {
        if (!this.active.get()) {
            throw new IllegalStateException("Cache has been shut down");
        }
    }

    private void keepResourceReference(HttpCacheEntry httpCacheEntry) {
        if (httpCacheEntry.getResource() != null) {
            this.resources.add(new ResourceReference(httpCacheEntry, this.morque));
        }
    }

    @Override // cz.msebera.android.httpclient.client.cache.HttpCacheStorage
    public void putEntry(String str, HttpCacheEntry httpCacheEntry) throws IOException {
        Args.notNull(str, "URL");
        Args.notNull(httpCacheEntry, "Cache entry");
        ensureValidState();
        synchronized (this) {
            this.entries.put(str, httpCacheEntry);
            keepResourceReference(httpCacheEntry);
        }
    }

    @Override // cz.msebera.android.httpclient.client.cache.HttpCacheStorage
    public HttpCacheEntry getEntry(String str) throws IOException {
        HttpCacheEntry httpCacheEntry;
        Args.notNull(str, "URL");
        ensureValidState();
        synchronized (this) {
            httpCacheEntry = this.entries.get(str);
        }
        return httpCacheEntry;
    }

    @Override // cz.msebera.android.httpclient.client.cache.HttpCacheStorage
    public void removeEntry(String str) throws IOException {
        Args.notNull(str, "URL");
        ensureValidState();
        synchronized (this) {
            this.entries.remove(str);
        }
    }

    @Override // cz.msebera.android.httpclient.client.cache.HttpCacheStorage
    public void updateEntry(String str, HttpCacheUpdateCallback httpCacheUpdateCallback) throws IOException {
        Args.notNull(str, "URL");
        Args.notNull(httpCacheUpdateCallback, "Callback");
        ensureValidState();
        synchronized (this) {
            HttpCacheEntry httpCacheEntry = this.entries.get(str);
            HttpCacheEntry httpCacheEntryUpdate = httpCacheUpdateCallback.update(httpCacheEntry);
            this.entries.put(str, httpCacheEntryUpdate);
            if (httpCacheEntry != httpCacheEntryUpdate) {
                keepResourceReference(httpCacheEntryUpdate);
            }
        }
    }

    public void cleanResources() {
        if (!this.active.get()) {
            return;
        }
        while (true) {
            ResourceReference resourceReference = (ResourceReference) this.morque.poll();
            if (resourceReference == null) {
                return;
            }
            synchronized (this) {
                this.resources.remove(resourceReference);
            }
            resourceReference.getResource().dispose();
        }
    }

    public void shutdown() {
        if (this.active.compareAndSet(true, false)) {
            synchronized (this) {
                this.entries.clear();
                Iterator<ResourceReference> it = this.resources.iterator();
                while (it.hasNext()) {
                    it.next().getResource().dispose();
                }
                this.resources.clear();
                while (this.morque.poll() != null) {
                }
            }
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (this.active.compareAndSet(true, false)) {
            synchronized (this) {
                while (true) {
                    ResourceReference resourceReference = (ResourceReference) this.morque.poll();
                    if (resourceReference != null) {
                        this.resources.remove(resourceReference);
                        resourceReference.getResource().dispose();
                    }
                }
            }
        }
    }
}
