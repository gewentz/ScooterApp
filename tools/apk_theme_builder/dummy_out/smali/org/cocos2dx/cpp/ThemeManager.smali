.class public Lorg/cocos2dx/cpp/ThemeManager;
.super Ljava/lang/Object;
.source "ThemeManager.java"


# static fields
.field private static final KEY_CURRENT_THEME:Ljava/lang/String; = "current_theme"

.field private static final PREF_NAME:Ljava/lang/String; = "scooter_theme_prefs"

.field private static final TAG:Ljava/lang/String; = "ThemeManager"

.field private static final THEME_KEYS:[Ljava/lang/String;

.field private static final THEME_NAMES:[Ljava/lang/String;

.field private static sFabView:Landroid/view/View;


# direct methods
.method static constructor <clinit>()V
    .locals 7

    .line 31
    const/4 v0, 0x4

    new-array v1, v0, [Ljava/lang/String;

    const/4 v2, 0x0

    const-string v3, "default"

    aput-object v3, v1, v2

    const/4 v3, 0x1

    const-string v4, "minimal"

    aput-object v4, v1, v3

    const/4 v4, 0x2

    const-string v5, "sport"

    aput-object v5, v1, v4

    const/4 v5, 0x3

    const-string v6, "cyber"

    aput-object v6, v1, v5

    sput-object v1, Lorg/cocos2dx/cpp/ThemeManager;->THEME_KEYS:[Ljava/lang/String;

    .line 32
    new-array v0, v0, [Ljava/lang/String;

    const-string v1, "\ud83d\udcf1 Padr\u00e3o Original (Visual de F\u00e1brica)"

    aput-object v1, v0, v2

    const-string v1, "\ud83c\udf11 Minimalista Escuro (Dark Slate & Emerald)"

    aput-object v1, v0, v3

    const-string v1, "\ud83c\udfce\ufe0f Sport Racing (Carbon Fiber & Neon Red)"

    aput-object v1, v0, v4

    const-string v1, "\u26a1 Cyberpunk Neon (Electric Cyan & Magenta)"

    aput-object v1, v0, v5

    sput-object v0, Lorg/cocos2dx/cpp/ThemeManager;->THEME_NAMES:[Ljava/lang/String;

    .line 39
    const/4 v0, 0x0

    sput-object v0, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 26
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method static synthetic access$000(Landroid/app/Activity;)V
    .locals 0

    .line 26
    invoke-static {p0}, Lorg/cocos2dx/cpp/ThemeManager;->addFloatingThemeButton(Landroid/app/Activity;)V

    return-void
.end method

.method static synthetic access$100(Landroid/app/Activity;)V
    .locals 0

    .line 26
    invoke-static {p0}, Lorg/cocos2dx/cpp/ThemeManager;->showThemeDialog(Landroid/app/Activity;)V

    return-void
.end method

.method static synthetic access$200()[Ljava/lang/String;
    .locals 1

    .line 26
    sget-object v0, Lorg/cocos2dx/cpp/ThemeManager;->THEME_KEYS:[Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$300(Landroid/content/Context;Ljava/lang/String;)V
    .locals 0

    .line 26
    invoke-static {p0, p1}, Lorg/cocos2dx/cpp/ThemeManager;->writeThemeFile(Landroid/content/Context;Ljava/lang/String;)V

    return-void
.end method

.method static synthetic access$400(Landroid/app/Activity;)V
    .locals 0

    .line 26
    invoke-static {p0}, Lorg/cocos2dx/cpp/ThemeManager;->restartApplication(Landroid/app/Activity;)V

    return-void
.end method

.method private static addFloatingThemeButton(Landroid/app/Activity;)V
    .locals 9

    .line 84
    const-string v0, "ThemeManager"

    invoke-virtual {p0}, Landroid/app/Activity;->isFinishing()Z

    move-result v1

    if-eqz v1, :cond_0

    return-void

    .line 85
    :cond_0
    sget-object v1, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    if-eqz v1, :cond_1

    .line 87
    :try_start_0
    invoke-virtual {p0}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object v1

    sget-object v2, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    invoke-interface {v1, v2}, Landroid/view/WindowManager;->removeView(Landroid/view/View;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    .line 88
    :catchall_0
    move-exception v1

    :goto_0
    nop

    .line 89
    const/4 v1, 0x0

    sput-object v1, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    .line 92
    :cond_1
    new-instance v1, Landroid/widget/TextView;

    invoke-direct {v1, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 93
    const-string v2, "\ud83c\udfa8 ESTILO"

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 94
    const/4 v2, -0x1

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setTextColor(I)V

    .line 95
    const/high16 v2, 0x41400000    # 12.0f

    const/4 v3, 0x2

    invoke-virtual {v1, v3, v2}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 96
    sget-object v2, Landroid/graphics/Typeface;->DEFAULT_BOLD:Landroid/graphics/Typeface;

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 97
    const/16 v2, 0x11

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setGravity(I)V

    .line 99
    const/16 v2, 0xc

    invoke-static {p0, v2}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v2

    .line 100
    const/4 v4, 0x7

    invoke-static {p0, v4}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v4

    .line 101
    invoke-virtual {v1, v2, v4, v2, v4}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 103
    new-instance v2, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v2}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 104
    const/4 v4, 0x0

    invoke-virtual {v2, v4}, Landroid/graphics/drawable/GradientDrawable;->setShape(I)V

    .line 105
    const/16 v4, 0x12

    invoke-static {p0, v4}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v4

    int-to-float v4, v4

    invoke-virtual {v2, v4}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 106
    const v4, -0x11f4eee3

    invoke-virtual {v2, v4}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 107
    invoke-static {p0, v3}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v3

    const v4, -0xff1a01

    invoke-virtual {v2, v3, v4}, Landroid/graphics/drawable/GradientDrawable;->setStroke(II)V

    .line 108
    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 110
    sget v2, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v3, 0x15

    if-lt v2, v3, :cond_2

    .line 111
    const/16 v2, 0xa

    invoke-static {p0, v2}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v2

    int-to-float v2, v2

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setElevation(F)V

    .line 114
    :cond_2
    new-instance v2, Landroid/view/WindowManager$LayoutParams;

    const/16 v7, 0x108

    const/4 v8, -0x3

    const/4 v4, -0x2

    const/4 v5, -0x2

    const/4 v6, 0x2

    move-object v3, v2

    invoke-direct/range {v3 .. v8}, Landroid/view/WindowManager$LayoutParams;-><init>(IIIII)V

    .line 121
    const v3, 0x800035

    iput v3, v2, Landroid/view/WindowManager$LayoutParams;->gravity:I

    .line 122
    const/16 v3, 0xe

    invoke-static {p0, v3}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v3

    iput v3, v2, Landroid/view/WindowManager$LayoutParams;->x:I

    .line 123
    const/16 v3, 0x2c

    invoke-static {p0, v3}, Lorg/cocos2dx/cpp/ThemeManager;->dpToPx(Landroid/content/Context;I)I

    move-result v3

    iput v3, v2, Landroid/view/WindowManager$LayoutParams;->y:I

    .line 125
    new-instance v3, Lorg/cocos2dx/cpp/ThemeManager$2;

    invoke-direct {v3, p0}, Lorg/cocos2dx/cpp/ThemeManager$2;-><init>(Landroid/app/Activity;)V

    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 133
    :try_start_1
    invoke-virtual {p0}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object p0

    invoke-interface {p0, v1, v2}, Landroid/view/WindowManager;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 134
    sput-object v1, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    .line 135
    const-string p0, "Theme FAB successfully added to WindowManager!"

    invoke-static {v0, p0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 138
    goto :goto_1

    .line 136
    :catchall_1
    move-exception p0

    .line 137
    const-string v1, "Failed to add FAB via WindowManager"

    invoke-static {v0, v1, p0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 139
    :goto_1
    return-void
.end method

.method private static dpToPx(Landroid/content/Context;I)I
    .locals 1

    .line 199
    int-to-float p1, p1

    .line 202
    invoke-virtual {p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object p0

    invoke-virtual {p0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object p0

    .line 199
    const/4 v0, 0x1

    invoke-static {v0, p1, p0}, Landroid/util/TypedValue;->applyDimension(IFLandroid/util/DisplayMetrics;)F

    move-result p0

    float-to-int p0, p0

    return p0
.end method

.method public static init(Landroid/app/Activity;)V
    .locals 4

    .line 42
    if-nez p0, :cond_0

    return-void

    .line 45
    :cond_0
    :try_start_0
    const-string v0, "scooter_theme_prefs"

    const/4 v1, 0x0

    invoke-virtual {p0, v0, v1}, Landroid/app/Activity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object v0

    .line 46
    const-string v1, "current_theme"

    const-string v2, "default"

    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 47
    invoke-static {p0, v0}, Lorg/cocos2dx/cpp/ThemeManager;->writeThemeFile(Landroid/content/Context;Ljava/lang/String;)V

    .line 50
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object v0

    invoke-virtual {v0}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    move-result-object v0

    new-instance v1, Lorg/cocos2dx/cpp/ThemeManager$1;

    invoke-direct {v1, p0}, Lorg/cocos2dx/cpp/ThemeManager$1;-><init>(Landroid/app/Activity;)V

    const-wide/16 v2, 0x320

    invoke-virtual {v0, v1, v2, v3}, Landroid/view/View;->postDelayed(Ljava/lang/Runnable;J)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 62
    goto :goto_0

    .line 60
    :catchall_0
    move-exception p0

    .line 61
    const-string v0, "ThemeManager"

    const-string v1, "Error in ThemeManager.init"

    invoke-static {v0, v1, p0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 63
    :goto_0
    return-void
.end method

.method private static restartApplication(Landroid/app/Activity;)V
    .locals 3

    .line 175
    :try_start_0
    sget-object v0, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    if-eqz v0, :cond_0

    .line 176
    invoke-virtual {p0}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object v0

    sget-object v1, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    invoke-interface {v0, v1}, Landroid/view/WindowManager;->removeView(Landroid/view/View;)V

    .line 177
    const/4 v0, 0x0

    sput-object v0, Lorg/cocos2dx/cpp/ThemeManager;->sFabView:Landroid/view/View;

    .line 179
    :cond_0
    invoke-virtual {p0}, Landroid/app/Activity;->getApplicationContext()Landroid/content/Context;

    move-result-object v0

    .line 180
    invoke-virtual {v0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v1

    invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Landroid/content/pm/PackageManager;->getLaunchIntentForPackage(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    .line 181
    if-eqz v1, :cond_1

    .line 182
    const v2, 0x14008000

    invoke-virtual {v1, v2}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    .line 183
    invoke-virtual {v0, v1}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 187
    :cond_1
    goto :goto_0

    .line 185
    :catchall_0
    move-exception v0

    .line 186
    const-string v1, "ThemeManager"

    const-string v2, "Error starting restart intent"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 188
    :goto_0
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V

    .line 189
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    move-result-object p0

    invoke-virtual {p0}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    move-result-object p0

    new-instance v0, Lorg/cocos2dx/cpp/ThemeManager$4;

    invoke-direct {v0}, Lorg/cocos2dx/cpp/ThemeManager$4;-><init>()V

    const-wide/16 v1, 0xc8

    invoke-virtual {p0, v0, v1, v2}, Landroid/view/View;->postDelayed(Ljava/lang/Runnable;J)Z

    .line 196
    return-void
.end method

.method private static showThemeDialog(Landroid/app/Activity;)V
    .locals 5

    .line 142
    const-string v0, "scooter_theme_prefs"

    const/4 v1, 0x0

    invoke-virtual {p0, v0, v1}, Landroid/app/Activity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object v0

    .line 143
    const-string v2, "current_theme"

    const-string v3, "default"

    invoke-interface {v0, v2, v3}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v2

    .line 145
    nop

    .line 146
    const/4 v3, 0x0

    :goto_0
    sget-object v4, Lorg/cocos2dx/cpp/ThemeManager;->THEME_KEYS:[Ljava/lang/String;

    array-length v4, v4

    if-ge v3, v4, :cond_1

    .line 147
    sget-object v4, Lorg/cocos2dx/cpp/ThemeManager;->THEME_KEYS:[Ljava/lang/String;

    aget-object v4, v4, v3

    invoke-virtual {v4, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_0

    .line 148
    nop

    .line 149
    move v1, v3

    goto :goto_1

    .line 146
    :cond_0
    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 153
    :cond_1
    :goto_1
    new-instance v2, Landroid/app/AlertDialog$Builder;

    invoke-direct {v2, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    .line 154
    const-string v3, "\ud83c\udfa8 Escolha o Estilo do Painel"

    invoke-virtual {v2, v3}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    .line 155
    sget-object v3, Lorg/cocos2dx/cpp/ThemeManager;->THEME_NAMES:[Ljava/lang/String;

    new-instance v4, Lorg/cocos2dx/cpp/ThemeManager$3;

    invoke-direct {v4, v0, p0}, Lorg/cocos2dx/cpp/ThemeManager$3;-><init>(Landroid/content/SharedPreferences;Landroid/app/Activity;)V

    invoke-virtual {v2, v3, v1, v4}, Landroid/app/AlertDialog$Builder;->setSingleChoiceItems([Ljava/lang/CharSequence;ILandroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    .line 169
    const-string p0, "Fechar"

    const/4 v0, 0x0

    invoke-virtual {v2, p0, v0}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    .line 170
    invoke-virtual {v2}, Landroid/app/AlertDialog$Builder;->show()Landroid/app/AlertDialog;

    .line 171
    return-void
.end method

.method private static writeThemeFile(Landroid/content/Context;Ljava/lang/String;)V
    .locals 3

    .line 67
    const-string v0, "ThemeManager"

    :try_start_0
    invoke-virtual {p0}, Landroid/content/Context;->getFilesDir()Ljava/io/File;

    move-result-object p0

    .line 68
    invoke-virtual {p0}, Ljava/io/File;->exists()Z

    move-result v1

    if-nez v1, :cond_0

    .line 69
    invoke-virtual {p0}, Ljava/io/File;->mkdirs()Z

    .line 71
    :cond_0
    new-instance v1, Ljava/io/File;

    const-string v2, "theme.txt"

    invoke-direct {v1, p0, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 72
    new-instance p0, Ljava/io/FileOutputStream;

    invoke-direct {p0, v1}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    .line 73
    const-string v2, "UTF-8"

    invoke-virtual {p1, v2}, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B

    move-result-object v2

    invoke-virtual {p0, v2}, Ljava/io/FileOutputStream;->write([B)V

    .line 74
    const/16 v2, 0xa

    invoke-virtual {p0, v2}, Ljava/io/FileOutputStream;->write(I)V

    .line 75
    invoke-virtual {p0}, Ljava/io/FileOutputStream;->flush()V

    .line 76
    invoke-virtual {p0}, Ljava/io/FileOutputStream;->close()V

    .line 77
    new-instance p0, Ljava/lang/StringBuilder;

    invoke-direct {p0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Written theme \'"

    invoke-virtual {p0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    const-string p1, "\' to "

    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    invoke-virtual {v1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-static {v0, p0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 80
    goto :goto_0

    .line 78
    :catchall_0
    move-exception p0

    .line 79
    const-string p1, "Failed writing theme file"

    invoke-static {v0, p1, p0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 81
    :goto_0
    return-void
.end method
