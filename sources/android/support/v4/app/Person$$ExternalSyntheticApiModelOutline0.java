package android.support.v4.app;

import android.app.Notification;
import android.app.job.JobWorkItem;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SubscriptionManager;
import android.view.DragAndDropPermissions;
import android.view.inputmethod.InputContentInfo;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Person$$ExternalSyntheticApiModelOutline0 {
    public static /* synthetic */ Notification.Builder m(Context context, String str) {
        return new Notification.Builder(context, str);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* synthetic */ Notification.DecoratedCustomViewStyle m3m() {
        return new Notification.DecoratedCustomViewStyle();
    }

    public static /* synthetic */ Notification.MessagingStyle.Message m(CharSequence charSequence, long j, android.app.Person person) {
        return new Notification.MessagingStyle.Message(charSequence, j, person);
    }

    public static /* synthetic */ Notification.MessagingStyle.Message m(CharSequence charSequence, long j, CharSequence charSequence2) {
        return new Notification.MessagingStyle.Message(charSequence, j, charSequence2);
    }

    public static /* synthetic */ Notification.MessagingStyle m(android.app.Person person) {
        return new Notification.MessagingStyle(person);
    }

    public static /* synthetic */ Notification.MessagingStyle m(CharSequence charSequence) {
        return new Notification.MessagingStyle(charSequence);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* synthetic */ android.app.Person.Builder m4m() {
        return new android.app.Person.Builder();
    }

    public static /* bridge */ /* synthetic */ android.app.Person m(Object obj) {
        return (android.app.Person) obj;
    }

    public static /* synthetic */ JobWorkItem m(Intent intent) {
        return new JobWorkItem(intent);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ DragAndDropPermissions m8m(Object obj) {
        return (DragAndDropPermissions) obj;
    }

    public static /* synthetic */ InputContentInfo m(Uri uri, ClipDescription clipDescription, Uri uri2) {
        return new InputContentInfo(uri, clipDescription, uri2);
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ InputContentInfo m9m(Object obj) {
        return (InputContentInfo) obj;
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* bridge */ /* synthetic */ Class m12m() {
        return SubscriptionManager.class;
    }

    /* JADX INFO: renamed from: m, reason: collision with other method in class */
    public static /* synthetic */ void m16m() {
    }

    public static /* synthetic */ void m$1() {
    }

    public static /* synthetic */ void m$2() {
    }
}
