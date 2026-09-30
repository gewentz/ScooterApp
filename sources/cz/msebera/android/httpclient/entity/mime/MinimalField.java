package cz.msebera.android.httpclient.entity.mime;

/* JADX INFO: loaded from: classes.dex */
public class MinimalField {
    private final String name;
    private final String value;

    public MinimalField(String str, String str2) {
        this.name = str;
        this.value = str2;
    }

    public String getName() {
        return this.name;
    }

    public String getBody() {
        return this.value;
    }

    public String toString() {
        return this.name + ": " + this.value;
    }
}
