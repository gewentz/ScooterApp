package cz.msebera.android.httpclient.client.protocol;

import com.loopj.android.http.AsyncHttpClient;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpResponseInterceptor;
import cz.msebera.android.httpclient.client.entity.DecompressingEntity;
import cz.msebera.android.httpclient.client.entity.DeflateInputStream;
import cz.msebera.android.httpclient.client.entity.InputStreamFactory;
import cz.msebera.android.httpclient.config.Lookup;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.protocol.HttpContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/* JADX INFO: loaded from: classes.dex */
public class ResponseContentEncoding implements HttpResponseInterceptor {
    public static final String UNCOMPRESSED = "http.client.response.uncompressed";
    private final Lookup<InputStreamFactory> decoderRegistry;
    private static final InputStreamFactory GZIP = new InputStreamFactory() { // from class: cz.msebera.android.httpclient.client.protocol.ResponseContentEncoding.1
        @Override // cz.msebera.android.httpclient.client.entity.InputStreamFactory
        public InputStream create(InputStream inputStream) throws IOException {
            return new GZIPInputStream(inputStream);
        }
    };
    private static final InputStreamFactory DEFLATE = new InputStreamFactory() { // from class: cz.msebera.android.httpclient.client.protocol.ResponseContentEncoding.2
        @Override // cz.msebera.android.httpclient.client.entity.InputStreamFactory
        public InputStream create(InputStream inputStream) throws IOException {
            return new DeflateInputStream(inputStream);
        }
    };

    public ResponseContentEncoding(Lookup<InputStreamFactory> lookup) {
        if (lookup == null) {
            RegistryBuilder registryBuilderCreate = RegistryBuilder.create();
            InputStreamFactory inputStreamFactory = GZIP;
            lookup = registryBuilderCreate.register(AsyncHttpClient.ENCODING_GZIP, inputStreamFactory).register("x-gzip", inputStreamFactory).register("deflate", DEFLATE).build();
        }
        this.decoderRegistry = lookup;
    }

    public ResponseContentEncoding() {
        this(null);
    }

    @Override // cz.msebera.android.httpclient.HttpResponseInterceptor
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        Header contentEncoding;
        HttpEntity entity = httpResponse.getEntity();
        if (!HttpClientContext.adapt(httpContext).getRequestConfig().isDecompressionEnabled() || entity == null || entity.getContentLength() == 0 || (contentEncoding = entity.getContentEncoding()) == null) {
            return;
        }
        for (HeaderElement headerElement : contentEncoding.getElements()) {
            String lowerCase = headerElement.getName().toLowerCase(Locale.ROOT);
            InputStreamFactory inputStreamFactoryLookup = this.decoderRegistry.lookup(lowerCase);
            if (inputStreamFactoryLookup != null) {
                httpResponse.setEntity(new DecompressingEntity(httpResponse.getEntity(), inputStreamFactoryLookup));
                httpResponse.removeHeaders("Content-Length");
                httpResponse.removeHeaders("Content-Encoding");
                httpResponse.removeHeaders(HttpHeaders.CONTENT_MD5);
            } else if (!HTTP.IDENTITY_CODING.equals(lowerCase)) {
                throw new HttpException("Unsupported Content-Coding: " + headerElement.getName());
            }
        }
    }
}
