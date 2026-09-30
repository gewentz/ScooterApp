package org.cocos2dx.enginedata.magic;

import android.util.Log;
import com.huawei.android.hwgamesdk.HwGameSDK;
import com.huawei.android.hwgamesdk.NoExtAPIException;
import org.cocos2dx.enginedata.IEngineDataManager;

/* JADX INFO: compiled from: EngineDataManagerHuawei.java */
/* JADX INFO: loaded from: classes.dex */
public class a implements IEngineDataManager {
    private static final String a = "EngineDataManagerHuawei";
    private static /* synthetic */ int[] f;
    private IEngineDataManager.OnSystemCommandListener b;
    private boolean c = false;
    private HwGameSDK d = new HwGameSDK();
    private HwGameSDK.GameEngineCallBack e = new HwGameSDK.GameEngineCallBack() { // from class: org.cocos2dx.enginedata.magic.a.1
        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void changeDxFpsRate(int i, float f2) {
            if (a.this.b != null) {
                a.this.b.onChangeLowFpsConfig(i, f2);
            }
        }

        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void queryExpectedFps(int[] iArr, int[] iArr2) {
            if (a.this.b != null) {
                a.this.b.onQueryFps(iArr, iArr2);
            }
        }

        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void changeFpsRate(int i) {
            if (!a.this.c || a.this.b == null) {
                return;
            }
            a.this.b.onChangeExpectedFps(i);
        }

        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void changeSpecialEffects(int i) {
            if (a.this.b != null) {
                a.this.b.onChangeSpecialEffectLevel(i);
            }
        }

        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void changeMuteEnabled(boolean z) {
            if (a.this.b != null) {
                a.this.b.onChangeMuteEnabled(z);
            }
        }

        @Override // com.huawei.android.hwgamesdk.HwGameSDK.GameEngineCallBack
        public void changeContinuousFpsMissedRate(int i, int i2) {
            if (a.this.b != null) {
                a.this.b.onChangeContinuousFrameLostConfig(i, i2);
            }
        }
    };

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void destroy() {
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void pause() {
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void resume() {
    }

    static /* synthetic */ int[] a() {
        int[] iArr = f;
        if (iArr != null) {
            return iArr;
        }
        int[] iArr2 = new int[IEngineDataManager.GameStatus.valuesCustom().length];
        try {
            iArr2[IEngineDataManager.GameStatus.INVALID.ordinal()] = 6;
        } catch (NoSuchFieldError unused) {
        }
        try {
            iArr2[IEngineDataManager.GameStatus.IN_SCENE.ordinal()] = 5;
        } catch (NoSuchFieldError unused2) {
        }
        try {
            iArr2[IEngineDataManager.GameStatus.LAUNCH_BEGIN.ordinal()] = 1;
        } catch (NoSuchFieldError unused3) {
        }
        try {
            iArr2[IEngineDataManager.GameStatus.LAUNCH_END.ordinal()] = 2;
        } catch (NoSuchFieldError unused4) {
        }
        try {
            iArr2[IEngineDataManager.GameStatus.SCENE_CHANGE_BEGIN.ordinal()] = 3;
        } catch (NoSuchFieldError unused5) {
        }
        try {
            iArr2[IEngineDataManager.GameStatus.SCENE_CHANGE_END.ordinal()] = 4;
        } catch (NoSuchFieldError unused6) {
        }
        f = iArr2;
        return iArr2;
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyGameStatus(IEngineDataManager.GameStatus gameStatus, int i, int i2) {
        HwGameSDK.GameScene gameScene;
        try {
            int i3 = a()[gameStatus.ordinal()];
            if (i3 == 1) {
                gameScene = HwGameSDK.GameScene.GAME_LAUNCH_BEGIN;
            } else if (i3 == 2) {
                gameScene = HwGameSDK.GameScene.GAME_LAUNCH_END;
            } else if (i3 == 3) {
                gameScene = HwGameSDK.GameScene.GAME_SCENECHANGE_BEGIN;
            } else if (i3 == 4) {
                gameScene = HwGameSDK.GameScene.GAME_SCENECHANGE_END;
            } else if (i3 == 5) {
                gameScene = HwGameSDK.GameScene.GAME_INSCENE;
            } else {
                Log.e(a, "error type: " + gameStatus);
                return;
            }
            this.d.notifyGameScene(gameScene, i, i2);
        } catch (NoExtAPIException unused) {
            Log.d(a, "Unsupported function: notifyGameStatus");
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyContinuousFrameLost(int i, int i2, int i3) {
        try {
            this.d.notifyContinuousFpsMissed(i, i2, i3);
        } catch (NoExtAPIException unused) {
            Log.d(a, "Unsupported function: notifyGameStatus");
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyLowFps(int i, float f2, int i2) {
        try {
            this.d.notifyFpsDx(i, f2, i2);
        } catch (NoExtAPIException unused) {
            Log.d(a, "Unsupported function: notifyGameStatus");
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyFpsChanged(float f2, float f3) {
        try {
            this.d.notifyFpsChanged(f2, f3);
        } catch (NoExtAPIException unused) {
            Log.d(a, "Unsupported function: notifyFpsChanged");
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public String getVendorInfo() {
        return "HuaWei:" + this.d.getApiLevel();
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public boolean init(IEngineDataManager.OnSystemCommandListener onSystemCommandListener) {
        if (onSystemCommandListener == null) {
            return false;
        }
        this.b = onSystemCommandListener;
        try {
            int apiLevel = this.d.getApiLevel();
            if (apiLevel < 1) {
                Log.d(a, "Unsupported function: HwGameSDK.getApiLevel: " + apiLevel + ", min: 1");
                return false;
            }
            if (apiLevel >= 2) {
                this.c = true;
            }
            return this.d.registerGame(this.e);
        } catch (NoExtAPIException | NoSuchMethodError unused) {
            return false;
        }
    }
}
