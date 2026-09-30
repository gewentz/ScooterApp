package org.cocos2dx.cpp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class BluetoothHolder {
    private static final String TAG = "BluetoothHolder";
    private boolean bindService;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeService bluetoothLeService;
    private BluetoothManager bluetoothManager;
    private BroadcastCallback broadcastCallback;
    private boolean connected;
    private Context context;
    private String deviceAddress;
    private String deviceName;
    private HashMap<BluetoothGattService, ArrayList<BluetoothGattCharacteristic>> gattServiceData;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private boolean scanning;
    public static final UUID UUID_NOTIFY2 = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID UUID_SERVICE2 = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE3 = UUID.fromString("0000ae00-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_NOTIFY3 = UUID.fromString("0000ae01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_Read3 = UUID.fromString("0000ae02-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE4 = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_NOTIFY4 = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_Read4 = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE5 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_NOTIFY5 = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_Read5 = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_NOTIFY6 = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_Read6 = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static BluetoothHolder holder = new BluetoothHolder();
    private static int _isSmallType = 1;
    private ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
    private final ServiceConnection serviceConnection = new ServiceConnection() { // from class: org.cocos2dx.cpp.BluetoothHolder.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(BluetoothHolder.TAG, "管理服务生命周期on");
            BluetoothHolder.this.bluetoothLeService = (BluetoothLeService) ((BaseService.LocalBinder) iBinder).getService();
            if (!BluetoothHolder.this.bluetoothLeService.initialize()) {
                Log.e(BluetoothHolder.TAG, "无法初始化蓝牙");
            }
            if (TextUtils.isEmpty(BluetoothHolder.this.deviceAddress) || TextUtils.isEmpty(BluetoothHolder.this.deviceName)) {
                return;
            }
            Log.e(BluetoothHolder.TAG, "初始化成功启动自动连接到设备");
            BluetoothHolder bluetoothHolder = BluetoothHolder.this;
            bluetoothHolder.connect(bluetoothHolder.deviceName, BluetoothHolder.this.deviceAddress);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(BluetoothHolder.TAG, "管理服务生命周期off");
            BluetoothHolder.this.bluetoothLeService = null;
            BluetoothHolder.this.connected = false;
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: org.cocos2dx.cpp.BluetoothHolder.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                if (BluetoothHolder.this.broadcastCallback != null) {
                    BluetoothHolder.this.broadcastCallback.connected(intent);
                    BluetoothHolder.this.connected = true;
                    return;
                }
                return;
            }
            if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                BluetoothHolder.this.connected = false;
                if (BluetoothHolder.this.broadcastCallback != null) {
                    BluetoothHolder.this.broadcastCallback.disconnected(intent);
                    return;
                }
                return;
            }
            if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (BluetoothHolder.this.bluetoothLeService != null) {
                    BluetoothHolder bluetoothHolder = BluetoothHolder.this;
                    bluetoothHolder.searchGattServices(bluetoothHolder.bluetoothLeService.getSupportedGattServices());
                }
                if (BluetoothHolder.this.broadcastCallback != null) {
                    BluetoothHolder.this.broadcastCallback.discovered(intent);
                    return;
                }
                return;
            }
            if (!BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                BluetoothHolder.this.connected = false;
                return;
            }
            if (intent == null || intent.getExtras() == null) {
                return;
            }
            byte[] byteArray = intent.getExtras().getByteArray(BluetoothService.EXTRA_DATA);
            if (BluetoothHolder.this.broadcastCallback == null || byteArray == null) {
                if (BluetoothHolder.this.broadcastCallback == null) {
                    Log.d("onReceive data", "(broadcastCallback == null");
                }
                if (byteArray == null) {
                    Log.d("onReceive data", "(data == null");
                    return;
                }
                return;
            }
            BluetoothHolder.this.broadcastCallback.data(intent, byteArray);
        }
    };

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    private BluetoothHolder() {
    }

    public static boolean init(Context context) {
        BluetoothHolder bluetoothHolder = holder;
        bluetoothHolder.context = context;
        return bluetoothHolder.initBluetootAdaper();
    }

    public static BluetoothHolder getInstance() {
        BluetoothHolder bluetoothHolder = holder;
        if (bluetoothHolder.context != null) {
            return bluetoothHolder;
        }
        throw new NullPointerException("请在此之前先初始化：BluetoothHolder.init(context)");
    }

    public boolean isSupportedBLE() {
        return this.context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    public boolean initBluetootAdaper() {
        if (isSupportedBLE()) {
            BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService("bluetooth");
            this.bluetoothManager = bluetoothManager;
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            this.bluetoothAdapter = adapter;
            if (adapter != null) {
                return true;
            }
            Log.e(TAG, "bluetoothAdapter == null");
            return true;
        }
        Log.d(TAG, "不支持蓝牙");
        return false;
    }

    public boolean openBluetooth() {
        if (this.bluetoothAdapter != null) {
            initBluetootAdaper();
        }
        if (isOpen()) {
            return false;
        }
        Log.d(TAG, "打开蓝牙");
        return this.bluetoothAdapter.enable();
    }

    public boolean isOpen() {
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public boolean isBluetoothAvailable() {
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled() && this.bluetoothAdapter.getState() == 12;
    }

    public void scanDevice(boolean z, BluetoothAdapter.LeScanCallback leScanCallback) {
        if (isBluetoothAvailable()) {
            this.scanning = z;
            if (z) {
                Log.d(TAG, "开始扫描蓝牙");
                this.bluetoothAdapter.startLeScan(null, leScanCallback);
                return;
            } else {
                Log.d(TAG, "结束扫描蓝牙");
                this.bluetoothAdapter.stopLeScan(leScanCallback);
                return;
            }
        }
        Log.d(TAG, "bluetoothAdapter == null");
        openBluetooth();
    }

    public boolean isScanning() {
        return this.scanning;
    }

    public boolean isBindService() {
        return this.bindService;
    }

    public void bindService() {
        if (this.bindService) {
            return;
        }
        String str = TAG;
        Log.d(str, "绑定服务");
        boolean zBindService = this.context.bindService(new Intent(this.context, (Class<?>) BluetoothLeService.class), this.serviceConnection, 1);
        this.bindService = zBindService;
        if (zBindService) {
            Log.d(str, "绑定成功");
        } else {
            Log.e(str, "绑定失败");
        }
    }

    public void unbindService(boolean z) {
        if (this.bindService) {
            Log.d(TAG, "解除绑定服务");
            this.context.unbindService(this.serviceConnection);
        }
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService != null) {
            bluetoothLeService.close();
        }
        this.bindService = false;
        this.bluetoothLeService = null;
    }

    public boolean connect(String str, String str2) {
        String str3 = TAG;
        Log.e(str3, "connect");
        if (this.bluetoothLeService != null) {
            this.deviceName = str;
            this.deviceAddress = str2;
            AppSaveData.getInstance().setDataForString("deviceAddress", this.deviceAddress);
            return this.bluetoothLeService.connect(this.deviceAddress);
        }
        Log.e(str3, "bluetoothLeService == null");
        return false;
    }

    public boolean reConnect() {
        String str = TAG;
        Log.d(str, "reConnect");
        if (this.bluetoothLeService != null) {
            this.deviceAddress = AppSaveData.getInstance().getDataForString("deviceAddress", this.deviceAddress);
            Log.d(str, "reConnect:deviceAddress" + this.deviceAddress);
            return this.bluetoothLeService.connect(this.deviceAddress);
        }
        Log.e(str, "bluetoothLeService == null");
        return false;
    }

    public void disconnect() {
        this.characteristics.clear();
        if (this.bluetoothLeService != null) {
            Log.d(TAG, "断开连接");
            this.bluetoothLeService.disconnect();
        }
    }

    public void readCharacteristic() {
        ArrayList<BluetoothGattCharacteristic> next;
        HashMap<BluetoothGattService, ArrayList<BluetoothGattCharacteristic>> map = this.gattServiceData;
        if (map == null || map.isEmpty()) {
            return;
        }
        Iterator<ArrayList<BluetoothGattCharacteristic>> it = this.gattServiceData.values().iterator();
        while (it.hasNext() && (next = it.next()) != null) {
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : next) {
                int properties = bluetoothGattCharacteristic.getProperties();
                if ((properties | 2) > 0) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic2 = this.notifyCharacteristic;
                    if (bluetoothGattCharacteristic2 != null) {
                        this.bluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristic2, false, _isSmallType);
                        this.notifyCharacteristic = null;
                    }
                    this.bluetoothLeService.readCharacteristic(bluetoothGattCharacteristic);
                }
                if ((properties | 16) > 0) {
                    this.notifyCharacteristic = bluetoothGattCharacteristic;
                    this.bluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristic, true, _isSmallType);
                }
            }
        }
    }

    public void enableTXNotification() {
        BluetoothLeService bluetoothLeService;
        if (_isSmallType == 0 || (bluetoothLeService = this.bluetoothLeService) == null) {
            return;
        }
        bluetoothLeService.enableTXNotification();
    }

    public void writeCharacteristic(byte[] bArr) {
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService == null) {
            Log.e(TAG, "bluetoothLeService == null");
            return;
        }
        if (_isSmallType == 1) {
            bluetoothLeService.writeCharacteristic(bArr);
            return;
        }
        HashMap<BluetoothGattService, ArrayList<BluetoothGattCharacteristic>> map = this.gattServiceData;
        if (map == null || map.isEmpty()) {
            return;
        }
        Iterator<ArrayList<BluetoothGattCharacteristic>> it = this.gattServiceData.values().iterator();
        while (it.hasNext()) {
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : it.next()) {
                int properties = bluetoothGattCharacteristic.getProperties();
                this.notifyCharacteristic = null;
                if ((properties | 16) > 0) {
                    this.notifyCharacteristic = bluetoothGattCharacteristic;
                    BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
                    if (bluetoothLeService2 != null) {
                        bluetoothLeService2.setCharacteristicNotification(bluetoothGattCharacteristic, true, _isSmallType);
                    }
                }
                if ((properties | 8) > 0) {
                    bluetoothGattCharacteristic.setValue(bArr);
                    if (this.notifyCharacteristic != null) {
                        this.bluetoothLeService.writeCharacteristic(bluetoothGattCharacteristic);
                    }
                }
            }
        }
    }

    public void registerReceiver(BroadcastCallback broadcastCallback) {
        String str = TAG;
        Log.e(str, "已经注册广播接收者");
        this.broadcastCallback = broadcastCallback;
        if (Build.VERSION.SDK_INT >= 33) {
            this.context.registerReceiver(this.broadcastReceiver, makeGattUpdateIntentFilter(), 2);
        } else {
            this.context.registerReceiver(this.broadcastReceiver, makeGattUpdateIntentFilter());
        }
        if (this.bluetoothLeService == null || TextUtils.isEmpty(this.deviceAddress)) {
            return;
        }
        Log.d(str, "连接请求的结果：" + this.bluetoothLeService.connect(this.deviceAddress));
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void unregisterReceiver(boolean z) {
        this.context.unregisterReceiver(this.broadcastReceiver);
        this.broadcastCallback = null;
        if (z) {
            this.deviceAddress = null;
        }
    }

    public boolean isConnected() {
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        return bluetoothLeService != null && bluetoothLeService.getConnectionState() > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:27:0x00de  */
    /* JADX WARN: Code duplicated, block: B:28:0x00e5 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:29:0x00e7  */
    /* JADX WARN: Code duplicated, block: B:30:0x00ee A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:31:0x00f0  */
    /* JADX WARN: Code duplicated, block: B:32:0x00f7 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:33:0x00f9  */
    /* JADX WARN: Code duplicated, block: B:37:0x010d  */
    /* JADX WARN: Code duplicated, block: B:39:0x012a  */
    /* JADX WARN: Code duplicated, block: B:40:0x0148  */
    /* JADX WARN: Code duplicated, block: B:42:0x015d A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:43:0x015f  */
    /* JADX WARN: Code duplicated, block: B:45:0x0182  */
    /* JADX WARN: Code duplicated, block: B:47:0x0196  */
    /* JADX WARN: Code duplicated, block: B:48:0x019b  */
    /* JADX WARN: Code duplicated, block: B:50:0x01af  */
    /* JADX WARN: Code duplicated, block: B:51:0x01cf  */
    /* JADX WARN: Code duplicated, block: B:53:0x01e3  */
    /* JADX WARN: Code duplicated, block: B:54:0x0202  */
    /* JADX WARN: Code duplicated, block: B:56:0x0216 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:57:0x0218  */
    /* JADX WARN: Code duplicated, block: B:59:0x023c  */
    /* JADX WARN: Instruction removed from duplicated block: B:39:0x012a, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:50:0x01af, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:53:0x01e3, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:59:0x023c, please report this as an issue */
    public void searchGattServices(List<BluetoothGattService> list) {
        int i;
        ArrayList<BluetoothGattCharacteristic> arrayList;
        String string;
        if (list == null) {
            return;
        }
        this.gattServiceData = new HashMap<>();
        Log.d(TAG, "开始遍历可用的服务");
        for (BluetoothGattService bluetoothGattService : list) {
            String str = TAG;
            Log.d(str, "Sevice uuid : " + bluetoothGattService.getUuid().toString());
            if (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                i = _isSmallType == 1 ? 3 : 0;
                arrayList = new ArrayList<>();
                this.bluetoothLeService.SetAppType(i);
                Log.d(str, "type:" + i);
                string = UUID_NOTIFY2.toString();
                if (i == 0) {
                    string = UUID_NOTIFY.toString();
                } else if (i == 2) {
                    string = UUID_NOTIFY3.toString();
                } else if (i == 3) {
                    string = UUID_NOTIFY4.toString();
                } else if (i == 4) {
                    string = UUID_NOTIFY5.toString();
                }
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                    this.bluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristic, true, _isSmallType);
                    if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(string)) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "写特征:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY6.toString())) {
                        if (i == 4) {
                            this.bluetoothLeService.SetAppType(5);
                        }
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "写特征:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read3.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read4.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read4:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read5.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read5:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read6.toString())) {
                        if (i == 4) {
                            this.bluetoothLeService.SetAppType(5);
                        }
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read6:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else {
                        Log.d(TAG, "UUID_NOTIFY=" + bluetoothGattCharacteristic.getUuid().toString());
                    }
                    arrayList.add(bluetoothGattCharacteristic);
                    Log.d(TAG, "特征值:" + bluetoothGattCharacteristic.getUuid().toString());
                }
                this.gattServiceData.put(bluetoothGattService, arrayList);
            } else {
                if (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE2.toString())) {
                    _isSmallType = 1;
                    i = 1;
                } else if (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE3.toString())) {
                    _isSmallType = 1;
                    i = 2;
                } else if (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE4.toString())) {
                    _isSmallType = 1;
                } else if (bluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE5.toString())) {
                    _isSmallType = 1;
                    i = 4;
                }
                arrayList = new ArrayList<>();
                this.bluetoothLeService.SetAppType(i);
                Log.d(str, "type:" + i);
                string = UUID_NOTIFY2.toString();
                if (i == 0) {
                    string = UUID_NOTIFY.toString();
                } else if (i == 2) {
                    string = UUID_NOTIFY3.toString();
                } else if (i == 3) {
                    string = UUID_NOTIFY4.toString();
                } else if (i == 4) {
                    string = UUID_NOTIFY5.toString();
                }
                while (r3.hasNext()) {
                    this.bluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristic, true, _isSmallType);
                    if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(string)) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "写特征:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY6.toString())) {
                        if (i == 4) {
                            this.bluetoothLeService.SetAppType(5);
                        }
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "写特征:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read3.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read4.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read4:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read5.toString())) {
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read5:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_Read6.toString())) {
                        if (i == 4) {
                            this.bluetoothLeService.SetAppType(5);
                        }
                        arrayList.add(bluetoothGattCharacteristic);
                        Log.d(TAG, "UUID_Read6:" + bluetoothGattCharacteristic.getUuid().toString());
                    } else {
                        Log.d(TAG, "UUID_NOTIFY=" + bluetoothGattCharacteristic.getUuid().toString());
                    }
                    arrayList.add(bluetoothGattCharacteristic);
                    Log.d(TAG, "特征值:" + bluetoothGattCharacteristic.getUuid().toString());
                }
                this.gattServiceData.put(bluetoothGattService, arrayList);
            }
        }
    }

    public static int IsSmallType() {
        return _isSmallType;
    }

    public static void SetIsSmallType(int i) {
        _isSmallType = i;
    }
}
