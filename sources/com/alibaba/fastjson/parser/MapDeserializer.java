package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* JADX INFO: loaded from: classes.dex */
class MapDeserializer implements ObjectDeserializer {
    public static MapDeserializer instance = new MapDeserializer();

    MapDeserializer() {
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        if (type == JSONObject.class && defaultJSONParser.fieldTypeResolver == null) {
            return (T) defaultJSONParser.parseObject();
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token == 8) {
            jSONLexer.nextToken(16);
            return null;
        }
        Map<?, ?> mapCreateMap = createMap(type);
        ParseContext parseContext = defaultJSONParser.contex;
        try {
            defaultJSONParser.setContext(parseContext, mapCreateMap, obj);
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type type2 = parameterizedType.getActualTypeArguments()[0];
                Type type3 = parameterizedType.getActualTypeArguments()[1];
                if (String.class == type2) {
                    return (T) parseMap(defaultJSONParser, mapCreateMap, type3, obj);
                }
                return (T) parseMap(defaultJSONParser, mapCreateMap, type2, type3, obj);
            }
            return (T) defaultJSONParser.parseObject(mapCreateMap, obj);
        } finally {
            defaultJSONParser.setContext(parseContext);
        }
    }

    public static Map parseMap(DefaultJSONParser defaultJSONParser, Map<String, Object> map, Type type, Object obj) {
        String strScanSymbolUnQuoted;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token != 12) {
            throw new JSONException("syntax error, expect {, actual " + jSONLexer.token);
        }
        ParseContext parseContext = defaultJSONParser.contex;
        while (true) {
            try {
                jSONLexer.skipWhitespace();
                char c = jSONLexer.ch;
                while (c == ',') {
                    jSONLexer.next();
                    jSONLexer.skipWhitespace();
                    c = jSONLexer.ch;
                }
                if (c == '\"') {
                    strScanSymbolUnQuoted = jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\"');
                    jSONLexer.skipWhitespace();
                    if (jSONLexer.ch != ':') {
                        throw new JSONException("syntax error, " + jSONLexer.info());
                    }
                } else {
                    if (c == '}') {
                        jSONLexer.next();
                        jSONLexer.sp = 0;
                        jSONLexer.nextToken(16);
                        defaultJSONParser.setContext(parseContext);
                        return map;
                    }
                    if (c == '\'') {
                        strScanSymbolUnQuoted = jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\'');
                        jSONLexer.skipWhitespace();
                        if (jSONLexer.ch != ':') {
                            throw new JSONException("syntax error, " + jSONLexer.info());
                        }
                    } else {
                        strScanSymbolUnQuoted = jSONLexer.scanSymbolUnQuoted(defaultJSONParser.symbolTable);
                        jSONLexer.skipWhitespace();
                        char c2 = jSONLexer.ch;
                        if (c2 != ':') {
                            throw new JSONException("expect ':' at " + jSONLexer.pos + ", actual " + c2);
                        }
                    }
                }
                jSONLexer.next();
                jSONLexer.skipWhitespace();
                char c3 = jSONLexer.ch;
                jSONLexer.sp = 0;
                Object object = null;
                if (strScanSymbolUnQuoted != JSON.DEFAULT_TYPE_KEY || jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    jSONLexer.nextToken();
                    defaultJSONParser.setContext(parseContext);
                    if (jSONLexer.token == 8) {
                        jSONLexer.nextToken();
                    } else {
                        object = defaultJSONParser.parseObject(type, strScanSymbolUnQuoted);
                    }
                    map.put(strScanSymbolUnQuoted, object);
                    if (defaultJSONParser.resolveStatus == 1) {
                        defaultJSONParser.checkMapResolve(map, strScanSymbolUnQuoted);
                    }
                    defaultJSONParser.setContext(parseContext, object, strScanSymbolUnQuoted);
                    int i = jSONLexer.token;
                    if (i != 20 && i != 15) {
                        if (i == 13) {
                            jSONLexer.nextToken();
                            defaultJSONParser.setContext(parseContext);
                            return map;
                        }
                    }
                    defaultJSONParser.setContext(parseContext);
                    return map;
                }
                Class<?> clsCheckAutoType = defaultJSONParser.config.checkAutoType(jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\"'), null, jSONLexer.features);
                if (clsCheckAutoType != map.getClass()) {
                    ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(clsCheckAutoType);
                    jSONLexer.nextToken(16);
                    defaultJSONParser.resolveStatus = 2;
                    if (parseContext != null && !(obj instanceof Integer)) {
                        defaultJSONParser.popContext();
                    }
                    Map map2 = (Map) deserializer.deserialze(defaultJSONParser, clsCheckAutoType, obj);
                    defaultJSONParser.setContext(parseContext);
                    return map2;
                }
                jSONLexer.nextToken(16);
                if (jSONLexer.token == 13) {
                    jSONLexer.nextToken(16);
                    defaultJSONParser.setContext(parseContext);
                    return map;
                }
            } catch (Throwable th) {
                defaultJSONParser.setContext(parseContext);
                throw th;
            }
        }
    }

    public static Object parseMap(DefaultJSONParser defaultJSONParser, Map<Object, Object> map, Type type, Type type2, Object obj) {
        Object obj2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token;
        int i2 = 16;
        if (i != 12 && i != 16) {
            throw new JSONException("syntax error, expect {, actual " + JSONToken.name(i));
        }
        ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(type);
        ObjectDeserializer deserializer2 = defaultJSONParser.config.getDeserializer(type2);
        jSONLexer.nextToken();
        ParseContext parseContext = defaultJSONParser.contex;
        while (true) {
            try {
                int i3 = jSONLexer.token;
                if (i3 == 13) {
                    jSONLexer.nextToken(i2);
                    defaultJSONParser.setContext(parseContext);
                    return map;
                }
                if (i3 == 4 && jSONLexer.sp == 4 && jSONLexer.text.startsWith("$ref", jSONLexer.np + 1) && !jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    jSONLexer.nextTokenWithChar(':');
                    if (jSONLexer.token != 4) {
                        throw new JSONException("illegal ref, " + JSONToken.name(i3));
                    }
                    String strStringVal = jSONLexer.stringVal();
                    if ("..".equals(strStringVal)) {
                        obj2 = parseContext.parent.object;
                    } else if ("$".equals(strStringVal)) {
                        ParseContext parseContext2 = parseContext;
                        while (parseContext2.parent != null) {
                            parseContext2 = parseContext2.parent;
                        }
                        obj2 = parseContext2.object;
                    } else {
                        defaultJSONParser.addResolveTask(new DefaultJSONParser.ResolveTask(parseContext, strStringVal));
                        defaultJSONParser.resolveStatus = 1;
                        obj2 = null;
                    }
                    jSONLexer.nextToken(13);
                    if (jSONLexer.token != 13) {
                        throw new JSONException("illegal ref");
                    }
                    jSONLexer.nextToken(16);
                    defaultJSONParser.setContext(parseContext);
                    return obj2;
                }
                if (map.size() == 0 && i3 == 4 && JSON.DEFAULT_TYPE_KEY.equals(jSONLexer.stringVal()) && !jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    jSONLexer.nextTokenWithChar(':');
                    jSONLexer.nextToken(16);
                    if (jSONLexer.token == 13) {
                        jSONLexer.nextToken();
                        defaultJSONParser.setContext(parseContext);
                        return map;
                    }
                    jSONLexer.nextToken();
                }
                Object objDeserialze = deserializer.deserialze(defaultJSONParser, type, null);
                if (jSONLexer.token != 17) {
                    throw new JSONException("syntax error, expect :, actual " + jSONLexer.token);
                }
                jSONLexer.nextToken();
                Object objDeserialze2 = deserializer2.deserialze(defaultJSONParser, type2, objDeserialze);
                if (defaultJSONParser.resolveStatus == 1) {
                    defaultJSONParser.checkMapResolve(map, objDeserialze);
                }
                map.put(objDeserialze, objDeserialze2);
                if (jSONLexer.token == 16) {
                    jSONLexer.nextToken();
                }
                i2 = 16;
            } catch (Throwable th) {
                defaultJSONParser.setContext(parseContext);
                throw th;
            }
        }
    }

    protected Map<?, ?> createMap(Type type) {
        if (type == Properties.class) {
            return new Properties();
        }
        if (type == Hashtable.class) {
            return new Hashtable();
        }
        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }
        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }
        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }
        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }
        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }
        if (type == JSONObject.class) {
            return new JSONObject();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (EnumMap.class.equals(rawType)) {
                return new EnumMap((Class) parameterizedType.getActualTypeArguments()[0]);
            }
            return createMap(rawType);
        }
        Class cls = (Class) type;
        if (cls.isInterface()) {
            throw new JSONException("unsupport type " + type);
        }
        try {
            return (Map) cls.newInstance();
        } catch (Exception e) {
            throw new JSONException("unsupport type " + type, e);
        }
    }
}
