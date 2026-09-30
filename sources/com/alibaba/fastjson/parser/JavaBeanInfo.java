package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
class JavaBeanInfo {
    final Constructor<?> creatorConstructor;
    public final String[] creatorConstructorParameters;
    final Constructor<?> defaultConstructor;
    final int defaultConstructorParameterSize;
    final Method factoryMethod;
    final FieldInfo[] fields;
    final JSONType jsonType;
    boolean ordered = false;
    public final int parserFeatures;
    final FieldInfo[] sortedFields;
    final boolean supportBeanToArray;
    public final String typeKey;
    public final long typeKeyHashCode;
    public final String typeName;

    JavaBeanInfo(Class<?> cls, Constructor<?> constructor, Constructor<?> constructor2, Method method, FieldInfo[] fieldInfoArr, FieldInfo[] fieldInfoArr2, JSONType jSONType, String[] strArr) {
        int i;
        boolean z;
        int length = 0;
        this.defaultConstructor = constructor;
        this.creatorConstructor = constructor2;
        this.factoryMethod = method;
        this.fields = fieldInfoArr;
        this.jsonType = jSONType;
        if (strArr != null && strArr.length == fieldInfoArr.length) {
            this.creatorConstructorParameters = null;
        } else {
            this.creatorConstructorParameters = strArr;
        }
        if (jSONType != null) {
            String strTypeName = jSONType.typeName();
            this.typeName = strTypeName.length() <= 0 ? cls.getName() : strTypeName;
            String strTypeKey = jSONType.typeKey();
            this.typeKey = strTypeKey.length() > 0 ? strTypeKey : null;
            i = 0;
            for (Feature feature : jSONType.parseFeatures()) {
                i |= feature.mask;
            }
        } else {
            this.typeName = cls.getName();
            this.typeKey = null;
            i = 0;
        }
        String str = this.typeKey;
        if (str == null) {
            this.typeKeyHashCode = 0L;
        } else {
            this.typeKeyHashCode = TypeUtils.fnv_64_lower(str);
        }
        this.parserFeatures = i;
        if (jSONType != null) {
            Feature[] features = jSONType.parseFeatures();
            z = false;
            for (Feature feature2 : features) {
                if (feature2 == Feature.SupportArrayToBean) {
                    z = true;
                }
            }
        } else {
            z = false;
        }
        this.supportBeanToArray = z;
        FieldInfo[] fieldInfoArrComputeSortedFields = computeSortedFields(fieldInfoArr, fieldInfoArr2);
        this.sortedFields = Arrays.equals(fieldInfoArr, fieldInfoArrComputeSortedFields) ? fieldInfoArr : fieldInfoArrComputeSortedFields;
        if (constructor != null) {
            length = constructor.getParameterTypes().length;
        } else if (method != null) {
            length = method.getParameterTypes().length;
        }
        this.defaultConstructorParameterSize = length;
    }

    private FieldInfo[] computeSortedFields(FieldInfo[] fieldInfoArr, FieldInfo[] fieldInfoArr2) {
        String[] strArrOrders;
        JSONType jSONType = this.jsonType;
        if (jSONType != null && (strArrOrders = jSONType.orders()) != null && strArrOrders.length != 0) {
            for (String str : strArrOrders) {
                for (FieldInfo fieldInfo : fieldInfoArr2) {
                    if (fieldInfo.name.equals(str)) {
                    }
                }
                return fieldInfoArr2;
            }
            if (strArrOrders.length == fieldInfoArr.length) {
                for (int i = 0; i < strArrOrders.length; i++) {
                    if (!fieldInfoArr2[i].name.equals(strArrOrders[i])) {
                        FieldInfo[] fieldInfoArr3 = new FieldInfo[fieldInfoArr2.length];
                        for (int i2 = 0; i2 < strArrOrders.length; i2++) {
                            for (int i3 = 0; i3 < fieldInfoArr2.length; i3++) {
                                if (fieldInfoArr2[i3].name.equals(strArrOrders[i2])) {
                                    fieldInfoArr3[i2] = fieldInfoArr2[i3];
                                    break;
                                }
                            }
                        }
                        this.ordered = true;
                        return fieldInfoArr3;
                    }
                }
            } else {
                int length = fieldInfoArr2.length;
                FieldInfo[] fieldInfoArr4 = new FieldInfo[length];
                for (int i4 = 0; i4 < strArrOrders.length; i4++) {
                    for (int i5 = 0; i5 < fieldInfoArr2.length; i5++) {
                        if (fieldInfoArr2[i5].name.equals(strArrOrders[i4])) {
                            fieldInfoArr4[i4] = fieldInfoArr2[i5];
                            break;
                        }
                    }
                }
                int length2 = strArrOrders.length;
                for (int i6 = 0; i6 < fieldInfoArr2.length; i6++) {
                    int i7 = 0;
                    while (true) {
                        if (i7 < length && i7 < length2) {
                            if (fieldInfoArr4[i6].equals(fieldInfoArr2[i7])) {
                                break;
                            }
                            i7++;
                        } else {
                            fieldInfoArr4[length2] = fieldInfoArr2[i6];
                            length2++;
                            break;
                        }
                    }
                }
                this.ordered = true;
            }
        }
        return fieldInfoArr2;
    }

    static boolean addField(List<FieldInfo> list, FieldInfo fieldInfo, boolean z) {
        if (!z) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                FieldInfo fieldInfo2 = list.get(i);
                if (fieldInfo2.name.equals(fieldInfo.name) && (!fieldInfo2.getOnly || fieldInfo.getOnly)) {
                    return false;
                }
            }
        }
        list.add(fieldInfo);
        return true;
    }

    /* JADX WARN: Code duplicated, block: B:195:0x03b0  */
    /* JADX WARN: Code duplicated, block: B:221:0x0461 A[PHI: r18 r19 r21 r25 r28
  0x0461: PHI (r18v10 int) = (r5v38 int), (r5v38 int), (r5v38 int), (r18v14 int) binds: [B:220:0x045f, B:236:0x04ba, B:238:0x04c4, B:216:0x040e] A[DONT_GENERATE, DONT_INLINE]
  0x0461: PHI (r19v13 int) = (r19v12 int), (r19v12 int), (r19v12 int), (r19v17 int) binds: [B:220:0x045f, B:236:0x04ba, B:238:0x04c4, B:216:0x040e] A[DONT_GENERATE, DONT_INLINE]
  0x0461: PHI (r21v14 java.lang.reflect.Field[]) = 
  (r13v8 java.lang.reflect.Field[])
  (r13v8 java.lang.reflect.Field[])
  (r13v8 java.lang.reflect.Field[])
  (r21v20 java.lang.reflect.Field[])
 binds: [B:220:0x045f, B:236:0x04ba, B:238:0x04c4, B:216:0x040e] A[DONT_GENERATE, DONT_INLINE]
  0x0461: PHI (r25v4 java.lang.reflect.Constructor<?>) = 
  (r9v19 java.lang.reflect.Constructor<?>)
  (r9v19 java.lang.reflect.Constructor<?>)
  (r9v19 java.lang.reflect.Constructor<?>)
  (r25v6 java.lang.reflect.Constructor<?>)
 binds: [B:220:0x045f, B:236:0x04ba, B:238:0x04c4, B:216:0x040e] A[DONT_GENERATE, DONT_INLINE]
  0x0461: PHI (r28v5 java.lang.reflect.Method[]) = 
  (r24v6 java.lang.reflect.Method[])
  (r24v6 java.lang.reflect.Method[])
  (r24v6 java.lang.reflect.Method[])
  (r28v7 java.lang.reflect.Method[])
 binds: [B:220:0x045f, B:236:0x04ba, B:238:0x04c4, B:216:0x040e] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:249:0x0508 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:250:0x050a  */
    /* JADX WARN: Code duplicated, block: B:251:0x0516  */
    /* JADX WARN: Code duplicated, block: B:253:0x051c  */
    /* JADX WARN: Code duplicated, block: B:255:0x0532  */
    /* JADX WARN: Code duplicated, block: B:256:0x054d  */
    /* JADX WARN: Code duplicated, block: B:258:0x0556  */
    /* JADX WARN: Code duplicated, block: B:259:0x0558  */
    /* JADX WARN: Code duplicated, block: B:263:0x0567  */
    /* JADX WARN: Code duplicated, block: B:279:0x05c9  */
    /* JADX WARN: Code duplicated, block: B:281:0x05d1  */
    /* JADX WARN: Code duplicated, block: B:298:0x060c  */
    /* JADX WARN: Code duplicated, block: B:300:0x0610  */
    /* JADX WARN: Code duplicated, block: B:350:0x06fd  */
    /* JADX WARN: Code duplicated, block: B:352:0x073a  */
    /* JADX WARN: Code duplicated, block: B:372:0x05d4 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:379:0x0613 A[SYNTHETIC] */
    /* JADX WARN: Instruction removed from duplicated block: B:350:0x06fd, please report this as an issue */
    public static JavaBeanInfo build(Class<?> cls, int i, Type type, boolean z, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        Constructor<?> constructor;
        boolean z5;
        int i2;
        Method[] methodArr;
        Method method;
        Class<?> cls2;
        Constructor<?> constructor2;
        Field[] fieldArr;
        Method[] methodArr2;
        Constructor<?> constructor3;
        Method method2;
        String[] strArr;
        String[] strArr2;
        Class<?>[] clsArr;
        JSONField jSONField;
        int iOrdinal;
        int i3;
        JSONField jSONField2;
        int i4;
        JSONField jSONField3;
        Method[] methodArr3;
        int i5;
        String strName;
        int i6;
        int i7;
        Class<?> returnType;
        int i8;
        int i9;
        int i10;
        int i11;
        JSONField jSONField4;
        String strDecapitalize;
        Field field;
        Field field2;
        int i12;
        int iOrdinal2;
        JSONField jSONField5;
        int iOf;
        Constructor<?> declaredConstructor;
        Class<?> cls3 = cls;
        ArrayList arrayList = new ArrayList();
        HashMap map = new HashMap();
        Constructor<?>[] declaredConstructors = cls3.getDeclaredConstructors();
        boolean zIsKotlin = TypeUtils.isKotlin(cls3);
        int i13 = i & 1024;
        if (i13 == 0 && (declaredConstructors.length == 1 || !zIsKotlin)) {
            try {
                declaredConstructor = cls3.getDeclaredConstructor(null);
            } catch (Exception unused) {
                declaredConstructor = null;
            }
            if (declaredConstructor != null || !cls3.isMemberClass() || (i & 8) != 0) {
                constructor = declaredConstructor;
                break;
            }
            int length = declaredConstructors.length;
            int i14 = 0;
            while (true) {
                if (i14 >= length) {
                    constructor = declaredConstructor;
                    break;
                }
                Constructor<?> constructor4 = declaredConstructors[i14];
                Class<?>[] parameterTypes = constructor4.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].equals(cls3.getDeclaringClass())) {
                    constructor = constructor4;
                    break;
                }
                i14++;
            }
        } else {
            constructor = null;
        }
        if (z) {
            z5 = zIsKotlin;
            i2 = i13;
            methodArr = null;
            method = null;
        } else {
            ArrayList arrayList2 = new ArrayList();
            Class<?> superclass = cls3;
            Method method3 = null;
            while (superclass != null && superclass != Object.class) {
                Method[] declaredMethods = superclass.getDeclaredMethods();
                int length2 = declaredMethods.length;
                int i15 = 0;
                while (i15 < length2) {
                    boolean z6 = zIsKotlin;
                    Method method4 = declaredMethods[i15];
                    int i16 = i13;
                    int modifiers = method4.getModifiers();
                    if ((modifiers & 8) == 0) {
                        if ((modifiers & 2) == 0) {
                            cls2 = superclass;
                            if ((modifiers & 256) == 0 && (modifiers & 4) == 0) {
                                arrayList2.add(method4);
                            }
                        }
                        i15++;
                        zIsKotlin = z6;
                        i13 = i16;
                        superclass = cls2;
                    } else if (method4.isAnnotationPresent(JSONCreator.class)) {
                        if (method3 != null) {
                            throw new JSONException("multi-json creator");
                        }
                        method3 = method4;
                    }
                    cls2 = superclass;
                    i15++;
                    zIsKotlin = z6;
                    i13 = i16;
                    superclass = cls2;
                }
                superclass = superclass.getSuperclass();
            }
            z5 = zIsKotlin;
            i2 = i13;
            Method[] methodArr4 = new Method[arrayList2.size()];
            arrayList2.toArray(methodArr4);
            methodArr = methodArr4;
            method = method3;
        }
        Field[] declaredFields = cls3.getDeclaredFields();
        boolean z7 = cls3.isInterface() || i2 != 0;
        Method[] methodArr5 = methodArr;
        if (constructor == null || z7) {
            int length3 = declaredConstructors.length;
            int i17 = 0;
            while (true) {
                if (i17 >= length3) {
                    constructor2 = null;
                    break;
                }
                Constructor<?> constructor5 = declaredConstructors[i17];
                if (((JSONCreator) constructor5.getAnnotation(JSONCreator.class)) != null) {
                    constructor2 = constructor5;
                    break;
                }
                i17++;
            }
            String str = "illegal json creator";
            if (constructor2 != null) {
                TypeUtils.setAccessible(cls3, constructor2, i);
                Class<?>[] parameterTypes2 = constructor2.getParameterTypes();
                Type[] genericParameterTypes = z4 ? constructor2.getGenericParameterTypes() : parameterTypes2;
                Annotation[][] parameterAnnotations = constructor2.getParameterAnnotations();
                int i18 = 0;
                while (i18 < parameterTypes2.length) {
                    Annotation[] annotationArr = parameterAnnotations[i18];
                    int length4 = annotationArr.length;
                    Class<?>[] clsArr2 = parameterTypes2;
                    int i19 = 0;
                    while (true) {
                        if (i19 >= length4) {
                            i4 = i18;
                            jSONField3 = null;
                            break;
                        }
                        int i20 = i19;
                        Annotation annotation = annotationArr[i20];
                        i4 = i18;
                        if (annotation instanceof JSONField) {
                            jSONField3 = (JSONField) annotation;
                            break;
                        }
                        i19 = i20 + 1;
                        i18 = i4;
                    }
                    if (jSONField3 == null) {
                        throw new JSONException(str);
                    }
                    Method[] methodArr6 = methodArr5;
                    Class<?> cls4 = clsArr2[i4];
                    Type type2 = genericParameterTypes[i4];
                    Field field3 = TypeUtils.getField(cls3, jSONField3.name(), declaredFields, map);
                    if (field3 != null) {
                        TypeUtils.setAccessible(cls3, field3, i);
                    }
                    cls3 = cls;
                    addField(arrayList, new FieldInfo(jSONField3.name(), cls, cls4, type2, field3, jSONField3.ordinal(), SerializerFeature.of(jSONField3.serialzeFeatures())), z);
                    i18 = i4 + 1;
                    str = str;
                    declaredFields = declaredFields;
                    parameterTypes2 = clsArr2;
                    constructor2 = constructor2;
                    methodArr5 = methodArr6;
                }
                fieldArr = declaredFields;
                methodArr2 = methodArr5;
                constructor3 = constructor2;
                int size = arrayList.size();
                FieldInfo[] fieldInfoArr = new FieldInfo[size];
                arrayList.toArray(fieldInfoArr);
                FieldInfo[] fieldInfoArr2 = new FieldInfo[size];
                System.arraycopy(fieldInfoArr, 0, fieldInfoArr2, 0, size);
                Arrays.sort(fieldInfoArr2);
                if (z2) {
                }
                String[] strArr3 = new String[size];
                for (int i21 = 0; i21 < size; i21++) {
                    strArr3[i21] = fieldInfoArr[i21].name;
                }
                strArr2 = strArr3;
                method2 = method;
            } else {
                fieldArr = declaredFields;
                methodArr2 = methodArr5;
                constructor3 = constructor2;
                if (method != null) {
                    TypeUtils.setAccessible(cls3, method, i);
                    Class<?>[] parameterTypes3 = method.getParameterTypes();
                    if (parameterTypes3.length > 0) {
                        Type[] genericParameterTypes2 = z4 ? method.getGenericParameterTypes() : parameterTypes3;
                        Annotation[][] parameterAnnotations2 = method.getParameterAnnotations();
                        int i22 = 0;
                        while (i22 < parameterTypes3.length) {
                            Annotation[] annotationArr2 = parameterAnnotations2[i22];
                            int length5 = annotationArr2.length;
                            int i23 = 0;
                            while (true) {
                                if (i23 >= length5) {
                                    jSONField2 = null;
                                    break;
                                }
                                Annotation annotation2 = annotationArr2[i23];
                                if (annotation2 instanceof JSONField) {
                                    jSONField2 = (JSONField) annotation2;
                                    break;
                                }
                                i23++;
                            }
                            if (jSONField2 == null) {
                                throw new JSONException("illegal json creator");
                            }
                            JSONField jSONField6 = jSONField2;
                            Class<?> cls5 = cls3;
                            cls3 = cls5;
                            addField(arrayList, new FieldInfo(jSONField6.name(), cls5, parameterTypes3[i22], genericParameterTypes2[i22], TypeUtils.getField(cls3, jSONField2.name(), fieldArr, map), jSONField6.ordinal(), SerializerFeature.of(jSONField6.serialzeFeatures())), z);
                            i22++;
                            parameterTypes3 = parameterTypes3;
                        }
                        int size2 = arrayList.size();
                        FieldInfo[] fieldInfoArr3 = new FieldInfo[size2];
                        arrayList.toArray(fieldInfoArr3);
                        FieldInfo[] fieldInfoArr4 = new FieldInfo[size2];
                        System.arraycopy(fieldInfoArr3, 0, fieldInfoArr4, 0, size2);
                        Arrays.sort(fieldInfoArr4);
                        return new JavaBeanInfo(cls3, null, null, method, fieldInfoArr3, Arrays.equals(fieldInfoArr3, fieldInfoArr4) ? fieldInfoArr3 : fieldInfoArr4, z2 ? (JSONType) cls3.getAnnotation(JSONType.class) : null, null);
                    }
                    method2 = method;
                    strArr = null;
                } else {
                    method2 = method;
                    strArr = null;
                    if (!z7) {
                        if (!z5 || declaredConstructors.length <= 0) {
                            throw new JSONException("default constructor not found. " + cls3);
                        }
                        String[] koltinConstructorParameters = TypeUtils.getKoltinConstructorParameters(cls3);
                        if (koltinConstructorParameters == null) {
                            throw new JSONException("default constructor not found. " + cls3);
                        }
                        Constructor<?> constructor6 = constructor3;
                        for (Constructor<?> constructor7 : declaredConstructors) {
                            Class<?>[] parameterTypes4 = constructor7.getParameterTypes();
                            if ((parameterTypes4.length <= 0 || !parameterTypes4[parameterTypes4.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")) && (constructor6 == null || constructor6.getParameterTypes().length < parameterTypes4.length)) {
                                constructor6 = constructor7;
                            }
                        }
                        constructor6.setAccessible(true);
                        TypeUtils.setAccessible(cls3, constructor6, i);
                        Class<?>[] parameterTypes5 = constructor6.getParameterTypes();
                        Type[] genericParameterTypes3 = z4 ? constructor6.getGenericParameterTypes() : parameterTypes5;
                        Annotation[][] parameterAnnotations3 = constructor6.getParameterAnnotations();
                        int i24 = 0;
                        while (i24 < parameterTypes5.length) {
                            String str2 = koltinConstructorParameters[i24];
                            Annotation[] annotationArr3 = parameterAnnotations3[i24];
                            int length6 = annotationArr3.length;
                            int i25 = 0;
                            while (true) {
                                if (i25 >= length6) {
                                    clsArr = parameterTypes5;
                                    jSONField = null;
                                    break;
                                }
                                Annotation annotation3 = annotationArr3[i25];
                                clsArr = parameterTypes5;
                                if (annotation3 instanceof JSONField) {
                                    jSONField = (JSONField) annotation3;
                                    break;
                                }
                                i25++;
                                parameterTypes5 = clsArr;
                            }
                            Class<?> cls6 = clsArr[i24];
                            Type type3 = genericParameterTypes3[i24];
                            Field field4 = TypeUtils.getField(cls3, str2, fieldArr, map);
                            if (field4 != null && jSONField == null) {
                                jSONField = (JSONField) field4.getAnnotation(JSONField.class);
                            }
                            if (jSONField != null) {
                                iOrdinal = jSONField.ordinal();
                                int iOf2 = SerializerFeature.of(jSONField.serialzeFeatures());
                                String strName2 = jSONField.name();
                                if (strName2.length() != 0) {
                                    str2 = strName2;
                                }
                                i3 = iOf2;
                            } else {
                                iOrdinal = 0;
                                i3 = 0;
                            }
                            cls3 = cls;
                            addField(arrayList, new FieldInfo(str2, cls, cls6, type3, field4, iOrdinal, i3), z);
                            i24++;
                            parameterTypes5 = clsArr;
                        }
                        int size3 = arrayList.size();
                        FieldInfo[] fieldInfoArr5 = new FieldInfo[size3];
                        arrayList.toArray(fieldInfoArr5);
                        FieldInfo[] fieldInfoArr6 = new FieldInfo[size3];
                        System.arraycopy(fieldInfoArr5, 0, fieldInfoArr6, 0, size3);
                        Arrays.sort(fieldInfoArr6);
                        String[] strArr4 = new String[size3];
                        for (int i26 = 0; i26 < size3; i26++) {
                            strArr4[i26] = fieldInfoArr5[i26].name;
                        }
                        strArr2 = strArr4;
                        constructor3 = constructor6;
                    }
                }
                strArr2 = strArr;
            }
        } else {
            fieldArr = declaredFields;
            methodArr2 = methodArr5;
            method2 = method;
            strArr2 = null;
            constructor3 = null;
        }
        if (constructor != null) {
            TypeUtils.setAccessible(cls3, constructor, i);
        }
        int i27 = 4;
        if (z) {
            methodArr3 = methodArr2;
        } else {
            Method[] methodArr7 = methodArr2;
            int length7 = methodArr7.length;
            int i28 = 0;
            while (i28 < length7) {
                Method[] methodArr8 = methodArr7;
                Method method5 = methodArr8[i28];
                String name = method5.getName();
                if (name.length() >= i27 && (((returnType = method5.getReturnType()) == Void.TYPE || returnType == method5.getDeclaringClass()) && method5.getParameterTypes().length == 1)) {
                    JSONField supperMethodAnnotation = z3 ? (JSONField) method5.getAnnotation(JSONField.class) : null;
                    if (supperMethodAnnotation == null && z3) {
                        supperMethodAnnotation = TypeUtils.getSupperMethodAnnotation(cls3, method5);
                    }
                    if (supperMethodAnnotation == null) {
                        i8 = i28;
                        i9 = 0;
                        i10 = 0;
                        i11 = 3;
                        jSONField4 = null;
                    } else if (supperMethodAnnotation.deserialize()) {
                        int i29 = i28;
                        int iOrdinal3 = supperMethodAnnotation.ordinal();
                        int iOf3 = SerializerFeature.of(supperMethodAnnotation.serialzeFeatures());
                        if (supperMethodAnnotation.name().length() != 0) {
                            constructor = constructor;
                            fieldArr = fieldArr;
                            methodArr8 = methodArr8;
                            i8 = i29;
                            length7 = length7;
                            addField(arrayList, new FieldInfo(supperMethodAnnotation.name(), method5, null, cls3, type, iOrdinal3, iOf3, supperMethodAnnotation, null, z4), z);
                            TypeUtils.setAccessible(cls3, method5, i);
                        } else {
                            i8 = i29;
                            i9 = iOf3;
                            i10 = iOrdinal3;
                            i11 = 3;
                            jSONField4 = null;
                        }
                        fieldArr = fieldArr;
                    } else {
                        length7 = length7;
                        i8 = i28;
                        constructor = constructor;
                        methodArr8 = methodArr8;
                    }
                    if (name.startsWith("set")) {
                        char cCharAt = name.charAt(i11);
                        if (Character.isUpperCase(cCharAt)) {
                            if (TypeUtils.compatibleWithJavaBean) {
                                strDecapitalize = TypeUtils.decapitalize(name.substring(i11));
                            } else {
                                strDecapitalize = Character.toLowerCase(name.charAt(i11)) + name.substring(4);
                            }
                            field = TypeUtils.getField(cls3, strDecapitalize, fieldArr, map);
                            if (field != null && method5.getParameterTypes()[0] == Boolean.TYPE) {
                                field = TypeUtils.getField(cls3, "is" + Character.toUpperCase(strDecapitalize.charAt(0)) + strDecapitalize.substring(1), fieldArr, map);
                            }
                            field2 = field;
                            if (field2 != null) {
                                if (z3) {
                                    jSONField5 = (JSONField) field2.getAnnotation(JSONField.class);
                                } else {
                                    jSONField5 = jSONField4;
                                }
                                if (jSONField5 != null) {
                                    iOrdinal2 = jSONField5.ordinal();
                                    iOf = SerializerFeature.of(jSONField5.serialzeFeatures());
                                    if (jSONField5.name().length() != 0) {
                                        fieldArr = fieldArr;
                                        addField(arrayList, new FieldInfo(jSONField5.name(), method5, field2, cls3, type, iOrdinal2, iOf, supperMethodAnnotation, jSONField5, z4), z);
                                        cls3 = cls;
                                    } else {
                                        i12 = iOf;
                                        map = map;
                                        if (supperMethodAnnotation == null) {
                                            fieldArr = fieldArr;
                                            supperMethodAnnotation = jSONField5;
                                        }
                                    }
                                }
                                fieldArr = fieldArr;
                                if (propertyNamingStrategy != null) {
                                    strDecapitalize = propertyNamingStrategy.translate(strDecapitalize);
                                }
                                cls3 = cls;
                                addField(arrayList, new FieldInfo(strDecapitalize, method5, null, cls3, type, iOrdinal2, i12, supperMethodAnnotation, null, z4), z);
                                TypeUtils.setAccessible(cls3, method5, i);
                            } else {
                                i10 = i10;
                            }
                            fieldArr = fieldArr;
                            map = map;
                            i12 = i9;
                            iOrdinal2 = i10;
                            fieldArr = fieldArr;
                            if (propertyNamingStrategy != null) {
                                strDecapitalize = propertyNamingStrategy.translate(strDecapitalize);
                            }
                            cls3 = cls;
                            addField(arrayList, new FieldInfo(strDecapitalize, method5, null, cls3, type, iOrdinal2, i12, supperMethodAnnotation, null, z4), z);
                            TypeUtils.setAccessible(cls3, method5, i);
                        } else if (cCharAt == '_') {
                            strDecapitalize = name.substring(4);
                        } else if (cCharAt == 'f') {
                            strDecapitalize = name.substring(i11);
                        } else if (name.length() < 5 || !Character.isUpperCase(name.charAt(4))) {
                            fieldArr = fieldArr;
                        } else {
                            strDecapitalize = TypeUtils.decapitalize(name.substring(i11));
                        }
                        field = TypeUtils.getField(cls3, strDecapitalize, fieldArr, map);
                        if (field != null) {
                        }
                        field2 = field;
                        if (field2 != null) {
                            if (z3) {
                                jSONField5 = (JSONField) field2.getAnnotation(JSONField.class);
                            } else {
                                jSONField5 = jSONField4;
                            }
                            if (jSONField5 != null) {
                                iOrdinal2 = jSONField5.ordinal();
                                iOf = SerializerFeature.of(jSONField5.serialzeFeatures());
                                if (jSONField5.name().length() != 0) {
                                    fieldArr = fieldArr;
                                    addField(arrayList, new FieldInfo(jSONField5.name(), method5, field2, cls3, type, iOrdinal2, iOf, supperMethodAnnotation, jSONField5, z4), z);
                                    cls3 = cls;
                                } else {
                                    i12 = iOf;
                                    map = map;
                                    if (supperMethodAnnotation == null) {
                                        fieldArr = fieldArr;
                                        supperMethodAnnotation = jSONField5;
                                    }
                                }
                            }
                            fieldArr = fieldArr;
                            if (propertyNamingStrategy != null) {
                                strDecapitalize = propertyNamingStrategy.translate(strDecapitalize);
                            }
                            cls3 = cls;
                            addField(arrayList, new FieldInfo(strDecapitalize, method5, null, cls3, type, iOrdinal2, i12, supperMethodAnnotation, null, z4), z);
                            TypeUtils.setAccessible(cls3, method5, i);
                        } else {
                            i10 = i10;
                        }
                        fieldArr = fieldArr;
                        map = map;
                        i12 = i9;
                        iOrdinal2 = i10;
                        fieldArr = fieldArr;
                        if (propertyNamingStrategy != null) {
                            strDecapitalize = propertyNamingStrategy.translate(strDecapitalize);
                        }
                        cls3 = cls;
                        addField(arrayList, new FieldInfo(strDecapitalize, method5, null, cls3, type, iOrdinal2, i12, supperMethodAnnotation, null, z4), z);
                        TypeUtils.setAccessible(cls3, method5, i);
                    } else {
                        fieldArr = fieldArr;
                    }
                } else {
                    length7 = length7;
                    i8 = i28;
                    constructor = constructor;
                    methodArr8 = methodArr8;
                }
                i28 = i8 + 1;
                length7 = length7;
                map = map;
                constructor = constructor;
                methodArr7 = methodArr8;
                i27 = 4;
            }
            methodArr3 = methodArr7;
        }
        Constructor<?> constructor8 = constructor;
        ArrayList<Field> arrayList3 = new ArrayList(fieldArr.length);
        for (Field field5 : fieldArr) {
            int modifiers2 = field5.getModifiers();
            if ((modifiers2 & 8) == 0) {
                if ((modifiers2 & 16) != 0) {
                    Class<?> type4 = field5.getType();
                    if (Map.class.isAssignableFrom(type4) || Collection.class.isAssignableFrom(type4)) {
                        if ((field5.getModifiers() & 1) != 0) {
                            arrayList3.add(field5);
                        }
                    }
                } else if ((field5.getModifiers() & 1) != 0) {
                    arrayList3.add(field5);
                }
            }
        }
        for (Class<? super Object> superclass2 = cls3.getSuperclass(); superclass2 != null && superclass2 != Object.class; superclass2 = superclass2.getSuperclass()) {
            for (Field field6 : superclass2.getDeclaredFields()) {
                int modifiers3 = field6.getModifiers();
                if ((modifiers3 & 8) == 0) {
                    if ((modifiers3 & 16) != 0) {
                        Class<?> type5 = field6.getType();
                        if (Map.class.isAssignableFrom(type5) || Collection.class.isAssignableFrom(type5)) {
                            if ((modifiers3 & 1) != 0) {
                                arrayList3.add(field6);
                            }
                        }
                    } else if ((modifiers3 & 1) != 0) {
                        arrayList3.add(field6);
                    }
                }
            }
        }
        for (Field field7 : arrayList3) {
            String name2 = field7.getName();
            int size4 = arrayList.size();
            boolean z8 = false;
            for (int i30 = 0; i30 < size4; i30++) {
                if (((FieldInfo) arrayList.get(i30)).name.equals(name2)) {
                    z8 = true;
                }
            }
            if (!z8) {
                JSONField jSONField7 = z3 ? (JSONField) field7.getAnnotation(JSONField.class) : null;
                if (jSONField7 != null) {
                    int iOrdinal4 = jSONField7.ordinal();
                    int iOf4 = SerializerFeature.of(jSONField7.serialzeFeatures());
                    if (jSONField7.name().length() != 0) {
                        name2 = jSONField7.name();
                    }
                    i6 = iOrdinal4;
                    i7 = iOf4;
                } else {
                    i6 = 0;
                    i7 = 0;
                }
                if (propertyNamingStrategy != null) {
                    name2 = propertyNamingStrategy.translate(name2);
                }
                TypeUtils.setAccessible(cls3, field7, i);
                addField(arrayList, new FieldInfo(name2, null, field7, cls3, type, i6, i7, null, jSONField7, z4), z);
                cls3 = cls;
            }
        }
        if (!z) {
            Method[] methodArr9 = methodArr3;
            int length8 = methodArr9.length;
            int i31 = 0;
            while (i31 < length8) {
                Method method6 = methodArr9[i31];
                String name3 = method6.getName();
                if (name3.length() >= 4 && name3.startsWith("get") && Character.isUpperCase(name3.charAt(3)) && method6.getParameterTypes().length == 0) {
                    Class<?> returnType2 = method6.getReturnType();
                    if (Collection.class.isAssignableFrom(returnType2) || Map.class.isAssignableFrom(returnType2)) {
                        JSONField jSONField8 = z3 ? (JSONField) method6.getAnnotation(JSONField.class) : null;
                        if (jSONField8 != null) {
                            strName = jSONField8.name();
                            if (strName.length() <= 0) {
                                strName = Character.toLowerCase(name3.charAt(3)) + name3.substring(4);
                            }
                        } else {
                            strName = Character.toLowerCase(name3.charAt(3)) + name3.substring(4);
                        }
                        i5 = i31;
                        addField(arrayList, new FieldInfo(strName, method6, null, cls, type, 0, 0, jSONField8, null, z4), z);
                        TypeUtils.setAccessible(cls, method6, i);
                    } else {
                        i5 = i31;
                    }
                } else {
                    i5 = i31;
                }
                i31 = i5 + 1;
            }
        }
        int size5 = arrayList.size();
        FieldInfo[] fieldInfoArr7 = new FieldInfo[size5];
        arrayList.toArray(fieldInfoArr7);
        FieldInfo[] fieldInfoArr8 = new FieldInfo[size5];
        System.arraycopy(fieldInfoArr7, 0, fieldInfoArr8, 0, size5);
        Arrays.sort(fieldInfoArr8);
        return new JavaBeanInfo(cls, constructor8, constructor3, method2, fieldInfoArr7, fieldInfoArr8, z2 ? (JSONType) cls.getAnnotation(JSONType.class) : null, strArr2);
    }
}
