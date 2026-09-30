package android.support.v4.text;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.CursorWindow;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.hardware.fingerprint.FingerprintManager;
import android.os.LocaleList;
import android.text.PrecomputedText;
import java.io.FileDescriptor;
import java.security.Signature;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.Mac;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class HtmlCompat$$ExternalSyntheticApiModelOutline0 {
    public static /* synthetic */ ShortcutInfo.Builder m(Context context, String str) {
        return new ShortcutInfo.Builder(context, str);
    }

    public static /* bridge */ /* synthetic */ ShortcutManager m(Object obj) {
        return (ShortcutManager) obj;
    }

    public static /* synthetic */ CursorWindow m(String str, long j) {
        return new CursorWindow(str, j);
    }

    public static /* synthetic */ Typeface.Builder m(FileDescriptor fileDescriptor) {
        return new Typeface.Builder(fileDescriptor);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ Icon m30m(Object obj) {
        return (Icon) obj;
    }

    public static /* synthetic */ FingerprintManager.CryptoObject m(Signature signature) {
        return new FingerprintManager.CryptoObject(signature);
    }

    public static /* synthetic */ FingerprintManager.CryptoObject m(Cipher cipher) {
        return new FingerprintManager.CryptoObject(cipher);
    }

    public static /* synthetic */ FingerprintManager.CryptoObject m(Mac mac) {
        return new FingerprintManager.CryptoObject(mac);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ FingerprintManager m31m(Object obj) {
        return (FingerprintManager) obj;
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ LocaleList m33m(Object obj) {
        return (LocaleList) obj;
    }

    public static /* synthetic */ LocaleList m(Locale[] localeArr) {
        return new LocaleList(localeArr);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ PrecomputedText m34m(Object obj) {
        return (PrecomputedText) obj;
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ Class m36m() {
        return ShortcutManager.class;
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* synthetic */ void m41m() {
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ boolean m43m(Object obj) {
        return obj instanceof LocaleList;
    }

    /* JADX INFO: renamed from: m$1, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ Class m46m$1() {
        return FingerprintManager.class;
    }

    /* JADX INFO: renamed from: m$1, reason: collision with other method in class */
    public static /* synthetic */ void m48m$1() {
    }

    public static /* bridge */ /* synthetic */ boolean m$1(Object obj) {
        return obj instanceof PrecomputedText;
    }

    public static /* synthetic */ void m$2() {
    }

    public static /* synthetic */ void m$3() {
    }
}
