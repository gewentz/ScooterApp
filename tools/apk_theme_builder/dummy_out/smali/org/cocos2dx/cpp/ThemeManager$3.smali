.class Lorg/cocos2dx/cpp/ThemeManager$3;
.super Ljava/lang/Object;
.source "ThemeManager.java"

# interfaces
.implements Landroid/content/DialogInterface$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lorg/cocos2dx/cpp/ThemeManager;->showThemeDialog(Landroid/app/Activity;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$activity:Landroid/app/Activity;

.field final synthetic val$prefs:Landroid/content/SharedPreferences;


# direct methods
.method constructor <init>(Landroid/content/SharedPreferences;Landroid/app/Activity;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 155
    iput-object p1, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$prefs:Landroid/content/SharedPreferences;

    iput-object p2, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$activity:Landroid/app/Activity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/content/DialogInterface;I)V
    .locals 3

    .line 158
    invoke-static {}, Lorg/cocos2dx/cpp/ThemeManager;->access$200()[Ljava/lang/String;

    move-result-object v0

    aget-object v0, v0, p2

    .line 159
    iget-object v1, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$prefs:Landroid/content/SharedPreferences;

    invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v1

    const-string v2, "current_theme"

    invoke-interface {v1, v2, v0}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;

    move-result-object v1

    invoke-interface {v1}, Landroid/content/SharedPreferences$Editor;->apply()V

    .line 160
    iget-object v1, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$activity:Landroid/app/Activity;

    invoke-static {v1, v0}, Lorg/cocos2dx/cpp/ThemeManager;->access$300(Landroid/content/Context;Ljava/lang/String;)V

    .line 162
    if-nez p2, :cond_0

    const-string p2, "PADR\u00c3O ORIGINAL"

    goto :goto_0

    :cond_0
    invoke-virtual {v0}, Ljava/lang/String;->toUpperCase()Ljava/lang/String;

    move-result-object p2

    .line 163
    :goto_0
    iget-object v0, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$activity:Landroid/app/Activity;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Estilo "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p2

    const-string v1, " aplicado! Reiniciando..."

    invoke-virtual {p2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p2

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    const/4 v1, 0x0

    invoke-static {v0, p2, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p2

    invoke-virtual {p2}, Landroid/widget/Toast;->show()V

    .line 164
    invoke-interface {p1}, Landroid/content/DialogInterface;->dismiss()V

    .line 166
    iget-object p1, p0, Lorg/cocos2dx/cpp/ThemeManager$3;->val$activity:Landroid/app/Activity;

    invoke-static {p1}, Lorg/cocos2dx/cpp/ThemeManager;->access$400(Landroid/app/Activity;)V

    .line 167
    return-void
.end method
