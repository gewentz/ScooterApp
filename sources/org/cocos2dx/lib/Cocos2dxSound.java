package org.cocos2dx.lib;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxSound {
    private static final int INVALID_SOUND_ID = -1;
    private static final int INVALID_STREAM_ID = -1;
    private static final int LOAD_TIME_OUT = 500;
    private static final int MAX_SIMULTANEOUS_STREAMS_DEFAULT = 5;
    private static final int MAX_SIMULTANEOUS_STREAMS_I9100 = 3;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 5;
    private static final float SOUND_RATE = 1.0f;
    private static final String TAG = "Cocos2dxSound";
    private final Context mContext;
    private float mLeftVolume;
    private float mRightVolume;
    private SoundPool mSoundPool;
    private boolean mIsAudioFocus = true;
    private final HashMap<String, ArrayList<Integer>> mPathStreamIDsMap = new HashMap<>();
    private final Object mLockPathStreamIDsMap = new Object();
    private final HashMap<String, Integer> mPathSoundIDMap = new HashMap<>();
    private ConcurrentHashMap<Integer, SoundInfoForLoadedCompleted> mPlayWhenLoadedEffects = new ConcurrentHashMap<>();

    public Cocos2dxSound(Context context) {
        this.mContext = context;
        initData();
    }

    private void initData() {
        if (Cocos2dxHelper.getDeviceModel().contains("GT-I9100")) {
            this.mSoundPool = new SoundPool(3, 3, 5);
        } else {
            this.mSoundPool = new SoundPool(5, 3, 5);
        }
        this.mSoundPool.setOnLoadCompleteListener(new OnLoadCompletedListener());
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
    }

    public int preloadEffect(String str) {
        Integer num = this.mPathSoundIDMap.get(str);
        if (num == null) {
            int iCreateSoundIDFromAsset = createSoundIDFromAsset(str);
            Integer numValueOf = Integer.valueOf(iCreateSoundIDFromAsset);
            if (iCreateSoundIDFromAsset != -1) {
                this.mPathSoundIDMap.put(str, numValueOf);
            }
            num = numValueOf;
        }
        return num.intValue();
    }

    public void unloadEffect(String str) {
        synchronized (this.mLockPathStreamIDsMap) {
            ArrayList<Integer> arrayList = this.mPathStreamIDsMap.get(str);
            if (arrayList != null) {
                int size = arrayList.size();
                int i = 0;
                while (i < size) {
                    Integer num = arrayList.get(i);
                    i++;
                    this.mSoundPool.stop(num.intValue());
                }
            }
            this.mPathStreamIDsMap.remove(str);
        }
        Integer num2 = this.mPathSoundIDMap.get(str);
        if (num2 != null) {
            this.mSoundPool.unload(num2.intValue());
            this.mPathSoundIDMap.remove(str);
        }
    }

    public int playEffect(String str, boolean z, float f, float f2, float f3) {
        Integer num = this.mPathSoundIDMap.get(str);
        if (num != null) {
            return doPlayEffect(str, num.intValue(), z, f, f2, f3);
        }
        int iPreloadEffect = preloadEffect(str);
        Integer numValueOf = Integer.valueOf(iPreloadEffect);
        if (iPreloadEffect == -1) {
            return -1;
        }
        SoundInfoForLoadedCompleted soundInfoForLoadedCompleted = new SoundInfoForLoadedCompleted(str, z, f, f2, f3);
        this.mPlayWhenLoadedEffects.putIfAbsent(numValueOf, soundInfoForLoadedCompleted);
        synchronized (soundInfoForLoadedCompleted) {
            try {
                soundInfoForLoadedCompleted.wait(500L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int i = soundInfoForLoadedCompleted.effectID;
        this.mPlayWhenLoadedEffects.remove(numValueOf);
        return i;
    }

    public void stopEffect(int i) {
        this.mSoundPool.stop(i);
        synchronized (this.mLockPathStreamIDsMap) {
            for (String str : this.mPathStreamIDsMap.keySet()) {
                if (this.mPathStreamIDsMap.get(str).contains(Integer.valueOf(i))) {
                    this.mPathStreamIDsMap.get(str).remove(this.mPathStreamIDsMap.get(str).indexOf(Integer.valueOf(i)));
                    break;
                }
            }
        }
    }

    public void pauseEffect(int i) {
        this.mSoundPool.pause(i);
    }

    public void resumeEffect(int i) {
        this.mSoundPool.resume(i);
    }

    public void pauseAllEffects() {
        synchronized (this.mLockPathStreamIDsMap) {
            if (!this.mPathStreamIDsMap.isEmpty()) {
                Iterator<Map.Entry<String, ArrayList<Integer>>> it = this.mPathStreamIDsMap.entrySet().iterator();
                while (it.hasNext()) {
                    ArrayList<Integer> value = it.next().getValue();
                    int size = value.size();
                    int i = 0;
                    while (i < size) {
                        Integer num = value.get(i);
                        i++;
                        this.mSoundPool.pause(num.intValue());
                    }
                }
            }
        }
    }

    public void resumeAllEffects() {
        synchronized (this.mLockPathStreamIDsMap) {
            if (!this.mPathStreamIDsMap.isEmpty()) {
                Iterator<Map.Entry<String, ArrayList<Integer>>> it = this.mPathStreamIDsMap.entrySet().iterator();
                while (it.hasNext()) {
                    ArrayList<Integer> value = it.next().getValue();
                    int size = value.size();
                    int i = 0;
                    while (i < size) {
                        Integer num = value.get(i);
                        i++;
                        this.mSoundPool.resume(num.intValue());
                    }
                }
            }
        }
    }

    public void stopAllEffects() {
        synchronized (this.mLockPathStreamIDsMap) {
            if (!this.mPathStreamIDsMap.isEmpty()) {
                Iterator<Map.Entry<String, ArrayList<Integer>>> it = this.mPathStreamIDsMap.entrySet().iterator();
                while (it.hasNext()) {
                    ArrayList<Integer> value = it.next().getValue();
                    int size = value.size();
                    int i = 0;
                    while (i < size) {
                        Integer num = value.get(i);
                        i++;
                        this.mSoundPool.stop(num.intValue());
                    }
                }
            }
            this.mPathStreamIDsMap.clear();
        }
    }

    public float getEffectsVolume() {
        return (this.mLeftVolume + this.mRightVolume) / 2.0f;
    }

    public void setEffectsVolume(float f) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f > SOUND_RATE) {
            f = SOUND_RATE;
        }
        this.mRightVolume = f;
        this.mLeftVolume = f;
        if (this.mIsAudioFocus) {
            setEffectsVolumeInternal(f, f);
        }
    }

    private void setEffectsVolumeInternal(float f, float f2) {
        synchronized (this.mLockPathStreamIDsMap) {
            if (!this.mPathStreamIDsMap.isEmpty()) {
                Iterator<Map.Entry<String, ArrayList<Integer>>> it = this.mPathStreamIDsMap.entrySet().iterator();
                while (it.hasNext()) {
                    ArrayList<Integer> value = it.next().getValue();
                    int size = value.size();
                    int i = 0;
                    while (i < size) {
                        Integer num = value.get(i);
                        i++;
                        this.mSoundPool.setVolume(num.intValue(), f, f2);
                    }
                }
            }
        }
    }

    public void end() {
        this.mSoundPool.release();
        synchronized (this.mLockPathStreamIDsMap) {
            this.mPathStreamIDsMap.clear();
        }
        this.mPathSoundIDMap.clear();
        this.mPlayWhenLoadedEffects.clear();
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
        initData();
    }

    private int createSoundIDFromAsset(String str) {
        int iLoad;
        try {
            if (str.startsWith("/")) {
                iLoad = this.mSoundPool.load(str, 0);
            } else if (Cocos2dxHelper.getObbFile() != null) {
                iLoad = this.mSoundPool.load(Cocos2dxHelper.getObbFile().getAssetFileDescriptor(str), 0);
            } else {
                iLoad = this.mSoundPool.load(this.mContext.getAssets().openFd(str), 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
            iLoad = -1;
        }
        if (iLoad == 0) {
            return -1;
        }
        return iLoad;
    }

    private float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f, f3));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized int doPlayEffect(String str, int i, boolean z, float f, float f2, float f3) {
        int iPlay;
        float fClamp = this.mLeftVolume * f3 * (SOUND_RATE - clamp(f2, 0.0f, SOUND_RATE));
        float fClamp2 = this.mRightVolume * f3 * (SOUND_RATE - clamp(-f2, 0.0f, SOUND_RATE));
        iPlay = this.mSoundPool.play(i, clamp(fClamp, 0.0f, SOUND_RATE), clamp(fClamp2, 0.0f, SOUND_RATE), 1, z ? -1 : 0, clamp(f * SOUND_RATE, 0.5f, 2.0f));
        synchronized (this.mLockPathStreamIDsMap) {
            ArrayList<Integer> arrayList = this.mPathStreamIDsMap.get(str);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                this.mPathStreamIDsMap.put(str, arrayList);
            }
            arrayList.add(Integer.valueOf(iPlay));
        }
        return iPlay;
    }

    public void onEnterBackground() {
        this.mSoundPool.autoPause();
    }

    public void onEnterForeground() {
        this.mSoundPool.autoResume();
    }

    void setAudioFocus(boolean z) {
        this.mIsAudioFocus = z;
        setEffectsVolumeInternal(z ? this.mLeftVolume : 0.0f, z ? this.mRightVolume : 0.0f);
    }

    private class SoundInfoForLoadedCompleted {
        int effectID = -1;
        float gain;
        boolean isLoop;
        float pan;
        String path;
        float pitch;

        SoundInfoForLoadedCompleted(String str, boolean z, float f, float f2, float f3) {
            this.path = str;
            this.isLoop = z;
            this.pitch = f;
            this.pan = f2;
            this.gain = f3;
        }
    }

    public class OnLoadCompletedListener implements SoundPool.OnLoadCompleteListener {
        public OnLoadCompletedListener() {
        }

        @Override // android.media.SoundPool.OnLoadCompleteListener
        public void onLoadComplete(SoundPool soundPool, int i, int i2) {
            SoundInfoForLoadedCompleted soundInfoForLoadedCompleted;
            if (i2 != 0 || (soundInfoForLoadedCompleted = (SoundInfoForLoadedCompleted) Cocos2dxSound.this.mPlayWhenLoadedEffects.get(Integer.valueOf(i))) == null) {
                return;
            }
            soundInfoForLoadedCompleted.effectID = Cocos2dxSound.this.doPlayEffect(soundInfoForLoadedCompleted.path, i, soundInfoForLoadedCompleted.isLoop, soundInfoForLoadedCompleted.pitch, soundInfoForLoadedCompleted.pan, soundInfoForLoadedCompleted.gain);
            synchronized (soundInfoForLoadedCompleted) {
                soundInfoForLoadedCompleted.notifyAll();
            }
        }
    }
}
