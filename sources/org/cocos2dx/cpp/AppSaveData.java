package org.cocos2dx.cpp;

import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes.dex */
public class AppSaveData {
    private static AppSaveData _instance;
    SharedPreferences.Editor _editor;
    private SharedPreferences _sp;

    public static AppSaveData getInstance() {
        if (_instance == null) {
            _instance = new AppSaveData();
        }
        return _instance;
    }

    public AppSaveData() {
        SharedPreferences sharedPreferences = AppActivity.getInstance().getApplicationContext().getSharedPreferences("DATABASE", 0);
        this._sp = sharedPreferences;
        this._editor = sharedPreferences.edit();
    }

    public void setDataForBoolean(String str, boolean z) {
        this._editor.putBoolean(str, z).commit();
    }

    public boolean getDataForBoolean(String str, boolean z) {
        return this._sp.getBoolean(str, z);
    }

    public void setDataForString(String str, String str2) {
        this._editor.putString(str, str2).commit();
    }

    public String getDataForString(String str, String str2) {
        return this._sp.getString(str, str2);
    }

    public void clearAllData() {
        this._editor.clear().commit();
    }
}
