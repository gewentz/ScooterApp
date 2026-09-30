package cz.msebera.android.httpclient.client.entity;

import cz.msebera.android.httpclient.HttpEntity;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class DeflateDecompressingEntity extends DecompressingEntity {
    public DeflateDecompressingEntity(HttpEntity httpEntity) {
        super(httpEntity, new InputStreamFactory() { // from class: cz.msebera.android.httpclient.client.entity.DeflateDecompressingEntity.1
            @Override // cz.msebera.android.httpclient.client.entity.InputStreamFactory
            public InputStream create(InputStream inputStream) throws IOException {
                return new DeflateInputStream(inputStream);
            }
        });
    }
}
