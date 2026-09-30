package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxTypefaces {
    private static final HashMap<String, Typeface> sTypefaceCache = new HashMap<>();

    public static synchronized Typeface get(Context context, String str) {
        HashMap<String, Typeface> map;
        Typeface typefaceCreateFromAsset;
        map = sTypefaceCache;
        if (!map.containsKey(str)) {
            if (str.startsWith("/")) {
                typefaceCreateFromAsset = Typeface.createFromFile(str);
            } else {
                typefaceCreateFromAsset = Typeface.createFromAsset(context.getAssets(), str);
            }
            map.put(str, typefaceCreateFromAsset);
        }
        return map.get(str);
    }
}
