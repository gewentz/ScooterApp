package com.alibaba.fastjson;

import cz.msebera.android.httpclient.message.TokenParser;

/* JADX INFO: loaded from: classes.dex */
public enum PropertyNamingStrategy {
    CamelCase,
    PascalCase,
    SnakeCase,
    KebabCase;

    /* JADX INFO: renamed from: com.alibaba.fastjson.PropertyNamingStrategy$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy;

        static {
            int[] iArr = new int[PropertyNamingStrategy.values().length];
            $SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy = iArr;
            try {
                iArr[PropertyNamingStrategy.SnakeCase.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.KebabCase.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.PascalCase.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy[PropertyNamingStrategy.CamelCase.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public String translate(String str) {
        char cCharAt;
        int i = AnonymousClass1.$SwitchMap$com$alibaba$fastjson$PropertyNamingStrategy[ordinal()];
        int i2 = 0;
        if (i == 1) {
            StringBuilder sb = new StringBuilder();
            while (i2 < str.length()) {
                char cCharAt2 = str.charAt(i2);
                if (cCharAt2 >= 'A' && cCharAt2 <= 'Z') {
                    char c = (char) (cCharAt2 + TokenParser.SP);
                    if (i2 > 0) {
                        sb.append('_');
                    }
                    sb.append(c);
                } else {
                    sb.append(cCharAt2);
                }
                i2++;
            }
            return sb.toString();
        }
        if (i == 2) {
            StringBuilder sb2 = new StringBuilder();
            while (i2 < str.length()) {
                char cCharAt3 = str.charAt(i2);
                if (cCharAt3 >= 'A' && cCharAt3 <= 'Z') {
                    char c2 = (char) (cCharAt3 + TokenParser.SP);
                    if (i2 > 0) {
                        sb2.append('-');
                    }
                    sb2.append(c2);
                } else {
                    sb2.append(cCharAt3);
                }
                i2++;
            }
            return sb2.toString();
        }
        if (i == 3) {
            char cCharAt4 = str.charAt(0);
            if (cCharAt4 >= 'a' && cCharAt4 <= 'z') {
                char[] charArray = str.toCharArray();
                charArray[0] = (char) (charArray[0] - ' ');
                return new String(charArray);
            }
        } else if (i == 4 && (cCharAt = str.charAt(0)) >= 'A' && cCharAt <= 'Z') {
            char[] charArray2 = str.toCharArray();
            charArray2[0] = (char) (charArray2[0] + TokenParser.SP);
            return new String(charArray2);
        }
        return str;
    }
}
