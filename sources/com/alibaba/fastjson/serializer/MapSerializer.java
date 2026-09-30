package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/* JADX INFO: loaded from: classes.dex */
public final class MapSerializer implements ObjectSerializer {
    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        boolean z;
        Object objProcessKey;
        Object objProcessValue;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            serializeWriter.writeNull();
            return;
        }
        Map<String, Object> treeMap = (Map) obj;
        Class<?> cls = treeMap.getClass();
        boolean z2 = (cls == JSONObject.class || cls == HashMap.class || cls == LinkedHashMap.class) && treeMap.containsKey(JSON.DEFAULT_TYPE_KEY);
        if ((serializeWriter.features & SerializerFeature.SortField.mask) != 0) {
            if (treeMap instanceof JSONObject) {
                treeMap = ((JSONObject) treeMap).getInnerMap();
            }
            if (!(treeMap instanceof SortedMap) && !(treeMap instanceof LinkedHashMap)) {
                try {
                    treeMap = new TreeMap(treeMap);
                } catch (Exception unused) {
                }
            }
        }
        if (jSONSerializer.references != null && jSONSerializer.references.containsKey(obj)) {
            jSONSerializer.writeReference(obj);
            return;
        }
        SerialContext serialContext = jSONSerializer.context;
        jSONSerializer.setContext(serialContext, obj, obj2, 0);
        try {
            serializeWriter.write(123);
            jSONSerializer.incrementIndent();
            if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) == 0 || z2) {
                z = true;
            } else {
                serializeWriter.writeFieldName(jSONSerializer.config.typeKey, false);
                serializeWriter.writeString(obj.getClass().getName());
                z = false;
            }
            Class<?> cls2 = null;
            ObjectSerializer objectSerializer = null;
            for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (jSONSerializer.applyName(obj, key) && jSONSerializer.apply(obj, key, value) && ((objProcessValue = JSONSerializer.processValue(jSONSerializer, obj, (objProcessKey = jSONSerializer.processKey(obj, key, value)), value)) != null || (serializeWriter.features & SerializerFeature.WriteMapNullValue.mask) != 0)) {
                    if (objProcessKey instanceof String) {
                        String str = (String) objProcessKey;
                        if (!z) {
                            serializeWriter.write(44);
                        }
                        if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                            jSONSerializer.println();
                        }
                        serializeWriter.writeFieldName(str, true);
                    } else {
                        if (!z) {
                            serializeWriter.write(44);
                        }
                        if ((serializeWriter.features & SerializerFeature.WriteNonStringKeyAsString.mask) != 0 && !(objProcessKey instanceof Enum)) {
                            jSONSerializer.write(JSON.toJSONString(objProcessKey));
                        } else {
                            jSONSerializer.write(objProcessKey);
                        }
                        serializeWriter.write(58);
                    }
                    if (objProcessValue == null) {
                        serializeWriter.writeNull();
                    } else {
                        Class<?> cls3 = objProcessValue.getClass();
                        if (cls3 == cls2) {
                            objectSerializer.write(jSONSerializer, objProcessValue, objProcessKey, null);
                        } else {
                            ObjectSerializer objectSerializer2 = jSONSerializer.config.get(cls3);
                            objectSerializer2.write(jSONSerializer, objProcessValue, objProcessKey, null);
                            objectSerializer = objectSerializer2;
                            cls2 = cls3;
                        }
                    }
                    z = false;
                }
            }
            jSONSerializer.context = serialContext;
            jSONSerializer.decrementIdent();
            if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0 && treeMap.size() > 0) {
                jSONSerializer.println();
            }
            serializeWriter.write(125);
        } catch (Throwable th) {
            jSONSerializer.context = serialContext;
            throw th;
        }
    }
}
