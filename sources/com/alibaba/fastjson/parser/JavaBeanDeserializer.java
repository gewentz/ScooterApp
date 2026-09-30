package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessable;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.parser.deserializer.ExtraTypeProvider;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* JADX INFO: loaded from: classes.dex */
public class JavaBeanDeserializer implements ObjectDeserializer {
    private final Map<String, FieldDeserializer> alterNameFieldDeserializers;
    public final JavaBeanInfo beanInfo;
    protected final Class<?> clazz;
    private ConcurrentMap<String, Object> extraFieldDeserializers;
    private final FieldDeserializer[] fieldDeserializers;
    private transient long[] smartMatchHashArray;
    private transient int[] smartMatchHashArrayMapping;
    private final FieldDeserializer[] sortedFieldDeserializers;

    public JavaBeanDeserializer(ParserConfig parserConfig, Class<?> cls, Type type) {
        this(parserConfig, cls, type, JavaBeanInfo.build(cls, cls.getModifiers(), type, false, true, true, true, parserConfig.propertyNamingStrategy));
    }

    public JavaBeanDeserializer(ParserConfig parserConfig, Class<?> cls, Type type, JavaBeanInfo javaBeanInfo) {
        this.clazz = cls;
        this.beanInfo = javaBeanInfo;
        this.sortedFieldDeserializers = new FieldDeserializer[javaBeanInfo.sortedFields.length];
        int length = javaBeanInfo.sortedFields.length;
        HashMap map = null;
        for (int i = 0; i < length; i++) {
            FieldInfo fieldInfo = javaBeanInfo.sortedFields[i];
            FieldDeserializer fieldDeserializerCreateFieldDeserializer = parserConfig.createFieldDeserializer(parserConfig, cls, fieldInfo);
            this.sortedFieldDeserializers[i] = fieldDeserializerCreateFieldDeserializer;
            for (String str : fieldInfo.alternateNames) {
                if (map == null) {
                    map = new HashMap();
                }
                map.put(str, fieldDeserializerCreateFieldDeserializer);
            }
        }
        this.alterNameFieldDeserializers = map;
        this.fieldDeserializers = new FieldDeserializer[javaBeanInfo.fields.length];
        int length2 = javaBeanInfo.fields.length;
        for (int i2 = 0; i2 < length2; i2++) {
            this.fieldDeserializers[i2] = getFieldDeserializer(javaBeanInfo.fields[i2].name);
        }
    }

    protected Object createInstance(DefaultJSONParser defaultJSONParser, Type type) {
        Object objNewInstance;
        if ((type instanceof Class) && this.clazz.isInterface()) {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{(Class) type}, new JSONObject((defaultJSONParser.lexer.features & Feature.OrderedField.mask) != 0));
        }
        if (this.beanInfo.defaultConstructor == null && this.beanInfo.factoryMethod == null) {
            return null;
        }
        if (this.beanInfo.factoryMethod != null && this.beanInfo.defaultConstructorParameterSize > 0) {
            return null;
        }
        try {
            Constructor<?> constructor = this.beanInfo.defaultConstructor;
            if (this.beanInfo.defaultConstructorParameterSize != 0) {
                objNewInstance = constructor.newInstance(defaultJSONParser.contex.object);
            } else if (constructor != null) {
                objNewInstance = constructor.newInstance(null);
            } else {
                objNewInstance = this.beanInfo.factoryMethod.invoke(null, null);
            }
            if (defaultJSONParser != null && (defaultJSONParser.lexer.features & Feature.InitStringFieldAsEmpty.mask) != 0) {
                for (FieldInfo fieldInfo : this.beanInfo.fields) {
                    if (fieldInfo.fieldClass == String.class) {
                        fieldInfo.set(objNewInstance, "");
                    }
                }
            }
            return objNewInstance;
        } catch (Exception e) {
            throw new JSONException("create instance error, class " + this.clazz.getName(), e);
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        return (T) deserialze(defaultJSONParser, type, obj, null);
    }

    private <T> T deserialzeArrayMapping(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2) {
        Enum enumValueOf;
        String strScanStringValue;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        T t = (T) createInstance(defaultJSONParser, type);
        int length = this.sortedFieldDeserializers.length;
        int i = 0;
        while (i < length) {
            char c = i == length + (-1) ? ']' : ',';
            FieldDeserializer fieldDeserializer = this.sortedFieldDeserializers[i];
            FieldInfo fieldInfo = fieldDeserializer.fieldInfo;
            Class<?> cls = fieldInfo.fieldClass;
            try {
                if (cls == Integer.TYPE) {
                    int iScanLongValue = (int) jSONLexer.scanLongValue();
                    if (fieldInfo.fieldAccess) {
                        fieldInfo.field.setInt(t, iScanLongValue);
                    } else {
                        fieldDeserializer.setValue(t, new Integer(iScanLongValue));
                    }
                    if (jSONLexer.ch == ',') {
                        int i2 = jSONLexer.bp + 1;
                        jSONLexer.bp = i2;
                        jSONLexer.ch = i2 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i2);
                        jSONLexer.token = 16;
                    } else if (jSONLexer.ch == ']') {
                        int i3 = jSONLexer.bp + 1;
                        jSONLexer.bp = i3;
                        jSONLexer.ch = i3 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i3);
                        jSONLexer.token = 15;
                    } else {
                        jSONLexer.nextToken();
                    }
                } else {
                    if (cls == String.class) {
                        if (jSONLexer.ch == '\"') {
                            strScanStringValue = jSONLexer.scanStringValue('\"');
                        } else if (jSONLexer.ch == 'n' && jSONLexer.text.startsWith("null", jSONLexer.bp)) {
                            jSONLexer.bp += 4;
                            jSONLexer.ch = jSONLexer.bp >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(jSONLexer.bp);
                            strScanStringValue = null;
                        } else {
                            throw new JSONException("not match string. feild : " + obj);
                        }
                        if (fieldInfo.fieldAccess) {
                            fieldInfo.field.set(t, strScanStringValue);
                        } else {
                            fieldDeserializer.setValue(t, strScanStringValue);
                        }
                        if (jSONLexer.ch == ',') {
                            int i4 = jSONLexer.bp + 1;
                            jSONLexer.bp = i4;
                            jSONLexer.ch = i4 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i4);
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i5 = jSONLexer.bp + 1;
                            jSONLexer.bp = i5;
                            jSONLexer.ch = i5 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i5);
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls == Long.TYPE) {
                        long jScanLongValue = jSONLexer.scanLongValue();
                        if (fieldInfo.fieldAccess) {
                            fieldInfo.field.setLong(t, jScanLongValue);
                        } else {
                            fieldDeserializer.setValue(t, new Long(jScanLongValue));
                        }
                        if (jSONLexer.ch == ',') {
                            int i6 = jSONLexer.bp + 1;
                            jSONLexer.bp = i6;
                            jSONLexer.ch = i6 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i6);
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i7 = jSONLexer.bp + 1;
                            jSONLexer.bp = i7;
                            jSONLexer.ch = i7 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i7);
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls == Boolean.TYPE) {
                        boolean zScanBoolean = jSONLexer.scanBoolean();
                        if (fieldInfo.fieldAccess) {
                            fieldInfo.field.setBoolean(t, zScanBoolean);
                        } else {
                            fieldDeserializer.setValue(t, Boolean.valueOf(zScanBoolean));
                        }
                        if (jSONLexer.ch == ',') {
                            int i8 = jSONLexer.bp + 1;
                            jSONLexer.bp = i8;
                            jSONLexer.ch = i8 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i8);
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i9 = jSONLexer.bp + 1;
                            jSONLexer.bp = i9;
                            jSONLexer.ch = i9 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i9);
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls.isEnum()) {
                        char c2 = jSONLexer.ch;
                        if (c2 == '\"') {
                            String strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                            enumValueOf = strScanSymbol == null ? null : Enum.valueOf(cls, strScanSymbol);
                        } else if (c2 >= '0' && c2 <= '9') {
                            enumValueOf = ((EnumDeserializer) ((DefaultFieldDeserializer) fieldDeserializer).getFieldValueDeserilizer(defaultJSONParser.config)).ordinalEnums[(int) jSONLexer.scanLongValue()];
                        } else {
                            throw new JSONException("illegal enum." + jSONLexer.info());
                        }
                        fieldDeserializer.setValue(t, enumValueOf);
                        if (jSONLexer.ch == ',') {
                            int i10 = jSONLexer.bp + 1;
                            jSONLexer.bp = i10;
                            jSONLexer.ch = i10 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i10);
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i11 = jSONLexer.bp + 1;
                            jSONLexer.bp = i11;
                            jSONLexer.ch = i11 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i11);
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls == Date.class && jSONLexer.ch == '1') {
                        fieldDeserializer.setValue(t, new Date(jSONLexer.scanLongValue()));
                        if (jSONLexer.ch == ',') {
                            int i12 = jSONLexer.bp + 1;
                            jSONLexer.bp = i12;
                            jSONLexer.ch = i12 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i12);
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i13 = jSONLexer.bp + 1;
                            jSONLexer.bp = i13;
                            jSONLexer.ch = i13 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i13);
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else {
                        if (jSONLexer.ch == '[') {
                            int i14 = jSONLexer.bp + 1;
                            jSONLexer.bp = i14;
                            jSONLexer.ch = i14 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i14);
                            jSONLexer.token = 14;
                        } else if (jSONLexer.ch == '{') {
                            int i15 = jSONLexer.bp + 1;
                            jSONLexer.bp = i15;
                            jSONLexer.ch = i15 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i15);
                            jSONLexer.token = 12;
                        } else {
                            jSONLexer.nextToken();
                        }
                        fieldDeserializer.parseField(defaultJSONParser, t, fieldInfo.fieldType, null);
                        if (c == ']') {
                            if (jSONLexer.token != 15) {
                                throw new JSONException("syntax error");
                            }
                        } else if (c == ',' && jSONLexer.token != 16) {
                            throw new JSONException("syntax error");
                        }
                    }
                    i++;
                }
                i++;
            } catch (IllegalAccessException e) {
                throw new JSONException("set " + fieldInfo.name + "error", e);
            }
        }
        if (jSONLexer.ch == ',') {
            int i16 = jSONLexer.bp + 1;
            jSONLexer.bp = i16;
            jSONLexer.ch = i16 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i16);
            jSONLexer.token = 16;
            return t;
        }
        jSONLexer.nextToken();
        return t;
    }

    /* JADX WARN: Code duplicated, block: B:218:0x02cc A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:220:0x02d6 A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:223:0x02e7  */
    /* JADX WARN: Code duplicated, block: B:226:0x02ed  */
    /* JADX WARN: Code duplicated, block: B:229:0x02f7 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:266:0x039d A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:268:0x03a3  */
    /* JADX WARN: Code duplicated, block: B:272:0x03b1 A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:274:0x03be A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:284:0x03ef A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:286:0x03ff A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:293:0x041c  */
    /* JADX WARN: Code duplicated, block: B:296:0x0421 A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:298:0x042a A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:301:0x0434 A[Catch: all -> 0x00de, TRY_LEAVE, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:303:0x043a  */
    /* JADX WARN: Code duplicated, block: B:310:0x0453 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:317:0x0471  */
    /* JADX WARN: Code duplicated, block: B:320:0x0477 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:321:0x0479 A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:322:0x0480 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:323:0x0482 A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:325:0x0486  */
    /* JADX WARN: Code duplicated, block: B:347:0x04ba A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:350:0x04c7  */
    /* JADX WARN: Code duplicated, block: B:354:0x04cf  */
    /* JADX WARN: Code duplicated, block: B:391:0x053a A[Catch: IllegalAccessException -> 0x0552, all -> 0x057e, TryCatch #4 {IllegalAccessException -> 0x0552, blocks: (B:352:0x04cb, B:357:0x04d5, B:362:0x04de, B:367:0x04e7, B:372:0x04f0, B:373:0x04f7, B:375:0x04fb, B:377:0x04ff, B:378:0x0504, B:379:0x050d, B:381:0x0511, B:383:0x0515, B:384:0x0519, B:385:0x0522, B:387:0x0526, B:389:0x052a, B:390:0x0530, B:391:0x053a, B:393:0x053e, B:395:0x0542, B:396:0x0548), top: B:500:0x04cb, outer: #6 }] */
    /* JADX WARN: Code duplicated, block: B:393:0x053e A[Catch: IllegalAccessException -> 0x0552, all -> 0x057e, TryCatch #4 {IllegalAccessException -> 0x0552, blocks: (B:352:0x04cb, B:357:0x04d5, B:362:0x04de, B:367:0x04e7, B:372:0x04f0, B:373:0x04f7, B:375:0x04fb, B:377:0x04ff, B:378:0x0504, B:379:0x050d, B:381:0x0511, B:383:0x0515, B:384:0x0519, B:385:0x0522, B:387:0x0526, B:389:0x052a, B:390:0x0530, B:391:0x053a, B:393:0x053e, B:395:0x0542, B:396:0x0548), top: B:500:0x04cb, outer: #6 }] */
    /* JADX WARN: Code duplicated, block: B:396:0x0548 A[Catch: IllegalAccessException -> 0x0552, all -> 0x057e, TRY_LEAVE, TryCatch #4 {IllegalAccessException -> 0x0552, blocks: (B:352:0x04cb, B:357:0x04d5, B:362:0x04de, B:367:0x04e7, B:372:0x04f0, B:373:0x04f7, B:375:0x04fb, B:377:0x04ff, B:378:0x0504, B:379:0x050d, B:381:0x0511, B:383:0x0515, B:384:0x0519, B:385:0x0522, B:387:0x0526, B:389:0x052a, B:390:0x0530, B:391:0x053a, B:393:0x053e, B:395:0x0542, B:396:0x0548), top: B:500:0x04cb, outer: #6 }] */
    /* JADX WARN: Code duplicated, block: B:401:0x056c A[Catch: all -> 0x057e, TryCatch #6 {all -> 0x057e, blocks: (B:402:0x0571, B:352:0x04cb, B:357:0x04d5, B:362:0x04de, B:367:0x04e7, B:372:0x04f0, B:373:0x04f7, B:375:0x04fb, B:377:0x04ff, B:378:0x0504, B:379:0x050d, B:381:0x0511, B:383:0x0515, B:384:0x0519, B:385:0x0522, B:387:0x0526, B:389:0x052a, B:390:0x0530, B:391:0x053a, B:393:0x053e, B:395:0x0542, B:396:0x0548, B:401:0x056c, B:399:0x0553, B:400:0x056b), top: B:504:0x0571, inners: #4 }] */
    /* JADX WARN: Code duplicated, block: B:406:0x057a  */
    /* JADX WARN: Code duplicated, block: B:409:0x0583  */
    /* JADX WARN: Code duplicated, block: B:412:0x058d A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:415:0x0597  */
    /* JADX WARN: Code duplicated, block: B:417:0x059e A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:421:0x05aa  */
    /* JADX WARN: Code duplicated, block: B:422:0x05ac A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:476:0x0694  */
    /* JADX WARN: Code duplicated, block: B:479:0x069b A[Catch: all -> 0x00de, TryCatch #7 {all -> 0x00de, blocks: (B:61:0x00c9, B:63:0x00cf, B:71:0x00eb, B:76:0x0107, B:81:0x0118, B:88:0x0126, B:218:0x02cc, B:220:0x02d6, B:222:0x02e0, B:427:0x05b9, B:437:0x05d0, B:439:0x05d6, B:441:0x05db, B:443:0x05e0, B:445:0x05e8, B:448:0x05f7, B:449:0x05fd, B:446:0x05ef, B:450:0x0602, B:452:0x0608, B:465:0x065c, B:467:0x0662, B:470:0x066d, B:471:0x068b, B:463:0x063d, B:464:0x065b, B:440:0x05d8, B:230:0x02f9, B:232:0x0301, B:234:0x030d, B:253:0x035f, B:255:0x0366, B:260:0x0376, B:261:0x037d, B:236:0x0313, B:238:0x031b, B:240:0x0321, B:241:0x0324, B:242:0x0330, B:245:0x0339, B:247:0x033d, B:248:0x0340, B:250:0x0344, B:251:0x0347, B:252:0x0353, B:262:0x037e, B:263:0x0398, B:266:0x039d, B:270:0x03a7, B:272:0x03b1, B:274:0x03be, B:276:0x03cb, B:278:0x03d1, B:282:0x03e5, B:284:0x03ef, B:287:0x0401, B:290:0x0408, B:291:0x040f, B:292:0x0410, B:294:0x041d, B:296:0x0421, B:298:0x042a, B:300:0x0430, B:301:0x0434, B:306:0x0440, B:307:0x0447, B:311:0x0455, B:313:0x045b, B:316:0x046c, B:321:0x0479, B:419:0x05a4, B:422:0x05ac, B:424:0x05b2, B:477:0x0695, B:479:0x069b, B:482:0x06b1, B:483:0x06cd, B:323:0x0482, B:328:0x048b, B:333:0x0494, B:338:0x049d, B:348:0x04be, B:344:0x04a9, B:345:0x04af, B:346:0x04b5, B:347:0x04ba, B:410:0x0587, B:412:0x058d, B:414:0x0593, B:417:0x059e, B:484:0x06ce, B:485:0x06d5, B:92:0x0135, B:94:0x0139, B:98:0x0146, B:101:0x014f, B:103:0x0153, B:104:0x0157, B:109:0x0161, B:114:0x016b, B:119:0x0175, B:121:0x0179, B:123:0x0183, B:125:0x018b, B:126:0x0193, B:128:0x0197, B:133:0x01a8, B:136:0x01b2, B:138:0x01b6, B:141:0x01be, B:144:0x01c8, B:146:0x01cc, B:149:0x01d4, B:152:0x01de, B:154:0x01e2, B:157:0x01ea, B:160:0x01f4, B:162:0x01f8, B:165:0x0200, B:168:0x020a, B:170:0x020e, B:171:0x0212, B:174:0x021e, B:178:0x022c, B:180:0x0230, B:181:0x0234, B:185:0x0248, B:187:0x024c, B:189:0x0258, B:192:0x0266, B:194:0x026a, B:195:0x026d, B:199:0x0284, B:201:0x0288, B:203:0x0296, B:206:0x02a7, B:208:0x02ab, B:72:0x00f9), top: B:506:0x00c9, inners: #1, #2 }] */
    /* JADX WARN: Code duplicated, block: B:489:0x06dc  */
    /* JADX WARN: Code duplicated, block: B:500:0x04cb A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:508:0x0440 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:509:0x03e5 A[ADDED_TO_REGION, REMOVE, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:512:0x0448 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:513:0x02e0 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:529:0x02eb A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:532:0x0576 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:533:0x0593 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:534:0x06ce A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:535:0x05b2 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:536:0x06b1 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:537:0x06b1 A[SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r35v0, types: [com.alibaba.fastjson.parser.DefaultJSONParser] */
    /* JADX WARN: Type inference failed for: r3v32 */
    /* JADX WARN: Type inference failed for: r3v48 */
    /* JADX WARN: Type inference failed for: r3v89 */
    /* JADX WARN: Type inference failed for: r3v90 */
    /* JADX WARN: Type inference failed for: r3v91 */
    /* JADX WARN: Type inference failed for: r3v92 */
    /* JADX WARN: Type inference failed for: r3v93 */
    /* JADX WARN: Type inference failed for: r3v94 */
    /* JADX WARN: Type inference failed for: r4v46 */
    private <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2) throws Throwable {
        Object obj3;
        FieldInfo fieldInfo;
        Class<?> cls;
        FieldInfo fieldInfo2;
        Class<?> cls2;
        FieldDeserializer fieldDeserializer;
        FieldInfo fieldInfo3;
        Class<?> cls3;
        FieldDeserializer fieldDeserializer2;
        int i;
        long j;
        boolean z;
        Object objValueOf;
        boolean z2;
        float fScanFieldFloat;
        int i2;
        long jScanFieldLong;
        String strScanSymbol;
        HashMap map;
        HashMap map2;
        int i3;
        HashMap map3;
        HashMap map4;
        JavaBeanDeserializer javaBeanDeserializer;
        HashMap map5;
        JavaBeanDeserializer javaBeanDeserializer2;
        int i4;
        FieldInfo fieldInfo4;
        JavaBeanDeserializer javaBeanDeserializer3;
        HashMap map6;
        Object objValueOf2;
        ?? r3;
        ?? r4;
        String strStringVal;
        ObjectDeserializer seeAlso;
        Class<?> cls4;
        T t;
        FieldDeserializer fieldDeserializer3;
        Class<?> cls5;
        int i5;
        boolean z3;
        JavaBeanDeserializer javaBeanDeserializer4 = this;
        Type type2 = type;
        if (type2 == JSON.class || type2 == JSONObject.class) {
            return (T) defaultJSONParser.parse();
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i6 = jSONLexer.token;
        ParseContext parseContext = null;
        if (i6 == 8) {
            jSONLexer.nextToken(16);
            return null;
        }
        boolean z4 = jSONLexer.disableCircularReferenceDetect;
        ParseContext parseContext2 = defaultJSONParser.contex;
        if (obj2 != null && parseContext2 != null) {
            parseContext2 = parseContext2.parent;
        }
        ParseContext parseContext3 = parseContext2;
        try {
            if (i6 == 13) {
                jSONLexer.nextToken(16);
                T t2 = obj2 == null ? (T) createInstance((DefaultJSONParser) defaultJSONParser, type) : (T) obj2;
                defaultJSONParser.setContext(parseContext3);
                return t2;
            }
            if (i6 == 14 && (javaBeanDeserializer4.beanInfo.supportBeanToArray || (jSONLexer.features & Feature.SupportArrayToBean.mask) != 0)) {
                T t3 = (T) deserialzeArrayMapping(defaultJSONParser, type, obj, obj2);
                defaultJSONParser.setContext(parseContext3);
                return t3;
            }
            if (i6 != 12 && i6 != 16) {
                if (jSONLexer.isBlankInput()) {
                    defaultJSONParser.setContext(parseContext3);
                    return null;
                }
                if (i6 == 4 && jSONLexer.stringVal().length() == 0) {
                    jSONLexer.nextToken();
                    defaultJSONParser.setContext(parseContext3);
                    return null;
                }
                StringBuffer stringBufferAppend = new StringBuffer("syntax error, expect {, actual ").append(jSONLexer.info());
                if (obj instanceof String) {
                    stringBufferAppend.append(", fieldName ").append(obj);
                }
                throw new JSONException(stringBufferAppend.toString());
            }
            try {
                if (defaultJSONParser.resolveStatus == 2) {
                    defaultJSONParser.resolveStatus = 0;
                }
                String str = javaBeanDeserializer4.beanInfo.typeKey;
                int length = javaBeanDeserializer4.sortedFieldDeserializers.length;
                obj3 = obj2;
                ParseContext context = null;
                HashMap map7 = null;
                long j2 = 0;
                int i7 = 0;
                while (true) {
                    if (j2 != 0) {
                        try {
                            FieldDeserializer fieldDeserializerByHash = javaBeanDeserializer4.getFieldDeserializerByHash(j2);
                            if (fieldDeserializerByHash != null) {
                                fieldInfo = fieldDeserializerByHash.fieldInfo;
                                cls = fieldInfo.fieldClass;
                            } else {
                                fieldInfo = null;
                                cls = null;
                            }
                            fieldInfo2 = fieldInfo;
                            cls2 = cls;
                            fieldDeserializer = fieldDeserializerByHash;
                            j2 = 0;
                        } catch (Throwable th) {
                            th = th;
                            parseContext = context;
                            if (parseContext != null) {
                                parseContext.object = obj3;
                            }
                            defaultJSONParser.setContext(parseContext3);
                            throw th;
                        }
                    } else {
                        fieldDeserializer = null;
                        fieldInfo2 = null;
                        cls2 = null;
                    }
                    if (fieldDeserializer != null) {
                        i7 = i7;
                        fieldInfo3 = fieldInfo2;
                        cls3 = cls2;
                        fieldDeserializer2 = fieldDeserializer;
                    } else if (i7 < length) {
                        FieldDeserializer fieldDeserializer4 = javaBeanDeserializer4.sortedFieldDeserializers[i7];
                        FieldInfo fieldInfo5 = fieldDeserializer4.fieldInfo;
                        cls3 = fieldInfo5.fieldClass;
                        i7++;
                        fieldInfo3 = fieldInfo5;
                        fieldDeserializer2 = fieldDeserializer4;
                    } else {
                        i7++;
                        i7 = i7;
                        fieldInfo3 = fieldInfo2;
                        cls3 = cls2;
                        fieldDeserializer2 = fieldDeserializer;
                    }
                    double dScanFieldDouble = 0.0d;
                    if (fieldDeserializer2 != null) {
                        j = j2;
                        long j3 = fieldInfo3.nameHashCode;
                        i = length;
                        if (cls3 == Integer.TYPE || cls3 == Integer.class) {
                            int iScanFieldInt = jSONLexer.scanFieldInt(j3);
                            if (jSONLexer.matchStat > 0) {
                                i2 = iScanFieldInt;
                                dScanFieldDouble = 0.0d;
                                z = true;
                                objValueOf = null;
                                z2 = true;
                            } else if (jSONLexer.matchStat == -2) {
                                j2 = jSONLexer.fieldHash;
                            } else {
                                i2 = iScanFieldInt;
                                dScanFieldDouble = 0.0d;
                                z = false;
                                objValueOf = null;
                                z2 = false;
                            }
                            fScanFieldFloat = 0.0f;
                            jScanFieldLong = 0;
                            if (z2) {
                                fieldInfo3 = fieldInfo3;
                                objValueOf = objValueOf;
                                strScanSymbol = null;
                            } else {
                                strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                                if (strScanSymbol == null) {
                                    i5 = jSONLexer.token;
                                    if (i5 == 13) {
                                        jSONLexer.nextToken(16);
                                        r4 = strScanSymbol;
                                    } else {
                                        if (i5 == 16) {
                                        }
                                        j2 = j;
                                        length = i;
                                    }
                                    map6 = map7;
                                    javaBeanDeserializer3 = javaBeanDeserializer4;
                                    r3 = r4;
                                    break;
                                }
                                if ("$ref" != strScanSymbol && parseContext3 != null) {
                                    jSONLexer.nextTokenWithChar(':');
                                    int i8 = jSONLexer.token;
                                    if (i8 != 4) {
                                        throw new JSONException("illegal ref, " + JSONToken.name(i8));
                                    }
                                    String strStringVal2 = jSONLexer.stringVal();
                                    if ("@".equals(strStringVal2)) {
                                        obj3 = (T) parseContext3.object;
                                    } else if ("..".equals(strStringVal2)) {
                                        ParseContext parseContext4 = parseContext3.parent;
                                        if (parseContext4.object != null) {
                                            obj3 = (T) parseContext4.object;
                                        } else {
                                            defaultJSONParser.addResolveTask(new DefaultJSONParser.ResolveTask(parseContext4, strStringVal2));
                                            defaultJSONParser.resolveStatus = 1;
                                        }
                                    } else if ("$".equals(strStringVal2)) {
                                        ParseContext parseContext5 = parseContext3;
                                        while (parseContext5.parent != null) {
                                            parseContext5 = parseContext5.parent;
                                        }
                                        if (parseContext5.object != null) {
                                            obj3 = (T) parseContext5.object;
                                        } else {
                                            defaultJSONParser.addResolveTask(new DefaultJSONParser.ResolveTask(parseContext5, strStringVal2));
                                            defaultJSONParser.resolveStatus = 1;
                                        }
                                    } else {
                                        defaultJSONParser.addResolveTask(new DefaultJSONParser.ResolveTask(parseContext3, strStringVal2));
                                        defaultJSONParser.resolveStatus = 1;
                                    }
                                    jSONLexer.nextToken(13);
                                    if (jSONLexer.token != 13) {
                                        throw new JSONException("illegal ref");
                                    }
                                    jSONLexer.nextToken(16);
                                    defaultJSONParser.setContext(parseContext3, obj3, obj);
                                    if (context != null) {
                                        context.object = obj3;
                                    }
                                    defaultJSONParser.setContext(parseContext3);
                                    return (T) obj3;
                                }
                                if ((str == null && str.equals(strScanSymbol)) || JSON.DEFAULT_TYPE_KEY == strScanSymbol) {
                                    jSONLexer.nextTokenWithChar(':');
                                    if (jSONLexer.token != 4) {
                                        throw new JSONException("syntax error");
                                    }
                                    strStringVal = jSONLexer.stringVal();
                                    jSONLexer.nextToken(16);
                                    if ((type2 instanceof Class) || !strStringVal.equals(((Class) type2).getName())) {
                                        seeAlso = javaBeanDeserializer4.getSeeAlso(defaultJSONParser.config, javaBeanDeserializer4.beanInfo, strStringVal);
                                        if (seeAlso == null) {
                                            Class<?> clsCheckAutoType = defaultJSONParser.config.checkAutoType(strStringVal, javaBeanDeserializer4.clazz, jSONLexer.features);
                                            cls5 = TypeUtils.getClass(type2);
                                            if (cls5 != null && (clsCheckAutoType == null || !cls5.isAssignableFrom(clsCheckAutoType))) {
                                                throw new JSONException("type not match");
                                            }
                                            seeAlso = defaultJSONParser.config.getDeserializer(clsCheckAutoType);
                                            cls4 = clsCheckAutoType;
                                        } else {
                                            cls4 = null;
                                        }
                                        if (seeAlso instanceof JavaBeanDeserializer) {
                                            JavaBeanDeserializer javaBeanDeserializer5 = (JavaBeanDeserializer) seeAlso;
                                            t = (T) javaBeanDeserializer5.deserialze(defaultJSONParser, cls4, obj, null);
                                            if (str != null && (fieldDeserializer3 = javaBeanDeserializer5.getFieldDeserializer(str)) != null) {
                                                fieldDeserializer3.setValue(t, strStringVal);
                                            }
                                        } else {
                                            t = (T) seeAlso.deserialze(defaultJSONParser, cls4, obj);
                                        }
                                        if (context != null) {
                                            context.object = obj3;
                                        }
                                        defaultJSONParser.setContext(parseContext3);
                                        return t;
                                    }
                                    int i9 = jSONLexer.token;
                                    if (i9 == 13) {
                                        jSONLexer.nextToken();
                                        r4 = i9;
                                        map6 = map7;
                                        javaBeanDeserializer3 = javaBeanDeserializer4;
                                        r3 = r4;
                                        break;
                                    }
                                    j2 = j;
                                    length = i;
                                }
                            }
                            if (obj3 == null || map7 != null) {
                                strScanSymbol = strScanSymbol;
                            } else {
                                obj3 = (T) createInstance((DefaultJSONParser) defaultJSONParser, type);
                                if (obj3 == null) {
                                    map7 = new HashMap(javaBeanDeserializer4.fieldDeserializers.length);
                                }
                                if (!z4) {
                                    map7 = map7;
                                    context = defaultJSONParser.setContext(parseContext3, obj3, obj);
                                }
                            }
                            map7 = map7;
                            map = map7;
                            if (z2) {
                                if (z) {
                                    if (obj3 == null) {
                                        if (cls3 != Integer.TYPE || cls3 == Integer.class) {
                                            objValueOf2 = Integer.valueOf(i2);
                                        } else if (cls3 == Long.TYPE || cls3 == Long.class) {
                                            objValueOf2 = Long.valueOf(jScanFieldLong);
                                        } else if (cls3 == Float.TYPE || cls3 == Float.class) {
                                            objValueOf2 = new Float(fScanFieldFloat);
                                        } else {
                                            objValueOf2 = (cls3 == Double.TYPE || cls3 == Double.class) ? new Double(dScanFieldDouble) : objValueOf;
                                        }
                                        map.put(fieldInfo3.name, objValueOf2);
                                    } else {
                                        fieldInfo4 = fieldInfo3;
                                        if (objValueOf == null) {
                                            try {
                                                if (cls3 != Integer.TYPE || cls3 == Integer.class) {
                                                    if (fieldInfo4.fieldAccess || cls3 != Integer.TYPE) {
                                                        fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                                    } else {
                                                        fieldDeserializer2.setValue(obj3, i2);
                                                    }
                                                } else if (cls3 == Long.TYPE || cls3 == Long.class) {
                                                    if (fieldInfo4.fieldAccess && cls3 == Long.TYPE) {
                                                        fieldDeserializer2.setValue(obj3, jScanFieldLong);
                                                    } else {
                                                        fieldDeserializer2.setValue(obj3, Long.valueOf(jScanFieldLong));
                                                    }
                                                } else if (cls3 == Float.TYPE || cls3 == Float.class) {
                                                    if (fieldInfo4.fieldAccess && cls3 == Float.TYPE) {
                                                        fieldDeserializer2.setValue(obj3, fScanFieldFloat);
                                                    } else {
                                                        fieldDeserializer2.setValue(obj3, new Float(fScanFieldFloat));
                                                    }
                                                } else if (cls3 != Double.TYPE && cls3 != Double.class) {
                                                    fieldDeserializer2.setValue(obj3, objValueOf);
                                                } else if (fieldInfo4.fieldAccess && cls3 == Double.TYPE) {
                                                    fieldDeserializer2.setValue(obj3, dScanFieldDouble);
                                                } else {
                                                    fieldDeserializer2.setValue(obj3, new Double(dScanFieldDouble));
                                                }
                                            } catch (IllegalAccessException e) {
                                                throw new JSONException("set property error, " + fieldInfo4.name, e);
                                            }
                                        } else {
                                            fieldDeserializer2.setValue(obj3, objValueOf);
                                        }
                                    }
                                    try {
                                        if (jSONLexer.matchStat == 4) {
                                            javaBeanDeserializer3 = this;
                                            map6 = map;
                                            r3 = map;
                                            break;
                                        }
                                        javaBeanDeserializer2 = this;
                                        map5 = map;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        parseContext = context;
                                        if (parseContext != null) {
                                            parseContext.object = obj3;
                                        }
                                        defaultJSONParser.setContext(parseContext3);
                                        throw th;
                                    }
                                } else {
                                    fieldDeserializer2.parseField(defaultJSONParser, obj3, type2, map);
                                    map5 = map;
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                }
                                javaBeanDeserializer2 = javaBeanDeserializer4;
                                map5 = map2;
                                if (jSONLexer.token == 16) {
                                    i4 = jSONLexer.token;
                                    if (i4 == 13) {
                                        jSONLexer.nextToken(16);
                                        javaBeanDeserializer3 = javaBeanDeserializer2;
                                        r3 = i4;
                                        map6 = map5;
                                        break;
                                    }
                                    if (jSONLexer.token == 18) {
                                        if (jSONLexer.token == 1) {
                                            javaBeanDeserializer4 = javaBeanDeserializer2;
                                            map3 = map5;
                                        }
                                    }
                                    throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                }
                                javaBeanDeserializer = javaBeanDeserializer2;
                                map4 = map5;
                                javaBeanDeserializer4 = javaBeanDeserializer;
                                map3 = map4;
                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                map3 = map5;
                                type2 = type;
                                map7 = map3;
                                j2 = j;
                                length = i;
                                i7 = i7;
                            } else {
                                map2 = map;
                                if (javaBeanDeserializer4.parseField(defaultJSONParser, strScanSymbol, obj3, type2, map2)) {
                                    if (jSONLexer.token == 17) {
                                        javaBeanDeserializer2 = javaBeanDeserializer4;
                                        map5 = map2;
                                        throw new JSONException("syntax error, unexpect token ':'");
                                    }
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                    map5 = map2;
                                    if (jSONLexer.token == 16) {
                                        i4 = jSONLexer.token;
                                        if (i4 == 13) {
                                            jSONLexer.nextToken(16);
                                            javaBeanDeserializer3 = javaBeanDeserializer2;
                                            r3 = i4;
                                            map6 = map5;
                                            break;
                                        }
                                        if (jSONLexer.token == 18) {
                                            if (jSONLexer.token == 1) {
                                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                                map3 = map5;
                                            }
                                        }
                                        throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer2;
                                    map4 = map5;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                } else {
                                    i3 = jSONLexer.token;
                                    if (i3 == 13) {
                                        jSONLexer.nextToken();
                                        javaBeanDeserializer3 = javaBeanDeserializer4;
                                        r3 = i3;
                                        map6 = map2;
                                        break;
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer4;
                                    map4 = map2;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                }
                            }
                        } else if (cls3 == Long.TYPE || cls3 == Long.class) {
                            jScanFieldLong = jSONLexer.scanFieldLong(j3);
                            if (jSONLexer.matchStat > 0) {
                                dScanFieldDouble = 0.0d;
                                z = true;
                                objValueOf = null;
                                z2 = true;
                            } else if (jSONLexer.matchStat == -2) {
                                j2 = jSONLexer.fieldHash;
                            } else {
                                dScanFieldDouble = 0.0d;
                                z = false;
                                objValueOf = null;
                                z2 = false;
                            }
                            i2 = 0;
                            fScanFieldFloat = 0.0f;
                            if (z2) {
                                strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                                if (strScanSymbol == null) {
                                    i5 = jSONLexer.token;
                                    if (i5 == 13) {
                                        jSONLexer.nextToken(16);
                                        r4 = strScanSymbol;
                                    } else {
                                        if (i5 == 16) {
                                        }
                                        j2 = j;
                                        length = i;
                                    }
                                    map6 = map7;
                                    javaBeanDeserializer3 = javaBeanDeserializer4;
                                    r3 = r4;
                                    break;
                                }
                                if ("$ref" != strScanSymbol) {
                                }
                                if (str == null) {
                                }
                                jSONLexer.nextTokenWithChar(':');
                                if (jSONLexer.token != 4) {
                                    throw new JSONException("syntax error");
                                }
                                strStringVal = jSONLexer.stringVal();
                                jSONLexer.nextToken(16);
                                if (type2 instanceof Class) {
                                }
                                seeAlso = javaBeanDeserializer4.getSeeAlso(defaultJSONParser.config, javaBeanDeserializer4.beanInfo, strStringVal);
                                if (seeAlso == null) {
                                    Class<?> clsCheckAutoType2 = defaultJSONParser.config.checkAutoType(strStringVal, javaBeanDeserializer4.clazz, jSONLexer.features);
                                    cls5 = TypeUtils.getClass(type2);
                                    if (cls5 != null) {
                                        throw new JSONException("type not match");
                                    }
                                    seeAlso = defaultJSONParser.config.getDeserializer(clsCheckAutoType2);
                                    cls4 = clsCheckAutoType2;
                                } else {
                                    cls4 = null;
                                }
                                if (seeAlso instanceof JavaBeanDeserializer) {
                                    JavaBeanDeserializer javaBeanDeserializer6 = (JavaBeanDeserializer) seeAlso;
                                    t = (T) javaBeanDeserializer6.deserialze(defaultJSONParser, cls4, obj, null);
                                    if (str != null) {
                                        fieldDeserializer3.setValue(t, strStringVal);
                                    }
                                } else {
                                    t = (T) seeAlso.deserialze(defaultJSONParser, cls4, obj);
                                }
                                if (context != null) {
                                    context.object = obj3;
                                }
                                defaultJSONParser.setContext(parseContext3);
                                return t;
                            }
                            fieldInfo3 = fieldInfo3;
                            objValueOf = objValueOf;
                            strScanSymbol = null;
                            if (obj3 == null) {
                                strScanSymbol = strScanSymbol;
                            } else {
                                strScanSymbol = strScanSymbol;
                            }
                            map7 = map7;
                            map = map7;
                            if (z2) {
                                if (z) {
                                    fieldDeserializer2.parseField(defaultJSONParser, obj3, type2, map);
                                    map5 = map;
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                } else {
                                    if (obj3 == null) {
                                        if (cls3 != Integer.TYPE) {
                                            objValueOf2 = Integer.valueOf(i2);
                                        } else {
                                            objValueOf2 = Integer.valueOf(i2);
                                        }
                                        map.put(fieldInfo3.name, objValueOf2);
                                    } else {
                                        fieldInfo4 = fieldInfo3;
                                        if (objValueOf == null) {
                                            fieldDeserializer2.setValue(obj3, objValueOf);
                                        } else if (cls3 != Integer.TYPE) {
                                            if (fieldInfo4.fieldAccess) {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            } else {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            }
                                        } else if (fieldInfo4.fieldAccess) {
                                            fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                        } else {
                                            fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                        }
                                    }
                                    if (jSONLexer.matchStat == 4) {
                                        javaBeanDeserializer3 = this;
                                        map6 = map;
                                        r3 = map;
                                        break;
                                    }
                                    javaBeanDeserializer2 = this;
                                    map5 = map;
                                }
                                javaBeanDeserializer2 = javaBeanDeserializer4;
                                map5 = map2;
                                if (jSONLexer.token == 16) {
                                    i4 = jSONLexer.token;
                                    if (i4 == 13) {
                                        jSONLexer.nextToken(16);
                                        javaBeanDeserializer3 = javaBeanDeserializer2;
                                        r3 = i4;
                                        map6 = map5;
                                        break;
                                    }
                                    if (jSONLexer.token == 18) {
                                        if (jSONLexer.token == 1) {
                                            javaBeanDeserializer4 = javaBeanDeserializer2;
                                            map3 = map5;
                                        }
                                    }
                                    throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                }
                                javaBeanDeserializer = javaBeanDeserializer2;
                                map4 = map5;
                                javaBeanDeserializer4 = javaBeanDeserializer;
                                map3 = map4;
                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                map3 = map5;
                                type2 = type;
                                map7 = map3;
                                j2 = j;
                                length = i;
                                i7 = i7;
                            } else {
                                map2 = map;
                                if (javaBeanDeserializer4.parseField(defaultJSONParser, strScanSymbol, obj3, type2, map2)) {
                                    i3 = jSONLexer.token;
                                    if (i3 == 13) {
                                        jSONLexer.nextToken();
                                        javaBeanDeserializer3 = javaBeanDeserializer4;
                                        r3 = i3;
                                        map6 = map2;
                                        break;
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer4;
                                    map4 = map2;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                } else {
                                    if (jSONLexer.token == 17) {
                                        javaBeanDeserializer2 = javaBeanDeserializer4;
                                        map5 = map2;
                                        throw new JSONException("syntax error, unexpect token ':'");
                                    }
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                    map5 = map2;
                                    if (jSONLexer.token == 16) {
                                        i4 = jSONLexer.token;
                                        if (i4 == 13) {
                                            jSONLexer.nextToken(16);
                                            javaBeanDeserializer3 = javaBeanDeserializer2;
                                            r3 = i4;
                                            map6 = map5;
                                            break;
                                        }
                                        if (jSONLexer.token == 18) {
                                            if (jSONLexer.token == 1) {
                                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                                map3 = map5;
                                            }
                                        }
                                        throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer2;
                                    map4 = map5;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                }
                            }
                        } else {
                            if (cls3 == String.class) {
                                objValueOf = jSONLexer.scanFieldString(j3);
                                if (jSONLexer.matchStat <= 0) {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                                dScanFieldDouble = 0.0d;
                                z = true;
                                z2 = true;
                                i2 = 0;
                            } else if (cls3 == Date.class) {
                                objValueOf = jSONLexer.scanFieldDate(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else if (cls3 == Boolean.TYPE || cls3 == Boolean.class) {
                                objValueOf = Boolean.valueOf(jSONLexer.scanFieldBoolean(j3));
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else if (cls3 == Float.TYPE || cls3 == Float.class) {
                                fScanFieldFloat = jSONLexer.scanFieldFloat(j3);
                                if (jSONLexer.matchStat > 0) {
                                    z = true;
                                    objValueOf = null;
                                    z2 = true;
                                } else if (jSONLexer.matchStat == -2) {
                                    j2 = jSONLexer.fieldHash;
                                } else {
                                    z = false;
                                    objValueOf = null;
                                    z2 = false;
                                }
                                i2 = 0;
                                jScanFieldLong = 0;
                                if (z2) {
                                    strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                                    if (strScanSymbol == null) {
                                        i5 = jSONLexer.token;
                                        if (i5 == 13) {
                                            jSONLexer.nextToken(16);
                                            r4 = strScanSymbol;
                                        } else {
                                            if (i5 == 16) {
                                            }
                                            j2 = j;
                                            length = i;
                                        }
                                        map6 = map7;
                                        javaBeanDeserializer3 = javaBeanDeserializer4;
                                        r3 = r4;
                                        break;
                                    }
                                    if ("$ref" != strScanSymbol) {
                                    }
                                    if (str == null) {
                                    }
                                    jSONLexer.nextTokenWithChar(':');
                                    if (jSONLexer.token != 4) {
                                        throw new JSONException("syntax error");
                                    }
                                    strStringVal = jSONLexer.stringVal();
                                    jSONLexer.nextToken(16);
                                    if (type2 instanceof Class) {
                                    }
                                    seeAlso = javaBeanDeserializer4.getSeeAlso(defaultJSONParser.config, javaBeanDeserializer4.beanInfo, strStringVal);
                                    if (seeAlso == null) {
                                        Class<?> clsCheckAutoType3 = defaultJSONParser.config.checkAutoType(strStringVal, javaBeanDeserializer4.clazz, jSONLexer.features);
                                        cls5 = TypeUtils.getClass(type2);
                                        if (cls5 != null) {
                                            throw new JSONException("type not match");
                                        }
                                        seeAlso = defaultJSONParser.config.getDeserializer(clsCheckAutoType3);
                                        cls4 = clsCheckAutoType3;
                                    } else {
                                        cls4 = null;
                                    }
                                    if (seeAlso instanceof JavaBeanDeserializer) {
                                        JavaBeanDeserializer javaBeanDeserializer7 = (JavaBeanDeserializer) seeAlso;
                                        t = (T) javaBeanDeserializer7.deserialze(defaultJSONParser, cls4, obj, null);
                                        if (str != null) {
                                            fieldDeserializer3.setValue(t, strStringVal);
                                        }
                                    } else {
                                        t = (T) seeAlso.deserialze(defaultJSONParser, cls4, obj);
                                    }
                                    if (context != null) {
                                        context.object = obj3;
                                    }
                                    defaultJSONParser.setContext(parseContext3);
                                    return t;
                                }
                                fieldInfo3 = fieldInfo3;
                                objValueOf = objValueOf;
                                strScanSymbol = null;
                                if (obj3 == null) {
                                    strScanSymbol = strScanSymbol;
                                } else {
                                    strScanSymbol = strScanSymbol;
                                }
                                map7 = map7;
                                map = map7;
                                if (z2) {
                                    if (z) {
                                        fieldDeserializer2.parseField(defaultJSONParser, obj3, type2, map);
                                        map5 = map;
                                        javaBeanDeserializer2 = javaBeanDeserializer4;
                                    } else {
                                        if (obj3 == null) {
                                            if (cls3 != Integer.TYPE) {
                                                objValueOf2 = Integer.valueOf(i2);
                                            } else {
                                                objValueOf2 = Integer.valueOf(i2);
                                            }
                                            map.put(fieldInfo3.name, objValueOf2);
                                        } else {
                                            fieldInfo4 = fieldInfo3;
                                            if (objValueOf == null) {
                                                fieldDeserializer2.setValue(obj3, objValueOf);
                                            } else if (cls3 != Integer.TYPE) {
                                                if (fieldInfo4.fieldAccess) {
                                                    fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                                } else {
                                                    fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                                }
                                            } else if (fieldInfo4.fieldAccess) {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            } else {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            }
                                        }
                                        if (jSONLexer.matchStat == 4) {
                                            javaBeanDeserializer3 = this;
                                            map6 = map;
                                            r3 = map;
                                            break;
                                        }
                                        javaBeanDeserializer2 = this;
                                        map5 = map;
                                    }
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                    map5 = map2;
                                    if (jSONLexer.token == 16) {
                                        i4 = jSONLexer.token;
                                        if (i4 == 13) {
                                            jSONLexer.nextToken(16);
                                            javaBeanDeserializer3 = javaBeanDeserializer2;
                                            r3 = i4;
                                            map6 = map5;
                                            break;
                                        }
                                        if (jSONLexer.token == 18) {
                                            if (jSONLexer.token == 1) {
                                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                                map3 = map5;
                                            }
                                        }
                                        throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer2;
                                    map4 = map5;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                } else {
                                    map2 = map;
                                    if (javaBeanDeserializer4.parseField(defaultJSONParser, strScanSymbol, obj3, type2, map2)) {
                                        i3 = jSONLexer.token;
                                        if (i3 == 13) {
                                            jSONLexer.nextToken();
                                            javaBeanDeserializer3 = javaBeanDeserializer4;
                                            r3 = i3;
                                            map6 = map2;
                                            break;
                                        }
                                        javaBeanDeserializer = javaBeanDeserializer4;
                                        map4 = map2;
                                        javaBeanDeserializer4 = javaBeanDeserializer;
                                        map3 = map4;
                                        javaBeanDeserializer4 = javaBeanDeserializer2;
                                        map3 = map5;
                                        type2 = type;
                                        map7 = map3;
                                        j2 = j;
                                        length = i;
                                        i7 = i7;
                                    } else {
                                        if (jSONLexer.token == 17) {
                                            javaBeanDeserializer2 = javaBeanDeserializer4;
                                            map5 = map2;
                                            throw new JSONException("syntax error, unexpect token ':'");
                                        }
                                        javaBeanDeserializer2 = javaBeanDeserializer4;
                                        map5 = map2;
                                        if (jSONLexer.token == 16) {
                                            i4 = jSONLexer.token;
                                            if (i4 == 13) {
                                                jSONLexer.nextToken(16);
                                                javaBeanDeserializer3 = javaBeanDeserializer2;
                                                r3 = i4;
                                                map6 = map5;
                                                break;
                                            }
                                            if (jSONLexer.token == 18) {
                                                if (jSONLexer.token == 1) {
                                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                                    map3 = map5;
                                                }
                                            }
                                            throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                        }
                                        javaBeanDeserializer = javaBeanDeserializer2;
                                        map4 = map5;
                                        javaBeanDeserializer4 = javaBeanDeserializer;
                                        map3 = map4;
                                        javaBeanDeserializer4 = javaBeanDeserializer2;
                                        map3 = map5;
                                        type2 = type;
                                        map7 = map3;
                                        j2 = j;
                                        length = i;
                                        i7 = i7;
                                    }
                                }
                            } else if (cls3 == Double.TYPE || cls3 == Double.class) {
                                dScanFieldDouble = jSONLexer.scanFieldDouble(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = dScanFieldDouble;
                                    z = true;
                                    objValueOf = null;
                                    z2 = true;
                                    i2 = 0;
                                } else if (jSONLexer.matchStat == -2) {
                                    j2 = jSONLexer.fieldHash;
                                }
                            } else if (fieldInfo3.isEnum && (defaultJSONParser.config.getDeserializer(cls3) instanceof EnumDeserializer)) {
                                long jScanFieldSymbol = jSONLexer.scanFieldSymbol(j3);
                                if (jSONLexer.matchStat > 0) {
                                    objValueOf = fieldDeserializer2.getEnumByHashCode(jScanFieldSymbol);
                                    z = true;
                                    z3 = true;
                                } else if (jSONLexer.matchStat == -2) {
                                    j2 = jSONLexer.fieldHash;
                                } else {
                                    z = false;
                                    objValueOf = null;
                                    z3 = false;
                                }
                                z2 = z3;
                                dScanFieldDouble = 0.0d;
                                i2 = 0;
                            } else if (cls3 == int[].class) {
                                objValueOf = jSONLexer.scanFieldIntArray(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else if (cls3 == float[].class) {
                                objValueOf = jSONLexer.scanFieldFloatArray(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else if (cls3 == double[].class) {
                                objValueOf = jSONLexer.scanFieldDoubleArray(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else if (cls3 == float[][].class) {
                                objValueOf = jSONLexer.scanFieldFloatArray2(j3);
                                if (jSONLexer.matchStat > 0) {
                                    dScanFieldDouble = 0.0d;
                                    z = true;
                                    z2 = true;
                                    i2 = 0;
                                } else {
                                    if (jSONLexer.matchStat == -2) {
                                        j2 = jSONLexer.fieldHash;
                                    }
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    z2 = false;
                                    i2 = 0;
                                }
                            } else {
                                if (cls3 == double[][].class) {
                                    objValueOf = jSONLexer.scanFieldDoubleArray2(j3);
                                    if (jSONLexer.matchStat > 0) {
                                        dScanFieldDouble = 0.0d;
                                        z = true;
                                        z2 = true;
                                        i2 = 0;
                                    } else {
                                        if (jSONLexer.matchStat == -2) {
                                            j2 = jSONLexer.fieldHash;
                                        }
                                        dScanFieldDouble = 0.0d;
                                        z = false;
                                        z2 = false;
                                        i2 = 0;
                                    }
                                } else if (jSONLexer.matchField(fieldInfo3.nameHashCode)) {
                                    dScanFieldDouble = 0.0d;
                                    z = false;
                                    objValueOf = null;
                                    z2 = true;
                                    i2 = 0;
                                }
                                j2 = j;
                                length = i;
                            }
                            fScanFieldFloat = 0.0f;
                            jScanFieldLong = 0;
                            if (z2) {
                                strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                                if (strScanSymbol == null) {
                                    i5 = jSONLexer.token;
                                    if (i5 == 13) {
                                        jSONLexer.nextToken(16);
                                        r4 = strScanSymbol;
                                    } else {
                                        if (i5 == 16) {
                                        }
                                        j2 = j;
                                        length = i;
                                    }
                                    map6 = map7;
                                    javaBeanDeserializer3 = javaBeanDeserializer4;
                                    r3 = r4;
                                    break;
                                }
                                if ("$ref" != strScanSymbol) {
                                }
                                if (str == null) {
                                }
                                jSONLexer.nextTokenWithChar(':');
                                if (jSONLexer.token != 4) {
                                    throw new JSONException("syntax error");
                                }
                                strStringVal = jSONLexer.stringVal();
                                jSONLexer.nextToken(16);
                                if (type2 instanceof Class) {
                                }
                                seeAlso = javaBeanDeserializer4.getSeeAlso(defaultJSONParser.config, javaBeanDeserializer4.beanInfo, strStringVal);
                                if (seeAlso == null) {
                                    Class<?> clsCheckAutoType4 = defaultJSONParser.config.checkAutoType(strStringVal, javaBeanDeserializer4.clazz, jSONLexer.features);
                                    cls5 = TypeUtils.getClass(type2);
                                    if (cls5 != null) {
                                        throw new JSONException("type not match");
                                    }
                                    seeAlso = defaultJSONParser.config.getDeserializer(clsCheckAutoType4);
                                    cls4 = clsCheckAutoType4;
                                } else {
                                    cls4 = null;
                                }
                                if (seeAlso instanceof JavaBeanDeserializer) {
                                    JavaBeanDeserializer javaBeanDeserializer8 = (JavaBeanDeserializer) seeAlso;
                                    t = (T) javaBeanDeserializer8.deserialze(defaultJSONParser, cls4, obj, null);
                                    if (str != null) {
                                        fieldDeserializer3.setValue(t, strStringVal);
                                    }
                                } else {
                                    t = (T) seeAlso.deserialze(defaultJSONParser, cls4, obj);
                                }
                                if (context != null) {
                                    context.object = obj3;
                                }
                                defaultJSONParser.setContext(parseContext3);
                                return t;
                            }
                            fieldInfo3 = fieldInfo3;
                            objValueOf = objValueOf;
                            strScanSymbol = null;
                            if (obj3 == null) {
                                strScanSymbol = strScanSymbol;
                            } else {
                                strScanSymbol = strScanSymbol;
                            }
                            map7 = map7;
                            map = map7;
                            if (z2) {
                                if (z) {
                                    fieldDeserializer2.parseField(defaultJSONParser, obj3, type2, map);
                                    map5 = map;
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                } else {
                                    if (obj3 == null) {
                                        if (cls3 != Integer.TYPE) {
                                            objValueOf2 = Integer.valueOf(i2);
                                        } else {
                                            objValueOf2 = Integer.valueOf(i2);
                                        }
                                        map.put(fieldInfo3.name, objValueOf2);
                                    } else {
                                        fieldInfo4 = fieldInfo3;
                                        if (objValueOf == null) {
                                            fieldDeserializer2.setValue(obj3, objValueOf);
                                        } else if (cls3 != Integer.TYPE) {
                                            if (fieldInfo4.fieldAccess) {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            } else {
                                                fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                            }
                                        } else if (fieldInfo4.fieldAccess) {
                                            fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                        } else {
                                            fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                        }
                                    }
                                    if (jSONLexer.matchStat == 4) {
                                        javaBeanDeserializer3 = this;
                                        map6 = map;
                                        r3 = map;
                                        break;
                                    }
                                    javaBeanDeserializer2 = this;
                                    map5 = map;
                                }
                                javaBeanDeserializer2 = javaBeanDeserializer4;
                                map5 = map2;
                                if (jSONLexer.token == 16) {
                                    i4 = jSONLexer.token;
                                    if (i4 == 13) {
                                        jSONLexer.nextToken(16);
                                        javaBeanDeserializer3 = javaBeanDeserializer2;
                                        r3 = i4;
                                        map6 = map5;
                                        break;
                                    }
                                    if (jSONLexer.token == 18) {
                                        if (jSONLexer.token == 1) {
                                            javaBeanDeserializer4 = javaBeanDeserializer2;
                                            map3 = map5;
                                        }
                                    }
                                    throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                }
                                javaBeanDeserializer = javaBeanDeserializer2;
                                map4 = map5;
                                javaBeanDeserializer4 = javaBeanDeserializer;
                                map3 = map4;
                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                map3 = map5;
                                type2 = type;
                                map7 = map3;
                                j2 = j;
                                length = i;
                                i7 = i7;
                            } else {
                                map2 = map;
                                if (javaBeanDeserializer4.parseField(defaultJSONParser, strScanSymbol, obj3, type2, map2)) {
                                    i3 = jSONLexer.token;
                                    if (i3 == 13) {
                                        jSONLexer.nextToken();
                                        javaBeanDeserializer3 = javaBeanDeserializer4;
                                        r3 = i3;
                                        map6 = map2;
                                        break;
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer4;
                                    map4 = map2;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                } else {
                                    if (jSONLexer.token == 17) {
                                        javaBeanDeserializer2 = javaBeanDeserializer4;
                                        map5 = map2;
                                        throw new JSONException("syntax error, unexpect token ':'");
                                    }
                                    javaBeanDeserializer2 = javaBeanDeserializer4;
                                    map5 = map2;
                                    if (jSONLexer.token == 16) {
                                        i4 = jSONLexer.token;
                                        if (i4 == 13) {
                                            jSONLexer.nextToken(16);
                                            javaBeanDeserializer3 = javaBeanDeserializer2;
                                            r3 = i4;
                                            map6 = map5;
                                            break;
                                        }
                                        if (jSONLexer.token == 18) {
                                            if (jSONLexer.token == 1) {
                                                javaBeanDeserializer4 = javaBeanDeserializer2;
                                                map3 = map5;
                                            }
                                        }
                                        throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                                    }
                                    javaBeanDeserializer = javaBeanDeserializer2;
                                    map4 = map5;
                                    javaBeanDeserializer4 = javaBeanDeserializer;
                                    map3 = map4;
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                    type2 = type;
                                    map7 = map3;
                                    j2 = j;
                                    length = i;
                                    i7 = i7;
                                }
                            }
                        }
                        length = i;
                    } else {
                        i = length;
                        j = j2;
                    }
                    dScanFieldDouble = dScanFieldDouble;
                    z = false;
                    objValueOf = null;
                    z2 = false;
                    i2 = 0;
                    fScanFieldFloat = 0.0f;
                    jScanFieldLong = 0;
                    if (z2) {
                        strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                        if (strScanSymbol == null) {
                            i5 = jSONLexer.token;
                            if (i5 == 13) {
                                jSONLexer.nextToken(16);
                                r4 = strScanSymbol;
                            } else {
                                if (i5 == 16) {
                                }
                                j2 = j;
                                length = i;
                            }
                            map6 = map7;
                            javaBeanDeserializer3 = javaBeanDeserializer4;
                            r3 = r4;
                            break;
                        }
                        if ("$ref" != strScanSymbol) {
                        }
                        if (str == null) {
                        }
                        jSONLexer.nextTokenWithChar(':');
                        if (jSONLexer.token != 4) {
                            throw new JSONException("syntax error");
                        }
                        strStringVal = jSONLexer.stringVal();
                        jSONLexer.nextToken(16);
                        if (type2 instanceof Class) {
                        }
                        seeAlso = javaBeanDeserializer4.getSeeAlso(defaultJSONParser.config, javaBeanDeserializer4.beanInfo, strStringVal);
                        if (seeAlso == null) {
                            Class<?> clsCheckAutoType5 = defaultJSONParser.config.checkAutoType(strStringVal, javaBeanDeserializer4.clazz, jSONLexer.features);
                            cls5 = TypeUtils.getClass(type2);
                            if (cls5 != null) {
                                throw new JSONException("type not match");
                            }
                            seeAlso = defaultJSONParser.config.getDeserializer(clsCheckAutoType5);
                            cls4 = clsCheckAutoType5;
                        } else {
                            cls4 = null;
                        }
                        if (seeAlso instanceof JavaBeanDeserializer) {
                            JavaBeanDeserializer javaBeanDeserializer9 = (JavaBeanDeserializer) seeAlso;
                            t = (T) javaBeanDeserializer9.deserialze(defaultJSONParser, cls4, obj, null);
                            if (str != null) {
                                fieldDeserializer3.setValue(t, strStringVal);
                            }
                        } else {
                            t = (T) seeAlso.deserialze(defaultJSONParser, cls4, obj);
                        }
                        if (context != null) {
                            context.object = obj3;
                        }
                        defaultJSONParser.setContext(parseContext3);
                        return t;
                    }
                    fieldInfo3 = fieldInfo3;
                    objValueOf = objValueOf;
                    strScanSymbol = null;
                    if (obj3 == null) {
                        strScanSymbol = strScanSymbol;
                    } else {
                        strScanSymbol = strScanSymbol;
                    }
                    map7 = map7;
                    map = map7;
                    if (z2) {
                        if (z) {
                            fieldDeserializer2.parseField(defaultJSONParser, obj3, type2, map);
                            map5 = map;
                            javaBeanDeserializer2 = javaBeanDeserializer4;
                        } else {
                            if (obj3 == null) {
                                if (cls3 != Integer.TYPE) {
                                    objValueOf2 = Integer.valueOf(i2);
                                } else {
                                    objValueOf2 = Integer.valueOf(i2);
                                }
                                map.put(fieldInfo3.name, objValueOf2);
                            } else {
                                fieldInfo4 = fieldInfo3;
                                if (objValueOf == null) {
                                    fieldDeserializer2.setValue(obj3, objValueOf);
                                } else if (cls3 != Integer.TYPE) {
                                    if (fieldInfo4.fieldAccess) {
                                        fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                    } else {
                                        fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                    }
                                } else if (fieldInfo4.fieldAccess) {
                                    fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                } else {
                                    fieldDeserializer2.setValue(obj3, Integer.valueOf(i2));
                                }
                            }
                            if (jSONLexer.matchStat == 4) {
                                javaBeanDeserializer3 = this;
                                map6 = map;
                                r3 = map;
                                break;
                            }
                            javaBeanDeserializer2 = this;
                            map5 = map;
                        }
                        javaBeanDeserializer2 = javaBeanDeserializer4;
                        map5 = map2;
                        if (jSONLexer.token == 16) {
                            i4 = jSONLexer.token;
                            if (i4 == 13) {
                                jSONLexer.nextToken(16);
                                javaBeanDeserializer3 = javaBeanDeserializer2;
                                r3 = i4;
                                map6 = map5;
                                break;
                            }
                            if (jSONLexer.token == 18) {
                                if (jSONLexer.token == 1) {
                                    javaBeanDeserializer4 = javaBeanDeserializer2;
                                    map3 = map5;
                                }
                            }
                            throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                        }
                        javaBeanDeserializer = javaBeanDeserializer2;
                        map4 = map5;
                        javaBeanDeserializer4 = javaBeanDeserializer;
                        map3 = map4;
                        javaBeanDeserializer4 = javaBeanDeserializer2;
                        map3 = map5;
                        type2 = type;
                        map7 = map3;
                        j2 = j;
                        length = i;
                        i7 = i7;
                    } else {
                        map2 = map;
                        if (javaBeanDeserializer4.parseField(defaultJSONParser, strScanSymbol, obj3, type2, map2)) {
                            i3 = jSONLexer.token;
                            if (i3 == 13) {
                                jSONLexer.nextToken();
                                javaBeanDeserializer3 = javaBeanDeserializer4;
                                r3 = i3;
                                map6 = map2;
                                break;
                            }
                            javaBeanDeserializer = javaBeanDeserializer4;
                            map4 = map2;
                            javaBeanDeserializer4 = javaBeanDeserializer;
                            map3 = map4;
                            javaBeanDeserializer4 = javaBeanDeserializer2;
                            map3 = map5;
                            type2 = type;
                            map7 = map3;
                            j2 = j;
                            length = i;
                            i7 = i7;
                        } else {
                            if (jSONLexer.token == 17) {
                                javaBeanDeserializer2 = javaBeanDeserializer4;
                                map5 = map2;
                                throw new JSONException("syntax error, unexpect token ':'");
                            }
                            javaBeanDeserializer2 = javaBeanDeserializer4;
                            map5 = map2;
                            if (jSONLexer.token == 16) {
                                i4 = jSONLexer.token;
                                if (i4 == 13) {
                                    jSONLexer.nextToken(16);
                                    javaBeanDeserializer3 = javaBeanDeserializer2;
                                    r3 = i4;
                                    map6 = map5;
                                    break;
                                }
                                if (jSONLexer.token == 18) {
                                    if (jSONLexer.token == 1) {
                                        javaBeanDeserializer4 = javaBeanDeserializer2;
                                        map3 = map5;
                                    }
                                }
                                throw new JSONException("syntax error, unexpect token " + JSONToken.name(jSONLexer.token));
                            }
                            javaBeanDeserializer = javaBeanDeserializer2;
                            map4 = map5;
                            javaBeanDeserializer4 = javaBeanDeserializer;
                            map3 = map4;
                            javaBeanDeserializer4 = javaBeanDeserializer2;
                            map3 = map5;
                            type2 = type;
                            map7 = map3;
                            j2 = j;
                            length = i;
                            i7 = i7;
                        }
                    }
                }
                if (obj3 == null) {
                    try {
                        if (map6 == null) {
                            T t4 = (T) createInstance((DefaultJSONParser) defaultJSONParser, type);
                            if (context == null) {
                                context = defaultJSONParser.setContext(parseContext3, t4, obj);
                            }
                            if (context != null) {
                                context.object = t4;
                            }
                            defaultJSONParser.setContext(parseContext3);
                            return t4;
                        }
                        String[] strArr = javaBeanDeserializer3.beanInfo.creatorConstructorParameters;
                        int length2 = strArr != null ? strArr.length : javaBeanDeserializer3.fieldDeserializers.length;
                        Object[] objArr = new Object[length2];
                        for (int i10 = 0; i10 < length2; i10++) {
                            FieldInfo fieldInfo6 = javaBeanDeserializer3.fieldDeserializers[i10].fieldInfo;
                            Object objRemove = strArr != null ? map6.remove(fieldInfo6.name) : map6.get(fieldInfo6.name);
                            if (objRemove == null) {
                                objRemove = TypeUtils.defaultValue(fieldInfo6.fieldClass);
                            }
                            objArr[i10] = objRemove;
                        }
                        if (javaBeanDeserializer3.beanInfo.creatorConstructor != null) {
                            try {
                                Object objNewInstance = javaBeanDeserializer3.beanInfo.creatorConstructor.newInstance(objArr);
                                if (strArr != null) {
                                    for (Map.Entry<String, Object> entry : map6.entrySet()) {
                                        FieldDeserializer fieldDeserializer5 = javaBeanDeserializer3.getFieldDeserializer(entry.getKey());
                                        if (fieldDeserializer5 != null) {
                                            fieldDeserializer5.setValue(objNewInstance, entry.getValue());
                                        }
                                    }
                                }
                                obj3 = (T) objNewInstance;
                            } catch (Exception e2) {
                                throw new JSONException("create instance error, " + javaBeanDeserializer3.beanInfo.creatorConstructor.toGenericString(), e2);
                            }
                        } else if (javaBeanDeserializer3.beanInfo.factoryMethod != null) {
                            try {
                                obj3 = (T) javaBeanDeserializer3.beanInfo.factoryMethod.invoke(null, objArr);
                            } catch (Exception e3) {
                                throw new JSONException("create factory method error, " + javaBeanDeserializer3.beanInfo.factoryMethod.toString(), e3);
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        obj3 = (T) r3;
                        parseContext = context;
                        if (parseContext != null) {
                            parseContext.object = obj3;
                        }
                        defaultJSONParser.setContext(parseContext3);
                        throw th;
                    }
                }
                if (context != null) {
                    context.object = obj3;
                }
                defaultJSONParser.setContext(parseContext3);
                return (T) obj3;
            } catch (Throwable th4) {
                th = th4;
                obj3 = obj2;
                if (parseContext != null) {
                    parseContext.object = obj3;
                }
                defaultJSONParser.setContext(parseContext3);
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    protected FieldDeserializer getFieldDeserializerByHash(long j) {
        int i = 0;
        while (true) {
            FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
            if (i >= fieldDeserializerArr.length) {
                return null;
            }
            FieldDeserializer fieldDeserializer = fieldDeserializerArr[i];
            if (fieldDeserializer.fieldInfo.nameHashCode == j) {
                return fieldDeserializer;
            }
            i++;
        }
    }

    protected FieldDeserializer getFieldDeserializer(String str) {
        if (str == null) {
            return null;
        }
        int i = 0;
        if (!this.beanInfo.ordered) {
            int length = this.sortedFieldDeserializers.length - 1;
            while (i <= length) {
                int i2 = (i + length) >>> 1;
                int iCompareTo = this.sortedFieldDeserializers[i2].fieldInfo.name.compareTo(str);
                if (iCompareTo < 0) {
                    i = i2 + 1;
                } else {
                    if (iCompareTo <= 0) {
                        return this.sortedFieldDeserializers[i2];
                    }
                    length = i2 - 1;
                }
            }
            Map<String, FieldDeserializer> map = this.alterNameFieldDeserializers;
            if (map != null) {
                return map.get(str);
            }
            return null;
        }
        while (true) {
            FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
            if (i >= fieldDeserializerArr.length) {
                return null;
            }
            FieldDeserializer fieldDeserializer = fieldDeserializerArr[i];
            if (fieldDeserializer.fieldInfo.name.equalsIgnoreCase(str)) {
                return fieldDeserializer;
            }
            i++;
        }
    }

    private boolean parseField(DefaultJSONParser defaultJSONParser, String str, Object obj, Type type, Map<String, Object> map) {
        boolean zStartsWith;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        FieldDeserializer fieldDeserializer = getFieldDeserializer(str);
        if (fieldDeserializer == null) {
            long jFnv_64_lower = TypeUtils.fnv_64_lower(str);
            if (this.smartMatchHashArray == null) {
                long[] jArr = new long[this.sortedFieldDeserializers.length];
                int i = 0;
                while (true) {
                    FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
                    if (i >= fieldDeserializerArr.length) {
                        break;
                    }
                    jArr[i] = TypeUtils.fnv_64_lower(fieldDeserializerArr[i].fieldInfo.name);
                    i++;
                }
                Arrays.sort(jArr);
                this.smartMatchHashArray = jArr;
            }
            int iBinarySearch = Arrays.binarySearch(this.smartMatchHashArray, jFnv_64_lower);
            if (iBinarySearch < 0) {
                zStartsWith = str.startsWith("is");
                if (zStartsWith) {
                    iBinarySearch = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv_64_lower(str.substring(2)));
                }
            } else {
                zStartsWith = false;
            }
            if (iBinarySearch >= 0) {
                if (this.smartMatchHashArrayMapping == null) {
                    int[] iArr = new int[this.smartMatchHashArray.length];
                    Arrays.fill(iArr, -1);
                    int i2 = 0;
                    while (true) {
                        FieldDeserializer[] fieldDeserializerArr2 = this.sortedFieldDeserializers;
                        if (i2 >= fieldDeserializerArr2.length) {
                            break;
                        }
                        int iBinarySearch2 = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv_64_lower(fieldDeserializerArr2[i2].fieldInfo.name));
                        if (iBinarySearch2 >= 0) {
                            iArr[iBinarySearch2] = i2;
                        }
                        i2++;
                    }
                    this.smartMatchHashArrayMapping = iArr;
                }
                int i3 = this.smartMatchHashArrayMapping[iBinarySearch];
                if (i3 != -1) {
                    fieldDeserializer = this.sortedFieldDeserializers[i3];
                    Class<?> cls = fieldDeserializer.fieldInfo.fieldClass;
                    if (zStartsWith && cls != Boolean.TYPE && cls != Boolean.class) {
                        fieldDeserializer = null;
                    }
                }
            }
        }
        int i4 = Feature.SupportNonPublicField.mask;
        if (fieldDeserializer == null && ((defaultJSONParser.lexer.features & i4) != 0 || (i4 & this.beanInfo.parserFeatures) != 0)) {
            if (this.extraFieldDeserializers == null) {
                ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(1, 0.75f, 1);
                for (Class<?> superclass = this.clazz; superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
                    for (Field field : superclass.getDeclaredFields()) {
                        String name = field.getName();
                        if (getFieldDeserializer(name) == null) {
                            int modifiers = field.getModifiers();
                            if ((modifiers & 16) == 0 && (modifiers & 8) == 0) {
                                concurrentHashMap.put(name, field);
                            }
                        }
                    }
                }
                this.extraFieldDeserializers = concurrentHashMap;
            }
            Object obj2 = this.extraFieldDeserializers.get(str);
            if (obj2 != null) {
                if (obj2 instanceof FieldDeserializer) {
                    fieldDeserializer = (FieldDeserializer) obj2;
                } else {
                    Field field2 = (Field) obj2;
                    field2.setAccessible(true);
                    DefaultFieldDeserializer defaultFieldDeserializer = new DefaultFieldDeserializer(defaultJSONParser.config, this.clazz, new FieldInfo(str, field2.getDeclaringClass(), field2.getType(), field2.getGenericType(), field2, 0, 0));
                    this.extraFieldDeserializers.put(str, defaultFieldDeserializer);
                    fieldDeserializer = defaultFieldDeserializer;
                }
            }
        }
        if (fieldDeserializer == null) {
            parseExtra(defaultJSONParser, obj, str);
            return false;
        }
        jSONLexer.nextTokenWithChar(':');
        fieldDeserializer.parseField(defaultJSONParser, obj, type, map);
        return true;
    }

    void parseExtra(DefaultJSONParser defaultJSONParser, Object obj, String str) {
        Object object;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if ((defaultJSONParser.lexer.features & Feature.IgnoreNotMatch.mask) == 0) {
            throw new JSONException("setter not found, class " + this.clazz.getName() + ", property " + str);
        }
        jSONLexer.nextTokenWithChar(':');
        List<ExtraTypeProvider> list = defaultJSONParser.extraTypeProviders;
        Type extraType = null;
        if (list != null) {
            Iterator<ExtraTypeProvider> it = list.iterator();
            while (it.hasNext()) {
                extraType = it.next().getExtraType(obj, str);
            }
        }
        if (extraType == null) {
            object = defaultJSONParser.parse();
        } else {
            object = defaultJSONParser.parseObject(extraType);
        }
        if (obj instanceof ExtraProcessable) {
            ((ExtraProcessable) obj).processExtra(str, object);
            return;
        }
        List<ExtraProcessor> list2 = defaultJSONParser.extraProcessors;
        if (list2 != null) {
            Iterator<ExtraProcessor> it2 = list2.iterator();
            while (it2.hasNext()) {
                it2.next().processExtra(obj, str, object);
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:61:0x00fb  */
    /* JADX WARN: Code duplicated, block: B:68:0x0111  */
    /* JADX WARN: Code duplicated, block: B:70:0x0115  */
    /* JADX WARN: Code duplicated, block: B:71:0x011c  */
    public Object createInstance(Map<String, Object> map, ParserConfig parserConfig) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String str;
        Object objCast;
        float f;
        double d;
        if (this.beanInfo.creatorConstructor == null) {
            Object objCreateInstance = createInstance((DefaultJSONParser) null, this.clazz);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                FieldDeserializer fieldDeserializer = getFieldDeserializer(entry.getKey());
                if (fieldDeserializer != null) {
                    Object value = entry.getValue();
                    Method method = fieldDeserializer.fieldInfo.method;
                    if (method != null) {
                        method.invoke(objCreateInstance, TypeUtils.cast(value, method.getGenericParameterTypes()[0], parserConfig));
                    } else {
                        Field field = fieldDeserializer.fieldInfo.field;
                        Type type = fieldDeserializer.fieldInfo.fieldType;
                        if (type == Boolean.TYPE) {
                            if (value == Boolean.FALSE) {
                                field.setBoolean(objCreateInstance, false);
                            } else if (value == Boolean.TRUE) {
                                field.setBoolean(objCreateInstance, true);
                            } else {
                                str = fieldDeserializer.fieldInfo.format;
                                if (str == null && type == Date.class && (value instanceof String)) {
                                    try {
                                        objCast = new SimpleDateFormat(str).parse((String) value);
                                    } catch (ParseException unused) {
                                        objCast = null;
                                    }
                                } else if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                                field.set(objCreateInstance, objCast);
                            }
                        } else if (type == Integer.TYPE) {
                            if (value instanceof Number) {
                                field.setInt(objCreateInstance, ((Number) value).intValue());
                            } else {
                                str = fieldDeserializer.fieldInfo.format;
                                if (str == null) {
                                    if (type instanceof ParameterizedType) {
                                        objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                    } else {
                                        objCast = TypeUtils.cast(value, type, parserConfig);
                                    }
                                } else if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                                field.set(objCreateInstance, objCast);
                            }
                        } else if (type == Long.TYPE) {
                            if (value instanceof Number) {
                                field.setLong(objCreateInstance, ((Number) value).longValue());
                            } else {
                                str = fieldDeserializer.fieldInfo.format;
                                if (str == null) {
                                    if (type instanceof ParameterizedType) {
                                        objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                    } else {
                                        objCast = TypeUtils.cast(value, type, parserConfig);
                                    }
                                } else if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                                field.set(objCreateInstance, objCast);
                            }
                        } else if (type == Float.TYPE) {
                            if (value instanceof Number) {
                                field.setFloat(objCreateInstance, ((Number) value).floatValue());
                            } else if (value instanceof String) {
                                String str2 = (String) value;
                                if (str2.length() <= 10) {
                                    f = TypeUtils.parseFloat(str2);
                                } else {
                                    f = Float.parseFloat(str2);
                                }
                                field.setFloat(objCreateInstance, f);
                            } else {
                                str = fieldDeserializer.fieldInfo.format;
                                if (str == null) {
                                    if (type instanceof ParameterizedType) {
                                        objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                    } else {
                                        objCast = TypeUtils.cast(value, type, parserConfig);
                                    }
                                } else if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                                field.set(objCreateInstance, objCast);
                            }
                        } else if (type == Double.TYPE) {
                            if (value instanceof Number) {
                                field.setDouble(objCreateInstance, ((Number) value).doubleValue());
                            } else if (value instanceof String) {
                                String str3 = (String) value;
                                if (str3.length() <= 10) {
                                    d = TypeUtils.parseDouble(str3);
                                } else {
                                    d = Double.parseDouble(str3);
                                }
                                field.setDouble(objCreateInstance, d);
                            } else {
                                str = fieldDeserializer.fieldInfo.format;
                                if (str == null) {
                                    if (type instanceof ParameterizedType) {
                                        objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                    } else {
                                        objCast = TypeUtils.cast(value, type, parserConfig);
                                    }
                                } else if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                                field.set(objCreateInstance, objCast);
                            }
                        } else if (value != null && type == value.getClass()) {
                            field.set(objCreateInstance, value);
                        } else {
                            str = fieldDeserializer.fieldInfo.format;
                            if (str == null) {
                                if (type instanceof ParameterizedType) {
                                    objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                                } else {
                                    objCast = TypeUtils.cast(value, type, parserConfig);
                                }
                            } else if (type instanceof ParameterizedType) {
                                objCast = TypeUtils.cast(value, (ParameterizedType) type, parserConfig);
                            } else {
                                objCast = TypeUtils.cast(value, type, parserConfig);
                            }
                            field.set(objCreateInstance, objCast);
                        }
                    }
                }
            }
            return objCreateInstance;
        }
        FieldInfo[] fieldInfoArr = this.beanInfo.fields;
        int length = fieldInfoArr.length;
        Object[] objArr = new Object[length];
        for (int i = 0; i < length; i++) {
            FieldInfo fieldInfo = fieldInfoArr[i];
            Object objDefaultValue = map.get(fieldInfo.name);
            if (objDefaultValue == null) {
                objDefaultValue = TypeUtils.defaultValue(fieldInfo.fieldClass);
            }
            objArr[i] = objDefaultValue;
        }
        if (this.beanInfo.creatorConstructor == null) {
            return null;
        }
        try {
            return this.beanInfo.creatorConstructor.newInstance(objArr);
        } catch (Exception e) {
            throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e);
        }
    }

    protected JavaBeanDeserializer getSeeAlso(ParserConfig parserConfig, JavaBeanInfo javaBeanInfo, String str) {
        if (javaBeanInfo.jsonType == null) {
            return null;
        }
        for (Class<?> cls : javaBeanInfo.jsonType.seeAlso()) {
            ObjectDeserializer deserializer = parserConfig.getDeserializer(cls);
            if (deserializer instanceof JavaBeanDeserializer) {
                JavaBeanDeserializer javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
                JavaBeanInfo javaBeanInfo2 = javaBeanDeserializer.beanInfo;
                if (javaBeanInfo2.typeName.equals(str)) {
                    return javaBeanDeserializer;
                }
                JavaBeanDeserializer seeAlso = getSeeAlso(parserConfig, javaBeanInfo2, str);
                if (seeAlso != null) {
                    return seeAlso;
                }
            }
        }
        return null;
    }
}
