package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class ThrowableDeserializer extends JavaBeanDeserializer {
    public ThrowableDeserializer(ParserConfig parserConfig, Class<?> cls) {
        super(parserConfig, cls, cls);
    }

    /* JADX WARN: Code duplicated, block: B:17:0x0035  */
    /* JADX WARN: Code duplicated, block: B:88:0x015f A[Catch: Exception -> 0x01ab, TRY_LEAVE, TryCatch #0 {Exception -> 0x01ab, blocks: (B:59:0x00e0, B:62:0x00ec, B:77:0x012c, B:65:0x00fa, B:67:0x0101, B:70:0x010e, B:72:0x0116, B:74:0x0120, B:80:0x0135, B:88:0x015f, B:82:0x0145, B:85:0x0153), top: B:113:0x00e0 }] */
    @Override // com.alibaba.fastjson.parser.JavaBeanDeserializer, com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        Class<?> clsLoadClass;
        Object obj2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        JavaBeanDeserializer javaBeanDeserializer = null;
        if (jSONLexer.token == 8) {
            jSONLexer.nextToken();
            return null;
        }
        boolean z = false;
        if (defaultJSONParser.resolveStatus == 2) {
            defaultJSONParser.resolveStatus = 0;
        } else if (jSONLexer.token != 12) {
            throw new JSONException("syntax error");
        }
        if (type == null || !(type instanceof Class)) {
            clsLoadClass = null;
        } else {
            clsLoadClass = (Class) type;
            if (!Throwable.class.isAssignableFrom(clsLoadClass)) {
                clsLoadClass = null;
            }
        }
        HashMap map = null;
        Throwable th = null;
        String strStringVal = null;
        StackTraceElement[] stackTraceElementArr = null;
        while (true) {
            String strScanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
            if (strScanSymbol == null) {
                if (jSONLexer.token == 13) {
                    jSONLexer.nextToken(16);
                    break;
                }
                if (jSONLexer.token == 16) {
                }
            }
            jSONLexer.nextTokenWithChar(':');
            if (JSON.DEFAULT_TYPE_KEY.equals(strScanSymbol)) {
                if (jSONLexer.token == 4) {
                    clsLoadClass = TypeUtils.loadClass(jSONLexer.stringVal(), defaultJSONParser.config.defaultClassLoader, z);
                    jSONLexer.nextToken(16);
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("message".equals(strScanSymbol)) {
                if (jSONLexer.token == 8) {
                    strStringVal = null;
                } else if (jSONLexer.token == 4) {
                    strStringVal = jSONLexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
                jSONLexer.nextToken();
            } else if ("cause".equals(strScanSymbol)) {
                th = (Throwable) deserialze(defaultJSONParser, null, "cause");
            } else if ("stackTrace".equals(strScanSymbol)) {
                stackTraceElementArr = (StackTraceElement[]) defaultJSONParser.parseObject((Class) StackTraceElement[].class);
            } else {
                if (map == null) {
                    map = new HashMap();
                }
                map.put(strScanSymbol, defaultJSONParser.parse());
            }
            if (jSONLexer.token == 13) {
                jSONLexer.nextToken(16);
                break;
            }
            z = false;
        }
        if (clsLoadClass == null) {
            obj2 = (T) new Exception(strStringVal, th);
        } else {
            try {
                Constructor<?> constructor = null;
                Constructor<?> constructor2 = null;
                Constructor<?> constructor3 = null;
                for (Constructor<?> constructor4 : clsLoadClass.getConstructors()) {
                    if (constructor4.getParameterTypes().length == 0) {
                        constructor3 = constructor4;
                    } else if (constructor4.getParameterTypes().length == 1 && constructor4.getParameterTypes()[0] == String.class) {
                        constructor2 = constructor4;
                    } else if (constructor4.getParameterTypes().length == 2 && constructor4.getParameterTypes()[0] == String.class && constructor4.getParameterTypes()[1] == Throwable.class) {
                        constructor = constructor4;
                    }
                }
                if (constructor != null) {
                    obj2 = (T) ((Throwable) constructor.newInstance(strStringVal, th));
                } else {
                    if (constructor2 != null) {
                        obj2 = (Throwable) constructor2.newInstance(strStringVal);
                    } else if (constructor3 != null) {
                        javaBeanDeserializer = null;
                        obj2 = (Throwable) constructor3.newInstance(null);
                    } else {
                        javaBeanDeserializer = null;
                        obj2 = null;
                    }
                    if (obj2 == null) {
                        obj2 = (T) new Exception(strStringVal, th);
                    }
                }
                javaBeanDeserializer = null;
                if (obj2 == null) {
                    obj2 = (T) new Exception(strStringVal, th);
                }
            } catch (Exception e) {
                throw new JSONException("create instance error", e);
            }
        }
        if (stackTraceElementArr != null) {
            ((Throwable) obj2).setStackTrace(stackTraceElementArr);
        }
        if (map != null) {
            if (clsLoadClass != null) {
                if (clsLoadClass == this.clazz) {
                    javaBeanDeserializer = this;
                } else {
                    ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(clsLoadClass);
                    if (deserializer instanceof JavaBeanDeserializer) {
                        javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
                    }
                }
            }
            if (javaBeanDeserializer != null) {
                for (Map.Entry entry : map.entrySet()) {
                    String str = (String) entry.getKey();
                    Object value = entry.getValue();
                    FieldDeserializer fieldDeserializer = javaBeanDeserializer.getFieldDeserializer(str);
                    if (fieldDeserializer != null) {
                        fieldDeserializer.setValue(obj2, value);
                    }
                }
            }
        }
        return (T) obj2;
    }
}
