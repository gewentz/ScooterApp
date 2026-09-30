package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/* JADX INFO: loaded from: classes.dex */
public final class DateCodec implements ObjectSerializer, ObjectDeserializer {
    public static final DateCodec instance = new DateCodec();

    private DateCodec() {
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        Date time;
        char[] charArray;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            serializeWriter.writeNull();
            return;
        }
        if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0 && obj.getClass() != type) {
            if (obj.getClass() == Date.class) {
                serializeWriter.write("new Date(");
                serializeWriter.writeLong(((Date) obj).getTime());
                serializeWriter.write(41);
                return;
            }
            serializeWriter.write(123);
            serializeWriter.writeFieldName(JSON.DEFAULT_TYPE_KEY, false);
            jSONSerializer.write(obj.getClass().getName());
            serializeWriter.write(44);
            serializeWriter.writeFieldName("val", false);
            serializeWriter.writeLong(((Date) obj).getTime());
            serializeWriter.write(125);
            return;
        }
        if (obj instanceof Calendar) {
            time = ((Calendar) obj).getTime();
        } else {
            time = (Date) obj;
        }
        if ((serializeWriter.features & SerializerFeature.WriteDateUseDateFormat.mask) != 0) {
            DateFormat dateFormat = jSONSerializer.getDateFormat();
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, jSONSerializer.locale);
                dateFormat.setTimeZone(jSONSerializer.timeZone);
            }
            serializeWriter.writeString(dateFormat.format(time));
            return;
        }
        long time2 = time.getTime();
        if ((serializeWriter.features & SerializerFeature.UseISO8601DateFormat.mask) != 0) {
            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                serializeWriter.write(39);
            } else {
                serializeWriter.write(34);
            }
            Calendar calendar = Calendar.getInstance(jSONSerializer.timeZone, jSONSerializer.locale);
            calendar.setTimeInMillis(time2);
            int i = calendar.get(1);
            int i2 = calendar.get(2) + 1;
            int i3 = calendar.get(5);
            int i4 = calendar.get(11);
            int i5 = calendar.get(12);
            int i6 = calendar.get(13);
            int i7 = calendar.get(14);
            if (i7 != 0) {
                charArray = "0000-00-00T00:00:00.000".toCharArray();
                SerializeWriter.getChars(i7, 23, charArray);
                SerializeWriter.getChars(i6, 19, charArray);
                SerializeWriter.getChars(i5, 16, charArray);
                SerializeWriter.getChars(i4, 13, charArray);
                SerializeWriter.getChars(i3, 10, charArray);
                SerializeWriter.getChars(i2, 7, charArray);
                SerializeWriter.getChars(i, 4, charArray);
            } else if (i6 == 0 && i5 == 0 && i4 == 0) {
                charArray = "0000-00-00".toCharArray();
                SerializeWriter.getChars(i3, 10, charArray);
                SerializeWriter.getChars(i2, 7, charArray);
                SerializeWriter.getChars(i, 4, charArray);
            } else {
                charArray = "0000-00-00T00:00:00".toCharArray();
                SerializeWriter.getChars(i6, 19, charArray);
                SerializeWriter.getChars(i5, 16, charArray);
                SerializeWriter.getChars(i4, 13, charArray);
                SerializeWriter.getChars(i3, 10, charArray);
                SerializeWriter.getChars(i2, 7, charArray);
                SerializeWriter.getChars(i, 4, charArray);
            }
            serializeWriter.write(charArray);
            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                serializeWriter.write(39);
                return;
            } else {
                serializeWriter.write(34);
                return;
            }
        }
        serializeWriter.writeLong(time2);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        return (T) deserialze(defaultJSONParser, type, obj, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v9, types: [T, java.util.Calendar] */
    /* JADX WARN: Type inference failed for: r1v18, types: [T, java.util.Calendar] */
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj, String str) {
        Type type2;
        Object obj2;
        String strStringVal;
        Object time;
        T t;
        Object obj3;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 2) {
            Object objValueOf = Long.valueOf(jSONLexer.longValue());
            jSONLexer.nextToken(16);
            obj3 = objValueOf;
        } else if (i == 4) {
            strStringVal = jSONLexer.stringVal();
            jSONLexer.nextToken(16);
            if ((jSONLexer.features & Feature.AllowISO8601DateFormat.mask) != 0) {
                JSONLexer jSONLexer2 = new JSONLexer(strStringVal);
                if (jSONLexer2.scanISO8601DateIfMatch(true)) {
                    ?? r1 = (T) jSONLexer2.calendar;
                    if (type == Calendar.class) {
                        obj3 = strStringVal;
                        time = strStringVal;
                        jSONLexer2.close();
                        return r1;
                    }
                    obj3 = strStringVal;
                    time = strStringVal;
                    time = r1.getTime();
                }
                obj3 = strStringVal;
                time = strStringVal;
                jSONLexer2.close();
                obj3 = time;
            }
        } else {
            if (i == 8) {
                jSONLexer.nextToken();
                type2 = type;
                obj2 = null;
            } else if (i == 12) {
                jSONLexer.nextToken();
                if (jSONLexer.token() == 4) {
                    if (JSON.DEFAULT_TYPE_KEY.equals(jSONLexer.stringVal())) {
                        jSONLexer.nextToken();
                        defaultJSONParser.accept(17);
                        Type typeCheckAutoType = defaultJSONParser.config.checkAutoType(jSONLexer.stringVal(), null, jSONLexer.features);
                        if (typeCheckAutoType != null) {
                            type = typeCheckAutoType;
                        }
                        defaultJSONParser.accept(4);
                        defaultJSONParser.accept(16);
                    }
                    jSONLexer.nextTokenWithChar(':');
                    int i2 = jSONLexer.token();
                    if (i2 == 2) {
                        long jLongValue = jSONLexer.longValue();
                        jSONLexer.nextToken();
                        Object objValueOf2 = Long.valueOf(jLongValue);
                        defaultJSONParser.accept(13);
                        obj3 = objValueOf2;
                    } else {
                        throw new JSONException("syntax error : " + JSONToken.name(i2));
                    }
                } else {
                    throw new JSONException("syntax error");
                }
            } else if (defaultJSONParser.resolveStatus == 2) {
                defaultJSONParser.resolveStatus = 0;
                defaultJSONParser.accept(16);
                if (jSONLexer.token() == 4) {
                    if (!"val".equals(jSONLexer.stringVal())) {
                        throw new JSONException("syntax error");
                    }
                    jSONLexer.nextToken();
                    defaultJSONParser.accept(17);
                    Object obj4 = defaultJSONParser.parse();
                    defaultJSONParser.accept(13);
                    obj3 = obj4;
                } else {
                    throw new JSONException("syntax error");
                }
            } else {
                obj3 = defaultJSONParser.parse();
            }
            t = (T) cast(defaultJSONParser, type2, obj, obj2, str);
            if (type2 == Calendar.class || (t instanceof Calendar)) {
                return t;
            }
            Date date = (Date) t;
            if (date == null) {
                return null;
            }
            ?? r12 = (T) Calendar.getInstance(jSONLexer.timeZone, jSONLexer.locale);
            r12.setTime(date);
            return r12;
        }
        obj3 = strStringVal;
        type2 = type;
        obj2 = obj3;
        t = (T) cast(defaultJSONParser, type2, obj, obj2, str);
        if (type2 == Calendar.class) {
        }
        return t;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v8, types: [T, java.util.Calendar] */
    protected <T> T cast(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2, String str) {
        DateFormat dateFormat;
        if (obj2 == 0) {
            return null;
        }
        if (obj2 instanceof Date) {
            return obj2;
        }
        if (obj2 instanceof BigDecimal) {
            return (T) new Date(((BigDecimal) obj2).longValueExact());
        }
        if (obj2 instanceof Number) {
            return (T) new Date(((Number) obj2).longValue());
        }
        if (obj2 instanceof String) {
            String str2 = (String) obj2;
            if (str2.length() == 0) {
                return null;
            }
            JSONLexer jSONLexer = new JSONLexer(str2);
            try {
                if (jSONLexer.scanISO8601DateIfMatch(false)) {
                    ?? r3 = (T) jSONLexer.calendar;
                    if (type != Calendar.class) {
                        T t = (T) r3.getTime();
                        jSONLexer.close();
                        return t;
                    }
                    jSONLexer.close();
                    return r3;
                }
                jSONLexer.close();
                if ("0000-00-00".equals(str2) || "0000-00-00T00:00:00".equalsIgnoreCase(str2) || "0001-01-01T00:00:00+08:00".equalsIgnoreCase(str2)) {
                    return null;
                }
                if (str != null) {
                    dateFormat = new SimpleDateFormat(str);
                } else {
                    dateFormat = defaultJSONParser.getDateFormat();
                }
                try {
                    return (T) dateFormat.parse(str2);
                } catch (ParseException unused) {
                    return (T) new Date(Long.parseLong(str2));
                }
            } catch (Throwable th) {
                jSONLexer.close();
                throw th;
            }
        }
        throw new JSONException("parse error");
    }
}
