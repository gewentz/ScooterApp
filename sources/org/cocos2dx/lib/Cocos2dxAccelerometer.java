package org.cocos2dx.lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxAccelerometer implements SensorEventListener {
    static final float ALPHA = 0.25f;
    private static final String TAG = "Cocos2dxAccelerometer";
    final float[] accelerometerValues = new float[3];
    final float[] compassFieldValues = new float[3];
    private final Sensor mAccelerometer;
    private final Sensor mCompass;
    private final Context mContext;
    private final int mNaturalOrientation;
    private final SensorManager mSensorManager;

    public static native void onSensorChanged(float f, float f2, float f3, long j);

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public Cocos2dxAccelerometer(Context context) {
        this.mContext = context;
        SensorManager sensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensorManager = sensorManager;
        this.mAccelerometer = sensorManager.getDefaultSensor(1);
        this.mCompass = sensorManager.getDefaultSensor(2);
        this.mNaturalOrientation = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getOrientation();
    }

    public void enableCompass() {
        this.mSensorManager.registerListener(this, this.mCompass, 1);
    }

    public void enableAccel() {
        this.mSensorManager.registerListener(this, this.mAccelerometer, 1);
    }

    public void setInterval(float f) {
        this.mSensorManager.registerListener(this, this.mAccelerometer, (int) (f * 1000000.0f));
    }

    public void disable() {
        this.mSensorManager.unregisterListener(this);
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        float f;
        if (sensorEvent.sensor.getType() == 1) {
            float[] fArr = sensorEvent.values;
            float f2 = fArr[0];
            float f3 = fArr[1];
            float f4 = fArr[2];
            float[] fArr2 = this.accelerometerValues;
            fArr2[0] = f2;
            fArr2[1] = f3;
            fArr2[2] = f4;
            int i = this.mContext.getResources().getConfiguration().orientation;
            if (i == 2 && this.mNaturalOrientation != 0) {
                f = -f3;
            } else if (i != 1 || this.mNaturalOrientation == 0) {
                f = f2;
                f2 = f3;
            } else {
                f2 = -f2;
                f = f3;
            }
            int rotation = Cocos2dxHelper.getActivity().getWindowManager().getDefaultDisplay().getRotation();
            if (rotation == 2 || rotation == 3) {
                f = -f;
                f2 = -f2;
            }
            Cocos2dxGLSurfaceView.queueAccelerometer(f, f2, f4, sensorEvent.timestamp);
            return;
        }
        if (sensorEvent.sensor.getType() == 2) {
            float[] fArr3 = this.compassFieldValues;
            float[] fArr4 = sensorEvent.values;
            fArr3[0] = fArr4[0];
            fArr3[1] = fArr4[1];
            fArr3[2] = fArr4[2];
        }
    }
}
