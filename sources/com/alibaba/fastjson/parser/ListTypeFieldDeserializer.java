package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
class ListTypeFieldDeserializer extends FieldDeserializer {
    private final boolean array;
    private ObjectDeserializer deserializer;
    private final Type itemType;

    public ListTypeFieldDeserializer(ParserConfig parserConfig, Class<?> cls, FieldInfo fieldInfo) {
        super(cls, fieldInfo, 14);
        Type type = fieldInfo.fieldType;
        Class<?> cls2 = fieldInfo.fieldClass;
        if (cls2.isArray()) {
            this.itemType = cls2.getComponentType();
            this.array = true;
        } else {
            this.itemType = TypeUtils.getCollectionItemType(type);
            this.array = false;
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map) {
        List arrayList;
        JSONArray jSONArray;
        Object array;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 8 || (i == 4 && jSONLexer.stringVal().length() == 0)) {
            setValue(obj, (Object) null);
            defaultJSONParser.lexer.nextToken();
            return;
        }
        if (this.array) {
            JSONArray jSONArray2 = new JSONArray();
            jSONArray2.setComponentType(this.itemType);
            jSONArray = jSONArray2;
            arrayList = jSONArray2;
        } else {
            arrayList = new ArrayList();
            jSONArray = null;
        }
        ParseContext parseContext = defaultJSONParser.contex;
        defaultJSONParser.setContext(parseContext, obj, this.fieldInfo.name);
        parseArray(defaultJSONParser, type, arrayList);
        defaultJSONParser.setContext(parseContext);
        Object obj2 = arrayList;
        if (this.array) {
            array = arrayList.toArray((Object[]) Array.newInstance((Class<?>) this.itemType, arrayList.size()));
            jSONArray.setRelatedArray(array);
        }
        if (obj == null) {
            obj2 = array;
            map.put(this.fieldInfo.name, obj2);
        } else {
            obj2 = array;
            setValue(obj, obj2);
        }
    }

    final void parseArray(DefaultJSONParser defaultJSONParser, Type type, Collection collection) {
        int i;
        Class cls;
        int i2;
        int i3;
        Type parameterizedTypeImpl = this.itemType;
        ObjectDeserializer deserializer = this.deserializer;
        if (type instanceof ParameterizedType) {
            if (parameterizedTypeImpl instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) parameterizedTypeImpl;
                ParameterizedType parameterizedType = (ParameterizedType) type;
                cls = parameterizedType.getRawType() instanceof Class ? (Class) parameterizedType.getRawType() : null;
                if (cls == null) {
                    i3 = -1;
                    break;
                }
                int length = cls.getTypeParameters().length;
                i3 = 0;
                while (true) {
                    if (i3 >= length) {
                        i3 = -1;
                        break;
                    } else if (cls.getTypeParameters()[i3].getName().equals(typeVariable.getName())) {
                        break;
                    } else {
                        i3++;
                    }
                }
                if (i3 != -1) {
                    parameterizedTypeImpl = parameterizedType.getActualTypeArguments()[i3];
                    if (!parameterizedTypeImpl.equals(this.itemType)) {
                        deserializer = defaultJSONParser.config.getDeserializer(parameterizedTypeImpl);
                    }
                }
            } else if (parameterizedTypeImpl instanceof ParameterizedType) {
                ParameterizedType parameterizedType2 = (ParameterizedType) parameterizedTypeImpl;
                Type[] actualTypeArguments = parameterizedType2.getActualTypeArguments();
                if (actualTypeArguments.length == 1) {
                    Type type2 = actualTypeArguments[0];
                    if (type2 instanceof TypeVariable) {
                        TypeVariable typeVariable2 = (TypeVariable) type2;
                        ParameterizedType parameterizedType3 = (ParameterizedType) type;
                        cls = parameterizedType3.getRawType() instanceof Class ? (Class) parameterizedType3.getRawType() : null;
                        if (cls == null) {
                            i = 0;
                            i2 = -1;
                            break;
                        }
                        int length2 = cls.getTypeParameters().length;
                        i2 = 0;
                        while (true) {
                            if (i2 >= length2) {
                                i = 0;
                                i2 = -1;
                                break;
                            } else {
                                i = 0;
                                if (cls.getTypeParameters()[i2].getName().equals(typeVariable2.getName())) {
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                        }
                        if (i2 != -1) {
                            actualTypeArguments[i] = parameterizedType3.getActualTypeArguments()[i2];
                            parameterizedTypeImpl = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType2.getOwnerType(), parameterizedType2.getRawType());
                        }
                    }
                }
            }
            i = 0;
        } else {
            i = 0;
            if ((parameterizedTypeImpl instanceof TypeVariable) && (type instanceof Class)) {
                Class cls2 = (Class) type;
                TypeVariable typeVariable3 = (TypeVariable) parameterizedTypeImpl;
                cls2.getTypeParameters();
                int length3 = cls2.getTypeParameters().length;
                for (int i4 = 0; i4 < length3; i4++) {
                    TypeVariable typeVariable4 = cls2.getTypeParameters()[i4];
                    if (typeVariable4.getName().equals(typeVariable3.getName())) {
                        Type[] bounds = typeVariable4.getBounds();
                        if (bounds.length != 1) {
                            break;
                        }
                        parameterizedTypeImpl = bounds[0];
                        break;
                    }
                }
            }
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (deserializer == null) {
            deserializer = defaultJSONParser.config.getDeserializer(parameterizedTypeImpl);
            this.deserializer = deserializer;
        }
        if (jSONLexer.token != 14) {
            if (jSONLexer.token == 12) {
                collection.add(deserializer.deserialze(defaultJSONParser, parameterizedTypeImpl, Integer.valueOf(i)));
                return;
            }
            String str = "exepct '[', but " + JSONToken.name(jSONLexer.token);
            if (type != null) {
                str = str + ", type : " + type;
            }
            throw new JSONException(str);
        }
        char c = jSONLexer.ch;
        char cCharAt = JSONLexer.EOI;
        if (c == '[') {
            int i5 = jSONLexer.bp + 1;
            jSONLexer.bp = i5;
            jSONLexer.ch = i5 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i5);
            jSONLexer.token = 14;
        } else if (c == '{') {
            int i6 = jSONLexer.bp + 1;
            jSONLexer.bp = i6;
            jSONLexer.ch = i6 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i6);
            jSONLexer.token = 12;
        } else if (c == '\"') {
            jSONLexer.scanString();
        } else if (c == ']') {
            int i7 = jSONLexer.bp + 1;
            jSONLexer.bp = i7;
            jSONLexer.ch = i7 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i7);
            jSONLexer.token = 15;
        } else {
            jSONLexer.nextToken();
        }
        while (true) {
            if (jSONLexer.token == 16) {
                jSONLexer.nextToken();
            } else {
                if (jSONLexer.token == 15) {
                    break;
                }
                collection.add(deserializer.deserialze(defaultJSONParser, parameterizedTypeImpl, Integer.valueOf(i)));
                if (defaultJSONParser.resolveStatus == 1) {
                    defaultJSONParser.checkListResolve(collection);
                }
                if (jSONLexer.token == 16) {
                    char c2 = jSONLexer.ch;
                    if (c2 == '[') {
                        int i8 = jSONLexer.bp + 1;
                        jSONLexer.bp = i8;
                        jSONLexer.ch = i8 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i8);
                        jSONLexer.token = 14;
                    } else if (c2 == '{') {
                        int i9 = jSONLexer.bp + 1;
                        jSONLexer.bp = i9;
                        jSONLexer.ch = i9 >= jSONLexer.len ? JSONLexer.EOI : jSONLexer.text.charAt(i9);
                        jSONLexer.token = 12;
                    } else if (c2 == '\"') {
                        jSONLexer.scanString();
                    } else {
                        jSONLexer.nextToken();
                    }
                }
                i++;
            }
        }
        if (jSONLexer.ch == ',') {
            int i10 = jSONLexer.bp + 1;
            jSONLexer.bp = i10;
            if (i10 < jSONLexer.len) {
                cCharAt = jSONLexer.text.charAt(i10);
            }
            jSONLexer.ch = cCharAt;
            jSONLexer.token = 16;
            return;
        }
        jSONLexer.nextToken();
    }
}
