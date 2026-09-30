package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import cz.msebera.android.httpclient.message.TokenParser;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* JADX INFO: loaded from: classes.dex */
public class TypeUtils {
    public static boolean compatibleWithJavaBean = false;
    private static volatile Map<Class, String[]> kotlinIgnores = null;
    private static volatile boolean kotlinIgnores_error = false;
    private static volatile boolean kotlin_class_klass_error = false;
    private static volatile boolean kotlin_error = false;
    private static volatile Constructor kotlin_kclass_constructor = null;
    private static volatile Method kotlin_kclass_getConstructors = null;
    private static volatile Method kotlin_kfunction_getParameters = null;
    private static volatile Method kotlin_kparameter_getName = null;
    private static volatile Class kotlin_metadata = null;
    private static volatile boolean kotlin_metadata_error = false;
    private static final ConcurrentMap<String, Class<?>> mappings;
    private static boolean setAccessibleEnable = true;

    public static boolean isKotlin(Class cls) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable unused) {
                kotlin_metadata_error = true;
            }
        }
        if (kotlin_metadata == null) {
            return false;
        }
        return cls.isAnnotationPresent(kotlin_metadata);
    }

    private static boolean isKotlinIgnore(Class cls, String str) {
        String[] strArr;
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                HashMap map = new HashMap();
                map.put(Class.forName("kotlin.ranges.CharRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.IntRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.LongRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.ClosedFloatRange"), new String[]{"getEndInclusive", "isEmpty"});
                map.put(Class.forName("kotlin.ranges.ClosedDoubleRange"), new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = map;
            } catch (Throwable unused) {
                kotlinIgnores_error = true;
            }
        }
        return (kotlinIgnores == null || (strArr = kotlinIgnores.get(cls)) == null || Arrays.binarySearch(strArr, str) < 0) ? false : true;
    }

    public static String[] getKoltinConstructorParameters(Class cls) {
        if (kotlin_kclass_constructor == null && !kotlin_class_klass_error) {
            try {
                Class<?> cls2 = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_constructor = cls2.getConstructor(Class.class);
                kotlin_kclass_getConstructors = cls2.getMethod("getConstructors", null);
                kotlin_kfunction_getParameters = Class.forName("kotlin.reflect.KFunction").getMethod("getParameters", null);
                kotlin_kparameter_getName = Class.forName("kotlin.reflect.KParameter").getMethod("getName", null);
            } catch (Throwable unused) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null || kotlin_error) {
            return null;
        }
        try {
            Iterator it = ((Iterable) kotlin_kclass_getConstructors.invoke(kotlin_kclass_constructor.newInstance(cls), null)).iterator();
            Object obj = null;
            while (it.hasNext()) {
                Object next = it.next();
                List list = (List) kotlin_kfunction_getParameters.invoke(next, null);
                if (obj == null || list.size() != 0) {
                    obj = next;
                }
                it.hasNext();
            }
            List list2 = (List) kotlin_kfunction_getParameters.invoke(obj, null);
            String[] strArr = new String[list2.size()];
            for (int i = 0; i < list2.size(); i++) {
                strArr[i] = (String) kotlin_kparameter_getName.invoke(list2.get(i), null);
            }
            return strArr;
        } catch (Throwable unused2) {
            kotlin_error = true;
            return null;
        }
    }

    public static final String castToString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static final Byte castToByte(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Byte.valueOf(((Number) obj).byteValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            return Byte.valueOf(Byte.parseByte(str));
        }
        throw new JSONException("can not cast to byte, value : " + obj);
    }

    public static final Character castToChar(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Character) {
            return (Character) obj;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0) {
                return null;
            }
            if (str.length() != 1) {
                throw new JSONException("can not cast to byte, value : " + obj);
            }
            return Character.valueOf(str.charAt(0));
        }
        throw new JSONException("can not cast to byte, value : " + obj);
    }

    public static final Short castToShort(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Short.valueOf(((Number) obj).shortValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            return Short.valueOf(Short.parseShort(str));
        }
        throw new JSONException("can not cast to short, value : " + obj);
    }

    public static final BigDecimal castToBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof BigInteger) {
            return new BigDecimal((BigInteger) obj);
        }
        String string = obj.toString();
        if (string.length() == 0 || "null".equals(string)) {
            return null;
        }
        return new BigDecimal(string);
    }

    public static final BigInteger castToBigInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        }
        if ((obj instanceof Float) || (obj instanceof Double)) {
            return BigInteger.valueOf(((Number) obj).longValue());
        }
        String string = obj.toString();
        if (string.length() == 0 || "null".equals(string)) {
            return null;
        }
        return new BigInteger(string);
    }

    public static final Float castToFloat(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        }
        if (obj instanceof String) {
            String string = obj.toString();
            if (string.length() == 0 || "null".equals(string)) {
                return null;
            }
            return Float.valueOf(Float.parseFloat(string));
        }
        throw new JSONException("can not cast to float, value : " + obj);
    }

    public static final Double castToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof String) {
            String string = obj.toString();
            if (string.length() == 0 || "null".equals(string) || "NULL".equals(string)) {
                return null;
            }
            return Double.valueOf(Double.parseDouble(string));
        }
        throw new JSONException("can not cast to double, value : " + obj);
    }

    public static final Date castToDate(Object obj) {
        long jLongValue;
        String str;
        if (obj == null) {
            return null;
        }
        if (obj instanceof Calendar) {
            return ((Calendar) obj).getTime();
        }
        if (obj instanceof Date) {
            return (Date) obj;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int iScale = bigDecimal.scale();
            if (iScale >= -100 && iScale <= 100) {
                jLongValue = bigDecimal.longValue();
            } else {
                jLongValue = bigDecimal.longValueExact();
            }
        } else if (obj instanceof Number) {
            jLongValue = ((Number) obj).longValue();
        } else if (obj instanceof String) {
            String str2 = (String) obj;
            if (str2.indexOf(45) != -1) {
                if (str2.length() == JSON.DEFFAULT_DATE_FORMAT.length()) {
                    str = JSON.DEFFAULT_DATE_FORMAT;
                } else if (str2.length() == 10) {
                    str = "yyyy-MM-dd";
                } else if (str2.length() == 19) {
                    str = "yyyy-MM-dd HH:mm:ss";
                } else if (str2.length() == 29 && str2.charAt(26) == ':' && str2.charAt(28) == '0') {
                    str = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                } else {
                    str = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(str, JSON.defaultLocale);
                simpleDateFormat.setTimeZone(JSON.defaultTimeZone);
                try {
                    return simpleDateFormat.parse(str2);
                } catch (ParseException unused) {
                    throw new JSONException("can not cast to Date, value : " + str2);
                }
            }
            if (str2.length() == 0 || "null".equals(str2)) {
                return null;
            }
            jLongValue = Long.parseLong(str2);
        } else {
            jLongValue = -1;
        }
        if (jLongValue < 0) {
            throw new JSONException("can not cast to Date, value : " + obj);
        }
        return new Date(jLongValue);
    }

    public static final Long castToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int iScale = bigDecimal.scale();
            if (iScale >= -100 && iScale <= 100) {
                return Long.valueOf(bigDecimal.longValue());
            }
            return Long.valueOf(bigDecimal.longValueExact());
        }
        if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            try {
                return Long.valueOf(Long.parseLong(str));
            } catch (NumberFormatException unused) {
                JSONLexer jSONLexer = new JSONLexer(str);
                Calendar calendar = jSONLexer.scanISO8601DateIfMatch(false) ? jSONLexer.calendar : null;
                jSONLexer.close();
                if (calendar != null) {
                    return Long.valueOf(calendar.getTimeInMillis());
                }
            }
        }
        throw new JSONException("can not cast to long, value : " + obj);
    }

    public static final Integer castToInt(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int iScale = bigDecimal.scale();
            if (iScale >= -100 && iScale <= 100) {
                return Integer.valueOf(bigDecimal.intValue());
            }
            return Integer.valueOf(bigDecimal.intValueExact());
        }
        if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            return Integer.valueOf(Integer.parseInt(str));
        }
        throw new JSONException("can not cast to int, value : " + obj);
    }

    public static final byte[] castToBytes(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return JSONLexer.decodeFast(str, 0, str.length());
        }
        throw new JSONException("can not cast to int, value : " + obj);
    }

    public static final Boolean castToBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof BigDecimal) {
            return Boolean.valueOf(((BigDecimal) obj).intValueExact() == 1);
        }
        if (obj instanceof Number) {
            return Boolean.valueOf(((Number) obj).intValue() == 1);
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
                return Boolean.FALSE;
            }
        }
        throw new JSONException("can not cast to int, value : " + obj);
    }

    public static final <T> T castToJavaBean(Object obj, Class<T> cls) {
        return (T) cast(obj, (Class) cls, ParserConfig.global);
    }

    public static final <T> T cast(Object obj, Class<T> cls, ParserConfig parserConfig) {
        return (T) cast(obj, cls, parserConfig, 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static final <T> T cast(Object obj, Class<T> cls, ParserConfig parserConfig, int i) {
        Object obj2;
        if (obj == 0) {
            return null;
        }
        if (cls == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (cls != obj.getClass()) {
            if (obj instanceof Map) {
                if (cls != Map.class) {
                    Map map = (Map) obj;
                    if (cls != Object.class || map.containsKey(JSON.DEFAULT_TYPE_KEY)) {
                        return (T) castToJavaBean(map, cls, parserConfig, i);
                    }
                }
            } else {
                int i2 = 0;
                if (cls.isArray()) {
                    if (obj instanceof Collection) {
                        Collection collection = (Collection) obj;
                        T t = (T) Array.newInstance(cls.getComponentType(), collection.size());
                        Iterator it = collection.iterator();
                        while (it.hasNext()) {
                            Array.set(t, i2, cast(it.next(), (Class) cls.getComponentType(), parserConfig));
                            i2++;
                        }
                        return t;
                    }
                    if (cls == byte[].class) {
                        return (T) castToBytes(obj);
                    }
                }
                if (!cls.isAssignableFrom(obj.getClass())) {
                    if (cls == Boolean.TYPE || cls == Boolean.class) {
                        return (T) castToBoolean(obj);
                    }
                    if (cls == Byte.TYPE || cls == Byte.class) {
                        return (T) castToByte(obj);
                    }
                    if ((cls == Character.TYPE || cls == Character.class) && (obj instanceof String)) {
                        String str = (String) obj;
                        if (str.length() == 1) {
                            return (T) Character.valueOf(str.charAt(0));
                        }
                    }
                    if (cls == Short.TYPE || cls == Short.class) {
                        return (T) castToShort(obj);
                    }
                    if (cls == Integer.TYPE || cls == Integer.class) {
                        return (T) castToInt(obj);
                    }
                    if (cls == Long.TYPE || cls == Long.class) {
                        return (T) castToLong(obj);
                    }
                    if (cls == Float.TYPE || cls == Float.class) {
                        return (T) castToFloat(obj);
                    }
                    if (cls == Double.TYPE || cls == Double.class) {
                        return (T) castToDouble(obj);
                    }
                    if (cls == String.class) {
                        return (T) castToString(obj);
                    }
                    if (cls == BigDecimal.class) {
                        return (T) castToBigDecimal(obj);
                    }
                    if (cls == BigInteger.class) {
                        return (T) castToBigInteger(obj);
                    }
                    if (cls == Date.class) {
                        return (T) castToDate(obj);
                    }
                    if (cls.isEnum()) {
                        return (T) castToEnum(obj, cls, parserConfig);
                    }
                    if (Calendar.class.isAssignableFrom(cls)) {
                        Date dateCastToDate = castToDate(obj);
                        if (cls == Calendar.class) {
                            obj2 = (T) Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                        } else {
                            try {
                                obj2 = (T) ((Calendar) cls.newInstance());
                            } catch (Exception e) {
                                throw new JSONException("can not cast to : " + cls.getName(), e);
                            }
                        }
                        ((Calendar) obj2).setTime(dateCastToDate);
                        return (T) obj2;
                    }
                    if (obj instanceof String) {
                        String str2 = (String) obj;
                        if (str2.length() == 0 || "null".equals(str2)) {
                            return null;
                        }
                        if (cls == Currency.class) {
                            return (T) Currency.getInstance(str2);
                        }
                    }
                    throw new JSONException("can not cast to : " + cls.getName());
                }
            }
        }
        return obj;
    }

    public static final <T> T castToEnum(Object obj, Class<T> cls, ParserConfig parserConfig) {
        try {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() == 0) {
                    return null;
                }
                return (T) Enum.valueOf(cls, str);
            }
            if ((obj instanceof Integer) || (obj instanceof Long)) {
                int iIntValue = ((Number) obj).intValue();
                T[] enumConstants = cls.getEnumConstants();
                if (iIntValue < enumConstants.length) {
                    return enumConstants[iIntValue];
                }
            }
            throw new JSONException("can not cast to : " + cls.getName());
        } catch (Exception e) {
            throw new JSONException("can not cast to : " + cls.getName(), e);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static final <T> T cast(Object obj, Type type, ParserConfig parserConfig) {
        if (obj == 0) {
            return null;
        }
        if (type instanceof Class) {
            return (T) cast(obj, (Class) type, parserConfig, 0);
        }
        if (type instanceof ParameterizedType) {
            return (T) cast(obj, (ParameterizedType) type, parserConfig);
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
        }
        if (type instanceof TypeVariable) {
            return obj;
        }
        throw new JSONException("can not cast to : " + type);
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [T, java.util.ArrayList] */
    /* JADX WARN: Type inference failed for: r8v9, types: [T, java.util.HashMap, java.util.Map] */
    public static final <T> T cast(Object obj, ParameterizedType parameterizedType, ParserConfig parserConfig) {
        Object objCast;
        T t;
        Object objCast2;
        Type rawType = parameterizedType.getRawType();
        if (rawType == List.class || rawType == ArrayList.class) {
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (obj instanceof List) {
                List list = (List) obj;
                int size = list.size();
                ?? r0 = (T) new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    Object obj2 = list.get(i);
                    if (type instanceof Class) {
                        if (obj2 != null && obj2.getClass() == JSONObject.class) {
                            objCast = ((JSONObject) obj2).toJavaObject((Class) type, parserConfig, 0);
                        } else {
                            objCast = cast(obj2, (Class) type, parserConfig, 0);
                        }
                    } else {
                        objCast = cast(obj2, type, parserConfig);
                    }
                    r0.add(objCast);
                }
                return r0;
            }
        }
        if (rawType == Set.class || rawType == HashSet.class || rawType == TreeSet.class || rawType == List.class || rawType == ArrayList.class) {
            Type type2 = parameterizedType.getActualTypeArguments()[0];
            if (obj instanceof Iterable) {
                if (rawType == Set.class || rawType == HashSet.class) {
                    t = (T) new HashSet();
                } else if (rawType == TreeSet.class) {
                    t = (T) new TreeSet();
                } else {
                    t = (T) new ArrayList();
                }
                for (T t2 : (Iterable) obj) {
                    if (type2 instanceof Class) {
                        if (t2 != null && t2.getClass() == JSONObject.class) {
                            objCast2 = ((JSONObject) t2).toJavaObject((Class) type2, parserConfig, 0);
                        } else {
                            objCast2 = cast(t2, (Class) type2, parserConfig, 0);
                        }
                    } else {
                        objCast2 = cast(t2, type2, parserConfig);
                    }
                    ((Collection) t).add(objCast2);
                }
                return t;
            }
        }
        if (rawType == Map.class || rawType == HashMap.class) {
            Type type3 = parameterizedType.getActualTypeArguments()[0];
            Type type4 = parameterizedType.getActualTypeArguments()[1];
            if (obj instanceof Map) {
                ?? r8 = (T) new HashMap();
                for (Map.Entry entry : ((Map) obj).entrySet()) {
                    r8.put(cast(entry.getKey(), type3, parserConfig), cast(entry.getValue(), type4, parserConfig));
                }
                return r8;
            }
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
        }
        if (parameterizedType.getActualTypeArguments().length == 1 && (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType)) {
            return (T) cast(obj, rawType, parserConfig);
        }
        throw new JSONException("can not cast to : " + parameterizedType);
    }

    public static final <T> T castToJavaBean(Map<String, Object> map, Class<T> cls, ParserConfig parserConfig) {
        return (T) castToJavaBean(map, cls, parserConfig, 0);
    }

    public static final <T> T castToJavaBean(Map<String, Object> map, Class<T> cls, ParserConfig parserConfig, int i) {
        JSONObject jSONObject;
        int iIntValue = 0;
        try {
            if (cls == StackTraceElement.class) {
                String str = (String) map.get("className");
                String str2 = (String) map.get("methodName");
                String str3 = (String) map.get("fileName");
                Number number = (Number) map.get("lineNumber");
                if (number != null) {
                    if (number instanceof BigDecimal) {
                        iIntValue = ((BigDecimal) number).intValueExact();
                    } else {
                        iIntValue = number.intValue();
                    }
                }
                return (T) new StackTraceElement(str, str2, str3, iIntValue);
            }
            Object obj = map.get(JSON.DEFAULT_TYPE_KEY);
            if (obj instanceof String) {
                String str4 = (String) obj;
                if (parserConfig == null) {
                    parserConfig = ParserConfig.global;
                }
                Class<?> clsCheckAutoType = parserConfig.checkAutoType(str4, null, i);
                if (clsCheckAutoType == null) {
                    throw new ClassNotFoundException(str4 + " not found");
                }
                if (!clsCheckAutoType.equals(cls)) {
                    return (T) castToJavaBean(map, clsCheckAutoType, parserConfig, i);
                }
            }
            if (cls.isInterface()) {
                if (map instanceof JSONObject) {
                    jSONObject = (JSONObject) map;
                } else {
                    jSONObject = new JSONObject(map);
                }
                if (parserConfig == null) {
                    parserConfig = ParserConfig.getGlobalInstance();
                }
                return parserConfig.getDeserializer(cls) != null ? (T) JSON.parseObject(JSON.toJSONString(jSONObject), cls) : (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{cls}, jSONObject);
            }
            if (cls == String.class && (map instanceof JSONObject)) {
                return (T) map.toString();
            }
            if (parserConfig == null) {
                parserConfig = ParserConfig.global;
            }
            ObjectDeserializer deserializer = parserConfig.getDeserializer(cls);
            JavaBeanDeserializer javaBeanDeserializer = deserializer instanceof JavaBeanDeserializer ? (JavaBeanDeserializer) deserializer : null;
            if (javaBeanDeserializer == null) {
                throw new JSONException("can not get javaBeanDeserializer");
            }
            return (T) javaBeanDeserializer.createInstance(map, parserConfig);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    static {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(36, 0.75f, 1);
        mappings = concurrentHashMap;
        concurrentHashMap.put("byte", Byte.TYPE);
        concurrentHashMap.put("short", Short.TYPE);
        concurrentHashMap.put("int", Integer.TYPE);
        concurrentHashMap.put("long", Long.TYPE);
        concurrentHashMap.put("float", Float.TYPE);
        concurrentHashMap.put("double", Double.TYPE);
        concurrentHashMap.put("boolean", Boolean.TYPE);
        concurrentHashMap.put("char", Character.TYPE);
        concurrentHashMap.put("[byte", byte[].class);
        concurrentHashMap.put("[short", short[].class);
        concurrentHashMap.put("[int", int[].class);
        concurrentHashMap.put("[long", long[].class);
        concurrentHashMap.put("[float", float[].class);
        concurrentHashMap.put("[double", double[].class);
        concurrentHashMap.put("[boolean", boolean[].class);
        concurrentHashMap.put("[char", char[].class);
        concurrentHashMap.put("[B", byte[].class);
        concurrentHashMap.put("[S", short[].class);
        concurrentHashMap.put("[I", int[].class);
        concurrentHashMap.put("[J", long[].class);
        concurrentHashMap.put("[F", float[].class);
        concurrentHashMap.put("[D", double[].class);
        concurrentHashMap.put("[C", char[].class);
        concurrentHashMap.put("[Z", boolean[].class);
        concurrentHashMap.put("java.util.HashMap", HashMap.class);
        concurrentHashMap.put("java.util.TreeMap", TreeMap.class);
        concurrentHashMap.put("java.util.Date", Date.class);
        concurrentHashMap.put("com.alibaba.fastjson.JSONObject", JSONObject.class);
        concurrentHashMap.put("java.util.concurrent.ConcurrentHashMap", ConcurrentHashMap.class);
        concurrentHashMap.put("java.text.SimpleDateFormat", SimpleDateFormat.class);
        concurrentHashMap.put("java.lang.StackTraceElement", StackTraceElement.class);
        concurrentHashMap.put("java.lang.RuntimeException", RuntimeException.class);
    }

    public static Class<?> getClassFromMapping(String str) {
        return mappings.get(str);
    }

    public static Class<?> loadClass(String str, ClassLoader classLoader) {
        return loadClass(str, classLoader, false);
    }

    public static Class<?> loadClass(String str, ClassLoader classLoader, boolean z) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (str.length() >= 256) {
            throw new JSONException("className too long. " + str);
        }
        ConcurrentMap<String, Class<?>> concurrentMap = mappings;
        Class<?> cls = concurrentMap.get(str);
        if (cls != null) {
            return cls;
        }
        if (str.charAt(0) == '[') {
            Class<?> clsLoadClass = loadClass(str.substring(1), classLoader, false);
            if (clsLoadClass == null) {
                return null;
            }
            return Array.newInstance(clsLoadClass, 0).getClass();
        }
        if (str.startsWith("L") && str.endsWith(";")) {
            return loadClass(str.substring(1, str.length() - 1), classLoader, false);
        }
        if (classLoader != null) {
            try {
                cls = classLoader.loadClass(str);
                if (z) {
                    concurrentMap.put(str, cls);
                }
                return cls;
            } catch (Exception unused) {
            }
        }
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null && contextClassLoader != classLoader) {
                Class<?> clsLoadClass2 = contextClassLoader.loadClass(str);
                if (z) {
                    try {
                        mappings.put(str, clsLoadClass2);
                    } catch (Exception unused2) {
                        cls = clsLoadClass2;
                    }
                }
                return clsLoadClass2;
            }
        } catch (Exception unused3) {
        }
        try {
            cls = Class.forName(str);
            mappings.put(str, cls);
            return cls;
        } catch (Exception unused4) {
            return cls;
        }
    }

    /* JADX WARN: Code duplicated, block: B:132:0x0262  */
    /* JADX WARN: Code duplicated, block: B:134:0x0269 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:135:0x026b  */
    /* JADX WARN: Code duplicated, block: B:136:0x0274  */
    /* JADX WARN: Code duplicated, block: B:138:0x0278  */
    /* JADX WARN: Code duplicated, block: B:141:0x0280  */
    /* JADX WARN: Code duplicated, block: B:143:0x0296  */
    /* JADX WARN: Code duplicated, block: B:145:0x029c  */
    /* JADX WARN: Code duplicated, block: B:150:0x02b5  */
    /* JADX WARN: Code duplicated, block: B:153:0x02c0 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:155:0x02c9  */
    /* JADX WARN: Code duplicated, block: B:157:0x02cd  */
    /* JADX WARN: Code duplicated, block: B:161:0x02f2 A[PHI: r11
  0x02f2: PHI (r11v6 java.util.HashMap) = (r11v10 java.util.HashMap), (r11v4 java.util.HashMap), (r11v4 java.util.HashMap), (r11v4 java.util.HashMap) binds: [B:195:0x0385, B:109:0x01f1, B:125:0x0247, B:127:0x0251] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:162:0x02f4 A[PHI: r11 r13
  0x02f4: PHI (r11v7 java.util.HashMap) = 
  (r11v6 java.util.HashMap)
  (r11v10 java.util.HashMap)
  (r11v4 java.util.HashMap)
  (r11v4 java.util.HashMap)
  (r11v4 java.util.HashMap)
  (r11v4 java.util.HashMap)
 binds: [B:161:0x02f2, B:202:0x03ad, B:111:0x01f9, B:130:0x025f, B:139:0x027c, B:146:0x02a2] A[DONT_GENERATE, DONT_INLINE]
  0x02f4: PHI (r13v6 java.util.Map<java.lang.String, java.lang.String>) = 
  (r13v5 java.util.Map<java.lang.String, java.lang.String>)
  (r13v13 java.util.Map<java.lang.String, java.lang.String>)
  (r13v2 java.util.Map<java.lang.String, java.lang.String>)
  (r13v2 java.util.Map<java.lang.String, java.lang.String>)
  (r13v2 java.util.Map<java.lang.String, java.lang.String>)
  (r13v2 java.util.Map<java.lang.String, java.lang.String>)
 binds: [B:161:0x02f2, B:202:0x03ad, B:111:0x01f9, B:130:0x025f, B:139:0x027c, B:146:0x02a2] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:170:0x0310  */
    /* JADX WARN: Code duplicated, block: B:204:0x03b1 A[PHI: r0 r3 r7
  0x03b1: PHI (r0v36 java.lang.String) = (r0v28 java.lang.String), (r0v28 java.lang.String), (r0v38 java.lang.String) binds: [B:193:0x037f, B:198:0x039d, B:200:0x03a3] A[DONT_GENERATE, DONT_INLINE]
  0x03b1: PHI (r3v24 int) = (r3v21 int), (r3v27 int), (r3v27 int) binds: [B:193:0x037f, B:198:0x039d, B:200:0x03a3] A[DONT_GENERATE, DONT_INLINE]
  0x03b1: PHI (r7v14 int) = (r7v12 int), (r7v16 int), (r7v16 int) binds: [B:193:0x037f, B:198:0x039d, B:200:0x03a3] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:287:0x0504  */
    /* JADX WARN: Code duplicated, block: B:289:0x0508 A[LOOP:11: B:288:0x0506->B:289:0x0508, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:290:0x0516  */
    /* JADX WARN: Code duplicated, block: B:293:0x0524 A[LOOP:12: B:291:0x051e->B:293:0x0524, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:295:0x0530  */
    /* JADX WARN: Code duplicated, block: B:88:0x016b  */
    /* JADX WARN: Instruction removed from duplicated block: B:287:0x0504, please report this as an issue */
    public static List<FieldInfo> computeGetters(Class<?> cls, int i, boolean z, JSONType jSONType, Map<String, String> map, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        String[] strArrOrders;
        boolean z5;
        Iterator it;
        int i2;
        boolean z6;
        int i3;
        int i4;
        HashMap map2;
        short[] sArr;
        Constructor<?>[] constructorArr;
        String[] strArr;
        int iOrdinal;
        int iOf;
        Field[] fieldArr;
        String str;
        int i5;
        String strSubstring;
        int i6;
        JSONField jSONField;
        String strDecapitalize;
        int iOrdinal2;
        Field field;
        int i7;
        JSONField jSONField2;
        String strTranslate;
        int i8;
        boolean z7;
        PropertyNamingStrategy propertyNamingStrategy2;
        JSONField jSONField3;
        String strName;
        int i9;
        Annotation[] annotationArr;
        Class<?> cls2 = cls;
        int i10 = i;
        JSONType jSONType2 = jSONType;
        Map<String, String> map3 = map;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        HashMap map4 = new HashMap();
        Field[] declaredFields = cls2.getDeclaredFields();
        if (!z) {
            boolean zIsKotlin = isKotlin(cls2);
            ArrayList<Method> arrayList = new ArrayList();
            for (Class<?> superclass = cls2; superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
                for (Method method : superclass.getDeclaredMethods()) {
                    int modifiers = method.getModifiers();
                    if ((modifiers & 8) == 0 && (modifiers & 2) == 0 && (modifiers & 256) == 0 && (modifiers & 4) == 0 && !method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length == 0 && method.getReturnType() != ClassLoader.class && method.getDeclaringClass() != Object.class) {
                        arrayList.add(method);
                    }
                }
            }
            char c = 0;
            Constructor<?>[] declaredConstructors = null;
            String[] koltinConstructorParameters = null;
            short[] sArr2 = null;
            Annotation[][] parameterAnnotations = null;
            for (Method method2 : arrayList) {
                String name = method2.getName();
                if (!name.equals("getMetaClass") || !method2.getReturnType().getName().equals("groovy.lang.MetaClass")) {
                    JSONField supperMethodAnnotation = z3 ? (JSONField) method2.getAnnotation(JSONField.class) : null;
                    if (supperMethodAnnotation == null && z3) {
                        supperMethodAnnotation = getSupperMethodAnnotation(cls2, method2);
                    }
                    if (!zIsKotlin || !isKotlinIgnore(cls2, name)) {
                        short[] sArr3 = sArr2;
                        if (supperMethodAnnotation == null && zIsKotlin) {
                            if (declaredConstructors == null) {
                                declaredConstructors = cls2.getDeclaredConstructors();
                                map2 = map4;
                                if (declaredConstructors.length == 1) {
                                    parameterAnnotations = declaredConstructors[c].getParameterAnnotations();
                                    koltinConstructorParameters = getKoltinConstructorParameters(cls2);
                                    if (koltinConstructorParameters != null) {
                                        String[] strArr2 = new String[koltinConstructorParameters.length];
                                        System.arraycopy(koltinConstructorParameters, 0, strArr2, 0, koltinConstructorParameters.length);
                                        Arrays.sort(strArr2);
                                        sArr3 = new short[koltinConstructorParameters.length];
                                        for (short s = 0; s < koltinConstructorParameters.length; s = (short) (s + 1)) {
                                            sArr3[Arrays.binarySearch(strArr2, koltinConstructorParameters[s])] = s;
                                        }
                                        koltinConstructorParameters = strArr2;
                                    }
                                }
                                short[] sArr4 = sArr3;
                                if (koltinConstructorParameters == null && sArr4 != null && name.startsWith("get")) {
                                    String strDecapitalize2 = decapitalize(name.substring(3));
                                    int iBinarySearch = Arrays.binarySearch(koltinConstructorParameters, strDecapitalize2);
                                    constructorArr = declaredConstructors;
                                    if (iBinarySearch >= 0) {
                                        i9 = iBinarySearch;
                                        break;
                                    }
                                    i9 = 0;
                                    while (true) {
                                        if (i9 >= koltinConstructorParameters.length) {
                                            i9 = iBinarySearch;
                                            break;
                                        }
                                        if (strDecapitalize2.equalsIgnoreCase(koltinConstructorParameters[i9])) {
                                            break;
                                        }
                                        i9++;
                                    }
                                    if (i9 >= 0 && (annotationArr = parameterAnnotations[sArr4[i9]]) != null) {
                                        int length = annotationArr.length;
                                        int i11 = 0;
                                        while (i11 < length) {
                                            Annotation[] annotationArr2 = annotationArr;
                                            Annotation annotation = annotationArr2[i11];
                                            int i12 = length;
                                            if (annotation instanceof JSONField) {
                                                supperMethodAnnotation = (JSONField) annotation;
                                                break;
                                            }
                                            i11++;
                                            annotationArr = annotationArr2;
                                            length = i12;
                                        }
                                    }
                                } else {
                                    constructorArr = declaredConstructors;
                                }
                                strArr = koltinConstructorParameters;
                                sArr = sArr4;
                            } else {
                                map2 = map4;
                            }
                            short[] sArr5 = sArr3;
                            if (koltinConstructorParameters == null) {
                                constructorArr = declaredConstructors;
                            } else {
                                constructorArr = declaredConstructors;
                            }
                            strArr = koltinConstructorParameters;
                            sArr = sArr5;
                        } else {
                            map2 = map4;
                            declaredFields = declaredFields;
                            sArr = sArr3;
                            constructorArr = declaredConstructors;
                            strArr = koltinConstructorParameters;
                        }
                        Annotation[][] annotationArr3 = parameterAnnotations;
                        if (supperMethodAnnotation != null) {
                            if (supperMethodAnnotation.serialize()) {
                                int iOrdinal3 = supperMethodAnnotation.ordinal();
                                int iOf2 = SerializerFeature.of(supperMethodAnnotation.serialzeFeatures());
                                if (supperMethodAnnotation.name().length() != 0) {
                                    String strName2 = supperMethodAnnotation.name();
                                    if (map3 == null || (strName2 = map3.get(strName2)) != null) {
                                        setAccessible(cls2, method2, i10);
                                        fieldArr = declaredFields;
                                        c = 0;
                                        linkedHashMap.put(strName2, new FieldInfo(strName2, method2, null, cls2, null, iOrdinal3, iOf2, supperMethodAnnotation, null, true));
                                    }
                                    jSONType2 = jSONType;
                                    declaredFields = fieldArr;
                                    map4 = map2;
                                    declaredConstructors = constructorArr;
                                    koltinConstructorParameters = strArr;
                                    sArr2 = sArr;
                                    parameterAnnotations = annotationArr3;
                                } else {
                                    iOf = iOf2;
                                    iOrdinal = iOrdinal3;
                                }
                                i10 = i;
                                jSONType2 = jSONType;
                                declaredFields = fieldArr;
                                map4 = map2;
                                declaredConstructors = constructorArr;
                                koltinConstructorParameters = strArr;
                                sArr2 = sArr;
                                parameterAnnotations = annotationArr3;
                            }
                            fieldArr = declaredFields;
                            c = 0;
                            jSONType2 = jSONType;
                            declaredFields = fieldArr;
                            map4 = map2;
                            declaredConstructors = constructorArr;
                            koltinConstructorParameters = strArr;
                            sArr2 = sArr;
                            parameterAnnotations = annotationArr3;
                        } else {
                            iOrdinal = 0;
                            iOf = 0;
                        }
                        map2 = map2;
                        fieldArr = declaredFields;
                        c = 0;
                        if (name.startsWith("get")) {
                            if (name.length() < 4) {
                                map3 = map;
                            } else if (!name.equals("getClass")) {
                                char cCharAt = name.charAt(3);
                                if (Character.isUpperCase(cCharAt)) {
                                    if (compatibleWithJavaBean) {
                                        strDecapitalize = decapitalize(name.substring(3));
                                    } else {
                                        strDecapitalize = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                                    }
                                    if (!isJSONTypeIgnore(cls2, jSONType2, strDecapitalize)) {
                                        iOrdinal2 = iOrdinal;
                                        field = getField(cls2, strDecapitalize, fieldArr, map2);
                                        if (field != null) {
                                            if (z3) {
                                                jSONField3 = (JSONField) field.getAnnotation(JSONField.class);
                                            } else {
                                                jSONField3 = null;
                                            }
                                            if (jSONField3 != null) {
                                                if (jSONField3.serialize()) {
                                                    iOrdinal2 = jSONField3.ordinal();
                                                    iOf = SerializerFeature.of(jSONField3.serialzeFeatures());
                                                    if (jSONField3.name().length() != 0) {
                                                        strName = jSONField3.name();
                                                        if (map3 != null || (strName = map3.get(strName)) != null) {
                                                            name = name;
                                                            supperMethodAnnotation = supperMethodAnnotation;
                                                            jSONField2 = jSONField3;
                                                            i7 = iOf;
                                                            strTranslate = strName;
                                                            i8 = iOrdinal2;
                                                            z7 = true;
                                                            if (propertyNamingStrategy != null || z7) {
                                                                propertyNamingStrategy2 = propertyNamingStrategy;
                                                            } else {
                                                                propertyNamingStrategy2 = propertyNamingStrategy;
                                                                strTranslate = propertyNamingStrategy2.translate(strTranslate);
                                                            }
                                                            if (map3 != null || (strTranslate = map3.get(strTranslate)) != null) {
                                                                setAccessible(cls2, method2, i);
                                                                String str2 = strTranslate;
                                                                iOf = i7;
                                                                str = name;
                                                                i5 = 3;
                                                                linkedHashMap.put(str2, new FieldInfo(str2, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                                                iOrdinal = i8;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            jSONField2 = jSONField3;
                                            i7 = iOf;
                                        } else {
                                            i7 = iOf;
                                            jSONField2 = null;
                                        }
                                        strTranslate = strDecapitalize;
                                        i8 = iOrdinal2;
                                        z7 = false;
                                        if (propertyNamingStrategy != null) {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        } else {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        }
                                        if (map3 != null) {
                                        }
                                        setAccessible(cls2, method2, i);
                                        String str3 = strTranslate;
                                        iOf = i7;
                                        str = name;
                                        i5 = 3;
                                        linkedHashMap.put(str3, new FieldInfo(str3, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                        iOrdinal = i8;
                                    }
                                } else if (cCharAt == '_') {
                                    strDecapitalize = name.substring(4);
                                    if (!isJSONTypeIgnore(cls2, jSONType2, strDecapitalize)) {
                                        iOrdinal2 = iOrdinal;
                                        field = getField(cls2, strDecapitalize, fieldArr, map2);
                                        if (field != null) {
                                            if (z3) {
                                                jSONField3 = (JSONField) field.getAnnotation(JSONField.class);
                                            } else {
                                                jSONField3 = null;
                                            }
                                            if (jSONField3 != null) {
                                                if (jSONField3.serialize()) {
                                                    iOrdinal2 = jSONField3.ordinal();
                                                    iOf = SerializerFeature.of(jSONField3.serialzeFeatures());
                                                    if (jSONField3.name().length() != 0) {
                                                        strName = jSONField3.name();
                                                        if (map3 != null) {
                                                        }
                                                        name = name;
                                                        supperMethodAnnotation = supperMethodAnnotation;
                                                        jSONField2 = jSONField3;
                                                        i7 = iOf;
                                                        strTranslate = strName;
                                                        i8 = iOrdinal2;
                                                        z7 = true;
                                                        if (propertyNamingStrategy != null) {
                                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                                        } else {
                                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                                        }
                                                        if (map3 != null) {
                                                        }
                                                        setAccessible(cls2, method2, i);
                                                        String str4 = strTranslate;
                                                        iOf = i7;
                                                        str = name;
                                                        i5 = 3;
                                                        linkedHashMap.put(str4, new FieldInfo(str4, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                                        iOrdinal = i8;
                                                    }
                                                }
                                            }
                                            jSONField2 = jSONField3;
                                            i7 = iOf;
                                        } else {
                                            i7 = iOf;
                                            jSONField2 = null;
                                        }
                                        strTranslate = strDecapitalize;
                                        i8 = iOrdinal2;
                                        z7 = false;
                                        if (propertyNamingStrategy != null) {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        } else {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        }
                                        if (map3 != null) {
                                        }
                                        setAccessible(cls2, method2, i);
                                        String str5 = strTranslate;
                                        iOf = i7;
                                        str = name;
                                        i5 = 3;
                                        linkedHashMap.put(str5, new FieldInfo(str5, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                        iOrdinal = i8;
                                    }
                                } else {
                                    if (cCharAt == 'f') {
                                        strDecapitalize = name.substring(3);
                                    } else if (name.length() >= 5 && Character.isUpperCase(name.charAt(4))) {
                                        strDecapitalize = decapitalize(name.substring(3));
                                    }
                                    if (!isJSONTypeIgnore(cls2, jSONType2, strDecapitalize)) {
                                        iOrdinal2 = iOrdinal;
                                        field = getField(cls2, strDecapitalize, fieldArr, map2);
                                        if (field != null) {
                                            if (z3) {
                                                jSONField3 = (JSONField) field.getAnnotation(JSONField.class);
                                            } else {
                                                jSONField3 = null;
                                            }
                                            if (jSONField3 != null) {
                                                if (jSONField3.serialize()) {
                                                    iOrdinal2 = jSONField3.ordinal();
                                                    iOf = SerializerFeature.of(jSONField3.serialzeFeatures());
                                                    if (jSONField3.name().length() != 0) {
                                                        strName = jSONField3.name();
                                                        if (map3 != null) {
                                                        }
                                                        name = name;
                                                        supperMethodAnnotation = supperMethodAnnotation;
                                                        jSONField2 = jSONField3;
                                                        i7 = iOf;
                                                        strTranslate = strName;
                                                        i8 = iOrdinal2;
                                                        z7 = true;
                                                        if (propertyNamingStrategy != null) {
                                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                                        } else {
                                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                                        }
                                                        if (map3 != null) {
                                                        }
                                                        setAccessible(cls2, method2, i);
                                                        String str6 = strTranslate;
                                                        iOf = i7;
                                                        str = name;
                                                        i5 = 3;
                                                        linkedHashMap.put(str6, new FieldInfo(str6, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                                        iOrdinal = i8;
                                                    }
                                                }
                                            }
                                            jSONField2 = jSONField3;
                                            i7 = iOf;
                                        } else {
                                            i7 = iOf;
                                            jSONField2 = null;
                                        }
                                        strTranslate = strDecapitalize;
                                        i8 = iOrdinal2;
                                        z7 = false;
                                        if (propertyNamingStrategy != null) {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        } else {
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        }
                                        if (map3 != null) {
                                        }
                                        setAccessible(cls2, method2, i);
                                        String str7 = strTranslate;
                                        iOf = i7;
                                        str = name;
                                        i5 = 3;
                                        linkedHashMap.put(str7, new FieldInfo(str7, method2, field, cls2, null, i8, iOf, supperMethodAnnotation, jSONField2, z4));
                                        iOrdinal = i8;
                                    }
                                }
                                map3 = map;
                            }
                            map2 = map2;
                            i10 = i;
                            jSONType2 = jSONType;
                            declaredFields = fieldArr;
                            map4 = map2;
                            declaredConstructors = constructorArr;
                            koltinConstructorParameters = strArr;
                            sArr2 = sArr;
                            parameterAnnotations = annotationArr3;
                        } else {
                            str = name;
                            supperMethodAnnotation = supperMethodAnnotation;
                            i5 = 3;
                        }
                        if (!str.startsWith("is") || str.length() < i5) {
                            i10 = i;
                            map3 = map;
                        } else {
                            char cCharAt2 = str.charAt(2);
                            if (Character.isUpperCase(cCharAt2)) {
                                strSubstring = compatibleWithJavaBean ? decapitalize(str.substring(2)) : Character.toLowerCase(str.charAt(2)) + str.substring(i5);
                            } else if (cCharAt2 == '_') {
                                strSubstring = str.substring(i5);
                            } else if (cCharAt2 == 'f') {
                                strSubstring = str.substring(2);
                            } else {
                                i10 = i;
                                map3 = map;
                            }
                            if (isJSONTypeIgnore(cls2, jSONType2, strSubstring)) {
                                i10 = i;
                                map3 = map;
                            } else {
                                Field field2 = getField(cls2, strSubstring, fieldArr, map2);
                                if (field2 == null) {
                                    map2 = map2;
                                    field2 = getField(cls2, str, fieldArr, map2);
                                }
                                if (field2 != null) {
                                    JSONField jSONField4 = z3 ? (JSONField) field2.getAnnotation(JSONField.class) : null;
                                    if (jSONField4 == null) {
                                        map3 = map;
                                    } else {
                                        if (jSONField4.serialize()) {
                                            iOrdinal = jSONField4.ordinal();
                                            iOf = SerializerFeature.of(jSONField4.serialzeFeatures());
                                            if (jSONField4.name().length() != 0) {
                                                strSubstring = jSONField4.name();
                                                if (map != null) {
                                                    map3 = map;
                                                    strSubstring = map3.get(strSubstring);
                                                    if (strSubstring == null) {
                                                    }
                                                    map2 = map2;
                                                    i10 = i;
                                                } else {
                                                    map3 = map;
                                                }
                                            } else {
                                                map3 = map;
                                            }
                                        } else {
                                            map3 = map;
                                        }
                                        map2 = map2;
                                        i10 = i;
                                    }
                                    i6 = iOrdinal;
                                    jSONField = jSONField4;
                                } else {
                                    map3 = map;
                                    i6 = iOrdinal;
                                    jSONField = null;
                                }
                                if (propertyNamingStrategy != null) {
                                    strSubstring = propertyNamingStrategy.translate(strSubstring);
                                }
                                if (map3 == null || (strSubstring = map3.get(strSubstring)) != null) {
                                    setAccessible(cls2, field2, i);
                                    setAccessible(cls2, method2, i);
                                    Field field3 = field2;
                                    String str8 = strSubstring;
                                    map2 = map2;
                                    i10 = i;
                                    linkedHashMap.put(str8, new FieldInfo(str8, method2, field3, cls2, null, i6, iOf, supperMethodAnnotation, jSONField, z4));
                                } else {
                                    map2 = map2;
                                    i10 = i;
                                }
                            }
                        }
                        jSONType2 = jSONType;
                        declaredFields = fieldArr;
                        map4 = map2;
                        declaredConstructors = constructorArr;
                        koltinConstructorParameters = strArr;
                        sArr2 = sArr;
                        parameterAnnotations = annotationArr3;
                    }
                }
            }
        }
        Field[] fieldArr2 = declaredFields;
        ArrayList<Field> arrayList2 = new ArrayList(fieldArr2.length);
        for (Field field4 : fieldArr2) {
            if ((field4.getModifiers() & 8) == 0 && !field4.getName().equals("this$0") && (field4.getModifiers() & 1) != 0) {
                arrayList2.add(field4);
            }
        }
        for (Class<? super Object> superclass2 = cls2.getSuperclass(); superclass2 != null && superclass2 != Object.class; superclass2 = superclass2.getSuperclass()) {
            for (Field field5 : superclass2.getDeclaredFields()) {
                if ((field5.getModifiers() & 8) == 0 && (field5.getModifiers() & 1) != 0) {
                    arrayList2.add(field5);
                }
            }
        }
        for (Field field6 : arrayList2) {
            JSONField jSONField5 = z3 ? (JSONField) field6.getAnnotation(JSONField.class) : null;
            String name2 = field6.getName();
            if (jSONField5 == null) {
                i3 = 0;
                i4 = 0;
            } else if (jSONField5.serialize()) {
                int iOrdinal4 = jSONField5.ordinal();
                int iOf3 = SerializerFeature.of(jSONField5.serialzeFeatures());
                if (jSONField5.name().length() != 0) {
                    name2 = jSONField5.name();
                }
                i3 = iOrdinal4;
                i4 = iOf3;
            }
            if (map3 == null || (name2 = map3.get(name2)) != null) {
                if (propertyNamingStrategy != null) {
                    name2 = propertyNamingStrategy.translate(name2);
                }
                String str9 = name2;
                if (!linkedHashMap.containsKey(str9)) {
                    setAccessible(cls2, field6, i10);
                    linkedHashMap.put(str9, new FieldInfo(str9, null, field6, cls2, null, i3, i4, null, jSONField5, z4));
                }
                cls2 = cls;
            }
        }
        ArrayList arrayList3 = new ArrayList();
        if (jSONType != null) {
            strArrOrders = jSONType.orders();
            if (strArrOrders != null && strArrOrders.length == linkedHashMap.size()) {
                int length2 = strArrOrders.length;
                int i13 = 0;
                while (true) {
                    if (i13 >= length2) {
                        z6 = true;
                        break;
                    }
                    if (!linkedHashMap.containsKey(strArrOrders[i13])) {
                        z6 = false;
                        break;
                    }
                    i13++;
                }
                z5 = z6;
            }
            if (z5) {
                for (String str10 : strArrOrders) {
                    arrayList3.add((FieldInfo) linkedHashMap.get(str10));
                }
            } else {
                it = linkedHashMap.values().iterator();
                while (it.hasNext()) {
                    arrayList3.add((FieldInfo) it.next());
                }
                if (z2) {
                    Collections.sort(arrayList3);
                }
            }
            return arrayList3;
        }
        strArrOrders = null;
        z5 = false;
        if (z5) {
            while (i2 < r2) {
                arrayList3.add((FieldInfo) linkedHashMap.get(str10));
            }
        } else {
            it = linkedHashMap.values().iterator();
            while (it.hasNext()) {
                arrayList3.add((FieldInfo) it.next());
            }
            if (z2) {
                Collections.sort(arrayList3);
            }
        }
        return arrayList3;
    }

    public static JSONField getSupperMethodAnnotation(Class<?> cls, Method method) {
        for (Class<?> cls2 : cls.getInterfaces()) {
            for (Method method2 : cls2.getMethods()) {
                if (method2.getName().equals(method.getName())) {
                    Class<?>[] parameterTypes = method2.getParameterTypes();
                    Class<?>[] parameterTypes2 = method.getParameterTypes();
                    if (parameterTypes.length == parameterTypes2.length) {
                        int i = 0;
                        while (true) {
                            if (i < parameterTypes.length) {
                                if (!parameterTypes[i].equals(parameterTypes2[i])) {
                                    break;
                                }
                                i++;
                            } else {
                                JSONField jSONField = (JSONField) method2.getAnnotation(JSONField.class);
                                if (jSONField == null) {
                                    break;
                                }
                                return jSONField;
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        if (superclass != null && Modifier.isAbstract(superclass.getModifiers())) {
            Class<?>[] parameterTypes3 = method.getParameterTypes();
            for (Method method3 : superclass.getMethods()) {
                Class<?>[] parameterTypes4 = method3.getParameterTypes();
                if (parameterTypes4.length == parameterTypes3.length && method3.getName().equals(method.getName())) {
                    int i2 = 0;
                    while (true) {
                        if (i2 < parameterTypes3.length) {
                            if (!parameterTypes4[i2].equals(parameterTypes3[i2])) {
                                break;
                            }
                            i2++;
                        } else {
                            JSONField jSONField2 = (JSONField) method3.getAnnotation(JSONField.class);
                            if (jSONField2 == null) {
                                break;
                            }
                            return jSONField2;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isJSONTypeIgnore(Class<?> cls, JSONType jSONType, String str) {
        if (jSONType != null && jSONType.ignores() != null) {
            for (String str2 : jSONType.ignores()) {
                if (str.equalsIgnoreCase(str2)) {
                    return true;
                }
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        return (superclass == Object.class || superclass == null || !isJSONTypeIgnore(superclass, (JSONType) superclass.getAnnotation(JSONType.class), str)) ? false : true;
    }

    public static boolean isGenericParamType(Type type) {
        Type genericSuperclass;
        if (type instanceof ParameterizedType) {
            return true;
        }
        return (type instanceof Class) && (genericSuperclass = ((Class) type).getGenericSuperclass()) != Object.class && isGenericParamType(genericSuperclass);
    }

    public static Type getGenericParamType(Type type) {
        return type instanceof Class ? getGenericParamType(((Class) type).getGenericSuperclass()) : type;
    }

    public static Class<?> getClass(Type type) {
        if (type.getClass() == Class.class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof TypeVariable) {
            return (Class) ((TypeVariable) type).getBounds()[0];
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
            return Object.class;
        }
        return Object.class;
    }

    public static String decapitalize(String str) {
        if (str == null || str.length() == 0 || (str.length() > 1 && Character.isUpperCase(str.charAt(1)) && Character.isUpperCase(str.charAt(0)))) {
            return str;
        }
        char[] charArray = str.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        return new String(charArray);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean setAccessible(Class<?> cls, Member member, int i) {
        if (member != 0 && setAccessibleEnable) {
            Class<? super Object> superclass = cls.getSuperclass();
            if ((superclass == null || superclass == Object.class) && (member.getModifiers() & 1) != 0 && (i & 1) != 0) {
                return false;
            }
            try {
                ((AccessibleObject) member).setAccessible(true);
                return true;
            } catch (AccessControlException unused) {
                setAccessibleEnable = false;
            }
        }
        return false;
    }

    public static Field getField(Class<?> cls, String str, Field[] fieldArr) {
        return getField(cls, str, fieldArr, null);
    }

    public static Field getField(Class<?> cls, String str, Field[] fieldArr, Map<Class<?>, Field[]> map) {
        Field field0 = getField0(cls, str, fieldArr, map);
        if (field0 == null) {
            field0 = getField0(cls, "_" + str, fieldArr, map);
        }
        if (field0 == null) {
            field0 = getField0(cls, "m_" + str, fieldArr, map);
        }
        if (field0 != null) {
            return field0;
        }
        return getField0(cls, "m" + str.substring(0, 1).toUpperCase() + str.substring(1), fieldArr, map);
    }

    private static Field getField0(Class<?> cls, String str, Field[] fieldArr, Map<Class<?>, Field[]> map) {
        char cCharAt;
        char cCharAt2;
        for (Field field : fieldArr) {
            String name = field.getName();
            if (str.equals(name) || (str.length() > 2 && (cCharAt = str.charAt(0)) >= 'a' && cCharAt <= 'z' && (cCharAt2 = str.charAt(1)) >= 'A' && cCharAt2 <= 'Z' && str.equalsIgnoreCase(name))) {
                return field;
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        Field[] declaredFields = map != null ? map.get(superclass) : null;
        if (declaredFields == null) {
            declaredFields = superclass.getDeclaredFields();
            if (map != null) {
                map.put(superclass, declaredFields);
            }
        }
        return getField(superclass, str, declaredFields, map);
    }

    /* JADX WARN: Code duplicated, block: B:14:0x003a  */
    public static Type getCollectionItemType(Type type) {
        Type collectionItemType;
        if (type instanceof ParameterizedType) {
            collectionItemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (collectionItemType instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType) collectionItemType).getUpperBounds();
                if (upperBounds.length == 1) {
                    collectionItemType = upperBounds[0];
                }
            }
        } else if (type instanceof Class) {
            Class cls = (Class) type;
            if (cls.getName().startsWith("java.")) {
                collectionItemType = null;
            } else {
                collectionItemType = getCollectionItemType(cls.getGenericSuperclass());
            }
        } else {
            collectionItemType = null;
        }
        return collectionItemType == null ? Object.class : collectionItemType;
    }

    public static Object defaultValue(Class<?> cls) {
        if (cls == Byte.TYPE) {
            return (byte) 0;
        }
        if (cls == Short.TYPE) {
            return (short) 0;
        }
        if (cls == Integer.TYPE) {
            return 0;
        }
        if (cls == Long.TYPE) {
            return 0L;
        }
        if (cls == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (cls == Double.TYPE) {
            return Double.valueOf(0.0d);
        }
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        return cls == Character.TYPE ? '0' : null;
    }

    public static boolean getArgument(Type[] typeArr, TypeVariable[] typeVariableArr, Type[] typeArr2) {
        if (typeArr2 == null || typeVariableArr.length == 0) {
            return false;
        }
        boolean z = false;
        for (int i = 0; i < typeArr.length; i++) {
            Type type = typeArr[i];
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (getArgument(actualTypeArguments, typeVariableArr, typeArr2)) {
                    typeArr[i] = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
                    z = true;
                }
            } else if (type instanceof TypeVariable) {
                for (int i2 = 0; i2 < typeVariableArr.length; i2++) {
                    if (type.equals(typeVariableArr[i2])) {
                        typeArr[i] = typeArr2[i2];
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public static double parseDouble(String str) {
        double d;
        double d2;
        int length = str.length();
        if (length > 10) {
            return Double.parseDouble(str);
        }
        long j = 0;
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char cCharAt = str.charAt(i2);
            if (cCharAt == '-' && i2 == 0) {
                z = true;
            } else if (cCharAt == '.') {
                if (i != 0) {
                    return Double.parseDouble(str);
                }
                i = (length - i2) - 1;
            } else {
                if (cCharAt < '0' || cCharAt > '9') {
                    return Double.parseDouble(str);
                }
                j = (j * 10) + ((long) (cCharAt - '0'));
            }
        }
        if (z) {
            j = -j;
        }
        switch (i) {
            case 0:
                return j;
            case 1:
                d = j;
                d2 = 10.0d;
                break;
            case 2:
                d = j;
                d2 = 100.0d;
                break;
            case 3:
                d = j;
                d2 = 1000.0d;
                break;
            case 4:
                d = j;
                d2 = 10000.0d;
                break;
            case 5:
                d = j;
                d2 = 100000.0d;
                break;
            case 6:
                d = j;
                d2 = 1000000.0d;
                break;
            case 7:
                d = j;
                d2 = 1.0E7d;
                break;
            case 8:
                d = j;
                d2 = 1.0E8d;
                break;
            case 9:
                d = j;
                d2 = 1.0E9d;
                break;
            default:
                return Double.parseDouble(str);
        }
        return d / d2;
    }

    public static float parseFloat(String str) {
        float f;
        float f2;
        int length = str.length();
        if (length >= 10) {
            return Float.parseFloat(str);
        }
        long j = 0;
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char cCharAt = str.charAt(i2);
            if (cCharAt == '-' && i2 == 0) {
                z = true;
            } else if (cCharAt == '.') {
                if (i != 0) {
                    return Float.parseFloat(str);
                }
                i = (length - i2) - 1;
            } else {
                if (cCharAt < '0' || cCharAt > '9') {
                    return Float.parseFloat(str);
                }
                j = (j * 10) + ((long) (cCharAt - '0'));
            }
        }
        if (z) {
            j = -j;
        }
        switch (i) {
            case 0:
                return j;
            case 1:
                f = j;
                f2 = 10.0f;
                break;
            case 2:
                f = j;
                f2 = 100.0f;
                break;
            case 3:
                f = j;
                f2 = 1000.0f;
                break;
            case 4:
                f = j;
                f2 = 10000.0f;
                break;
            case 5:
                f = j;
                f2 = 100000.0f;
                break;
            case 6:
                f = j;
                f2 = 1000000.0f;
                break;
            case 7:
                f = j;
                f2 = 1.0E7f;
                break;
            case 8:
                f = j;
                f2 = 1.0E8f;
                break;
            case 9:
                f = j;
                f2 = 1.0E9f;
                break;
            default:
                return Float.parseFloat(str);
        }
        return f / f2;
    }

    public static long fnv_64_lower(String str) {
        if (str == null) {
            return 0L;
        }
        long j = -3750763034362895579L;
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt != '_' && cCharAt != '-') {
                if (cCharAt >= 'A' && cCharAt <= 'Z') {
                    cCharAt = (char) (cCharAt + TokenParser.SP);
                }
                j = (j ^ ((long) cCharAt)) * 1099511628211L;
            }
        }
        return j;
    }

    public static void addMapping(String str, Class<?> cls) {
        mappings.put(str, cls);
    }

    public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type genericComponentType = genericArrayType.getGenericComponentType();
        String str = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType).getGenericComponentType();
            str = str + str;
        }
        if (!(genericComponentType instanceof Class)) {
            return genericArrayType;
        }
        Class cls = (Class) genericComponentType;
        if (!cls.isPrimitive()) {
            return genericArrayType;
        }
        try {
            if (cls == Boolean.TYPE) {
                return Class.forName(str + "Z");
            }
            if (cls == Character.TYPE) {
                return Class.forName(str + "C");
            }
            if (cls == Byte.TYPE) {
                return Class.forName(str + "B");
            }
            if (cls == Short.TYPE) {
                return Class.forName(str + "S");
            }
            if (cls == Integer.TYPE) {
                return Class.forName(str + "I");
            }
            if (cls == Long.TYPE) {
                return Class.forName(str + "J");
            }
            if (cls == Float.TYPE) {
                return Class.forName(str + "F");
            }
            if (cls != Double.TYPE) {
                return genericArrayType;
            }
            return Class.forName(str + "D");
        } catch (ClassNotFoundException unused) {
            return genericArrayType;
        }
    }
}
