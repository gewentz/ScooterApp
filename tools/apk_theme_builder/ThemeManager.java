package org.cocos2dx.cpp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class ThemeManager {
    private static final String TAG = "ThemeManager";
    private static final String PREF_NAME = "scooter_theme_prefs";
    private static final String KEY_CURRENT_THEME = "current_theme";

    private static final String[] THEME_KEYS = {"default", "minimal", "sport", "cyber"};
    private static final String[] THEME_NAMES = {
        "📱 Padrão Original (Visual de Fábrica)",
        "🌑 Minimalista Escuro (Dark Slate & Emerald)",
        "🏎️ Sport Racing (Carbon Fiber & Neon Red)",
        "⚡ Cyberpunk Neon (Electric Cyan & Magenta)"
    };

    private static View sFabView = null;

    public static void init(final Activity activity) {
        if (activity == null) return;
        try {
            // Ensure theme.txt exists on disk
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String curTheme = prefs.getString(KEY_CURRENT_THEME, "default");
            writeThemeFile(activity, curTheme);

            // Add floating theme selector button
            activity.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        addFloatingThemeButton(activity);
                    } catch (Throwable t) {
                        Log.e(TAG, "Error adding FAB", t);
                    }
                }
            }, 800);
        } catch (Throwable t) {
            Log.e(TAG, "Error in ThemeManager.init", t);
        }
    }

    private static void writeThemeFile(Context context, String themeKey) {
        try {
            File filesDir = context.getFilesDir();
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
            File themeFile = new File(filesDir, "theme.txt");
            FileOutputStream fos = new FileOutputStream(themeFile);
            fos.write(themeKey.getBytes("UTF-8"));
            fos.write('\n');
            fos.flush();
            fos.close();
            Log.i(TAG, "Written theme '" + themeKey + "' to " + themeFile.getAbsolutePath());
        } catch (Throwable t) {
            Log.e(TAG, "Failed writing theme file", t);
        }
    }

    private static void addFloatingThemeButton(final Activity activity) {
        if (activity.isFinishing()) return;
        if (sFabView != null) {
            try {
                activity.getWindowManager().removeView(sFabView);
            } catch (Throwable ignored) {}
            sFabView = null;
        }

        TextView fab = new TextView(activity);
        fab.setText("🎨 ESTILO");
        fab.setTextColor(Color.WHITE);
        fab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        fab.setTypeface(Typeface.DEFAULT_BOLD);
        fab.setGravity(Gravity.CENTER);

        int padH = dpToPx(activity, 12);
        int padV = dpToPx(activity, 7);
        fab.setPadding(padH, padV, padH, padV);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(activity, 18));
        bg.setColor(0xEE0B111D);
        bg.setStroke(dpToPx(activity, 2), 0xFF00E5FF);
        fab.setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setElevation(dpToPx(activity, 10));
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dpToPx(activity, 14);
        params.y = dpToPx(activity, 44);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog(activity);
            }
        });

        try {
            activity.getWindowManager().addView(fab, params);
            sFabView = fab;
            Log.i(TAG, "Theme FAB successfully added to WindowManager!");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to add FAB via WindowManager", t);
        }
    }

    private static void showThemeDialog(final Activity activity) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String currentTheme = prefs.getString(KEY_CURRENT_THEME, "default");

        int currentIdx = 0;
        for (int i = 0; i < THEME_KEYS.length; i++) {
            if (THEME_KEYS[i].equals(currentTheme)) {
                currentIdx = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("🎨 Escolha o Estilo do Painel");
        builder.setSingleChoiceItems(THEME_NAMES, currentIdx, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String chosen = THEME_KEYS[which];
                prefs.edit().putString(KEY_CURRENT_THEME, chosen).apply();
                writeThemeFile(activity, chosen);

                String displayName = (which == 0) ? "PADRÃO ORIGINAL" : chosen.toUpperCase();
                Toast.makeText(activity, "Estilo " + displayName + " aplicado! Reiniciando...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                restartApplication(activity);
            }
        });
        builder.setNegativeButton("Fechar", null);
        builder.show();
    }

    private static void restartApplication(final Activity activity) {
        try {
            if (sFabView != null) {
                activity.getWindowManager().removeView(sFabView);
                sFabView = null;
            }
            Context context = activity.getApplicationContext();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error starting restart intent", t);
        }
        activity.finish();
        activity.getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        }, 200);
    }

    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
}
