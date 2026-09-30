package com.loopj.android.http;

import android.text.TextUtils;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class JsonStreamerEntity implements HttpEntity {
    private static final int BUFFER_SIZE = 4096;
    private static final String LOG_TAG = "JsonStreamerEntity";
    private final Header contentEncoding;
    private final byte[] elapsedField;
    private final ResponseHandlerInterface progressHandler;
    private static final UnsupportedOperationException ERR_UNSUPPORTED = new UnsupportedOperationException("Unsupported operation in this implementation.");
    private static final byte[] JSON_TRUE = "true".getBytes();
    private static final byte[] JSON_FALSE = "false".getBytes();
    private static final byte[] JSON_NULL = "null".getBytes();
    private static final byte[] STREAM_NAME = escape("name");
    private static final byte[] STREAM_TYPE = escape("type");
    private static final byte[] STREAM_CONTENTS = escape("contents");
    private static final Header HEADER_JSON_CONTENT = new BasicHeader("Content-Type", RequestParams.APPLICATION_JSON);
    private static final Header HEADER_GZIP_ENCODING = new BasicHeader("Content-Encoding", AsyncHttpClient.ENCODING_GZIP);
    private final byte[] buffer = new byte[4096];
    private final Map<String, Object> jsonParams = new HashMap();

    @Override // cz.msebera.android.httpclient.HttpEntity
    public void consumeContent() throws UnsupportedOperationException, IOException {
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public long getContentLength() {
        return -1L;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public boolean isChunked() {
        return false;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public boolean isRepeatable() {
        return false;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public boolean isStreaming() {
        return false;
    }

    public JsonStreamerEntity(ResponseHandlerInterface responseHandlerInterface, boolean z, String str) {
        this.progressHandler = responseHandlerInterface;
        this.contentEncoding = z ? HEADER_GZIP_ENCODING : null;
        this.elapsedField = TextUtils.isEmpty(str) ? null : escape(str);
    }

    static byte[] escape(String str) {
        if (str == null) {
            return JSON_NULL;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append('\"');
        int length = str.length();
        int i = -1;
        while (true) {
            i++;
            if (i < length) {
                char cCharAt = str.charAt(i);
                if (cCharAt == '\f') {
                    sb.append("\\f");
                } else if (cCharAt == '\r') {
                    sb.append("\\r");
                } else if (cCharAt == '\"') {
                    sb.append("\\\"");
                } else if (cCharAt == '\\') {
                    sb.append("\\\\");
                } else {
                    switch (cCharAt) {
                        case '\b':
                            sb.append("\\b");
                            break;
                        case '\t':
                            sb.append("\\t");
                            break;
                        case '\n':
                            sb.append("\\n");
                            break;
                        default:
                            if (cCharAt <= 31 || ((cCharAt >= 127 && cCharAt <= 159) || (cCharAt >= 8192 && cCharAt <= 8447))) {
                                String hexString = Integer.toHexString(cCharAt);
                                sb.append("\\u");
                                int length2 = 4 - hexString.length();
                                for (int i2 = 0; i2 < length2; i2++) {
                                    sb.append('0');
                                }
                                sb.append(hexString.toUpperCase(Locale.US));
                            } else {
                                sb.append(cCharAt);
                            }
                            break;
                    }
                }
            } else {
                sb.append('\"');
                return sb.toString().getBytes();
            }
        }
    }

    public void addPart(String str, Object obj) {
        this.jsonParams.put(str, obj);
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public Header getContentEncoding() {
        return this.contentEncoding;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public Header getContentType() {
        return HEADER_JSON_CONTENT;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public InputStream getContent() throws UnsupportedOperationException, IOException {
        throw ERR_UNSUPPORTED;
    }

    @Override // cz.msebera.android.httpclient.HttpEntity
    public void writeTo(OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new IllegalStateException("Output stream cannot be null.");
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (this.contentEncoding != null) {
            outputStream = new GZIPOutputStream(outputStream, 4096);
        }
        outputStream.write(123);
        Set<String> setKeySet = this.jsonParams.keySet();
        int size = setKeySet.size();
        if (size > 0) {
            int i = 0;
            for (String str : setKeySet) {
                i++;
                try {
                    Object obj = this.jsonParams.get(str);
                    outputStream.write(escape(str));
                    outputStream.write(58);
                    if (obj == null) {
                        outputStream.write(JSON_NULL);
                    } else {
                        boolean z = obj instanceof RequestParams.FileWrapper;
                        if (z || (obj instanceof RequestParams.StreamWrapper)) {
                            outputStream.write(123);
                            if (z) {
                                writeToFromFile(outputStream, (RequestParams.FileWrapper) obj);
                            } else {
                                writeToFromStream(outputStream, (RequestParams.StreamWrapper) obj);
                            }
                            outputStream.write(125);
                        } else if (obj instanceof JsonValueInterface) {
                            outputStream.write(((JsonValueInterface) obj).getEscapedJsonValue());
                        } else if ((obj instanceof JSONObject) || (obj instanceof JSONArray)) {
                            outputStream.write(obj.toString().getBytes());
                        } else if (obj instanceof Boolean) {
                            outputStream.write(((Boolean) obj).booleanValue() ? JSON_TRUE : JSON_FALSE);
                        } else if (obj instanceof Long) {
                            outputStream.write((((Number) obj).longValue() + "").getBytes());
                        } else if (obj instanceof Double) {
                            outputStream.write((((Number) obj).doubleValue() + "").getBytes());
                        } else if (obj instanceof Float) {
                            outputStream.write((((Number) obj).floatValue() + "").getBytes());
                        } else if (obj instanceof Integer) {
                            outputStream.write((((Number) obj).intValue() + "").getBytes());
                        } else {
                            outputStream.write(escape(obj.toString()));
                        }
                    }
                    if (this.elapsedField != null || i < size) {
                        outputStream.write(44);
                    }
                } catch (Throwable th) {
                    if (this.elapsedField != null || i < size) {
                        outputStream.write(44);
                    }
                    throw th;
                }
            }
            long jCurrentTimeMillis2 = System.currentTimeMillis() - jCurrentTimeMillis;
            byte[] bArr = this.elapsedField;
            if (bArr != null) {
                outputStream.write(bArr);
                outputStream.write(58);
                outputStream.write((jCurrentTimeMillis2 + "").getBytes());
            }
            AsyncHttpClient.log.i(LOG_TAG, "Uploaded JSON in " + Math.floor(jCurrentTimeMillis2 / 1000) + " seconds");
        }
        outputStream.write(125);
        outputStream.flush();
        AsyncHttpClient.silentCloseOutputStream(outputStream);
    }

    private void writeToFromStream(OutputStream outputStream, RequestParams.StreamWrapper streamWrapper) throws IOException {
        writeMetaData(outputStream, streamWrapper.name, streamWrapper.contentType);
        Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream, 18);
        while (true) {
            int i = streamWrapper.inputStream.read(this.buffer);
            if (i == -1) {
                break;
            } else {
                base64OutputStream.write(this.buffer, 0, i);
            }
        }
        AsyncHttpClient.silentCloseOutputStream(base64OutputStream);
        endMetaData(outputStream);
        if (streamWrapper.autoClose) {
            AsyncHttpClient.silentCloseInputStream(streamWrapper.inputStream);
        }
    }

    private void writeToFromFile(OutputStream outputStream, RequestParams.FileWrapper fileWrapper) throws IOException {
        writeMetaData(outputStream, fileWrapper.file.getName(), fileWrapper.contentType);
        long length = fileWrapper.file.length();
        FileInputStream fileInputStream = new FileInputStream(fileWrapper.file);
        Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream, 18);
        long j = 0;
        while (true) {
            int i = fileInputStream.read(this.buffer);
            if (i != -1) {
                base64OutputStream.write(this.buffer, 0, i);
                j += (long) i;
                this.progressHandler.sendProgressMessage(j, length);
            } else {
                AsyncHttpClient.silentCloseOutputStream(base64OutputStream);
                endMetaData(outputStream);
                AsyncHttpClient.silentCloseInputStream(fileInputStream);
                return;
            }
        }
    }

    private void writeMetaData(OutputStream outputStream, String str, String str2) throws IOException {
        outputStream.write(STREAM_NAME);
        outputStream.write(58);
        outputStream.write(escape(str));
        outputStream.write(44);
        outputStream.write(STREAM_TYPE);
        outputStream.write(58);
        outputStream.write(escape(str2));
        outputStream.write(44);
        outputStream.write(STREAM_CONTENTS);
        outputStream.write(58);
        outputStream.write(34);
    }

    private void endMetaData(OutputStream outputStream) throws IOException {
        outputStream.write(34);
    }
}
