package cz.msebera.android.httpclient.conn.util;

import cz.msebera.android.httpclient.Consts;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.util.Args;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public final class PublicSuffixMatcherLoader {
    private static volatile PublicSuffixMatcher DEFAULT_INSTANCE;

    private static PublicSuffixMatcher load(InputStream inputStream) throws IOException {
        PublicSuffixList publicSuffixList = new PublicSuffixListParser().parse(new InputStreamReader(inputStream, Consts.UTF_8));
        return new PublicSuffixMatcher(publicSuffixList.getRules(), publicSuffixList.getExceptions());
    }

    public static PublicSuffixMatcher load(URL url) throws IOException {
        Args.notNull(url, "URL");
        InputStream inputStreamOpenStream = url.openStream();
        try {
            return load(inputStreamOpenStream);
        } finally {
            inputStreamOpenStream.close();
        }
    }

    public static PublicSuffixMatcher load(File file) throws IOException {
        Args.notNull(file, "File");
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            return load(fileInputStream);
        } finally {
            fileInputStream.close();
        }
    }

    public static PublicSuffixMatcher getDefault() {
        if (DEFAULT_INSTANCE == null) {
            synchronized (PublicSuffixMatcherLoader.class) {
                if (DEFAULT_INSTANCE == null) {
                    URL resource = PublicSuffixMatcherLoader.class.getResource("/mozilla/public-suffix-list.txt");
                    if (resource != null) {
                        try {
                            DEFAULT_INSTANCE = load(resource);
                        } catch (IOException e) {
                            HttpClientAndroidLog httpClientAndroidLog = new HttpClientAndroidLog(PublicSuffixMatcherLoader.class);
                            if (httpClientAndroidLog.isWarnEnabled()) {
                                httpClientAndroidLog.warn("Failure loading public suffix list from default resource", e);
                            }
                        }
                    } else {
                        DEFAULT_INSTANCE = new PublicSuffixMatcher(Arrays.asList("com"), null);
                    }
                }
            }
        }
        return DEFAULT_INSTANCE;
    }
}
