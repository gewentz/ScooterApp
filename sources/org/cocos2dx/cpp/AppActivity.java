package org.cocos2dx.cpp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class AppActivity extends BaseActivity implements SensorEventListener {
    private static String APK_URL_01 = "";
    private static final int CMD_AddDevice_AGV = 50011;
    private static final int CMD_BeginLoadApk = 20011;
    private static final int CMD_CheckCode_AGV = 40011;
    private static final int CMD_Copy = 30011;
    private static final int CMD_StartSensor = 30021;
    private static final boolean D = false;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2000;
    private static final String TAG = "AppActivity";
    private static String _DevicName = "";
    private static String _bleName = "";
    private static String _defaultDeviceName = "--";
    private static String _emailFrom = "895901747@qq.com";
    private static String _emailMessage = "";
    private static boolean _isAutoConnect = false;
    private static String _orderID = "订单号：88888888";
    private static String _phoneNum = "15815888888";
    private static String _url = "http://zhushou.360.cn/detail/index/soft_id/3246982";
    private static BluetoothHolder bluetoothHolder = null;
    private static int delayTime = 10;
    private static ArrayList<BluetoothDevice> devices = null;
    private static AppActivity instance = null;
    private static boolean isSupportBLE = false;
    private static Activity me;
    private static ArrayList<Integer> rissList;
    Double lat;
    Double lng;
    Location loc;
    LocationManager lom;
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private final int PERMISSON_LOCATION = 800;
    private final int PERMISSON_SCAN = 1001;
    private String[] permission = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private int _isChinese = 0;
    private final int CMD_ShowDialogGPS = 10003;
    boolean _is360 = true;
    private boolean haveStartSensor = false;
    private int _checkTime = 0;
    private int _checkTime2 = 0;
    boolean _haveInitBLE = false;
    int _rssi = 0;
    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: org.cocos2dx.cpp.AppActivity.7
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            try {
                if (AppActivity.this.IsMyDevice(bluetoothDevice.getName())) {
                    AppActivity.this.runOnUiThread(new Runnable() { // from class: org.cocos2dx.cpp.AppActivity.7.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Iterator it = AppActivity.devices.iterator();
                            while (it.hasNext()) {
                                if (((BluetoothDevice) it.next()).getName().equals(bluetoothDevice.getName())) {
                                    return;
                                }
                            }
                            AppActivity.devices.add(bluetoothDevice);
                            AppActivity.access$384(bluetoothDevice.getName() + "*");
                            if (AppActivity._isAutoConnect && AppActivity._defaultDeviceName.equals(bluetoothDevice.getName())) {
                                boolean unused = AppActivity._isAutoConnect = false;
                                AppActivity.this.FinshScanBLE();
                                BluetoothHolder.getInstance().connect(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                            }
                            Log.d(AppActivity.TAG, "add device name:" + bluetoothDevice.getName());
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("onLeScan", e.toString());
            }
        }
    };
    private boolean _isFirstInit = true;
    boolean haveSend = false;
    LocationListener los = new LocationListener() { // from class: org.cocos2dx.cpp.AppActivity.8
        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            if (location != null) {
                AppActivity.this.lat = Double.valueOf(location.getLatitude());
                AppActivity.this.lng = Double.valueOf(location.getLongitude());
                float bearing = location.getBearing();
                float speed = location.getSpeed();
                double altitude = location.getAltitude();
                Log.d("get_gps", "纬度:" + String.valueOf(AppActivity.this.lat));
                Log.d("get_gps", "经度:" + String.valueOf(AppActivity.this.lng));
                Log.d("get_gps", "方向:" + String.valueOf(bearing));
                Log.d("get_gps", "速度:" + String.valueOf(speed));
                Log.d("get_gps", "海拔:" + String.valueOf(altitude));
                return;
            }
            Log.d("los", "经度/纬度:未知");
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String str) {
            Log.d("onProviderDisabled", "provider:" + str);
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String str) {
            Log.d("onProviderEnabled", "provider:" + str);
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String str, int i, Bundle bundle) {
            Log.d("onStatusChanged", "provider:" + str);
        }
    };
    Handler myHandler = new Handler() { // from class: org.cocos2dx.cpp.AppActivity.9
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse(AppActivity._url));
                AppActivity.me.startActivity(intent);
            } else if (i == 10003) {
                AppActivity.this.showMissingGPSPermissionDialog();
            } else if (i == AppActivity.CMD_BeginLoadApk) {
                AppActivity.this.downloadApk();
            } else if (i == AppActivity.CMD_Copy) {
                AppActivity.CallBackOnTouchCopy(AppActivity.this.GetCopyString());
            } else if (i == AppActivity.CMD_StartSensor) {
                AppActivity.this.StartSensor();
            } else if (i == AppActivity.CMD_CheckCode_AGV) {
                AppActivity appActivity = AppActivity.this;
                appActivity.CheckCode(appActivity._checkCode, AppActivity.this._fun, AppActivity.this._deviceID);
            } else if (i == AppActivity.CMD_AddDevice_AGV) {
                AppActivity.this.AddNewDevice2();
            } else if (i == 264830) {
                AppActivity.this.sendCode(AppActivity._phoneNum);
            } else {
                switch (i) {
                    case 789461:
                        AppActivity appActivity2 = AppActivity.this;
                        appActivity2.checkPsw(appActivity2._userName, AppActivity.this._psw);
                        break;
                    case 789462:
                        AppActivity appActivity3 = AppActivity.this;
                        appActivity3.checkCode(appActivity3._checkCode, AppActivity._phoneNum, true);
                        break;
                    case 789463:
                        AppActivity.this.addUserToService(AppActivity._phoneNum, "", AppActivity._phoneNum);
                        break;
                    case 789464:
                        AppActivity appActivity4 = AppActivity.this;
                        appActivity4.checkCode(appActivity4._checkCode, AppActivity._phoneNum, true);
                        break;
                }
            }
            super.handleMessage(message);
        }
    };
    private String _userName = "";
    private final String USER_TABLE = "NormalUser";
    private final String KEY_USERNAME = "UserName";
    private final String KEY_PSW = "Password";
    private final String KEY_FRIENDS = "Friends";
    private String _checkCode = "";
    private String _psw = "";
    private final int CMD_LOGIN1 = 789461;
    private final int CMD_LOGIN2 = 789462;
    private final int CMD_LOGIN3 = 789463;
    private final int CMD_REGISTER = 789464;
    private final int CMD_GET_CHECKCODE = 264830;
    private final String DEVICE_TABLE = "Device";
    private final String CODE_TABLE = "CheckCode";
    private int _deviceID = 0;
    private int _fun = 0;

    /* JADX INFO: Access modifiers changed from: private */
    public void AddNewDevice2() {
    }

    public static native void CallBackOnTouchCopy(String str);

    public static native void CallBackRegist(int i);

    private boolean CheckNet() {
        return true;
    }

    public static native void CkeckCodeCallBack(int i);

    public static native void FinishLoad();

    public static int GetGameRunTime(String str) {
        return 0;
    }

    public static native void LoadCallBack(int i, int i2);

    static native void LoadTaoBao();

    public static native void OnAccuracyChanged(int i, int i2);

    public static native void ReadMsg(byte[] bArr);

    public static native void SendEmailCallBack(String str);

    public static native void SendEmailCallBack2(int i);

    public static native void SetDeviceID(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public void checkCode(String str, String str2, boolean z) {
    }

    void AddDeviceToService(int i, int i2) {
    }

    void CheckCode(String str, int i, int i2) {
    }

    public void MyResume() {
    }

    public void SendEmail(String str) {
    }

    public void TestSendData() {
    }

    void UpdataDeviceInfo(int i, int i2) {
    }

    public void addFriend(String str) {
    }

    public void addUserToService(String str, String str2, String str3) {
    }

    public void checkPsw(String str, String str2) {
    }

    void downloadApk() {
    }

    public int getMaxValue(int i, int i2, int i3) {
        if (i > i2 && i > i3) {
            return i;
        }
        if (i2 > i && i2 > i3) {
            return i2;
        }
        if (i3 <= i || i3 <= i2) {
            return 0;
        }
        return i3;
    }

    void installAPK(String str) {
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void sendCode(String str) {
    }

    static /* synthetic */ String access$384(Object obj) {
        String str = _DevicName + obj;
        _DevicName = str;
        return str;
    }

    public static AppActivity getInstance() {
        return instance;
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate");
        Log.d(TAG, "包名：" + getPackageName());
        super.onCreate(bundle);
        setStateBarColor(false);
        getWindow().addFlags(128);
        if (getResources().getConfiguration().locale.getLanguage().endsWith("zh")) {
            this._isChinese = 1;
        } else {
            this._isChinese = 0;
        }
        instance = this;
        devices = new ArrayList<>();
        rissList = new ArrayList<>();
        isSupportBLE = BluetoothHolder.init(this);
        me = this;
        ArrayList arrayList = new ArrayList();
        for (String str : this.permission) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                arrayList.add(str);
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[arrayList.size()]), 800);
    }

    public static void ToSensorRemote() {
        AppActivity appActivity = instance;
        if (appActivity.haveStartSensor) {
            return;
        }
        appActivity.SendMsg(CMD_StartSensor);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void StartSensor() {
        if (this.haveStartSensor) {
            return;
        }
        SensorManager sensorManager = (SensorManager) getSystemService("sensor");
        this.mSensorManager = sensorManager;
        this.mSensor = sensorManager.getDefaultSensor(1);
        if (this.mSensorManager == null) {
            Log.d(TAG, "deveice not support SensorManager");
        }
        this.mSensorManager.registerListener(this, this.mSensor, 3);
        this.haveStartSensor = true;
    }

    public boolean IsTraditionalChinese() {
        return getResources().getConfiguration().locale.getLanguage().endsWith("zh") && !Locale.getDefault().getCountry().equals("CN");
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 800) {
            int i2 = this._checkTime + 1;
            this._checkTime = i2;
            if (i2 > 3) {
                if (!verifyPermissions(iArr)) {
                    showMissingPermissionDialog();
                }
            } else {
                BeginScanBLE();
            }
        }
        if (i == 1001) {
            int i3 = this._checkTime2 + 1;
            this._checkTime2 = i3;
            if (i3 > 3 && !verifyPermissions(iArr)) {
                showMissingPermissionDialog2();
            }
            if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_CONNECT") == 0) {
                Log.d(TAG, "蓝牙权限已开启2");
            }
        }
    }

    private boolean verifyPermissions(int[] iArr) {
        try {
            for (int i : iArr) {
                if (i != 0) {
                    return false;
                }
            }
            return true;
        } catch (Throwable th) {
            th.printStackTrace();
            return true;
        }
    }

    private void showMissingPermissionDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this._isChinese == 1 ? "提示" : "Tip");
            builder.setMessage(this._isChinese == 1 ? "该应用需打开定位权限才能扫描到蓝牙设备。请点击\\\"设置\\\"-\\\"权限管理\\\"-打开定位权限" : "The application needs to get the location permission to scan to the Bluetooth device. Please click \\\"Settings\\\"-\\\"Permissions\\\"-Get location info");
            builder.setNegativeButton(this._isChinese == 1 ? "取消" : "Cancel", new DialogInterface.OnClickListener() { // from class: org.cocos2dx.cpp.AppActivity.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (AppActivity.this._is360) {
                            return;
                        }
                        AppActivity.this.finish();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
            builder.setPositiveButton(this._isChinese == 1 ? "设置" : "Settings", new DialogInterface.OnClickListener() { // from class: org.cocos2dx.cpp.AppActivity.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        AppActivity.this.startAppSettings();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void showMissingPermissionDialog2() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this._isChinese == 1 ? "提示" : "Tip");
            builder.setMessage(this._isChinese == 1 ? "该应用需打开连接附近的设备权限才能扫描到蓝牙设备。请点击\\\"设置\\\"-\\\"权限管理\\\"-连接附近的设备" : "The application needs to get the connect to nearby devices permission to scan to the Bluetooth device. Please click \\\"Settings\\\"-\\\"Permissions\\\"-Connect to nearby devices");
            builder.setNegativeButton(this._isChinese == 1 ? "取消" : "Cancel", new DialogInterface.OnClickListener() { // from class: org.cocos2dx.cpp.AppActivity.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (AppActivity.this._is360) {
                            return;
                        }
                        AppActivity.this.finish();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
            builder.setPositiveButton(this._isChinese == 1 ? "设置" : "Settings", new DialogInterface.OnClickListener() { // from class: org.cocos2dx.cpp.AppActivity.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        AppActivity.this.startAppSettings();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAppSettings() {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static final boolean isLocationEnable(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), "location_mode") != 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showMissingGPSPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this._isChinese == 1 ? "提示" : "Tip");
        builder.setMessage(this._isChinese == 1 ? "搜索蓝牙设备需要开启位置服务(GPS)" : "Searching for Bluetooth devices requires location service (GPS) to be turned on ");
        builder.setNegativeButton(this._isChinese == 1 ? "取消" : "Cancel", (DialogInterface.OnClickListener) null);
        builder.setPositiveButton(this._isChinese == 1 ? "去开启" : "Go to Settings", new DialogInterface.OnClickListener() { // from class: org.cocos2dx.cpp.AppActivity.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                AppActivity.this.setLocationService();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setLocationService() {
        startActivityForResult(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"), REQUEST_CODE_LOCATION_SETTINGS);
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (getResources().getConfiguration().locale.getLanguage().endsWith("zh")) {
            this._isChinese = 1;
        } else {
            this._isChinese = 0;
        }
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (this._haveInitBLE) {
            bluetoothDestroy(true);
        }
    }

    private void bluetoothResume() {
        bluetoothHolder = BluetoothHolder.getInstance();
        isSupportBLE = BluetoothHolder.init(this);
        bluetoothHolder.openBluetooth();
        if (!bluetoothHolder.isOpen()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 10123);
        }
        bluetoothHolder.bindService();
        bluetoothHolder.registerReceiver(this);
        this._haveInitBLE = true;
        Log.d(TAG, "buletoothResume");
    }

    private void bluetoothDestroy(boolean z) {
        try {
            DisConnect();
            bluetoothHolder.unbindService(z);
            bluetoothHolder.unregisterReceiver(z);
            this._haveInitBLE = false;
        } catch (Exception e) {
            Log.e("bluetoothDestroy", e.toString());
        }
    }

    private static List<Map.Entry<BluetoothDevice, Integer>> sortByValueFloatDesc(Map<BluetoothDevice, Integer> map) {
        ArrayList arrayList = new ArrayList(map.entrySet());
        Collections.sort(arrayList, new Comparator<Map.Entry<BluetoothDevice, Integer>>() { // from class: org.cocos2dx.cpp.AppActivity.6
            @Override // java.util.Comparator
            public int compare(Map.Entry<BluetoothDevice, Integer> entry, Map.Entry<BluetoothDevice, Integer> entry2) {
                return entry2.getValue().compareTo(entry.getValue());
            }
        });
        return arrayList;
    }

    boolean IsMyDevice(String str) {
        try {
            return str.startsWith("M0") || str.startsWith("H1") || str.startsWith("M1") || str.startsWith("Mini") || str.startsWith("Plus") || str.startsWith("X1") || str.startsWith("X3") || str.startsWith("M6") || str.startsWith("GoKart") || str.startsWith("A6") || str.startsWith("MI") || str.startsWith("N3MTenbot") || str.startsWith("miniPLUS_") || str.startsWith(" MiniPro") || str.startsWith("SFSO") || str.startsWith("V5Robot") || str.startsWith("EO STREET") || str.startsWith("XRIDER") || str.startsWith("TECAR") || str.startsWith("MAX") || str.startsWith("i10") || str.startsWith("NEXRIDE") || str.startsWith("E-WHEELS") || str.startsWith("E12") || str.startsWith("E9PRO") || str.startsWith("T10") || str.startsWith("KING") || str.startsWith("oP9G") || str.startsWith("Mentor") || str.startsWith("E9G") || str.startsWith("XT-") || str.startsWith("SPARK") || str.startsWith("EROBOT");
        } catch (Exception e) {
            Log.e("IsMyDevice", e.toString());
            return false;
        }
    }

    @Override // org.cocos2dx.cpp.BroadcastCallback
    public void disconnected(Intent intent) {
        Log.e(TAG, "disconnected");
    }

    @Override // org.cocos2dx.cpp.BroadcastCallback
    public void connected(Intent intent) {
        Log.e(TAG, "connected------");
    }

    @Override // org.cocos2dx.cpp.BroadcastCallback
    public void discovered(Intent intent) {
        Log.d(TAG, "discovered");
        BluetoothHolder.getInstance().enableTXNotification();
    }

    @Override // org.cocos2dx.cpp.BroadcastCallback
    public synchronized void data(Intent intent, byte[] bArr) {
        if (bArr != null) {
            ReadMsg(bArr);
        }
    }

    public static Object GetInstance() {
        return instance;
    }

    public boolean initBluetooth() {
        if (!isSupportBLE) {
            isSupportBLE = BluetoothHolder.init(this);
        }
        Log.e(TAG, "isSupportBLE:" + isSupportBLE);
        return isSupportBLE;
    }

    private void initPermission() {
        ArrayList arrayList = new ArrayList();
        Log.d(TAG, "BeginScanBLE() b:" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 31) {
            arrayList.add("android.permission.BLUETOOTH_CONNECT");
            arrayList.add("android.permission.BLUETOOTH_SCAN");
            Log.d(TAG, "BeginScanBLE() 12");
        }
        if (arrayList.size() > 0) {
            ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[0]), 1001);
        }
    }

    public void BeginScanBLE() {
        _isAutoConnect = false;
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList arrayList = new ArrayList();
            for (String str : this.permission) {
                if (ContextCompat.checkSelfPermission(this, str) != 0) {
                    arrayList.add(str);
                }
            }
            if (!arrayList.isEmpty()) {
                ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[arrayList.size()]), 800);
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= 23 && !isLocationEnable(this)) {
            instance.SendMsg(10003);
            return;
        }
        if (Build.VERSION.SDK_INT >= 31) {
            initPermission();
            if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_CONNECT") != 0) {
                Log.d(TAG, "BeginScanBLE() 55");
                return;
            } else if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_SCAN") != 0) {
                Log.d(TAG, "BeginScanBLE() 66");
                return;
            }
        }
        this._checkTime = 0;
        this._checkTime2 = 0;
        devices.clear();
        rissList.clear();
        _DevicName = "";
        DisConnect();
        if (this._haveInitBLE) {
            bluetoothDestroy(true);
        }
        bluetoothResume();
        BluetoothHolder.getInstance().scanDevice(false, this.leScanCallback);
        BluetoothHolder.getInstance().scanDevice(true, this.leScanCallback);
    }

    public static void SetDefaultConn(String str) {
        instance.DisConnect();
        _defaultDeviceName = str;
        instance.BeginScanBLE();
        _isAutoConnect = true;
    }

    public static void SetDefaultConn2(String str) {
        instance.DisConnect();
        BluetoothHolder.getInstance().reConnect();
    }

    public void FinshScanBLE() {
        if (BluetoothHolder.getInstance().isScanning()) {
            BluetoothHolder.getInstance().scanDevice(false, this.leScanCallback);
        }
    }

    public void DisConnect() {
        if (IsConnected()) {
            BluetoothHolder.getInstance().disconnect();
        }
    }

    public static String GetScanBleList() {
        return _DevicName;
    }

    public void ConnectBLE(int i) {
        this.haveSend = false;
        FinshScanBLE();
        BluetoothDevice bluetoothDevice = devices.get(i);
        BluetoothHolder.getInstance().connect(bluetoothDevice.getName(), bluetoothDevice.getAddress());
        Log.d(TAG, "reConnect:deviceAddress0" + bluetoothDevice.getAddress());
        SetAppType(bluetoothDevice.getName());
    }

    public void SetAppType(String str) {
        if (str.startsWith("M6")) {
            BluetoothHolder.SetIsSmallType(0);
        } else {
            BluetoothHolder.SetIsSmallType(1);
        }
    }

    public boolean IsConnected() {
        return BluetoothHolder.getInstance().isConnected();
    }

    public boolean IsHaveLocationPermission() {
        for (String str : this.permission) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                return false;
            }
        }
        return true;
    }

    public static void SendMessage(byte[] bArr, int i) {
        if (instance.IsConnected()) {
            byte[] bArr2 = new byte[i];
            for (int i2 = 0; i2 < i; i2++) {
                bArr2[i2] = bArr[i2];
            }
            BluetoothHolder.getInstance().writeCharacteristic(bArr2);
        }
    }

    public static void vibrate(long j) {
        ((Vibrator) instance.getSystemService("vibrator")).vibrate(j);
    }

    public static void vibrateWithPattern(long[] jArr, int i) {
        ((Vibrator) instance.getSystemService("vibrator")).vibrate(jArr, i);
    }

    public static void cancelVibrate() {
        ((Vibrator) instance.getSystemService("vibrator")).cancel();
    }

    public void MyPause() {
        DisConnect();
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor != null && IsConnected() && sensorEvent.sensor.getType() == 1) {
            ChangePos((int) sensorEvent.values[0], (int) sensorEvent.values[1], (int) sensorEvent.values[2]);
        }
    }

    private void ChangePos(int i, int i2, int i3) {
        int i4;
        int i5 = i < 0 ? i * (-1) : i;
        int i6 = i2 < 0 ? i2 * (-1) : i2;
        if (i3 < 0) {
            if (i6 > i5) {
                i2 = i2 > 0 ? 20 - i2 : (-20) - i2;
            } else {
                i = i > 0 ? 20 - i : (-20) - i;
            }
        }
        int i7 = i2 * (-1);
        int i8 = i * (-1);
        int i9 = 0;
        if (i8 > 1) {
            i4 = i8 - 1;
        } else {
            i4 = i8 < -1 ? i8 + 1 : 0;
        }
        if (i7 > 1) {
            i9 = i7 - 1;
        } else if (i7 < -1) {
            i9 = i7 + 1;
        }
        OnAccuracyChanged(i4 * 2, i9 * 2);
    }

    public void get_gps() {
        this.lom = (LocationManager) getSystemService("location");
        Location location = this.loc;
        if (location != null) {
            this.lat = Double.valueOf(location.getLatitude());
            this.lng = Double.valueOf(this.loc.getLongitude());
            Log.d("get_gps", "纬度:" + String.valueOf(this.lat));
            Log.d("get_gps", "经度:" + String.valueOf(this.lng));
        } else {
            Log.d("get_gps", "经度/纬度:未知");
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(1);
        this.lom.getBestProvider(criteria, true);
    }

    public static void OpenURL(String str) {
        _url = str;
        instance.SendMsg(100);
    }

    private void SendMsg(int i) {
        Message message = new Message();
        message.what = i;
        this.myHandler.sendMessage(message);
    }

    public static void AddOtherM3(String str) {
        _bleName = str;
        instance.SendMsg(1);
    }

    public static String GetCurTime() {
        return "";
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQUEST_CODE_LOCATION_SETTINGS) {
            if (isLocationEnable(this)) {
                BeginScanBLE();
                return;
            }
            return;
        }
        if (i == 8989) {
            Log.d("onActivityResult", "resultCode:" + i2);
            if (i2 == 0) {
                Intent intent2 = new Intent();
                intent2.setAction("android.intent.action.VIEW");
                intent2.setData(Uri.parse("https://market.m.taobao.com/app/starlink/wakeup-transit/pages/download?star_id=3027&slk_force_set_request=true&targetUrl=https%3A%2F%2Fh5.m.taobao.com%2Fawp%2Fcore%2Fdetail.htm%3Fwp_pk%3Dshop%2Findex_2212387364356_%26wp_m%3Ditem_rank_list_-2%26from%3Dinshop%26id%3D654274384555%26wp_app%3Dweapp%26wp_p%3D5"));
                startActivity(intent2);
                return;
            }
            return;
        }
        if (i == 9000) {
            Log.d("onActivityResult", "resultCode:" + i2);
            if (i2 == 0) {
                Intent intent3 = new Intent();
                intent3.setAction("android.intent.action.VIEW");
                intent3.setData(Uri.parse("https://z.douyin.com/YCvp?scheme=snssdk1128%3A%2F%2Fec_goods_detail%3Fpromotion_id%3D3552098369008746198%26meta_params%3D%257B%2522entrance_info%2522%253A%2522%257B%255C%2522share_content%255C%2522%253A%255C%2522product_detail%255C%2522%252C%255C%2522share_object%255C%2522%253A%255C%2522copy%255C%2522%257D%2522%257D%26enter_from%3Dnew_h5_product_detail%26request_additions%3D%257B%2522sec_author_id%2522%253A%2522MS4wLjABAAAA7goVXO3rYnUasFoMtoxhS_K_GCUXwJy5xQFuDSX4P2BEkhObhOVz7ofm91852RIL%2522%252C%2522enter_from%2522%253A%2522new_h5_product_detail%2522%257D%26scene_from%3Dshare_reflow%26use_link_command%3D1"));
                startActivity(intent3);
            }
        }
    }

    public static boolean isAppInstalled(Context context, String str) {
        return new File("/data/data/" + str).exists();
    }

    public void gotoShop() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("taobao://shop.m.taobao.com/shop/shop_index.htm?shop_id=339928477"));
            startActivityForResult(intent, 8989);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoDouYin() {
        if (isAppInstalled(getContext(), "com.ss.android.ugc.aweme")) {
            Intent intent = new Intent();
            intent.setData(Uri.parse("snssdk1128://user/profile/2304231254538824"));
            startActivityForResult(intent, 9000);
            return;
        }
        if (isAppInstalled(getContext(), "com.ss.android.article.video")) {
            Intent intent2 = new Intent();
            intent2.setData(Uri.parse("snssdk1112://user/profile/2304231254538824"));
            startActivityForResult(intent2, 9000);
        } else if (isAppInstalled(getContext(), "com.ss.android.ugc.live")) {
            Intent intent3 = new Intent();
            intent3.setData(Uri.parse("snssdk1112://profile?id=2304231254538824"));
            startActivityForResult(intent3, 9000);
        } else {
            try {
                Intent intent4 = new Intent();
                intent4.setData(Uri.parse("snssdk1128://user/profile/2304231254538824"));
                startActivityForResult(intent4, 9000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startTaobaoApp() {
        if (isAppInstalled(instance, "com.taobao.taobao")) {
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            intent.setFlags(268435456);
            intent.setData(Uri.parse("https://item.taobao.com/item.htm?spm=a1z10.1-c-s.w4004-23989050866.2.108c2ea4jRnp21&id=652257996818"));
            intent.setClassName("com.taobao.taobao", "com.taobao.tao.detail.activity.DetailActivity");
            instance.startActivity(intent);
            return;
        }
        LoadTaoBao();
        Log.e("startTaobaoApp", "您还没有安装淘宝客户端！");
    }

    public static void BeginLoadApk(String str) {
        APK_URL_01 = str;
        instance.SendMsg(CMD_BeginLoadApk);
    }

    public void TouchLogin1(String str, String str2) {
        this._userName = str;
        _phoneNum = str;
        this._psw = str2;
        SendMsg(789461);
        Log.d("TouchLogin1", "phoneNum：" + str + "  psw:" + str2);
    }

    public void TouchLogin2(String str, String str2) {
        this._userName = str;
        _phoneNum = str;
        this._checkCode = str2;
        this._psw = "";
        SendMsg(789462);
    }

    public void TouchLogin3(String str) {
        this._userName = str;
        _phoneNum = str;
        this._psw = "";
        SendMsg(789463);
    }

    public void TouchRegister(String str, String str2, String str3) {
        this._userName = str;
        _phoneNum = str;
        this._checkCode = str2;
        this._psw = str3;
        SendMsg(789464);
    }

    public void SendToGetCheckCode(String str) {
        Log.d("SendToGetCheckCode", "phoneNum：" + str);
        _phoneNum = str;
        if (this._isChinese == 0) {
            SendEmail(str);
        } else {
            SendMsg(264830);
        }
    }

    int GetRandID() {
        return (int) ((Math.random() * ((double) 268435455)) + ((double) 0));
    }

    public void AddNewDevice() {
        Log.d(TAG, "AddNewDevice");
        SendMsg(CMD_AddDevice_AGV);
    }

    public void ActivityAGV(String str, int i, int i2) {
        this._checkCode = str;
        this._fun = i;
        this._deviceID = i2;
        Log.d(TAG, "ActivityAGV");
        SendMsg(CMD_CheckCode_AGV);
    }

    public void OnTouchCopy() {
        Log.d(TAG, "OnTouchCopy");
        SendMsg(CMD_Copy);
    }

    public String GetCopyString() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        if (clipboardManager.getText() != null) {
            String string = clipboardManager.getText().toString();
            Log.d("GetCopyString", "msg:" + string);
            if (string != null) {
                return Pattern.compile("\\s*|\t|\r|\n").matcher(string).replaceAll("");
            }
        }
        return "";
    }

    public int GetYear() {
        return Calendar.getInstance().get(1);
    }

    public int GetMonth() {
        return Calendar.getInstance().get(2);
    }

    public int GetWeek() {
        return Calendar.getInstance().get(7) - 1;
    }

    public int GetDay() {
        return Calendar.getInstance().get(5);
    }

    public int GetHour() {
        return Calendar.getInstance().get(11);
    }

    public int GetMinute() {
        return Calendar.getInstance().get(12);
    }

    public int GetSecond() {
        return Calendar.getInstance().get(13);
    }

    public static String GetPhoneNumber() {
        try {
            if (ActivityCompat.checkSelfPermission(instance, "android.permission.READ_SMS") != 0 && ActivityCompat.checkSelfPermission(instance, "android.permission.READ_PHONE_NUMBERS") != 0 && ActivityCompat.checkSelfPermission(instance, "android.permission.READ_PHONE_STATE") != 0) {
                return "";
            }
            String line1Number = ((TelephonyManager) instance.getSystemService("phone")).getLine1Number();
            if (line1Number == null) {
                Log.e("GetPhoneNumber:", "获取手机号码失败");
                return "";
            }
            Log.d("GetPhoneNumber", "getLine1Number:" + line1Number);
            return line1Number;
        } catch (Exception e) {
            Log.e("GetPhoneNumber", e.toString());
            return "";
        }
    }

    public boolean isPhoneNumberValid(String str) {
        if (Pattern.compile("^1[3456789]\\d{9}$").matcher(str).matches()) {
            Log.d("isPhoneNumberValid：", "有效手机号:" + str);
            return true;
        }
        Log.d("isPhoneNumberValid：", "无效手机号:" + str);
        return false;
    }
}
