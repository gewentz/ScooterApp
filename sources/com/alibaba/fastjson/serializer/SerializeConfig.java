package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONStreamAware;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.util.IdentityHashMap;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.AbstractSequentialList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class SerializeConfig {
    public static final SerializeConfig globalInstance = new SerializeConfig();
    public PropertyNamingStrategy propertyNamingStrategy;
    private final IdentityHashMap<ObjectSerializer> serializers;
    protected String typeKey = JSON.DEFAULT_TYPE_KEY;

    public static final SerializeConfig getGlobalInstance() {
        return globalInstance;
    }

    public ObjectSerializer registerIfNotExists(Class<?> cls) {
        return registerIfNotExists(cls, cls.getModifiers(), false, true, true, true);
    }

    public ObjectSerializer registerIfNotExists(Class<?> cls, int i, boolean z, boolean z2, boolean z3, boolean z4) {
        ObjectSerializer objectSerializer = this.serializers.get(cls);
        if (objectSerializer != null) {
            return objectSerializer;
        }
        JavaBeanSerializer javaBeanSerializer = new JavaBeanSerializer(cls, i, null, z, z2, z3, z4, this.propertyNamingStrategy);
        this.serializers.put(cls, javaBeanSerializer);
        return javaBeanSerializer;
    }

    public SerializeConfig() {
        IdentityHashMap<ObjectSerializer> identityHashMap = new IdentityHashMap<>(1024);
        this.serializers = identityHashMap;
        identityHashMap.put(Boolean.class, BooleanCodec.instance);
        identityHashMap.put(Character.class, MiscCodec.instance);
        identityHashMap.put(Byte.class, IntegerCodec.instance);
        identityHashMap.put(Short.class, IntegerCodec.instance);
        identityHashMap.put(Integer.class, IntegerCodec.instance);
        identityHashMap.put(Long.class, IntegerCodec.instance);
        identityHashMap.put(Float.class, NumberCodec.instance);
        identityHashMap.put(Double.class, NumberCodec.instance);
        identityHashMap.put(Number.class, NumberCodec.instance);
        identityHashMap.put(BigDecimal.class, BigDecimalCodec.instance);
        identityHashMap.put(BigInteger.class, BigDecimalCodec.instance);
        identityHashMap.put(String.class, StringCodec.instance);
        identityHashMap.put(Object[].class, ArrayCodec.instance);
        identityHashMap.put(Class.class, MiscCodec.instance);
        identityHashMap.put(SimpleDateFormat.class, MiscCodec.instance);
        identityHashMap.put(Locale.class, MiscCodec.instance);
        identityHashMap.put(Currency.class, MiscCodec.instance);
        identityHashMap.put(TimeZone.class, MiscCodec.instance);
        identityHashMap.put(UUID.class, MiscCodec.instance);
        identityHashMap.put(URI.class, MiscCodec.instance);
        identityHashMap.put(URL.class, MiscCodec.instance);
        identityHashMap.put(Pattern.class, MiscCodec.instance);
        identityHashMap.put(Charset.class, MiscCodec.instance);
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    public ObjectSerializer get(Class<?> cls) {
        Class<? super Object> superclass;
        boolean z;
        ObjectSerializer javaBeanSerializer;
        DateCodec dateCodec;
        MiscCodec miscCodec;
        MiscCodec miscCodec2;
        MiscCodec miscCodec3;
        JavaBeanSerializer javaBeanSerializer2;
        ObjectSerializer objectSerializer;
        MiscCodec miscCodec4;
        MiscCodec miscCodec5;
        MiscCodec miscCodec6;
        DateCodec dateCodec2;
        CollectionCodec collectionCodec;
        ListSerializer listSerializer;
        CollectionCodec collectionCodec2;
        MapSerializer mapSerializer;
        ObjectSerializer objectSerializer2 = this.serializers.get(cls);
        if (objectSerializer2 != null) {
            return objectSerializer2;
        }
        if (Map.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap = this.serializers;
            mapSerializer = new MapSerializer();
            identityHashMap.put(cls, mapSerializer);
        } else if (AbstractSequentialList.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap2 = this.serializers;
            collectionCodec2 = CollectionCodec.instance;
            identityHashMap2.put(cls, collectionCodec2);
        } else if (List.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap3 = this.serializers;
            listSerializer = new ListSerializer();
            identityHashMap3.put(cls, listSerializer);
        } else if (Collection.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap4 = this.serializers;
            collectionCodec = CollectionCodec.instance;
            identityHashMap4.put(cls, collectionCodec);
        } else if (Date.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap5 = this.serializers;
            dateCodec2 = DateCodec.instance;
            identityHashMap5.put(cls, dateCodec2);
        } else if (JSONAware.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap6 = this.serializers;
            miscCodec6 = MiscCodec.instance;
            identityHashMap6.put(cls, miscCodec6);
        } else if (JSONSerializable.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap7 = this.serializers;
            miscCodec5 = MiscCodec.instance;
            identityHashMap7.put(cls, miscCodec5);
        } else if (JSONStreamAware.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap8 = this.serializers;
            miscCodec4 = MiscCodec.instance;
            identityHashMap8.put(cls, miscCodec4);
        } else if (cls.isEnum() || ((superclass = cls.getSuperclass()) != null && superclass != Object.class && superclass.isEnum())) {
            IdentityHashMap<ObjectSerializer> identityHashMap9 = this.serializers;
            EnumSerializer enumSerializer = new EnumSerializer();
            identityHashMap9.put(cls, enumSerializer);
            objectSerializer = enumSerializer;
        } else if (cls.isArray()) {
            Class<?> componentType = cls.getComponentType();
            ObjectSerializer objectSerializer3 = get(componentType);
            IdentityHashMap<ObjectSerializer> identityHashMap10 = this.serializers;
            ArraySerializer arraySerializer = new ArraySerializer(componentType, objectSerializer3);
            identityHashMap10.put(cls, arraySerializer);
            objectSerializer = arraySerializer;
        } else if (Throwable.class.isAssignableFrom(cls)) {
            javaBeanSerializer2 = new JavaBeanSerializer(cls, this.propertyNamingStrategy);
            javaBeanSerializer2.features |= SerializerFeature.WriteClassName.mask;
            this.serializers.put(cls, javaBeanSerializer2);
        } else if (TimeZone.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap11 = this.serializers;
            miscCodec3 = MiscCodec.instance;
            identityHashMap11.put(cls, miscCodec3);
        } else if (Charset.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap12 = this.serializers;
            miscCodec2 = MiscCodec.instance;
            identityHashMap12.put(cls, miscCodec2);
        } else if (Enumeration.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap13 = this.serializers;
            miscCodec = MiscCodec.instance;
            identityHashMap13.put(cls, miscCodec);
        } else if (Calendar.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap14 = this.serializers;
            dateCodec = DateCodec.instance;
            identityHashMap14.put(cls, dateCodec);
        } else {
            Class<?>[] interfaces = cls.getInterfaces();
            int length = interfaces.length;
            boolean z2 = false;
            int i = 0;
            while (true) {
                if (i < length) {
                    Class<?> cls2 = interfaces[i];
                    z = true;
                    if (cls2.getName().equals("net.sf.cglib.proxy.Factory") || cls2.getName().equals("org.springframework.cglib.proxy.Factory")) {
                        z2 = true;
                    } else {
                        if (cls2.getName().equals("javassist.util.proxy.ProxyObject")) {
                            break;
                        }
                        i++;
                    }
                }
                z = false;
                break;
            }
            if (z2 || z) {
                ObjectSerializer objectSerializer4 = get(cls.getSuperclass());
                this.serializers.put(cls, objectSerializer4);
                return objectSerializer4;
            }
            if (cls.getName().startsWith("android.net.Uri$")) {
                javaBeanSerializer = MiscCodec.instance;
            } else {
                javaBeanSerializer = new JavaBeanSerializer(cls, this.propertyNamingStrategy);
            }
            ObjectSerializer objectSerializer5 = javaBeanSerializer;
            this.serializers.put(cls, objectSerializer5);
            objectSerializer = objectSerializer5;
        }
        if (objectSerializer == null) {
            objectSerializer = dateCodec;
            objectSerializer = miscCodec;
            objectSerializer = miscCodec2;
            objectSerializer = miscCodec3;
            objectSerializer = javaBeanSerializer2;
            objectSerializer = miscCodec4;
            objectSerializer = miscCodec5;
            objectSerializer = miscCodec6;
            objectSerializer = dateCodec2;
            objectSerializer = collectionCodec;
            objectSerializer = listSerializer;
            objectSerializer = collectionCodec2;
            objectSerializer = mapSerializer;
            return this.serializers.get(cls);
        }
        objectSerializer = dateCodec;
        objectSerializer = miscCodec;
        objectSerializer = miscCodec2;
        objectSerializer = miscCodec3;
        objectSerializer = javaBeanSerializer2;
        objectSerializer = miscCodec4;
        objectSerializer = miscCodec5;
        objectSerializer = miscCodec6;
        objectSerializer = dateCodec2;
        objectSerializer = collectionCodec;
        objectSerializer = listSerializer;
        objectSerializer = collectionCodec2;
        objectSerializer = mapSerializer;
        return objectSerializer;
    }

    public boolean put(Type type, ObjectSerializer objectSerializer) {
        return this.serializers.put(type, objectSerializer);
    }

    public String getTypeKey() {
        return this.typeKey;
    }

    public void setTypeKey(String str) {
        this.typeKey = str;
    }
}
