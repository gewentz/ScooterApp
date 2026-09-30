package cz.msebera.android.httpclient.conn.ssl;

import cz.msebera.android.httpclient.conn.util.InetAddressUtils;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.util.Args;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public abstract class AbstractVerifier implements X509HostnameVerifier {
    static final String[] BAD_COUNTRY_2LDS;
    public HttpClientAndroidLog log = new HttpClientAndroidLog(getClass());

    static {
        String[] strArr = {"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org"};
        BAD_COUNTRY_2LDS = strArr;
        Arrays.sort(strArr);
    }

    @Override // cz.msebera.android.httpclient.conn.ssl.X509HostnameVerifier
    public final void verify(String str, SSLSocket sSLSocket) throws IOException {
        Args.notNull(str, "Host");
        SSLSession session = sSLSocket.getSession();
        if (session == null) {
            sSLSocket.getInputStream().available();
            session = sSLSocket.getSession();
            if (session == null) {
                sSLSocket.startHandshake();
                session = sSLSocket.getSession();
            }
        }
        verify(str, (X509Certificate) session.getPeerCertificates()[0]);
    }

    @Override // javax.net.ssl.HostnameVerifier
    public final boolean verify(String str, SSLSession sSLSession) {
        try {
            verify(str, (X509Certificate) sSLSession.getPeerCertificates()[0]);
            return true;
        } catch (SSLException e) {
            if (this.log.isDebugEnabled()) {
                this.log.debug(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override // cz.msebera.android.httpclient.conn.ssl.X509HostnameVerifier
    public final void verify(String str, X509Certificate x509Certificate) throws SSLException {
        List<String> listExtractSubjectAlts = DefaultHostnameVerifier.extractSubjectAlts(x509Certificate, (InetAddressUtils.isIPv4Address(str) || InetAddressUtils.isIPv6Address(str)) ? 7 : 2);
        String strFindMostSpecific = new DistinguishedNameParser(x509Certificate.getSubjectX500Principal()).findMostSpecific("cn");
        String[] strArr = null;
        String[] strArr2 = strFindMostSpecific != null ? new String[]{strFindMostSpecific} : null;
        if (listExtractSubjectAlts != null && !listExtractSubjectAlts.isEmpty()) {
            strArr = (String[]) listExtractSubjectAlts.toArray(new String[listExtractSubjectAlts.size()]);
        }
        verify(str, strArr2, strArr);
    }

    public final void verify(String str, String[] strArr, String[] strArr2, boolean z) throws SSLException {
        List<String> listAsList = null;
        String str2 = (strArr == null || strArr.length <= 0) ? null : strArr[0];
        if (strArr2 != null && strArr2.length > 0) {
            listAsList = Arrays.asList(strArr2);
        }
        String strNormaliseAddress = InetAddressUtils.isIPv6Address(str) ? DefaultHostnameVerifier.normaliseAddress(str.toLowerCase(Locale.ROOT)) : str;
        if (listAsList != null) {
            for (String strNormaliseAddress2 : listAsList) {
                if (InetAddressUtils.isIPv6Address(strNormaliseAddress2)) {
                    strNormaliseAddress2 = DefaultHostnameVerifier.normaliseAddress(strNormaliseAddress2);
                }
                if (matchIdentity(strNormaliseAddress, strNormaliseAddress2, z)) {
                    return;
                }
            }
            throw new SSLException("Certificate for <" + str + "> doesn't match any of the subject alternative names: " + listAsList);
        }
        if (str2 != null) {
            if (matchIdentity(strNormaliseAddress, InetAddressUtils.isIPv6Address(str2) ? DefaultHostnameVerifier.normaliseAddress(str2) : str2, z)) {
                return;
            }
            throw new SSLException("Certificate for <" + str + "> doesn't match common name of the certificate subject: " + str2);
        }
        throw new SSLException("Certificate subject for <" + str + "> doesn't contain a common name and does not have alternative names");
    }

    private static boolean matchIdentity(String str, String str2, boolean z) {
        boolean zEndsWith;
        if (str == null) {
            return false;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        String lowerCase2 = str2.toLowerCase(Locale.ROOT);
        String[] strArrSplit = lowerCase2.split("\\.");
        if (strArrSplit.length >= 3 && strArrSplit[0].endsWith("*") && (!z || validCountryWildcard(strArrSplit))) {
            String str3 = strArrSplit[0];
            if (str3.length() > 1) {
                String strSubstring = str3.substring(0, str3.length() - 1);
                zEndsWith = lowerCase.startsWith(strSubstring) && lowerCase.substring(strSubstring.length()).endsWith(lowerCase2.substring(str3.length()));
            } else {
                zEndsWith = lowerCase.endsWith(lowerCase2.substring(1));
            }
            return zEndsWith && (!z || countDots(lowerCase) == countDots(lowerCase2));
        }
        return lowerCase.equals(lowerCase2);
    }

    private static boolean validCountryWildcard(String[] strArr) {
        return (strArr.length == 3 && strArr[2].length() == 2 && Arrays.binarySearch(BAD_COUNTRY_2LDS, strArr[1]) >= 0) ? false : true;
    }

    public static boolean acceptableCountryWildcard(String str) {
        return validCountryWildcard(str.split("\\."));
    }

    public static String[] getCNs(X509Certificate x509Certificate) {
        String strFindMostSpecific = new DistinguishedNameParser(x509Certificate.getSubjectX500Principal()).findMostSpecific("cn");
        if (strFindMostSpecific != null) {
            return new String[]{strFindMostSpecific};
        }
        return null;
    }

    public static String[] getDNSSubjectAlts(X509Certificate x509Certificate) {
        List<String> listExtractSubjectAlts = DefaultHostnameVerifier.extractSubjectAlts(x509Certificate, 2);
        if (listExtractSubjectAlts == null || listExtractSubjectAlts.isEmpty()) {
            return null;
        }
        return (String[]) listExtractSubjectAlts.toArray(new String[listExtractSubjectAlts.size()]);
    }

    public static int countDots(String str) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == '.') {
                i++;
            }
        }
        return i;
    }
}
