package org.cocos2dx.lib;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
abstract class Cocos2dxAudioFocusManager {
    private static AudioManager.OnAudioFocusChangeListener sAfChangeListener = new AudioManager.OnAudioFocusChangeListener() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.1
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int i) {
            Log.d("AudioFocusManager", "onAudioFocusChange: " + i + ", thread: " + Thread.currentThread().getName());
            if (i == -1) {
                Log.d("AudioFocusManager", "Pause music by AUDIOFOCUS_LOSS");
                Cocos2dxHelper.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxAudioFocusManager.nativeOnAudioFocusChange(1);
                        Cocos2dxHelper.setAudioFocus(false);
                    }
                });
                return;
            }
            if (i == -2) {
                Log.d("AudioFocusManager", "Pause music by AUDIOFOCUS_LOSS_TRANSILENT");
                Cocos2dxHelper.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.1.2
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxAudioFocusManager.nativeOnAudioFocusChange(2);
                        Cocos2dxHelper.setAudioFocus(false);
                    }
                });
            } else if (i == -3) {
                Log.d("AudioFocusManager", "Lower the volume, keep playing by AUDIOFOCUS_LOSS_TRANSILENT_CAN_DUCK");
                Cocos2dxHelper.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.1.3
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxAudioFocusManager.nativeOnAudioFocusChange(3);
                        Cocos2dxHelper.setAudioFocus(false);
                    }
                });
            } else if (i == 1) {
                Log.d("AudioFocusManager", "Resume music by AUDIOFOCUS_GAIN");
                Cocos2dxHelper.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.1.4
                    @Override // java.lang.Runnable
                    public void run() {
                        Cocos2dxAudioFocusManager.nativeOnAudioFocusChange(0);
                        Cocos2dxHelper.setAudioFocus(true);
                    }
                });
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeOnAudioFocusChange(int i);

    static boolean registerAudioFocusListener(Context context) {
        if (((AudioManager) context.getSystemService("audio")).requestAudioFocus(sAfChangeListener, 3, 1) == 1) {
            Log.d("AudioFocusManager", "requestAudioFocus succeed");
            return true;
        }
        Log.e("AudioFocusManager", "requestAudioFocus failed!");
        return false;
    }

    static void unregisterAudioFocusListener(Context context) {
        if (((AudioManager) context.getSystemService("audio")).abandonAudioFocus(sAfChangeListener) == 1) {
            Log.d("AudioFocusManager", "abandonAudioFocus succeed!");
        } else {
            Log.e("AudioFocusManager", "abandonAudioFocus failed!");
        }
        Cocos2dxHelper.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxAudioFocusManager.2
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxHelper.setAudioFocus(true);
                Cocos2dxAudioFocusManager.nativeOnAudioFocusChange(0);
            }
        });
    }
}
