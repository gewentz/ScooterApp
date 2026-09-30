package org.cocos2dx.lib;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import org.cocos2dx.enginedata.EngineDataManager;
import org.cocos2dx.enginedata.IEngineDataManager;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxEngineDataManager {
    private static final String TAG = "CCEngineDataManager";
    private static EngineDataManager sManager = new EngineDataManager();
    private static boolean sIsEnabled = true;
    private static boolean sIsInited = false;

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnChangeContinuousFrameLostConfig(int i, int i2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnChangeExpectedFps(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnChangeLowFpsConfig(int i, float f);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnChangeMuteEnabled(boolean z);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnChangeSpecialEffectLevel(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnQueryFps(int[] iArr, int[] iArr2);

    private static native void nativeSetSupportOptimization(boolean z);

    private Cocos2dxEngineDataManager() {
    }

    public static void disable() {
        sIsEnabled = false;
    }

    public static boolean isInited() {
        return sIsInited;
    }

    public static boolean init(Context context, final GLSurfaceView gLSurfaceView) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }
        if (gLSurfaceView == null) {
            Log.e(TAG, "glSurfaceView is null");
            return false;
        }
        IEngineDataManager.OnSystemCommandListener onSystemCommandListener = new IEngineDataManager.OnSystemCommandListener() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1
            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onQueryFps(int[] iArr, int[] iArr2) {
                Cocos2dxEngineDataManager.nativeOnQueryFps(iArr, iArr2);
            }

            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onChangeContinuousFrameLostConfig(final int i, final int i2) {
                gLSurfaceView.queueEvent(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxEngineDataManager.nativeOnChangeContinuousFrameLostConfig(i, i2);
                    }
                });
            }

            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onChangeLowFpsConfig(final int i, final float f) {
                gLSurfaceView.queueEvent(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1.2
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxEngineDataManager.nativeOnChangeLowFpsConfig(i, f);
                    }
                });
            }

            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onChangeExpectedFps(final int i) {
                gLSurfaceView.queueEvent(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1.3
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxEngineDataManager.nativeOnChangeExpectedFps(i);
                    }
                });
            }

            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onChangeSpecialEffectLevel(final int i) {
                gLSurfaceView.queueEvent(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1.4
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxEngineDataManager.nativeOnChangeSpecialEffectLevel(i);
                    }
                });
            }

            @Override // org.cocos2dx.enginedata.IEngineDataManager.OnSystemCommandListener
            public void onChangeMuteEnabled(final boolean z) {
                gLSurfaceView.queueEvent(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEngineDataManager.1.5
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxEngineDataManager.nativeOnChangeMuteEnabled(z);
                    }
                });
            }
        };
        if (sIsEnabled) {
            sIsInited = sManager.init(onSystemCommandListener);
        }
        nativeSetSupportOptimization(sIsInited);
        return sIsInited;
    }

    public static void destroy() {
        if (sIsEnabled) {
            sManager.destroy();
        }
    }

    public static void pause() {
        if (sIsEnabled) {
            sManager.pause();
        }
    }

    public static void resume() {
        if (sIsEnabled) {
            sManager.resume();
        }
    }

    public static String getVendorInfo() {
        if (sIsEnabled) {
            return sManager.getVendorInfo();
        }
        return "";
    }

    private static IEngineDataManager.GameStatus convertIntegerToGameStatus(int i) {
        for (IEngineDataManager.GameStatus gameStatus : IEngineDataManager.GameStatus.valuesCustom()) {
            if (i == gameStatus.ordinal()) {
                return gameStatus;
            }
        }
        return IEngineDataManager.GameStatus.INVALID;
    }

    public static void notifyGameStatus(int i, int i2, int i3) {
        if (!sIsEnabled) {
            nativeSetSupportOptimization(false);
            return;
        }
        IEngineDataManager.GameStatus gameStatusConvertIntegerToGameStatus = convertIntegerToGameStatus(i);
        if (gameStatusConvertIntegerToGameStatus == IEngineDataManager.GameStatus.INVALID) {
            Log.e(TAG, "Invalid game status: " + i);
            return;
        }
        sManager.notifyGameStatus(gameStatusConvertIntegerToGameStatus, i2, i3);
    }

    public static void notifyContinuousFrameLost(int i, int i2, int i3) {
        if (!sIsEnabled) {
            nativeSetSupportOptimization(false);
        } else {
            sManager.notifyContinuousFrameLost(i, i2, i3);
        }
    }

    public static void notifyLowFps(int i, float f, int i2) {
        if (!sIsEnabled) {
            nativeSetSupportOptimization(false);
        } else {
            sManager.notifyLowFps(i, f, i2);
        }
    }

    public static void notifyFpsChanged(float f, float f2) {
        if (!sIsEnabled) {
            nativeSetSupportOptimization(false);
        } else {
            sManager.notifyFpsChanged(f, f2);
        }
    }
}
