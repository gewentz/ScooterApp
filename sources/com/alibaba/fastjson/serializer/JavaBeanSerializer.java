package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class JavaBeanSerializer implements ObjectSerializer {
    protected int features;
    private final FieldSerializer[] getters;
    private final FieldSerializer[] sortedGetters;
    protected final String typeKey;
    protected final String typeName;
    private static final char[] true_chars = {'t', 'r', 'u', 'e'};
    private static final char[] false_chars = {'f', 'a', 'l', 's', 'e'};

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public JavaBeanSerializer(Class<?> cls) {
        this(cls, (PropertyNamingStrategy) null);
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public JavaBeanSerializer(Class<?> cls, PropertyNamingStrategy propertyNamingStrategy) {
        this(cls, cls.getModifiers(), null, false, true, true, true, propertyNamingStrategy);
    }

    public JavaBeanSerializer(Class<?> cls, String... strArr) {
        this(cls, cls.getModifiers(), map(strArr), false, true, true, true, null);
    }

    private static Map<String, String> map(String... strArr) {
        HashMap map = new HashMap();
        for (String str : strArr) {
            map.put(str, str);
        }
        return map;
    }

    public JavaBeanSerializer(Class<?> cls, int i, Map<String, String> map, boolean z, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        PropertyNamingStrategy propertyNamingStrategy2;
        String strTypeName;
        String strTypeKey;
        PropertyNamingStrategy propertyNamingStrategyNaming;
        this.features = 0;
        JSONType jSONType = z2 ? (JSONType) cls.getAnnotation(JSONType.class) : null;
        if (jSONType != null) {
            this.features = SerializerFeature.of(jSONType.serialzeFeatures());
            strTypeName = jSONType.typeName();
            if (strTypeName.length() == 0) {
                strTypeName = null;
                strTypeKey = null;
            } else {
                strTypeKey = null;
                for (Class<? super Object> superclass = cls.getSuperclass(); superclass != null && superclass != Object.class; superclass = superclass.getSuperclass()) {
                    JSONType jSONType2 = (JSONType) superclass.getAnnotation(JSONType.class);
                    if (jSONType2 == null) {
                        break;
                    }
                    strTypeKey = jSONType2.typeKey();
                    if (strTypeKey.length() != 0) {
                        break;
                    }
                }
                for (Class<?> cls2 : cls.getInterfaces()) {
                    JSONType jSONType3 = (JSONType) cls2.getAnnotation(JSONType.class);
                    if (jSONType3 != null) {
                        strTypeKey = jSONType3.typeKey();
                        if (strTypeKey.length() != 0) {
                            break;
                        }
                    }
                }
                if (strTypeKey != null && strTypeKey.length() == 0) {
                    strTypeKey = null;
                }
            }
            propertyNamingStrategy2 = (propertyNamingStrategy != null || (propertyNamingStrategyNaming = jSONType.naming()) == PropertyNamingStrategy.CamelCase) ? propertyNamingStrategy : propertyNamingStrategyNaming;
        } else {
            propertyNamingStrategy2 = propertyNamingStrategy;
            strTypeName = null;
            strTypeKey = null;
        }
        this.typeName = strTypeName;
        this.typeKey = strTypeKey;
        List<FieldInfo> listComputeGetters = TypeUtils.computeGetters(cls, i, z, jSONType, map, false, z3, z4, propertyNamingStrategy2);
        ArrayList arrayList = new ArrayList();
        Iterator<FieldInfo> it = listComputeGetters.iterator();
        while (it.hasNext()) {
            arrayList.add(new FieldSerializer(it.next()));
        }
        FieldSerializer[] fieldSerializerArr = (FieldSerializer[]) arrayList.toArray(new FieldSerializer[arrayList.size()]);
        this.getters = fieldSerializerArr;
        String[] strArrOrders = jSONType != null ? jSONType.orders() : null;
        if (strArrOrders != null && strArrOrders.length != 0) {
            List<FieldInfo> listComputeGetters2 = TypeUtils.computeGetters(cls, i, z, jSONType, map, true, z3, z4, propertyNamingStrategy2);
            ArrayList arrayList2 = new ArrayList();
            Iterator<FieldInfo> it2 = listComputeGetters2.iterator();
            while (it2.hasNext()) {
                arrayList2.add(new FieldSerializer(it2.next()));
            }
            this.sortedGetters = (FieldSerializer[]) arrayList2.toArray(new FieldSerializer[arrayList2.size()]);
            return;
        }
        FieldSerializer[] fieldSerializerArr2 = new FieldSerializer[fieldSerializerArr.length];
        System.arraycopy(fieldSerializerArr, 0, fieldSerializerArr2, 0, fieldSerializerArr.length);
        Arrays.sort(fieldSerializerArr2);
        if (Arrays.equals(fieldSerializerArr2, fieldSerializerArr)) {
            this.sortedGetters = fieldSerializerArr;
        } else {
            this.sortedGetters = fieldSerializerArr2;
        }
    }

    /* JADX WARN: Code duplicated, block: B:121:0x01c2  */
    /* JADX WARN: Code duplicated, block: B:142:0x022d A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:143:0x022f  */
    /* JADX WARN: Code duplicated, block: B:146:0x0235 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:148:0x023e A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:150:0x0242 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:151:0x0247 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:153:0x024b A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:154:0x0250  */
    /* JADX WARN: Code duplicated, block: B:159:0x025e A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:162:0x0272 A[LOOP:3: B:157:0x0258->B:162:0x0272, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:164:0x027a  */
    /* JADX WARN: Code duplicated, block: B:167:0x0281  */
    /* JADX WARN: Code duplicated, block: B:168:0x0283 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:169:0x0285  */
    /* JADX WARN: Code duplicated, block: B:170:0x0287 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:181:0x02a6  */
    /* JADX WARN: Code duplicated, block: B:185:0x02b5 A[Catch: all -> 0x0664, Exception -> 0x0667, LOOP:4: B:183:0x02af->B:185:0x02b5, LOOP_END, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:187:0x02c9  */
    /* JADX WARN: Code duplicated, block: B:190:0x02ce  */
    /* JADX WARN: Code duplicated, block: B:191:0x02d0 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:194:0x02d6 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:196:0x02dd A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:198:0x02e1 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:199:0x02e6 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:201:0x02ea A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:205:0x02fb A[Catch: all -> 0x0664, Exception -> 0x0667, TRY_LEAVE, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:208:0x0311  */
    /* JADX WARN: Code duplicated, block: B:211:0x0317 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:275:0x03fd  */
    /* JADX WARN: Code duplicated, block: B:278:0x0403 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:304:0x0445  */
    /* JADX WARN: Code duplicated, block: B:306:0x0449 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:308:0x0452 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:310:0x0456 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:311:0x045a A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:314:0x0471 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:316:0x0476 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:317:0x0478 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:321:0x0484 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:322:0x0486 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:323:0x0488 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:326:0x048f  */
    /* JADX WARN: Code duplicated, block: B:327:0x0491 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:328:0x0493 A[Catch: all -> 0x05f6, Exception -> 0x05fa, TRY_ENTER, TRY_LEAVE, TryCatch #5 {Exception -> 0x05fa, all -> 0x05f6, blocks: (B:98:0x016e, B:106:0x0193, B:124:0x01cc, B:328:0x0493, B:338:0x04ca, B:139:0x021c), top: B:461:0x016e }] */
    /* JADX WARN: Code duplicated, block: B:330:0x049e A[Catch: all -> 0x0664, Exception -> 0x0667, TRY_ENTER, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:332:0x04a2 A[Catch: all -> 0x0664, Exception -> 0x0667, TryCatch #6 {Exception -> 0x0667, all -> 0x0664, blocks: (B:39:0x008b, B:41:0x0093, B:43:0x0097, B:44:0x009b, B:45:0x009f, B:47:0x00aa, B:49:0x00b3, B:50:0x00b9, B:52:0x00c4, B:55:0x00cf, B:57:0x00d8, B:59:0x00dc, B:72:0x0107, B:74:0x010b, B:75:0x0111, B:77:0x0117, B:81:0x0127, B:83:0x0130, B:87:0x013c, B:91:0x0148, B:95:0x0156, B:96:0x016b, B:101:0x018a, B:103:0x018e, B:108:0x0197, B:112:0x01a0, B:113:0x01a4, B:115:0x01aa, B:127:0x01d2, B:129:0x01d8, B:144:0x0231, B:146:0x0235, B:156:0x0254, B:157:0x0258, B:159:0x025e, B:171:0x0289, B:173:0x028d, B:182:0x02a8, B:183:0x02af, B:185:0x02b5, B:192:0x02d2, B:194:0x02d6, B:196:0x02dd, B:198:0x02e1, B:199:0x02e6, B:201:0x02ea, B:202:0x02ef, B:203:0x02f5, B:205:0x02fb, B:213:0x031b, B:215:0x032f, B:217:0x033d, B:219:0x0341, B:222:0x034b, B:224:0x034f, B:280:0x0407, B:282:0x040d, B:284:0x0411, B:286:0x0415, B:288:0x0419, B:290:0x041d, B:297:0x042f, B:299:0x0433, B:301:0x0437, B:306:0x0449, B:308:0x0452, B:310:0x0456, B:311:0x045a, B:312:0x045e, B:314:0x0471, B:317:0x0478, B:318:0x047c, B:323:0x0488, B:324:0x048b, B:330:0x049e, B:332:0x04a2, B:334:0x04a7, B:292:0x0421, B:294:0x0425, B:226:0x0355, B:229:0x035f, B:231:0x036d, B:233:0x0371, B:236:0x037a, B:238:0x037e, B:241:0x0388, B:243:0x0390, B:245:0x039e, B:247:0x03a2, B:250:0x03ab, B:252:0x03af, B:254:0x03b5, B:255:0x03ba, B:257:0x03c2, B:259:0x03d0, B:261:0x03d4, B:264:0x03de, B:266:0x03e2, B:268:0x03e8, B:270:0x03ef, B:272:0x03f3, B:175:0x0294, B:177:0x0298, B:178:0x029d, B:180:0x02a1, B:148:0x023e, B:150:0x0242, B:151:0x0247, B:153:0x024b, B:131:0x01ea, B:133:0x01ee, B:134:0x0200, B:136:0x0204, B:137:0x0215, B:61:0x00e2, B:63:0x00e8, B:67:0x00f1, B:69:0x00f8, B:70:0x0100, B:66:0x00ed), top: B:459:0x008b }] */
    /* JADX WARN: Code duplicated, block: B:333:0x04a6  */
    /* JADX WARN: Code duplicated, block: B:337:0x04c9  */
    /* JADX WARN: Code duplicated, block: B:341:0x04d6 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:342:0x04dc  */
    /* JADX WARN: Code duplicated, block: B:344:0x04e0 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:377:0x055b A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:378:0x055d  */
    /* JADX WARN: Code duplicated, block: B:380:0x0561 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:382:0x0568 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:384:0x0571 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:388:0x057e A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:389:0x0583 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:391:0x0587 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:392:0x058c A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:393:0x0593 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:395:0x0597 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:396:0x0599 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:398:0x05a2 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:400:0x05b1  */
    /* JADX WARN: Code duplicated, block: B:401:0x05b3  */
    /* JADX WARN: Code duplicated, block: B:403:0x05b6 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:404:0x05bb A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:405:0x05c0 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:406:0x05cb A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:407:0x05d0 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:408:0x05d5 A[Catch: all -> 0x065c, Exception -> 0x0660, TryCatch #7 {Exception -> 0x0660, all -> 0x065c, blocks: (B:410:0x05db, B:340:0x04ce, B:345:0x04e2, B:349:0x04ec, B:354:0x04f8, B:356:0x04fe, B:358:0x0502, B:359:0x0504, B:361:0x050c, B:363:0x0510, B:364:0x0514, B:367:0x0523, B:368:0x052d, B:369:0x0532, B:371:0x0536, B:372:0x053f, B:375:0x0545, B:376:0x0550, B:380:0x0561, B:382:0x0568, B:384:0x0571, B:387:0x0579, B:388:0x057e, B:389:0x0583, B:391:0x0587, B:392:0x058c, B:393:0x0593, B:396:0x0599, B:398:0x05a2, B:403:0x05b6, B:404:0x05bb, B:405:0x05c0, B:406:0x05cb, B:407:0x05d0, B:408:0x05d5, B:341:0x04d6, B:415:0x05fe, B:420:0x060d, B:421:0x0613, B:423:0x0619, B:424:0x0624, B:426:0x0627, B:428:0x0630, B:429:0x0636, B:431:0x0641, B:433:0x0645, B:434:0x0649, B:436:0x064f), top: B:457:0x05db }] */
    /* JADX WARN: Code duplicated, block: B:455:0x066d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:467:0x026e A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:468:0x0275 A[SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r17v0 */
    /* JADX WARN: Type inference failed for: r17v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r17v3 */
    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws Throwable {
        FieldSerializer[] fieldSerializerArr;
        SerialContext serialContext;
        String str;
        char cWriteBefore;
        int i;
        String str2;
        boolean z;
        Object propertyValue;
        Object objValueOf;
        boolean z2;
        int i2;
        boolean z3;
        boolean z4;
        boolean z5;
        char c;
        boolean z6;
        String strProcess;
        boolean z7;
        Object obj3;
        boolean z8;
        Object obj4;
        List<PropertyFilter> list;
        String string;
        boolean z9;
        String str3;
        boolean z10;
        char[] cArr;
        int length;
        int i3;
        int i4;
        int i5;
        int i6;
        Iterator<ValueFilter> it;
        Object objProcess;
        Object objValueOf2;
        Iterator<NameFilter> it2;
        Object obj5;
        Iterator<PropertyFilter> it3;
        Object objValueOf3;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            serializeWriter.writeNull();
            return;
        }
        if ((jSONSerializer.context == null || (jSONSerializer.context.features & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) && jSONSerializer.references != null && jSONSerializer.references.containsKey(obj)) {
            jSONSerializer.writeReference(obj);
            return;
        }
        if ((serializeWriter.features & SerializerFeature.SortField.mask) != 0) {
            fieldSerializerArr = this.sortedGetters;
        } else {
            fieldSerializerArr = this.getters;
        }
        SerialContext serialContext2 = jSONSerializer.context;
        if ((serializeWriter.features & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) {
            jSONSerializer.context = new SerialContext(serialContext2, obj, obj2, this.features);
            if (jSONSerializer.references == null) {
                jSONSerializer.references = new IdentityHashMap<>();
            }
            jSONSerializer.references.put(obj, jSONSerializer.context);
        }
        boolean z11 = ((this.features & SerializerFeature.BeanToArray.mask) == 0 && (serializeWriter.features & SerializerFeature.BeanToArray.mask) == 0) ? false : true;
        char c2 = z11 ? '[' : '{';
        char c3 = z11 ? ']' : '}';
        try {
            int i7 = serializeWriter.count + 1;
            if (i7 > serializeWriter.buf.length) {
                if (serializeWriter.writer == null) {
                    serializeWriter.expandCapacity(i7);
                } else {
                    serializeWriter.flush();
                    i7 = 1;
                }
            }
            serializeWriter.buf[serializeWriter.count] = c2;
            serializeWriter.count = i7;
            if (fieldSerializerArr.length > 0 && (serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                jSONSerializer.incrementIndent();
                jSONSerializer.println();
            }
            if (((this.features & SerializerFeature.WriteClassName.mask) == 0 && ((serializeWriter.features & SerializerFeature.WriteClassName.mask) == 0 || (type == null && (serializeWriter.features & SerializerFeature.NotWriteRootClassName.mask) != 0 && (jSONSerializer.context == null || jSONSerializer.context.parent == null)))) || obj.getClass() == type) {
                cWriteBefore = 0;
            } else {
                String str4 = this.typeKey;
                if (str4 == null) {
                    str4 = jSONSerializer.config.typeKey;
                }
                serializeWriter.writeFieldName(str4, false);
                String name = this.typeName;
                if (name == null) {
                    name = obj.getClass().getName();
                }
                jSONSerializer.write(name);
                cWriteBefore = ',';
            }
            if (jSONSerializer.beforeFilters != null) {
                Iterator<BeforeFilter> it4 = jSONSerializer.beforeFilters.iterator();
                while (it4.hasNext()) {
                    cWriteBefore = it4.next().writeBefore(jSONSerializer, obj, cWriteBefore);
                }
            }
            boolean z12 = cWriteBefore == ',';
            boolean z13 = (serializeWriter.features & SerializerFeature.QuoteFieldNames.mask) != 0 && (serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) == 0;
            boolean z14 = (serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0;
            boolean z15 = (SerializerFeature.NotWriteDefaultValue.mask & serializeWriter.features) != 0;
            List<PropertyFilter> list2 = jSONSerializer.propertyFilters;
            ?? r17 = 0;
            List<NameFilter> list3 = jSONSerializer.nameFilters;
            List<ValueFilter> list4 = jSONSerializer.valueFilters;
            boolean z16 = z12;
            List<PropertyPreFilter> list5 = jSONSerializer.propertyPreFilters;
            boolean z17 = z16;
            boolean z18 = z11;
            int i8 = 0;
            while (i8 < fieldSerializerArr.length) {
                try {
                    FieldSerializer fieldSerializer = fieldSerializerArr[i8];
                    int i9 = i8;
                    FieldInfo fieldInfo = fieldSerializer.fieldInfo;
                    List<NameFilter> list6 = list3;
                    Class<?> cls = fieldInfo.fieldClass;
                    List<ValueFilter> list7 = list4;
                    String str5 = fieldInfo.name;
                    boolean z19 = z13;
                    if (((serializeWriter.features & SerializerFeature.SkipTransientField.mask) == 0 || fieldInfo.field == null || !fieldInfo.fieldTransient) && ((str2 = this.typeKey) == null || !str2.equals(str5))) {
                        if (list5 == null) {
                            z = true;
                            break;
                        }
                        Iterator<PropertyPreFilter> it5 = list5.iterator();
                        while (true) {
                            if (!it5.hasNext()) {
                                z = true;
                                break;
                            }
                            Iterator<PropertyPreFilter> it6 = it5;
                            if (!it5.next().apply(jSONSerializer, obj, str5)) {
                                z = false;
                                break;
                            }
                            it5 = it6;
                        }
                        if (z) {
                            long j = 0;
                            if (fieldInfo.fieldAccess) {
                                objValueOf = null;
                                if (cls == Integer.TYPE) {
                                    z3 = false;
                                    z4 = false;
                                    i2 = fieldInfo.field.getInt(obj);
                                } else {
                                    if (cls == Long.TYPE) {
                                        j = fieldInfo.field.getLong(obj);
                                        objValueOf = null;
                                        z2 = true;
                                        i2 = 0;
                                        z3 = false;
                                        z4 = false;
                                    } else if (cls == Boolean.TYPE) {
                                        i2 = 0;
                                        z4 = false;
                                        z3 = fieldInfo.field.getBoolean(obj);
                                    } else {
                                        propertyValue = fieldInfo.field.get(obj);
                                    }
                                    if (list2 != null) {
                                        if (z2) {
                                            z5 = z2;
                                            if (cls == Integer.TYPE) {
                                                objValueOf3 = Integer.valueOf(i2);
                                            } else if (cls == Long.TYPE) {
                                                objValueOf3 = Long.valueOf(j);
                                            } else {
                                                if (cls == Boolean.TYPE) {
                                                    objValueOf3 = Boolean.valueOf(z3);
                                                }
                                                it3 = list2.iterator();
                                                while (true) {
                                                    if (it3.hasNext()) {
                                                        c = c3;
                                                        if (!it3.next().apply(obj, str5, obj5)) {
                                                            objValueOf = obj5;
                                                            z6 = false;
                                                            break;
                                                        }
                                                        c3 = c;
                                                    } else {
                                                        c = c3;
                                                        objValueOf = obj5;
                                                    }
                                                }
                                                if (!z6) {
                                                    if (list6 != null) {
                                                        if (z5 || z4) {
                                                            objValueOf2 = objValueOf;
                                                        } else {
                                                            if (cls == Integer.TYPE) {
                                                                objValueOf2 = Integer.valueOf(i2);
                                                            } else if (cls == Long.TYPE) {
                                                                objValueOf2 = Long.valueOf(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                objValueOf2 = Boolean.valueOf(z3);
                                                            } else {
                                                                objValueOf2 = objValueOf;
                                                            }
                                                            z4 = true;
                                                        }
                                                        it2 = list6.iterator();
                                                        strProcess = str5;
                                                        while (it2.hasNext()) {
                                                            strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                            z15 = z15;
                                                        }
                                                        objValueOf = objValueOf2;
                                                    } else {
                                                        strProcess = str5;
                                                    }
                                                    z7 = z15;
                                                    if (list7 != null) {
                                                        if (z5 && !z4) {
                                                            if (cls == Integer.TYPE) {
                                                                objValueOf = Integer.valueOf(i2);
                                                            } else if (cls == Long.TYPE) {
                                                                objValueOf = Long.valueOf(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                objValueOf = Boolean.valueOf(z3);
                                                            }
                                                            z4 = true;
                                                        }
                                                        it = list7.iterator();
                                                        objProcess = objValueOf;
                                                        while (it.hasNext()) {
                                                            Iterator<ValueFilter> it7 = it;
                                                            objProcess = it.next().process(obj, str5, objProcess);
                                                            it = it7;
                                                        }
                                                        obj3 = objValueOf;
                                                        objValueOf = objProcess;
                                                    } else {
                                                        obj3 = objValueOf;
                                                    }
                                                    if (z4 || objValueOf != null) {
                                                        z8 = z14;
                                                    } else {
                                                        z8 = z14;
                                                        int i10 = fieldInfo.serialzeFeatures | this.features | serializeWriter.features;
                                                        if (cls == Boolean.class) {
                                                            int i11 = SerializerFeature.WriteNullBooleanAsFalse.mask;
                                                            int i12 = i11 | SerializerFeature.WriteMapNullValue.mask;
                                                            if (z18 || (i10 & i12) != 0 || (serializeWriter.features & i12) != 0) {
                                                                if ((i10 & i11) != 0 || (serializeWriter.features & i11) != 0) {
                                                                    objValueOf = Boolean.valueOf((boolean) r17);
                                                                }
                                                            }
                                                            list = list2;
                                                            serialContext2 = serialContext2;
                                                        } else if (cls == String.class) {
                                                            int i13 = SerializerFeature.WriteNullStringAsEmpty.mask;
                                                            int i14 = i13 | SerializerFeature.WriteMapNullValue.mask;
                                                            if (!z18 && (i10 & i14) == 0 && (serializeWriter.features & i14) == 0) {
                                                                list = list2;
                                                                serialContext2 = serialContext2;
                                                            } else if ((i10 & i13) != 0 || (serializeWriter.features & i13) != 0) {
                                                                objValueOf = "";
                                                            }
                                                        } else if (Number.class.isAssignableFrom(cls)) {
                                                            int i15 = SerializerFeature.WriteNullNumberAsZero.mask;
                                                            int i16 = i15 | SerializerFeature.WriteMapNullValue.mask;
                                                            if (!z18 && (i10 & i16) == 0 && (serializeWriter.features & i16) == 0) {
                                                                list = list2;
                                                                serialContext2 = serialContext2;
                                                            } else if ((i10 & i15) != 0 || (serializeWriter.features & i15) != 0) {
                                                                objValueOf = Integer.valueOf((int) r17);
                                                            }
                                                        } else if (Collection.class.isAssignableFrom(cls)) {
                                                            int i17 = SerializerFeature.WriteNullListAsEmpty.mask;
                                                            int i18 = i17 | SerializerFeature.WriteMapNullValue.mask;
                                                            if (!z18 && (i10 & i18) == 0 && (serializeWriter.features & i18) == 0) {
                                                                list = list2;
                                                                serialContext2 = serialContext2;
                                                            } else if ((i10 & i17) != 0 || (serializeWriter.features & i17) != 0) {
                                                                objValueOf = Collections.EMPTY_LIST;
                                                            }
                                                        } else if (!z18 && !fieldSerializer.writeNull && !serializeWriter.isEnabled(SerializerFeature.WriteMapNullValue)) {
                                                            list = list2;
                                                            serialContext2 = serialContext2;
                                                        }
                                                    }
                                                    obj4 = objValueOf;
                                                    if (z4 || obj4 == null || !z7) {
                                                        list = list2;
                                                    } else {
                                                        list = list2;
                                                        if (((cls == Byte.TYPE || cls == Short.TYPE || cls == Integer.TYPE || cls == Long.TYPE || cls == Float.TYPE || cls == Double.TYPE) && (obj4 instanceof Number) && ((Number) obj4).byteValue() == 0) || (cls == Boolean.TYPE && (obj4 instanceof Boolean) && !((Boolean) obj4).booleanValue())) {
                                                            serialContext2 = serialContext2;
                                                        }
                                                    }
                                                    if (z17) {
                                                        i6 = serializeWriter.count + 1;
                                                        if (i6 > serializeWriter.buf.length) {
                                                            if (serializeWriter.writer == null) {
                                                                serializeWriter.expandCapacity(i6);
                                                            } else {
                                                                serializeWriter.flush();
                                                                i6 = 1;
                                                            }
                                                        }
                                                        serializeWriter.buf[serializeWriter.count] = ',';
                                                        serializeWriter.count = i6;
                                                        if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                            jSONSerializer.println();
                                                        }
                                                    }
                                                    if (strProcess != str5) {
                                                        if (!z18) {
                                                            serializeWriter.writeFieldName(strProcess, true);
                                                        }
                                                        jSONSerializer.write(obj4);
                                                    } else {
                                                        if (obj3 != obj4) {
                                                            if (!z18) {
                                                                fieldSerializer.writePrefix(jSONSerializer);
                                                            }
                                                            jSONSerializer.write(obj4);
                                                        } else {
                                                            if (z18) {
                                                                if (z19) {
                                                                    cArr = fieldSerializer.name_chars;
                                                                    length = cArr.length;
                                                                    i3 = serializeWriter.count + length;
                                                                    if (i3 <= serializeWriter.buf.length) {
                                                                        i4 = 0;
                                                                    } else if (serializeWriter.writer == null) {
                                                                        serializeWriter.expandCapacity(i3);
                                                                        i4 = 0;
                                                                    } else {
                                                                        i5 = 0;
                                                                        do {
                                                                            int length2 = serializeWriter.buf.length - serializeWriter.count;
                                                                            System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length2);
                                                                            serializeWriter.count = serializeWriter.buf.length;
                                                                            serializeWriter.flush();
                                                                            length -= length2;
                                                                            i5 += length2;
                                                                        } while (length > serializeWriter.buf.length);
                                                                        i4 = i5;
                                                                        i3 = length;
                                                                    }
                                                                    System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                                    serializeWriter.count = i3;
                                                                } else {
                                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                                }
                                                            }
                                                            if (z5 || z4) {
                                                                if (!z18) {
                                                                    if (cls == String.class) {
                                                                        int i19 = fieldSerializer.features | this.features;
                                                                        if (obj4 == null) {
                                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0 || (i19 & SerializerFeature.WriteNullStringAsEmpty.mask) != 0) {
                                                                                serializeWriter.writeString("");
                                                                            } else {
                                                                                serializeWriter.writeNull();
                                                                            }
                                                                        } else {
                                                                            str3 = (String) obj4;
                                                                            if (z8) {
                                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                                            } else {
                                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                            }
                                                                        }
                                                                    } else if (!fieldInfo.isEnum) {
                                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                                    } else if (obj4 != null) {
                                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                            string = ((Enum) obj4).toString();
                                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                                z9 = true;
                                                                            } else {
                                                                                z9 = false;
                                                                            }
                                                                            if (z9) {
                                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                                            } else {
                                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                            }
                                                                        } else {
                                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                        }
                                                                    } else {
                                                                        serializeWriter.writeNull();
                                                                    }
                                                                } else {
                                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                                }
                                                            } else if (cls == Integer.TYPE) {
                                                                int i20 = i2;
                                                                if (i20 == Integer.MIN_VALUE) {
                                                                    serializeWriter.write("-2147483648");
                                                                } else {
                                                                    int i21 = 0;
                                                                    while ((i20 < 0 ? -i20 : i20) > SerializeWriter.sizeTable[i21]) {
                                                                        i21++;
                                                                    }
                                                                    int i22 = i21 + 1;
                                                                    if (i20 < 0) {
                                                                        i22 = i21 + 2;
                                                                    }
                                                                    int i23 = serializeWriter.count + i22;
                                                                    if (i23 <= serializeWriter.buf.length) {
                                                                        z10 = false;
                                                                    } else if (serializeWriter.writer == null) {
                                                                        serializeWriter.expandCapacity(i23);
                                                                        z10 = false;
                                                                    } else {
                                                                        char[] cArr2 = new char[i22];
                                                                        SerializeWriter.getChars(i20, i22, cArr2);
                                                                        serializeWriter.write(cArr2, 0, i22);
                                                                        z10 = true;
                                                                    }
                                                                    if (!z10) {
                                                                        SerializeWriter.getChars(i20, i23, serializeWriter.buf);
                                                                        serializeWriter.count = i23;
                                                                    }
                                                                }
                                                            } else if (cls == Long.TYPE) {
                                                                jSONSerializer.out.writeLong(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                if (z3) {
                                                                    SerializeWriter serializeWriter2 = jSONSerializer.out;
                                                                    char[] cArr3 = true_chars;
                                                                    serializeWriter2.write(cArr3, 0, cArr3.length);
                                                                } else {
                                                                    SerializeWriter serializeWriter3 = jSONSerializer.out;
                                                                    char[] cArr4 = false_chars;
                                                                    serializeWriter3.write(cArr4, 0, cArr4.length);
                                                                }
                                                            }
                                                        }
                                                        z17 = true;
                                                    }
                                                    serialContext2 = serialContext2;
                                                    z17 = true;
                                                }
                                            }
                                            obj5 = objValueOf3;
                                            z4 = true;
                                            it3 = list2.iterator();
                                            while (true) {
                                                if (it3.hasNext()) {
                                                    c = c3;
                                                    if (!it3.next().apply(obj, str5, obj5)) {
                                                        objValueOf = obj5;
                                                        z6 = false;
                                                        break;
                                                    }
                                                    c3 = c;
                                                } else {
                                                    c = c3;
                                                    objValueOf = obj5;
                                                }
                                            }
                                            if (!z6) {
                                                if (list6 != null) {
                                                    if (z5) {
                                                        objValueOf2 = objValueOf;
                                                    } else {
                                                        objValueOf2 = objValueOf;
                                                    }
                                                    it2 = list6.iterator();
                                                    strProcess = str5;
                                                    while (it2.hasNext()) {
                                                        strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                        z15 = z15;
                                                    }
                                                    objValueOf = objValueOf2;
                                                } else {
                                                    strProcess = str5;
                                                }
                                                z7 = z15;
                                                if (list7 != null) {
                                                    if (z5) {
                                                        if (cls == Integer.TYPE) {
                                                            objValueOf = Integer.valueOf(i2);
                                                        } else if (cls == Long.TYPE) {
                                                            objValueOf = Long.valueOf(j);
                                                        } else if (cls == Boolean.TYPE) {
                                                            objValueOf = Boolean.valueOf(z3);
                                                        }
                                                        z4 = true;
                                                    }
                                                    it = list7.iterator();
                                                    objProcess = objValueOf;
                                                    while (it.hasNext()) {
                                                        Iterator<ValueFilter> it8 = it;
                                                        objProcess = it.next().process(obj, str5, objProcess);
                                                        it = it8;
                                                    }
                                                    obj3 = objValueOf;
                                                    objValueOf = objProcess;
                                                } else {
                                                    obj3 = objValueOf;
                                                }
                                                if (z4) {
                                                    z8 = z14;
                                                } else {
                                                    z8 = z14;
                                                }
                                                obj4 = objValueOf;
                                                if (z4) {
                                                    list = list2;
                                                } else {
                                                    list = list2;
                                                }
                                                if (z17) {
                                                    i6 = serializeWriter.count + 1;
                                                    if (i6 > serializeWriter.buf.length) {
                                                        if (serializeWriter.writer == null) {
                                                            serializeWriter.expandCapacity(i6);
                                                        } else {
                                                            serializeWriter.flush();
                                                            i6 = 1;
                                                        }
                                                    }
                                                    serializeWriter.buf[serializeWriter.count] = ',';
                                                    serializeWriter.count = i6;
                                                    if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                        jSONSerializer.println();
                                                    }
                                                }
                                                if (strProcess != str5) {
                                                    if (!z18) {
                                                        serializeWriter.writeFieldName(strProcess, true);
                                                    }
                                                    jSONSerializer.write(obj4);
                                                } else {
                                                    if (obj3 != obj4) {
                                                        if (!z18) {
                                                            fieldSerializer.writePrefix(jSONSerializer);
                                                        }
                                                        jSONSerializer.write(obj4);
                                                    } else {
                                                        if (z18) {
                                                            if (z19) {
                                                                cArr = fieldSerializer.name_chars;
                                                                length = cArr.length;
                                                                i3 = serializeWriter.count + length;
                                                                if (i3 <= serializeWriter.buf.length) {
                                                                    i4 = 0;
                                                                } else if (serializeWriter.writer == null) {
                                                                    serializeWriter.expandCapacity(i3);
                                                                    i4 = 0;
                                                                } else {
                                                                    i5 = 0;
                                                                    do {
                                                                        int length3 = serializeWriter.buf.length - serializeWriter.count;
                                                                        System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length3);
                                                                        serializeWriter.count = serializeWriter.buf.length;
                                                                        serializeWriter.flush();
                                                                        length -= length3;
                                                                        i5 += length3;
                                                                    } while (length > serializeWriter.buf.length);
                                                                    i4 = i5;
                                                                    i3 = length;
                                                                }
                                                                System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                                serializeWriter.count = i3;
                                                            } else {
                                                                fieldSerializer.writePrefix(jSONSerializer);
                                                            }
                                                        }
                                                        if (z5) {
                                                            if (!z18) {
                                                                if (cls == String.class) {
                                                                    int i110 = fieldSerializer.features | this.features;
                                                                    if (obj4 == null) {
                                                                        if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                            serializeWriter.writeString("");
                                                                        } else {
                                                                            serializeWriter.writeString("");
                                                                        }
                                                                    } else {
                                                                        str3 = (String) obj4;
                                                                        if (z8) {
                                                                            serializeWriter.writeStringWithSingleQuote(str3);
                                                                        } else {
                                                                            serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                        }
                                                                    }
                                                                } else if (!fieldInfo.isEnum) {
                                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                                } else if (obj4 != null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                        string = ((Enum) obj4).toString();
                                                                        if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                            z9 = true;
                                                                        } else {
                                                                            z9 = false;
                                                                        }
                                                                        if (z9) {
                                                                            serializeWriter.writeStringWithSingleQuote(string);
                                                                        } else {
                                                                            serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                        }
                                                                    } else {
                                                                        serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeNull();
                                                                }
                                                            } else {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            }
                                                        } else if (!z18) {
                                                            if (cls == String.class) {
                                                                int i111 = fieldSerializer.features | this.features;
                                                                if (obj4 == null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                        serializeWriter.writeString("");
                                                                    } else {
                                                                        serializeWriter.writeString("");
                                                                    }
                                                                } else {
                                                                    str3 = (String) obj4;
                                                                    if (z8) {
                                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                    }
                                                                }
                                                            } else if (!fieldInfo.isEnum) {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            } else if (obj4 != null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    string = ((Enum) obj4).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        z9 = true;
                                                                    } else {
                                                                        z9 = false;
                                                                    }
                                                                    if (z9) {
                                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                }
                                                            } else {
                                                                serializeWriter.writeNull();
                                                            }
                                                        } else {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        }
                                                    }
                                                    z17 = true;
                                                }
                                                serialContext2 = serialContext2;
                                                z17 = true;
                                            }
                                        } else {
                                            z5 = z2;
                                        }
                                        obj5 = objValueOf;
                                        it3 = list2.iterator();
                                        while (true) {
                                            if (it3.hasNext()) {
                                                c = c3;
                                                if (!it3.next().apply(obj, str5, obj5)) {
                                                    objValueOf = obj5;
                                                    z6 = false;
                                                    break;
                                                }
                                                c3 = c;
                                            } else {
                                                c = c3;
                                                objValueOf = obj5;
                                            }
                                        }
                                        if (!z6) {
                                            if (list6 != null) {
                                                if (z5) {
                                                    objValueOf2 = objValueOf;
                                                } else {
                                                    objValueOf2 = objValueOf;
                                                }
                                                it2 = list6.iterator();
                                                strProcess = str5;
                                                while (it2.hasNext()) {
                                                    strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                    z15 = z15;
                                                }
                                                objValueOf = objValueOf2;
                                            } else {
                                                strProcess = str5;
                                            }
                                            z7 = z15;
                                            if (list7 != null) {
                                                if (z5) {
                                                    if (cls == Integer.TYPE) {
                                                        objValueOf = Integer.valueOf(i2);
                                                    } else if (cls == Long.TYPE) {
                                                        objValueOf = Long.valueOf(j);
                                                    } else if (cls == Boolean.TYPE) {
                                                        objValueOf = Boolean.valueOf(z3);
                                                    }
                                                    z4 = true;
                                                }
                                                it = list7.iterator();
                                                objProcess = objValueOf;
                                                while (it.hasNext()) {
                                                    Iterator<ValueFilter> it9 = it;
                                                    objProcess = it.next().process(obj, str5, objProcess);
                                                    it = it9;
                                                }
                                                obj3 = objValueOf;
                                                objValueOf = objProcess;
                                            } else {
                                                obj3 = objValueOf;
                                            }
                                            if (z4) {
                                                z8 = z14;
                                            } else {
                                                z8 = z14;
                                            }
                                            obj4 = objValueOf;
                                            if (z4) {
                                                list = list2;
                                            } else {
                                                list = list2;
                                            }
                                            if (z17) {
                                                i6 = serializeWriter.count + 1;
                                                if (i6 > serializeWriter.buf.length) {
                                                    if (serializeWriter.writer == null) {
                                                        serializeWriter.expandCapacity(i6);
                                                    } else {
                                                        serializeWriter.flush();
                                                        i6 = 1;
                                                    }
                                                }
                                                serializeWriter.buf[serializeWriter.count] = ',';
                                                serializeWriter.count = i6;
                                                if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                    jSONSerializer.println();
                                                }
                                            }
                                            if (strProcess != str5) {
                                                if (!z18) {
                                                    serializeWriter.writeFieldName(strProcess, true);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (obj3 != obj4) {
                                                    if (!z18) {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                    jSONSerializer.write(obj4);
                                                } else {
                                                    if (z18) {
                                                        if (z19) {
                                                            cArr = fieldSerializer.name_chars;
                                                            length = cArr.length;
                                                            i3 = serializeWriter.count + length;
                                                            if (i3 <= serializeWriter.buf.length) {
                                                                i4 = 0;
                                                            } else if (serializeWriter.writer == null) {
                                                                serializeWriter.expandCapacity(i3);
                                                                i4 = 0;
                                                            } else {
                                                                i5 = 0;
                                                                do {
                                                                    int length4 = serializeWriter.buf.length - serializeWriter.count;
                                                                    System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length4);
                                                                    serializeWriter.count = serializeWriter.buf.length;
                                                                    serializeWriter.flush();
                                                                    length -= length4;
                                                                    i5 += length4;
                                                                } while (length > serializeWriter.buf.length);
                                                                i4 = i5;
                                                                i3 = length;
                                                            }
                                                            System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                            serializeWriter.count = i3;
                                                        } else {
                                                            fieldSerializer.writePrefix(jSONSerializer);
                                                        }
                                                    }
                                                    if (z5) {
                                                        if (!z18) {
                                                            if (cls == String.class) {
                                                                int i112 = fieldSerializer.features | this.features;
                                                                if (obj4 == null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                        serializeWriter.writeString("");
                                                                    } else {
                                                                        serializeWriter.writeString("");
                                                                    }
                                                                } else {
                                                                    str3 = (String) obj4;
                                                                    if (z8) {
                                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                    }
                                                                }
                                                            } else if (!fieldInfo.isEnum) {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            } else if (obj4 != null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    string = ((Enum) obj4).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        z9 = true;
                                                                    } else {
                                                                        z9 = false;
                                                                    }
                                                                    if (z9) {
                                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                }
                                                            } else {
                                                                serializeWriter.writeNull();
                                                            }
                                                        } else {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        }
                                                    } else if (!z18) {
                                                        if (cls == String.class) {
                                                            int i113 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                }
                                                z17 = true;
                                            }
                                            serialContext2 = serialContext2;
                                            z17 = true;
                                        }
                                    } else {
                                        z5 = z2;
                                        c = c3;
                                    }
                                    z6 = true;
                                    if (!z6) {
                                        if (list6 != null) {
                                            if (z5) {
                                                objValueOf2 = objValueOf;
                                            } else {
                                                objValueOf2 = objValueOf;
                                            }
                                            it2 = list6.iterator();
                                            strProcess = str5;
                                            while (it2.hasNext()) {
                                                strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                z15 = z15;
                                            }
                                            objValueOf = objValueOf2;
                                        } else {
                                            strProcess = str5;
                                        }
                                        z7 = z15;
                                        if (list7 != null) {
                                            if (z5) {
                                                if (cls == Integer.TYPE) {
                                                    objValueOf = Integer.valueOf(i2);
                                                } else if (cls == Long.TYPE) {
                                                    objValueOf = Long.valueOf(j);
                                                } else if (cls == Boolean.TYPE) {
                                                    objValueOf = Boolean.valueOf(z3);
                                                }
                                                z4 = true;
                                            }
                                            it = list7.iterator();
                                            objProcess = objValueOf;
                                            while (it.hasNext()) {
                                                Iterator<ValueFilter> it10 = it;
                                                objProcess = it.next().process(obj, str5, objProcess);
                                                it = it10;
                                            }
                                            obj3 = objValueOf;
                                            objValueOf = objProcess;
                                        } else {
                                            obj3 = objValueOf;
                                        }
                                        if (z4) {
                                            z8 = z14;
                                        } else {
                                            z8 = z14;
                                        }
                                        obj4 = objValueOf;
                                        if (z4) {
                                            list = list2;
                                        } else {
                                            list = list2;
                                        }
                                        if (z17) {
                                            i6 = serializeWriter.count + 1;
                                            if (i6 > serializeWriter.buf.length) {
                                                if (serializeWriter.writer == null) {
                                                    serializeWriter.expandCapacity(i6);
                                                } else {
                                                    serializeWriter.flush();
                                                    i6 = 1;
                                                }
                                            }
                                            serializeWriter.buf[serializeWriter.count] = ',';
                                            serializeWriter.count = i6;
                                            if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                jSONSerializer.println();
                                            }
                                        }
                                        if (strProcess != str5) {
                                            if (!z18) {
                                                serializeWriter.writeFieldName(strProcess, true);
                                            }
                                            jSONSerializer.write(obj4);
                                        } else {
                                            if (obj3 != obj4) {
                                                if (!z18) {
                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (z18) {
                                                    if (z19) {
                                                        cArr = fieldSerializer.name_chars;
                                                        length = cArr.length;
                                                        i3 = serializeWriter.count + length;
                                                        if (i3 <= serializeWriter.buf.length) {
                                                            i4 = 0;
                                                        } else if (serializeWriter.writer == null) {
                                                            serializeWriter.expandCapacity(i3);
                                                            i4 = 0;
                                                        } else {
                                                            i5 = 0;
                                                            do {
                                                                int length5 = serializeWriter.buf.length - serializeWriter.count;
                                                                System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length5);
                                                                serializeWriter.count = serializeWriter.buf.length;
                                                                serializeWriter.flush();
                                                                length -= length5;
                                                                i5 += length5;
                                                            } while (length > serializeWriter.buf.length);
                                                            i4 = i5;
                                                            i3 = length;
                                                        }
                                                        System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                        serializeWriter.count = i3;
                                                    } else {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                }
                                                if (z5) {
                                                    if (!z18) {
                                                        if (cls == String.class) {
                                                            int i114 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                } else if (!z18) {
                                                    if (cls == String.class) {
                                                        int i115 = fieldSerializer.features | this.features;
                                                        if (obj4 == null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                serializeWriter.writeString("");
                                                            } else {
                                                                serializeWriter.writeString("");
                                                            }
                                                        } else {
                                                            str3 = (String) obj4;
                                                            if (z8) {
                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                            }
                                                        }
                                                    } else if (!fieldInfo.isEnum) {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    } else if (obj4 != null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                            string = ((Enum) obj4).toString();
                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                z9 = true;
                                                            } else {
                                                                z9 = false;
                                                            }
                                                            if (z9) {
                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                            }
                                                        } else {
                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                        }
                                                    } else {
                                                        serializeWriter.writeNull();
                                                    }
                                                } else {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                }
                                            }
                                            z17 = true;
                                        }
                                        serialContext2 = serialContext2;
                                        z17 = true;
                                    }
                                }
                                z2 = true;
                                if (list2 != null) {
                                    if (z2) {
                                        z5 = z2;
                                        if (cls == Integer.TYPE) {
                                            objValueOf3 = Integer.valueOf(i2);
                                        } else if (cls == Long.TYPE) {
                                            objValueOf3 = Long.valueOf(j);
                                        } else {
                                            if (cls == Boolean.TYPE) {
                                                objValueOf3 = Boolean.valueOf(z3);
                                            }
                                            it3 = list2.iterator();
                                            while (true) {
                                                if (it3.hasNext()) {
                                                    c = c3;
                                                    if (!it3.next().apply(obj, str5, obj5)) {
                                                        objValueOf = obj5;
                                                        z6 = false;
                                                        break;
                                                    }
                                                    c3 = c;
                                                } else {
                                                    c = c3;
                                                    objValueOf = obj5;
                                                }
                                            }
                                            if (!z6) {
                                                if (list6 != null) {
                                                    if (z5) {
                                                        objValueOf2 = objValueOf;
                                                    } else {
                                                        objValueOf2 = objValueOf;
                                                    }
                                                    it2 = list6.iterator();
                                                    strProcess = str5;
                                                    while (it2.hasNext()) {
                                                        strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                        z15 = z15;
                                                    }
                                                    objValueOf = objValueOf2;
                                                } else {
                                                    strProcess = str5;
                                                }
                                                z7 = z15;
                                                if (list7 != null) {
                                                    if (z5) {
                                                        if (cls == Integer.TYPE) {
                                                            objValueOf = Integer.valueOf(i2);
                                                        } else if (cls == Long.TYPE) {
                                                            objValueOf = Long.valueOf(j);
                                                        } else if (cls == Boolean.TYPE) {
                                                            objValueOf = Boolean.valueOf(z3);
                                                        }
                                                        z4 = true;
                                                    }
                                                    it = list7.iterator();
                                                    objProcess = objValueOf;
                                                    while (it.hasNext()) {
                                                        Iterator<ValueFilter> it11 = it;
                                                        objProcess = it.next().process(obj, str5, objProcess);
                                                        it = it11;
                                                    }
                                                    obj3 = objValueOf;
                                                    objValueOf = objProcess;
                                                } else {
                                                    obj3 = objValueOf;
                                                }
                                                if (z4) {
                                                    z8 = z14;
                                                } else {
                                                    z8 = z14;
                                                }
                                                obj4 = objValueOf;
                                                if (z4) {
                                                    list = list2;
                                                } else {
                                                    list = list2;
                                                }
                                                if (z17) {
                                                    i6 = serializeWriter.count + 1;
                                                    if (i6 > serializeWriter.buf.length) {
                                                        if (serializeWriter.writer == null) {
                                                            serializeWriter.expandCapacity(i6);
                                                        } else {
                                                            serializeWriter.flush();
                                                            i6 = 1;
                                                        }
                                                    }
                                                    serializeWriter.buf[serializeWriter.count] = ',';
                                                    serializeWriter.count = i6;
                                                    if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                        jSONSerializer.println();
                                                    }
                                                }
                                                if (strProcess != str5) {
                                                    if (!z18) {
                                                        serializeWriter.writeFieldName(strProcess, true);
                                                    }
                                                    jSONSerializer.write(obj4);
                                                } else {
                                                    if (obj3 != obj4) {
                                                        if (!z18) {
                                                            fieldSerializer.writePrefix(jSONSerializer);
                                                        }
                                                        jSONSerializer.write(obj4);
                                                    } else {
                                                        if (z18) {
                                                            if (z19) {
                                                                cArr = fieldSerializer.name_chars;
                                                                length = cArr.length;
                                                                i3 = serializeWriter.count + length;
                                                                if (i3 <= serializeWriter.buf.length) {
                                                                    i4 = 0;
                                                                } else if (serializeWriter.writer == null) {
                                                                    serializeWriter.expandCapacity(i3);
                                                                    i4 = 0;
                                                                } else {
                                                                    i5 = 0;
                                                                    do {
                                                                        int length6 = serializeWriter.buf.length - serializeWriter.count;
                                                                        System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length6);
                                                                        serializeWriter.count = serializeWriter.buf.length;
                                                                        serializeWriter.flush();
                                                                        length -= length6;
                                                                        i5 += length6;
                                                                    } while (length > serializeWriter.buf.length);
                                                                    i4 = i5;
                                                                    i3 = length;
                                                                }
                                                                System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                                serializeWriter.count = i3;
                                                            } else {
                                                                fieldSerializer.writePrefix(jSONSerializer);
                                                            }
                                                        }
                                                        if (z5) {
                                                            if (!z18) {
                                                                if (cls == String.class) {
                                                                    int i116 = fieldSerializer.features | this.features;
                                                                    if (obj4 == null) {
                                                                        if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                            serializeWriter.writeString("");
                                                                        } else {
                                                                            serializeWriter.writeString("");
                                                                        }
                                                                    } else {
                                                                        str3 = (String) obj4;
                                                                        if (z8) {
                                                                            serializeWriter.writeStringWithSingleQuote(str3);
                                                                        } else {
                                                                            serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                        }
                                                                    }
                                                                } else if (!fieldInfo.isEnum) {
                                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                                } else if (obj4 != null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                        string = ((Enum) obj4).toString();
                                                                        if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                            z9 = true;
                                                                        } else {
                                                                            z9 = false;
                                                                        }
                                                                        if (z9) {
                                                                            serializeWriter.writeStringWithSingleQuote(string);
                                                                        } else {
                                                                            serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                        }
                                                                    } else {
                                                                        serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeNull();
                                                                }
                                                            } else {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            }
                                                        } else if (!z18) {
                                                            if (cls == String.class) {
                                                                int i117 = fieldSerializer.features | this.features;
                                                                if (obj4 == null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                        serializeWriter.writeString("");
                                                                    } else {
                                                                        serializeWriter.writeString("");
                                                                    }
                                                                } else {
                                                                    str3 = (String) obj4;
                                                                    if (z8) {
                                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                    }
                                                                }
                                                            } else if (!fieldInfo.isEnum) {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            } else if (obj4 != null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    string = ((Enum) obj4).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        z9 = true;
                                                                    } else {
                                                                        z9 = false;
                                                                    }
                                                                    if (z9) {
                                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                }
                                                            } else {
                                                                serializeWriter.writeNull();
                                                            }
                                                        } else {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        }
                                                    }
                                                    z17 = true;
                                                }
                                                serialContext2 = serialContext2;
                                                z17 = true;
                                            }
                                        }
                                        obj5 = objValueOf3;
                                        z4 = true;
                                        it3 = list2.iterator();
                                        while (true) {
                                            if (it3.hasNext()) {
                                                c = c3;
                                                if (!it3.next().apply(obj, str5, obj5)) {
                                                    objValueOf = obj5;
                                                    z6 = false;
                                                    break;
                                                }
                                                c3 = c;
                                            } else {
                                                c = c3;
                                                objValueOf = obj5;
                                            }
                                        }
                                        if (!z6) {
                                            if (list6 != null) {
                                                if (z5) {
                                                    objValueOf2 = objValueOf;
                                                } else {
                                                    objValueOf2 = objValueOf;
                                                }
                                                it2 = list6.iterator();
                                                strProcess = str5;
                                                while (it2.hasNext()) {
                                                    strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                    z15 = z15;
                                                }
                                                objValueOf = objValueOf2;
                                            } else {
                                                strProcess = str5;
                                            }
                                            z7 = z15;
                                            if (list7 != null) {
                                                if (z5) {
                                                    if (cls == Integer.TYPE) {
                                                        objValueOf = Integer.valueOf(i2);
                                                    } else if (cls == Long.TYPE) {
                                                        objValueOf = Long.valueOf(j);
                                                    } else if (cls == Boolean.TYPE) {
                                                        objValueOf = Boolean.valueOf(z3);
                                                    }
                                                    z4 = true;
                                                }
                                                it = list7.iterator();
                                                objProcess = objValueOf;
                                                while (it.hasNext()) {
                                                    Iterator<ValueFilter> it12 = it;
                                                    objProcess = it.next().process(obj, str5, objProcess);
                                                    it = it12;
                                                }
                                                obj3 = objValueOf;
                                                objValueOf = objProcess;
                                            } else {
                                                obj3 = objValueOf;
                                            }
                                            if (z4) {
                                                z8 = z14;
                                            } else {
                                                z8 = z14;
                                            }
                                            obj4 = objValueOf;
                                            if (z4) {
                                                list = list2;
                                            } else {
                                                list = list2;
                                            }
                                            if (z17) {
                                                i6 = serializeWriter.count + 1;
                                                if (i6 > serializeWriter.buf.length) {
                                                    if (serializeWriter.writer == null) {
                                                        serializeWriter.expandCapacity(i6);
                                                    } else {
                                                        serializeWriter.flush();
                                                        i6 = 1;
                                                    }
                                                }
                                                serializeWriter.buf[serializeWriter.count] = ',';
                                                serializeWriter.count = i6;
                                                if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                    jSONSerializer.println();
                                                }
                                            }
                                            if (strProcess != str5) {
                                                if (!z18) {
                                                    serializeWriter.writeFieldName(strProcess, true);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (obj3 != obj4) {
                                                    if (!z18) {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                    jSONSerializer.write(obj4);
                                                } else {
                                                    if (z18) {
                                                        if (z19) {
                                                            cArr = fieldSerializer.name_chars;
                                                            length = cArr.length;
                                                            i3 = serializeWriter.count + length;
                                                            if (i3 <= serializeWriter.buf.length) {
                                                                i4 = 0;
                                                            } else if (serializeWriter.writer == null) {
                                                                serializeWriter.expandCapacity(i3);
                                                                i4 = 0;
                                                            } else {
                                                                i5 = 0;
                                                                do {
                                                                    int length7 = serializeWriter.buf.length - serializeWriter.count;
                                                                    System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length7);
                                                                    serializeWriter.count = serializeWriter.buf.length;
                                                                    serializeWriter.flush();
                                                                    length -= length7;
                                                                    i5 += length7;
                                                                } while (length > serializeWriter.buf.length);
                                                                i4 = i5;
                                                                i3 = length;
                                                            }
                                                            System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                            serializeWriter.count = i3;
                                                        } else {
                                                            fieldSerializer.writePrefix(jSONSerializer);
                                                        }
                                                    }
                                                    if (z5) {
                                                        if (!z18) {
                                                            if (cls == String.class) {
                                                                int i118 = fieldSerializer.features | this.features;
                                                                if (obj4 == null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                        serializeWriter.writeString("");
                                                                    } else {
                                                                        serializeWriter.writeString("");
                                                                    }
                                                                } else {
                                                                    str3 = (String) obj4;
                                                                    if (z8) {
                                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                    }
                                                                }
                                                            } else if (!fieldInfo.isEnum) {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            } else if (obj4 != null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    string = ((Enum) obj4).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        z9 = true;
                                                                    } else {
                                                                        z9 = false;
                                                                    }
                                                                    if (z9) {
                                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                }
                                                            } else {
                                                                serializeWriter.writeNull();
                                                            }
                                                        } else {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        }
                                                    } else if (!z18) {
                                                        if (cls == String.class) {
                                                            int i119 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                }
                                                z17 = true;
                                            }
                                            serialContext2 = serialContext2;
                                            z17 = true;
                                        }
                                    } else {
                                        z5 = z2;
                                    }
                                    obj5 = objValueOf;
                                    it3 = list2.iterator();
                                    while (true) {
                                        if (it3.hasNext()) {
                                            c = c3;
                                            if (!it3.next().apply(obj, str5, obj5)) {
                                                objValueOf = obj5;
                                                z6 = false;
                                                break;
                                            }
                                            c3 = c;
                                        } else {
                                            c = c3;
                                            objValueOf = obj5;
                                        }
                                    }
                                    if (!z6) {
                                        if (list6 != null) {
                                            if (z5) {
                                                objValueOf2 = objValueOf;
                                            } else {
                                                objValueOf2 = objValueOf;
                                            }
                                            it2 = list6.iterator();
                                            strProcess = str5;
                                            while (it2.hasNext()) {
                                                strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                z15 = z15;
                                            }
                                            objValueOf = objValueOf2;
                                        } else {
                                            strProcess = str5;
                                        }
                                        z7 = z15;
                                        if (list7 != null) {
                                            if (z5) {
                                                if (cls == Integer.TYPE) {
                                                    objValueOf = Integer.valueOf(i2);
                                                } else if (cls == Long.TYPE) {
                                                    objValueOf = Long.valueOf(j);
                                                } else if (cls == Boolean.TYPE) {
                                                    objValueOf = Boolean.valueOf(z3);
                                                }
                                                z4 = true;
                                            }
                                            it = list7.iterator();
                                            objProcess = objValueOf;
                                            while (it.hasNext()) {
                                                Iterator<ValueFilter> it13 = it;
                                                objProcess = it.next().process(obj, str5, objProcess);
                                                it = it13;
                                            }
                                            obj3 = objValueOf;
                                            objValueOf = objProcess;
                                        } else {
                                            obj3 = objValueOf;
                                        }
                                        if (z4) {
                                            z8 = z14;
                                        } else {
                                            z8 = z14;
                                        }
                                        obj4 = objValueOf;
                                        if (z4) {
                                            list = list2;
                                        } else {
                                            list = list2;
                                        }
                                        if (z17) {
                                            i6 = serializeWriter.count + 1;
                                            if (i6 > serializeWriter.buf.length) {
                                                if (serializeWriter.writer == null) {
                                                    serializeWriter.expandCapacity(i6);
                                                } else {
                                                    serializeWriter.flush();
                                                    i6 = 1;
                                                }
                                            }
                                            serializeWriter.buf[serializeWriter.count] = ',';
                                            serializeWriter.count = i6;
                                            if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                jSONSerializer.println();
                                            }
                                        }
                                        if (strProcess != str5) {
                                            if (!z18) {
                                                serializeWriter.writeFieldName(strProcess, true);
                                            }
                                            jSONSerializer.write(obj4);
                                        } else {
                                            if (obj3 != obj4) {
                                                if (!z18) {
                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (z18) {
                                                    if (z19) {
                                                        cArr = fieldSerializer.name_chars;
                                                        length = cArr.length;
                                                        i3 = serializeWriter.count + length;
                                                        if (i3 <= serializeWriter.buf.length) {
                                                            i4 = 0;
                                                        } else if (serializeWriter.writer == null) {
                                                            serializeWriter.expandCapacity(i3);
                                                            i4 = 0;
                                                        } else {
                                                            i5 = 0;
                                                            do {
                                                                int length8 = serializeWriter.buf.length - serializeWriter.count;
                                                                System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length8);
                                                                serializeWriter.count = serializeWriter.buf.length;
                                                                serializeWriter.flush();
                                                                length -= length8;
                                                                i5 += length8;
                                                            } while (length > serializeWriter.buf.length);
                                                            i4 = i5;
                                                            i3 = length;
                                                        }
                                                        System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                        serializeWriter.count = i3;
                                                    } else {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                }
                                                if (z5) {
                                                    if (!z18) {
                                                        if (cls == String.class) {
                                                            int i1110 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                } else if (!z18) {
                                                    if (cls == String.class) {
                                                        int i1111 = fieldSerializer.features | this.features;
                                                        if (obj4 == null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                serializeWriter.writeString("");
                                                            } else {
                                                                serializeWriter.writeString("");
                                                            }
                                                        } else {
                                                            str3 = (String) obj4;
                                                            if (z8) {
                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                            }
                                                        }
                                                    } else if (!fieldInfo.isEnum) {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    } else if (obj4 != null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                            string = ((Enum) obj4).toString();
                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                z9 = true;
                                                            } else {
                                                                z9 = false;
                                                            }
                                                            if (z9) {
                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                            }
                                                        } else {
                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                        }
                                                    } else {
                                                        serializeWriter.writeNull();
                                                    }
                                                } else {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                }
                                            }
                                            z17 = true;
                                        }
                                        serialContext2 = serialContext2;
                                        z17 = true;
                                    }
                                } else {
                                    z5 = z2;
                                    c = c3;
                                }
                                z6 = true;
                                if (!z6) {
                                    if (list6 != null) {
                                        if (z5) {
                                            objValueOf2 = objValueOf;
                                        } else {
                                            objValueOf2 = objValueOf;
                                        }
                                        it2 = list6.iterator();
                                        strProcess = str5;
                                        while (it2.hasNext()) {
                                            strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                            z15 = z15;
                                        }
                                        objValueOf = objValueOf2;
                                    } else {
                                        strProcess = str5;
                                    }
                                    z7 = z15;
                                    if (list7 != null) {
                                        if (z5) {
                                            if (cls == Integer.TYPE) {
                                                objValueOf = Integer.valueOf(i2);
                                            } else if (cls == Long.TYPE) {
                                                objValueOf = Long.valueOf(j);
                                            } else if (cls == Boolean.TYPE) {
                                                objValueOf = Boolean.valueOf(z3);
                                            }
                                            z4 = true;
                                        }
                                        it = list7.iterator();
                                        objProcess = objValueOf;
                                        while (it.hasNext()) {
                                            Iterator<ValueFilter> it14 = it;
                                            objProcess = it.next().process(obj, str5, objProcess);
                                            it = it14;
                                        }
                                        obj3 = objValueOf;
                                        objValueOf = objProcess;
                                    } else {
                                        obj3 = objValueOf;
                                    }
                                    if (z4) {
                                        z8 = z14;
                                    } else {
                                        z8 = z14;
                                    }
                                    obj4 = objValueOf;
                                    if (z4) {
                                        list = list2;
                                    } else {
                                        list = list2;
                                    }
                                    if (z17) {
                                        i6 = serializeWriter.count + 1;
                                        if (i6 > serializeWriter.buf.length) {
                                            if (serializeWriter.writer == null) {
                                                serializeWriter.expandCapacity(i6);
                                            } else {
                                                serializeWriter.flush();
                                                i6 = 1;
                                            }
                                        }
                                        serializeWriter.buf[serializeWriter.count] = ',';
                                        serializeWriter.count = i6;
                                        if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                            jSONSerializer.println();
                                        }
                                    }
                                    if (strProcess != str5) {
                                        if (!z18) {
                                            serializeWriter.writeFieldName(strProcess, true);
                                        }
                                        jSONSerializer.write(obj4);
                                    } else {
                                        if (obj3 != obj4) {
                                            if (!z18) {
                                                fieldSerializer.writePrefix(jSONSerializer);
                                            }
                                            jSONSerializer.write(obj4);
                                        } else {
                                            if (z18) {
                                                if (z19) {
                                                    cArr = fieldSerializer.name_chars;
                                                    length = cArr.length;
                                                    i3 = serializeWriter.count + length;
                                                    if (i3 <= serializeWriter.buf.length) {
                                                        i4 = 0;
                                                    } else if (serializeWriter.writer == null) {
                                                        serializeWriter.expandCapacity(i3);
                                                        i4 = 0;
                                                    } else {
                                                        i5 = 0;
                                                        do {
                                                            int length9 = serializeWriter.buf.length - serializeWriter.count;
                                                            System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length9);
                                                            serializeWriter.count = serializeWriter.buf.length;
                                                            serializeWriter.flush();
                                                            length -= length9;
                                                            i5 += length9;
                                                        } while (length > serializeWriter.buf.length);
                                                        i4 = i5;
                                                        i3 = length;
                                                    }
                                                    System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                    serializeWriter.count = i3;
                                                } else {
                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                }
                                            }
                                            if (z5) {
                                                if (!z18) {
                                                    if (cls == String.class) {
                                                        int i1112 = fieldSerializer.features | this.features;
                                                        if (obj4 == null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                serializeWriter.writeString("");
                                                            } else {
                                                                serializeWriter.writeString("");
                                                            }
                                                        } else {
                                                            str3 = (String) obj4;
                                                            if (z8) {
                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                            }
                                                        }
                                                    } else if (!fieldInfo.isEnum) {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    } else if (obj4 != null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                            string = ((Enum) obj4).toString();
                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                z9 = true;
                                                            } else {
                                                                z9 = false;
                                                            }
                                                            if (z9) {
                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                            }
                                                        } else {
                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                        }
                                                    } else {
                                                        serializeWriter.writeNull();
                                                    }
                                                } else {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                }
                                            } else if (!z18) {
                                                if (cls == String.class) {
                                                    int i1113 = fieldSerializer.features | this.features;
                                                    if (obj4 == null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                            serializeWriter.writeString("");
                                                        } else {
                                                            serializeWriter.writeString("");
                                                        }
                                                    } else {
                                                        str3 = (String) obj4;
                                                        if (z8) {
                                                            serializeWriter.writeStringWithSingleQuote(str3);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                        }
                                                    }
                                                } else if (!fieldInfo.isEnum) {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                } else if (obj4 != null) {
                                                    if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                        string = ((Enum) obj4).toString();
                                                        if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                            z9 = true;
                                                        } else {
                                                            z9 = false;
                                                        }
                                                        if (z9) {
                                                            serializeWriter.writeStringWithSingleQuote(string);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                        }
                                                    } else {
                                                        serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                    }
                                                } else {
                                                    serializeWriter.writeNull();
                                                }
                                            } else {
                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                            }
                                        }
                                        z17 = true;
                                    }
                                    serialContext2 = serialContext2;
                                    z17 = true;
                                }
                            } else {
                                propertyValue = fieldSerializer.getPropertyValue(obj);
                            }
                            j = 0;
                            objValueOf = propertyValue;
                            z2 = false;
                            i2 = 0;
                            z3 = false;
                            z4 = true;
                            if (list2 != null) {
                                if (z2) {
                                    z5 = z2;
                                    if (cls == Integer.TYPE) {
                                        objValueOf3 = Integer.valueOf(i2);
                                    } else if (cls == Long.TYPE) {
                                        objValueOf3 = Long.valueOf(j);
                                    } else {
                                        if (cls == Boolean.TYPE) {
                                            objValueOf3 = Boolean.valueOf(z3);
                                        }
                                        it3 = list2.iterator();
                                        while (true) {
                                            if (it3.hasNext()) {
                                                c = c3;
                                                if (!it3.next().apply(obj, str5, obj5)) {
                                                    objValueOf = obj5;
                                                    z6 = false;
                                                    break;
                                                }
                                                c3 = c;
                                            } else {
                                                c = c3;
                                                objValueOf = obj5;
                                            }
                                        }
                                        if (!z6) {
                                            if (list6 != null) {
                                                if (z5) {
                                                    objValueOf2 = objValueOf;
                                                } else {
                                                    objValueOf2 = objValueOf;
                                                }
                                                it2 = list6.iterator();
                                                strProcess = str5;
                                                while (it2.hasNext()) {
                                                    strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                    z15 = z15;
                                                }
                                                objValueOf = objValueOf2;
                                            } else {
                                                strProcess = str5;
                                            }
                                            z7 = z15;
                                            if (list7 != null) {
                                                if (z5) {
                                                    if (cls == Integer.TYPE) {
                                                        objValueOf = Integer.valueOf(i2);
                                                    } else if (cls == Long.TYPE) {
                                                        objValueOf = Long.valueOf(j);
                                                    } else if (cls == Boolean.TYPE) {
                                                        objValueOf = Boolean.valueOf(z3);
                                                    }
                                                    z4 = true;
                                                }
                                                it = list7.iterator();
                                                objProcess = objValueOf;
                                                while (it.hasNext()) {
                                                    Iterator<ValueFilter> it15 = it;
                                                    objProcess = it.next().process(obj, str5, objProcess);
                                                    it = it15;
                                                }
                                                obj3 = objValueOf;
                                                objValueOf = objProcess;
                                            } else {
                                                obj3 = objValueOf;
                                            }
                                            if (z4) {
                                                z8 = z14;
                                            } else {
                                                z8 = z14;
                                            }
                                            obj4 = objValueOf;
                                            if (z4) {
                                                list = list2;
                                            } else {
                                                list = list2;
                                            }
                                            if (z17) {
                                                i6 = serializeWriter.count + 1;
                                                if (i6 > serializeWriter.buf.length) {
                                                    if (serializeWriter.writer == null) {
                                                        serializeWriter.expandCapacity(i6);
                                                    } else {
                                                        serializeWriter.flush();
                                                        i6 = 1;
                                                    }
                                                }
                                                serializeWriter.buf[serializeWriter.count] = ',';
                                                serializeWriter.count = i6;
                                                if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                    jSONSerializer.println();
                                                }
                                            }
                                            if (strProcess != str5) {
                                                if (!z18) {
                                                    serializeWriter.writeFieldName(strProcess, true);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (obj3 != obj4) {
                                                    if (!z18) {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                    jSONSerializer.write(obj4);
                                                } else {
                                                    if (z18) {
                                                        if (z19) {
                                                            cArr = fieldSerializer.name_chars;
                                                            length = cArr.length;
                                                            i3 = serializeWriter.count + length;
                                                            if (i3 <= serializeWriter.buf.length) {
                                                                i4 = 0;
                                                            } else if (serializeWriter.writer == null) {
                                                                serializeWriter.expandCapacity(i3);
                                                                i4 = 0;
                                                            } else {
                                                                i5 = 0;
                                                                do {
                                                                    int length10 = serializeWriter.buf.length - serializeWriter.count;
                                                                    System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length10);
                                                                    serializeWriter.count = serializeWriter.buf.length;
                                                                    serializeWriter.flush();
                                                                    length -= length10;
                                                                    i5 += length10;
                                                                } while (length > serializeWriter.buf.length);
                                                                i4 = i5;
                                                                i3 = length;
                                                            }
                                                            System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                            serializeWriter.count = i3;
                                                        } else {
                                                            fieldSerializer.writePrefix(jSONSerializer);
                                                        }
                                                    }
                                                    if (z5) {
                                                        if (!z18) {
                                                            if (cls == String.class) {
                                                                int i1114 = fieldSerializer.features | this.features;
                                                                if (obj4 == null) {
                                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                        serializeWriter.writeString("");
                                                                    } else {
                                                                        serializeWriter.writeString("");
                                                                    }
                                                                } else {
                                                                    str3 = (String) obj4;
                                                                    if (z8) {
                                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                    }
                                                                }
                                                            } else if (!fieldInfo.isEnum) {
                                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                                            } else if (obj4 != null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    string = ((Enum) obj4).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        z9 = true;
                                                                    } else {
                                                                        z9 = false;
                                                                    }
                                                                    if (z9) {
                                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                                }
                                                            } else {
                                                                serializeWriter.writeNull();
                                                            }
                                                        } else {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        }
                                                    } else if (!z18) {
                                                        if (cls == String.class) {
                                                            int i1115 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                }
                                                z17 = true;
                                            }
                                            serialContext2 = serialContext2;
                                            z17 = true;
                                        }
                                    }
                                    obj5 = objValueOf3;
                                    z4 = true;
                                    it3 = list2.iterator();
                                    while (true) {
                                        if (it3.hasNext()) {
                                            c = c3;
                                            if (!it3.next().apply(obj, str5, obj5)) {
                                                objValueOf = obj5;
                                                z6 = false;
                                                break;
                                            }
                                            c3 = c;
                                        } else {
                                            c = c3;
                                            objValueOf = obj5;
                                        }
                                    }
                                    if (!z6) {
                                        if (list6 != null) {
                                            if (z5) {
                                                objValueOf2 = objValueOf;
                                            } else {
                                                objValueOf2 = objValueOf;
                                            }
                                            it2 = list6.iterator();
                                            strProcess = str5;
                                            while (it2.hasNext()) {
                                                strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                                z15 = z15;
                                            }
                                            objValueOf = objValueOf2;
                                        } else {
                                            strProcess = str5;
                                        }
                                        z7 = z15;
                                        if (list7 != null) {
                                            if (z5) {
                                                if (cls == Integer.TYPE) {
                                                    objValueOf = Integer.valueOf(i2);
                                                } else if (cls == Long.TYPE) {
                                                    objValueOf = Long.valueOf(j);
                                                } else if (cls == Boolean.TYPE) {
                                                    objValueOf = Boolean.valueOf(z3);
                                                }
                                                z4 = true;
                                            }
                                            it = list7.iterator();
                                            objProcess = objValueOf;
                                            while (it.hasNext()) {
                                                Iterator<ValueFilter> it16 = it;
                                                objProcess = it.next().process(obj, str5, objProcess);
                                                it = it16;
                                            }
                                            obj3 = objValueOf;
                                            objValueOf = objProcess;
                                        } else {
                                            obj3 = objValueOf;
                                        }
                                        if (z4) {
                                            z8 = z14;
                                        } else {
                                            z8 = z14;
                                        }
                                        obj4 = objValueOf;
                                        if (z4) {
                                            list = list2;
                                        } else {
                                            list = list2;
                                        }
                                        if (z17) {
                                            i6 = serializeWriter.count + 1;
                                            if (i6 > serializeWriter.buf.length) {
                                                if (serializeWriter.writer == null) {
                                                    serializeWriter.expandCapacity(i6);
                                                } else {
                                                    serializeWriter.flush();
                                                    i6 = 1;
                                                }
                                            }
                                            serializeWriter.buf[serializeWriter.count] = ',';
                                            serializeWriter.count = i6;
                                            if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                jSONSerializer.println();
                                            }
                                        }
                                        if (strProcess != str5) {
                                            if (!z18) {
                                                serializeWriter.writeFieldName(strProcess, true);
                                            }
                                            jSONSerializer.write(obj4);
                                        } else {
                                            if (obj3 != obj4) {
                                                if (!z18) {
                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                }
                                                jSONSerializer.write(obj4);
                                            } else {
                                                if (z18) {
                                                    if (z19) {
                                                        cArr = fieldSerializer.name_chars;
                                                        length = cArr.length;
                                                        i3 = serializeWriter.count + length;
                                                        if (i3 <= serializeWriter.buf.length) {
                                                            i4 = 0;
                                                        } else if (serializeWriter.writer == null) {
                                                            serializeWriter.expandCapacity(i3);
                                                            i4 = 0;
                                                        } else {
                                                            i5 = 0;
                                                            do {
                                                                int length11 = serializeWriter.buf.length - serializeWriter.count;
                                                                System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length11);
                                                                serializeWriter.count = serializeWriter.buf.length;
                                                                serializeWriter.flush();
                                                                length -= length11;
                                                                i5 += length11;
                                                            } while (length > serializeWriter.buf.length);
                                                            i4 = i5;
                                                            i3 = length;
                                                        }
                                                        System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                        serializeWriter.count = i3;
                                                    } else {
                                                        fieldSerializer.writePrefix(jSONSerializer);
                                                    }
                                                }
                                                if (z5) {
                                                    if (!z18) {
                                                        if (cls == String.class) {
                                                            int i1116 = fieldSerializer.features | this.features;
                                                            if (obj4 == null) {
                                                                if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                    serializeWriter.writeString("");
                                                                } else {
                                                                    serializeWriter.writeString("");
                                                                }
                                                            } else {
                                                                str3 = (String) obj4;
                                                                if (z8) {
                                                                    serializeWriter.writeStringWithSingleQuote(str3);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                                }
                                                            }
                                                        } else if (!fieldInfo.isEnum) {
                                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                                        } else if (obj4 != null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                string = ((Enum) obj4).toString();
                                                                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                    z9 = true;
                                                                } else {
                                                                    z9 = false;
                                                                }
                                                                if (z9) {
                                                                    serializeWriter.writeStringWithSingleQuote(string);
                                                                } else {
                                                                    serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                                }
                                                            } else {
                                                                serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                            }
                                                        } else {
                                                            serializeWriter.writeNull();
                                                        }
                                                    } else {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    }
                                                } else if (!z18) {
                                                    if (cls == String.class) {
                                                        int i1117 = fieldSerializer.features | this.features;
                                                        if (obj4 == null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                serializeWriter.writeString("");
                                                            } else {
                                                                serializeWriter.writeString("");
                                                            }
                                                        } else {
                                                            str3 = (String) obj4;
                                                            if (z8) {
                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                            }
                                                        }
                                                    } else if (!fieldInfo.isEnum) {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    } else if (obj4 != null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                            string = ((Enum) obj4).toString();
                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                z9 = true;
                                                            } else {
                                                                z9 = false;
                                                            }
                                                            if (z9) {
                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                            }
                                                        } else {
                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                        }
                                                    } else {
                                                        serializeWriter.writeNull();
                                                    }
                                                } else {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                }
                                            }
                                            z17 = true;
                                        }
                                        serialContext2 = serialContext2;
                                        z17 = true;
                                    }
                                } else {
                                    z5 = z2;
                                }
                                obj5 = objValueOf;
                                it3 = list2.iterator();
                                while (true) {
                                    if (it3.hasNext()) {
                                        c = c3;
                                        if (!it3.next().apply(obj, str5, obj5)) {
                                            objValueOf = obj5;
                                            z6 = false;
                                            break;
                                        }
                                        c3 = c;
                                    } else {
                                        c = c3;
                                        objValueOf = obj5;
                                    }
                                }
                                if (!z6) {
                                    if (list6 != null) {
                                        if (z5) {
                                            objValueOf2 = objValueOf;
                                        } else {
                                            objValueOf2 = objValueOf;
                                        }
                                        it2 = list6.iterator();
                                        strProcess = str5;
                                        while (it2.hasNext()) {
                                            strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                            z15 = z15;
                                        }
                                        objValueOf = objValueOf2;
                                    } else {
                                        strProcess = str5;
                                    }
                                    z7 = z15;
                                    if (list7 != null) {
                                        if (z5) {
                                            if (cls == Integer.TYPE) {
                                                objValueOf = Integer.valueOf(i2);
                                            } else if (cls == Long.TYPE) {
                                                objValueOf = Long.valueOf(j);
                                            } else if (cls == Boolean.TYPE) {
                                                objValueOf = Boolean.valueOf(z3);
                                            }
                                            z4 = true;
                                        }
                                        it = list7.iterator();
                                        objProcess = objValueOf;
                                        while (it.hasNext()) {
                                            Iterator<ValueFilter> it17 = it;
                                            objProcess = it.next().process(obj, str5, objProcess);
                                            it = it17;
                                        }
                                        obj3 = objValueOf;
                                        objValueOf = objProcess;
                                    } else {
                                        obj3 = objValueOf;
                                    }
                                    if (z4) {
                                        z8 = z14;
                                    } else {
                                        z8 = z14;
                                    }
                                    obj4 = objValueOf;
                                    if (z4) {
                                        list = list2;
                                    } else {
                                        list = list2;
                                    }
                                    if (z17) {
                                        i6 = serializeWriter.count + 1;
                                        if (i6 > serializeWriter.buf.length) {
                                            if (serializeWriter.writer == null) {
                                                serializeWriter.expandCapacity(i6);
                                            } else {
                                                serializeWriter.flush();
                                                i6 = 1;
                                            }
                                        }
                                        serializeWriter.buf[serializeWriter.count] = ',';
                                        serializeWriter.count = i6;
                                        if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                            jSONSerializer.println();
                                        }
                                    }
                                    if (strProcess != str5) {
                                        if (!z18) {
                                            serializeWriter.writeFieldName(strProcess, true);
                                        }
                                        jSONSerializer.write(obj4);
                                    } else {
                                        if (obj3 != obj4) {
                                            if (!z18) {
                                                fieldSerializer.writePrefix(jSONSerializer);
                                            }
                                            jSONSerializer.write(obj4);
                                        } else {
                                            if (z18) {
                                                if (z19) {
                                                    cArr = fieldSerializer.name_chars;
                                                    length = cArr.length;
                                                    i3 = serializeWriter.count + length;
                                                    if (i3 <= serializeWriter.buf.length) {
                                                        i4 = 0;
                                                    } else if (serializeWriter.writer == null) {
                                                        serializeWriter.expandCapacity(i3);
                                                        i4 = 0;
                                                    } else {
                                                        i5 = 0;
                                                        do {
                                                            int length12 = serializeWriter.buf.length - serializeWriter.count;
                                                            System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length12);
                                                            serializeWriter.count = serializeWriter.buf.length;
                                                            serializeWriter.flush();
                                                            length -= length12;
                                                            i5 += length12;
                                                        } while (length > serializeWriter.buf.length);
                                                        i4 = i5;
                                                        i3 = length;
                                                    }
                                                    System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                    serializeWriter.count = i3;
                                                } else {
                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                }
                                            }
                                            if (z5) {
                                                if (!z18) {
                                                    if (cls == String.class) {
                                                        int i1118 = fieldSerializer.features | this.features;
                                                        if (obj4 == null) {
                                                            if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                                serializeWriter.writeString("");
                                                            } else {
                                                                serializeWriter.writeString("");
                                                            }
                                                        } else {
                                                            str3 = (String) obj4;
                                                            if (z8) {
                                                                serializeWriter.writeStringWithSingleQuote(str3);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                            }
                                                        }
                                                    } else if (!fieldInfo.isEnum) {
                                                        fieldSerializer.writeValue(jSONSerializer, obj4);
                                                    } else if (obj4 != null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                            string = ((Enum) obj4).toString();
                                                            if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                z9 = true;
                                                            } else {
                                                                z9 = false;
                                                            }
                                                            if (z9) {
                                                                serializeWriter.writeStringWithSingleQuote(string);
                                                            } else {
                                                                serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                            }
                                                        } else {
                                                            serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                        }
                                                    } else {
                                                        serializeWriter.writeNull();
                                                    }
                                                } else {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                }
                                            } else if (!z18) {
                                                if (cls == String.class) {
                                                    int i1119 = fieldSerializer.features | this.features;
                                                    if (obj4 == null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                            serializeWriter.writeString("");
                                                        } else {
                                                            serializeWriter.writeString("");
                                                        }
                                                    } else {
                                                        str3 = (String) obj4;
                                                        if (z8) {
                                                            serializeWriter.writeStringWithSingleQuote(str3);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                        }
                                                    }
                                                } else if (!fieldInfo.isEnum) {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                } else if (obj4 != null) {
                                                    if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                        string = ((Enum) obj4).toString();
                                                        if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                            z9 = true;
                                                        } else {
                                                            z9 = false;
                                                        }
                                                        if (z9) {
                                                            serializeWriter.writeStringWithSingleQuote(string);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                        }
                                                    } else {
                                                        serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                    }
                                                } else {
                                                    serializeWriter.writeNull();
                                                }
                                            } else {
                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                            }
                                        }
                                        z17 = true;
                                    }
                                    serialContext2 = serialContext2;
                                    z17 = true;
                                }
                            } else {
                                z5 = z2;
                                c = c3;
                            }
                            z6 = true;
                            if (!z6) {
                                if (list6 != null) {
                                    if (z5) {
                                        objValueOf2 = objValueOf;
                                    } else {
                                        objValueOf2 = objValueOf;
                                    }
                                    it2 = list6.iterator();
                                    strProcess = str5;
                                    while (it2.hasNext()) {
                                        strProcess = it2.next().process(obj, strProcess, objValueOf2);
                                        z15 = z15;
                                    }
                                    objValueOf = objValueOf2;
                                } else {
                                    strProcess = str5;
                                }
                                z7 = z15;
                                if (list7 != null) {
                                    if (z5) {
                                        if (cls == Integer.TYPE) {
                                            objValueOf = Integer.valueOf(i2);
                                        } else if (cls == Long.TYPE) {
                                            objValueOf = Long.valueOf(j);
                                        } else if (cls == Boolean.TYPE) {
                                            objValueOf = Boolean.valueOf(z3);
                                        }
                                        z4 = true;
                                    }
                                    it = list7.iterator();
                                    objProcess = objValueOf;
                                    while (it.hasNext()) {
                                        Iterator<ValueFilter> it18 = it;
                                        objProcess = it.next().process(obj, str5, objProcess);
                                        it = it18;
                                    }
                                    obj3 = objValueOf;
                                    objValueOf = objProcess;
                                } else {
                                    obj3 = objValueOf;
                                }
                                if (z4) {
                                    z8 = z14;
                                } else {
                                    z8 = z14;
                                }
                                obj4 = objValueOf;
                                if (z4) {
                                    list = list2;
                                } else {
                                    list = list2;
                                }
                                if (z17) {
                                    i6 = serializeWriter.count + 1;
                                    if (i6 > serializeWriter.buf.length) {
                                        if (serializeWriter.writer == null) {
                                            serializeWriter.expandCapacity(i6);
                                        } else {
                                            serializeWriter.flush();
                                            i6 = 1;
                                        }
                                    }
                                    serializeWriter.buf[serializeWriter.count] = ',';
                                    serializeWriter.count = i6;
                                    if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                        jSONSerializer.println();
                                    }
                                }
                                if (strProcess != str5) {
                                    if (!z18) {
                                        serializeWriter.writeFieldName(strProcess, true);
                                    }
                                    jSONSerializer.write(obj4);
                                } else {
                                    if (obj3 != obj4) {
                                        if (!z18) {
                                            fieldSerializer.writePrefix(jSONSerializer);
                                        }
                                        jSONSerializer.write(obj4);
                                    } else {
                                        if (z18) {
                                            if (z19) {
                                                cArr = fieldSerializer.name_chars;
                                                length = cArr.length;
                                                i3 = serializeWriter.count + length;
                                                if (i3 <= serializeWriter.buf.length) {
                                                    i4 = 0;
                                                } else if (serializeWriter.writer == null) {
                                                    serializeWriter.expandCapacity(i3);
                                                    i4 = 0;
                                                } else {
                                                    i5 = 0;
                                                    do {
                                                        int length13 = serializeWriter.buf.length - serializeWriter.count;
                                                        System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length13);
                                                        serializeWriter.count = serializeWriter.buf.length;
                                                        serializeWriter.flush();
                                                        length -= length13;
                                                        i5 += length13;
                                                    } while (length > serializeWriter.buf.length);
                                                    i4 = i5;
                                                    i3 = length;
                                                }
                                                System.arraycopy(cArr, i4, serializeWriter.buf, serializeWriter.count, length);
                                                serializeWriter.count = i3;
                                            } else {
                                                fieldSerializer.writePrefix(jSONSerializer);
                                            }
                                        }
                                        if (z5) {
                                            if (!z18) {
                                                if (cls == String.class) {
                                                    int i11110 = fieldSerializer.features | this.features;
                                                    if (obj4 == null) {
                                                        if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                            serializeWriter.writeString("");
                                                        } else {
                                                            serializeWriter.writeString("");
                                                        }
                                                    } else {
                                                        str3 = (String) obj4;
                                                        if (z8) {
                                                            serializeWriter.writeStringWithSingleQuote(str3);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                        }
                                                    }
                                                } else if (!fieldInfo.isEnum) {
                                                    fieldSerializer.writeValue(jSONSerializer, obj4);
                                                } else if (obj4 != null) {
                                                    if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                        string = ((Enum) obj4).toString();
                                                        if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                            z9 = true;
                                                        } else {
                                                            z9 = false;
                                                        }
                                                        if (z9) {
                                                            serializeWriter.writeStringWithSingleQuote(string);
                                                        } else {
                                                            serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                        }
                                                    } else {
                                                        serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                    }
                                                } else {
                                                    serializeWriter.writeNull();
                                                }
                                            } else {
                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                            }
                                        } else if (!z18) {
                                            if (cls == String.class) {
                                                int i11111 = fieldSerializer.features | this.features;
                                                if (obj4 == null) {
                                                    if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0) {
                                                        serializeWriter.writeString("");
                                                    } else {
                                                        serializeWriter.writeString("");
                                                    }
                                                } else {
                                                    str3 = (String) obj4;
                                                    if (z8) {
                                                        serializeWriter.writeStringWithSingleQuote(str3);
                                                    } else {
                                                        serializeWriter.writeStringWithDoubleQuote(str3, (char) 0, true);
                                                    }
                                                }
                                            } else if (!fieldInfo.isEnum) {
                                                fieldSerializer.writeValue(jSONSerializer, obj4);
                                            } else if (obj4 != null) {
                                                if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                    string = ((Enum) obj4).toString();
                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                        z9 = true;
                                                    } else {
                                                        z9 = false;
                                                    }
                                                    if (z9) {
                                                        serializeWriter.writeStringWithSingleQuote(string);
                                                    } else {
                                                        serializeWriter.writeStringWithDoubleQuote(string, (char) 0, false);
                                                    }
                                                } else {
                                                    serializeWriter.writeInt(((Enum) obj4).ordinal());
                                                }
                                            } else {
                                                serializeWriter.writeNull();
                                            }
                                        } else {
                                            fieldSerializer.writeValue(jSONSerializer, obj4);
                                        }
                                    }
                                    z17 = true;
                                }
                                serialContext2 = serialContext2;
                                z17 = true;
                            }
                        } else {
                            c = c3;
                        }
                        z7 = z15;
                        z8 = z14;
                        list = list2;
                        serialContext2 = serialContext2;
                    } else {
                        c = c3;
                        z7 = z15;
                        z8 = z14;
                        list = list2;
                        serialContext2 = serialContext2;
                    }
                    try {
                        i8 = i9 + 1;
                        list3 = list6;
                        list4 = list7;
                        z13 = z19;
                        list2 = list;
                        z14 = z8;
                        c3 = c;
                        z15 = z7;
                        serialContext2 = serialContext2;
                        r17 = 0;
                    } catch (Exception e) {
                        e = e;
                        serialContext = serialContext2;
                        str = "write javaBean error, fastjson version 1.1.71";
                        if (obj2 != null) {
                            try {
                                str = "write javaBean error, fastjson version 1.1.71, fieldName : " + obj2;
                            } catch (Throwable th) {
                                th = th;
                                jSONSerializer.context = serialContext;
                                throw th;
                            }
                        }
                        throw new JSONException(str, e);
                    } catch (Throwable th2) {
                        th = th2;
                        serialContext = serialContext2;
                        jSONSerializer.context = serialContext;
                        throw th;
                    }
                } catch (Exception e2) {
                    e = e2;
                    serialContext2 = serialContext2;
                    serialContext = serialContext2;
                    str = "write javaBean error, fastjson version 1.1.71";
                    if (obj2 != null) {
                        str = "write javaBean error, fastjson version 1.1.71, fieldName : " + obj2;
                    }
                    throw new JSONException(str, e);
                } catch (Throwable th3) {
                    th = th3;
                    serialContext2 = serialContext2;
                    serialContext = serialContext2;
                    jSONSerializer.context = serialContext;
                    throw th;
                }
            }
            serialContext2 = serialContext2;
            char c4 = c3;
            if (jSONSerializer.afterFilters != null) {
                char cWriteAfter = z17 ? ',' : (char) 0;
                Iterator<AfterFilter> it19 = jSONSerializer.afterFilters.iterator();
                while (it19.hasNext()) {
                    cWriteAfter = it19.next().writeAfter(jSONSerializer, obj, cWriteAfter);
                }
            }
            if (fieldSerializerArr.length > 0 && (serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                jSONSerializer.decrementIdent();
                jSONSerializer.println();
            }
            int i24 = serializeWriter.count + 1;
            if (i24 <= serializeWriter.buf.length) {
                i = i24;
            } else if (serializeWriter.writer == null) {
                serializeWriter.expandCapacity(i24);
                i = i24;
            } else {
                serializeWriter.flush();
                i = 1;
            }
            serializeWriter.buf[serializeWriter.count] = c4;
            serializeWriter.count = i;
            jSONSerializer.context = serialContext2;
        } catch (Exception e3) {
            e = e3;
            serialContext = serialContext2;
        } catch (Throwable th4) {
            th = th4;
            serialContext = serialContext2;
        }
    }

    public Map<String, Object> getFieldValuesMap(Object obj) throws Exception {
        LinkedHashMap linkedHashMap = new LinkedHashMap(this.sortedGetters.length);
        for (FieldSerializer fieldSerializer : this.sortedGetters) {
            linkedHashMap.put(fieldSerializer.fieldInfo.name, fieldSerializer.getPropertyValue(obj));
        }
        return linkedHashMap;
    }
}
