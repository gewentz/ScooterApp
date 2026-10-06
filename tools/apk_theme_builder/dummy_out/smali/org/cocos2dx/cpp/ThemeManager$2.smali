.class Lorg/cocos2dx/cpp/ThemeManager$2;
.super Ljava/lang/Object;
.source "ThemeManager.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lorg/cocos2dx/cpp/ThemeManager;->addFloatingThemeButton(Landroid/app/Activity;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$activity:Landroid/app/Activity;


# direct methods
.method constructor <init>(Landroid/app/Activity;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 125
    iput-object p1, p0, Lorg/cocos2dx/cpp/ThemeManager$2;->val$activity:Landroid/app/Activity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 0

    .line 128
    iget-object p1, p0, Lorg/cocos2dx/cpp/ThemeManager$2;->val$activity:Landroid/app/Activity;

    invoke-static {p1}, Lorg/cocos2dx/cpp/ThemeManager;->access$100(Landroid/app/Activity;)V

    .line 129
    return-void
.end method
