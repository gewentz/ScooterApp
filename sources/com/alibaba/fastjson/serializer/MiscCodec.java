package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONStreamAware;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;

/* JADX INFO: loaded from: classes.dex */
public final class MiscCodec implements ObjectSerializer, ObjectDeserializer {
    public static final MiscCodec instance = new MiscCodec();

    private MiscCodec() {
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            if (type == Character.TYPE || type == Character.class) {
                jSONSerializer.write("");
                return;
            } else if ((serializeWriter.features & SerializerFeature.WriteNullListAsEmpty.mask) != 0 && Enumeration.class.isAssignableFrom(TypeUtils.getClass(type))) {
                serializeWriter.write(HttpUrl.PATH_SEGMENT_ENCODE_SET_URI);
                return;
            } else {
                serializeWriter.writeNull();
                return;
            }
        }
        if (obj instanceof Pattern) {
            jSONSerializer.write(((Pattern) obj).pattern());
            return;
        }
        if (obj instanceof TimeZone) {
            jSONSerializer.write(((TimeZone) obj).getID());
            return;
        }
        if (obj instanceof Currency) {
            jSONSerializer.write(((Currency) obj).getCurrencyCode());
            return;
        }
        if (obj instanceof Class) {
            jSONSerializer.write(((Class) obj).getName());
            return;
        }
        if (obj instanceof Character) {
            Character ch = (Character) obj;
            if (ch.charValue() == 0) {
                jSONSerializer.write("\u0000");
                return;
            } else {
                jSONSerializer.write(ch.toString());
                return;
            }
        }
        int i = 0;
        if (obj instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) obj).toPattern();
            if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0 && obj.getClass() != type) {
                serializeWriter.write(123);
                serializeWriter.writeFieldName(JSON.DEFAULT_TYPE_KEY, false);
                jSONSerializer.write(obj.getClass().getName());
                serializeWriter.write(44);
                serializeWriter.writeFieldName("val", false);
                serializeWriter.writeString(pattern);
                serializeWriter.write(125);
                return;
            }
            serializeWriter.writeString(pattern);
            return;
        }
        if (obj instanceof JSONStreamAware) {
            ((JSONStreamAware) obj).writeJSONString(jSONSerializer.out);
            return;
        }
        if (obj instanceof JSONAware) {
            serializeWriter.write(((JSONAware) obj).toJSONString());
            return;
        }
        if (obj instanceof JSONSerializable) {
            ((JSONSerializable) obj).write(jSONSerializer, obj2, type);
            return;
        }
        if (obj instanceof Enumeration) {
            Type type2 = ((serializeWriter.features & SerializerFeature.WriteClassName.mask) == 0 || !(type instanceof ParameterizedType)) ? null : ((ParameterizedType) type).getActualTypeArguments()[0];
            Enumeration enumeration = (Enumeration) obj;
            SerialContext serialContext = jSONSerializer.context;
            jSONSerializer.setContext(serialContext, obj, obj2, 0);
            try {
                serializeWriter.write(91);
                while (enumeration.hasMoreElements()) {
                    Object objNextElement = enumeration.nextElement();
                    int i2 = i + 1;
                    if (i != 0) {
                        serializeWriter.write(44);
                    }
                    if (objNextElement == null) {
                        serializeWriter.writeNull();
                    } else {
                        jSONSerializer.config.get(objNextElement.getClass()).write(jSONSerializer, objNextElement, Integer.valueOf(i), type2);
                    }
                    i = i2;
                }
                serializeWriter.write(93);
                return;
            } finally {
                jSONSerializer.context = serialContext;
            }
        }
        jSONSerializer.write(obj.toString());
    }

    /* JADX WARN: Type inference failed for: r7v8, types: [T, java.text.SimpleDateFormat] */
    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        Object obj2;
        if (type == StackTraceElement.class) {
            return (T) parseStackTraceElement(defaultJSONParser);
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (defaultJSONParser.resolveStatus == 2) {
            defaultJSONParser.resolveStatus = 0;
            defaultJSONParser.accept(16);
            if (jSONLexer.token() == 4) {
                if (!"val".equals(jSONLexer.stringVal())) {
                    throw new JSONException("syntax error");
                }
                jSONLexer.nextToken();
                defaultJSONParser.accept(17);
                obj2 = defaultJSONParser.parse();
                defaultJSONParser.accept(13);
            } else {
                throw new JSONException("syntax error");
            }
        } else {
            obj2 = defaultJSONParser.parse();
        }
        if (obj2 == null) {
            return null;
        }
        if (obj2 instanceof String) {
            String str = (String) obj2;
            if (str.length() == 0) {
                return null;
            }
            if (type == UUID.class) {
                return (T) UUID.fromString(str);
            }
            if (type == Class.class) {
                return (T) TypeUtils.loadClass(str, defaultJSONParser.config.defaultClassLoader, false);
            }
            if (type == Locale.class) {
                String[] strArrSplit = str.split("_");
                if (strArrSplit.length == 1) {
                    return (T) new Locale(strArrSplit[0]);
                }
                if (strArrSplit.length == 2) {
                    return (T) new Locale(strArrSplit[0], strArrSplit[1]);
                }
                return (T) new Locale(strArrSplit[0], strArrSplit[1], strArrSplit[2]);
            }
            if (type == URI.class) {
                return (T) URI.create(str);
            }
            if (type == URL.class) {
                try {
                    return (T) new URL(str);
                } catch (MalformedURLException e) {
                    throw new JSONException("create url error", e);
                }
            }
            if (type == Pattern.class) {
                return (T) Pattern.compile(str);
            }
            if (type == Charset.class) {
                return (T) Charset.forName(str);
            }
            if (type == Currency.class) {
                return (T) Currency.getInstance(str);
            }
            if (type == SimpleDateFormat.class) {
                ?? r7 = (T) new SimpleDateFormat(str, defaultJSONParser.lexer.locale);
                r7.setTimeZone(defaultJSONParser.lexer.timeZone);
                return r7;
            }
            if (type == Character.TYPE || type == Character.class) {
                return (T) TypeUtils.castToChar(str);
            }
            if ((type instanceof Class) && "android.net.Uri".equals(((Class) type).getName())) {
                try {
                    return (T) Class.forName("android.net.Uri").getMethod("parse", String.class).invoke(null, str);
                } catch (Exception e2) {
                    throw new JSONException("parse android.net.Uri error.", e2);
                }
            }
            return (T) TimeZone.getTimeZone(str);
        }
        if (obj2 instanceof JSONObject) {
            JSONObject jSONObject = (JSONObject) obj2;
            if (type == Currency.class) {
                String string = jSONObject.getString("currency");
                if (string != null) {
                    return (T) Currency.getInstance(string);
                }
                String string2 = jSONObject.getString("currencyCode");
                if (string2 != null) {
                    return (T) Currency.getInstance(string2);
                }
            }
            if (type == Map.Entry.class) {
                return (T) jSONObject.entrySet().iterator().next();
            }
        }
        throw new JSONException("except string value");
    }

    protected <T> T parseStackTraceElement(DefaultJSONParser defaultJSONParser) {
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token() == 8) {
            jSONLexer.nextToken();
            return null;
        }
        if (jSONLexer.token() != 12 && jSONLexer.token() != 16) {
            throw new JSONException("syntax error: " + JSONToken.name(jSONLexer.token()));
        }
        String strStringVal = null;
        String strStringVal2 = null;
        String strStringVal3 = null;
        int iIntValue = 0;
        while (true) {
            String strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
            if (strScanSymbol == null) {
                if (jSONLexer.token() == 13) {
                    jSONLexer.nextToken(16);
                    break;
                }
                if (jSONLexer.token() == 16) {
                    continue;
                }
            }
            jSONLexer.nextTokenWithChar(':');
            if ("className".equals(strScanSymbol)) {
                if (jSONLexer.token() == 8) {
                    strStringVal = null;
                } else if (jSONLexer.token() == 4) {
                    strStringVal = jSONLexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("methodName".equals(strScanSymbol)) {
                if (jSONLexer.token() == 8) {
                    strStringVal2 = null;
                } else if (jSONLexer.token() == 4) {
                    strStringVal2 = jSONLexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("fileName".equals(strScanSymbol)) {
                if (jSONLexer.token() == 8) {
                    strStringVal3 = null;
                } else if (jSONLexer.token() == 4) {
                    strStringVal3 = jSONLexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("lineNumber".equals(strScanSymbol)) {
                if (jSONLexer.token() == 8) {
                    iIntValue = 0;
                } else if (jSONLexer.token() == 2) {
                    iIntValue = jSONLexer.intValue();
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("nativeMethod".equals(strScanSymbol)) {
                if (jSONLexer.token() == 8 || jSONLexer.token() == 6 || jSONLexer.token() == 7) {
                    jSONLexer.nextToken(16);
                } else {
                    throw new JSONException("syntax error");
                }
            } else if (strScanSymbol == JSON.DEFAULT_TYPE_KEY) {
                if (jSONLexer.token() == 4) {
                    String strStringVal4 = jSONLexer.stringVal();
                    if (!strStringVal4.equals("java.lang.StackTraceElement")) {
                        throw new JSONException("syntax error : " + strStringVal4);
                    }
                } else if (jSONLexer.token() != 8) {
                    throw new JSONException("syntax error");
                }
            } else {
                throw new JSONException("syntax error : " + strScanSymbol);
            }
            if (jSONLexer.token() == 13) {
                jSONLexer.nextToken(16);
                break;
            }
        }
        return (T) new StackTraceElement(strStringVal, strStringVal2, strStringVal3, iIntValue);
    }
}
