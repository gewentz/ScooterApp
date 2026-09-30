package cz.msebera.android.httpclient.client.entity;

import cz.msebera.android.httpclient.HttpEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/* JADX INFO: loaded from: classes.dex */
public class GzipDecompressingEntity extends DecompressingEntity {
    public GzipDecompressingEntity(HttpEntity httpEntity) {
        super(httpEntity, new InputStreamFactory() { // from class: cz.msebera.android.httpclient.client.entity.GzipDecompressingEntity.1
            @Override // cz.msebera.android.httpclient.client.entity.InputStreamFactory
            public InputStream create(InputStream inputStream) throws IOException {
                return new GZIPInputStream(inputStream);
            }
        });
    }
}
