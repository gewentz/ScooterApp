package org.cocos2dx.enginedata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.cocos2dx.enginedata.magic.a;

/* JADX INFO: loaded from: classes.dex */
public class EngineDataManager implements IEngineDataManager {
    private static final String a = "EngineDataManager";
    private static final int b = 1000;
    private static final String c = "1.0.0.0";
    private List<IEngineDataManager> d;

    public int getVersionCode() {
        return 1000;
    }

    public EngineDataManager() {
        ArrayList arrayList = new ArrayList();
        this.d = arrayList;
        arrayList.add(new a());
    }

    public String getVersionName() {
        return c;
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public String getVendorInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.d.size(); i++) {
            sb.append(this.d.get(i).getVendorInfo());
            if (i < this.d.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public boolean init(IEngineDataManager.OnSystemCommandListener onSystemCommandListener) {
        Iterator<IEngineDataManager> it = this.d.iterator();
        boolean z = false;
        while (it.hasNext()) {
            if (it.next().init(onSystemCommandListener)) {
                z = true;
            }
        }
        return z;
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void destroy() {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().destroy();
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void pause() {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().pause();
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void resume() {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().resume();
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyGameStatus(IEngineDataManager.GameStatus gameStatus, int i, int i2) {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().notifyGameStatus(gameStatus, i, i2);
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyContinuousFrameLost(int i, int i2, int i3) {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().notifyContinuousFrameLost(i, i2, i3);
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyLowFps(int i, float f, int i2) {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().notifyLowFps(i, f, i2);
        }
    }

    @Override // org.cocos2dx.enginedata.IEngineDataManager
    public void notifyFpsChanged(float f, float f2) {
        Iterator<IEngineDataManager> it = this.d.iterator();
        while (it.hasNext()) {
            it.next().notifyFpsChanged(f, f2);
        }
    }
}
