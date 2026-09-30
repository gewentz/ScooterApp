package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.DateCodec;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class DefaultFieldDeserializer extends FieldDeserializer {
    protected ObjectDeserializer fieldValueDeserilizer;

    public DefaultFieldDeserializer(ParserConfig parserConfig, Class<?> cls, FieldInfo fieldInfo) {
        super(cls, fieldInfo, 2);
    }

    public ObjectDeserializer getFieldValueDeserilizer(ParserConfig parserConfig) {
        if (this.fieldValueDeserilizer == null) {
            this.fieldValueDeserilizer = parserConfig.getDeserializer(this.fieldInfo.fieldClass, this.fieldInfo.fieldType);
        }
        return this.fieldValueDeserilizer;
    }

    /* JADX WARN: Code duplicated, block: B:24:0x007b  */
    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map) {
        Object objDeserialze;
        Class<?> cls;
        if (this.fieldValueDeserilizer == null) {
            this.fieldValueDeserilizer = defaultJSONParser.config.getDeserializer(this.fieldInfo.fieldClass, this.fieldInfo.fieldType);
        }
        Type parameterizedTypeImpl = this.fieldInfo.fieldType;
        boolean z = type instanceof ParameterizedType;
        if (z) {
            ParseContext parseContext = defaultJSONParser.contex;
            if (parseContext != null) {
                parseContext.type = type;
            }
            parameterizedTypeImpl = FieldInfo.getFieldType(this.clazz, type, parameterizedTypeImpl);
            this.fieldValueDeserilizer = defaultJSONParser.config.getDeserializer(parameterizedTypeImpl);
        }
        if ((parameterizedTypeImpl instanceof ParameterizedType) && z) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterizedTypeImpl;
            ParameterizedType parameterizedType2 = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type rawType = parameterizedType2.getRawType();
            if ((rawType instanceof Class) && TypeUtils.getArgument(actualTypeArguments, ((Class) rawType).getTypeParameters(), parameterizedType2.getActualTypeArguments())) {
                parameterizedTypeImpl = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
            }
        }
        String str = this.fieldInfo.format;
        if (str != null) {
            ObjectDeserializer objectDeserializer = this.fieldValueDeserilizer;
            if (objectDeserializer instanceof DateCodec) {
                objDeserialze = ((DateCodec) objectDeserializer).deserialze(defaultJSONParser, parameterizedTypeImpl, this.fieldInfo.name, str);
            } else {
                objDeserialze = this.fieldValueDeserilizer.deserialze(defaultJSONParser, parameterizedTypeImpl, this.fieldInfo.name);
            }
        } else {
            objDeserialze = this.fieldValueDeserilizer.deserialze(defaultJSONParser, parameterizedTypeImpl, this.fieldInfo.name);
        }
        if (defaultJSONParser.resolveStatus == 1) {
            DefaultJSONParser.ResolveTask lastResolveTask = defaultJSONParser.getLastResolveTask();
            lastResolveTask.fieldDeserializer = this;
            lastResolveTask.ownerContext = defaultJSONParser.contex;
            defaultJSONParser.resolveStatus = 0;
            return;
        }
        if (obj == null) {
            map.put(this.fieldInfo.name, objDeserialze);
            return;
        }
        if (objDeserialze == null && ((cls = this.fieldInfo.fieldClass) == Byte.TYPE || cls == Short.TYPE || cls == Float.TYPE || cls == Double.TYPE)) {
            return;
        }
        setValue(obj, objDeserialze);
    }
}
