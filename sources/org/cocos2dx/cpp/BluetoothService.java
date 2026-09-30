package org.cocos2dx.cpp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kotlin.UByte;

/* JADX INFO: loaded from: classes.dex */
public class BluetoothService extends BaseService {
    public static final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    private static final String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_SERVICE_UUID3 = UUID.fromString("0000ae00-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID3 = UUID.fromString("0000ae01-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID3 = UUID.fromString("0000ae02-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID4 = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID4 = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID4 = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID5 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID5 = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID5 = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID6 = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID6 = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private int connectionState = 0;
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() { // from class: org.cocos2dx.cpp.BluetoothService.1
        @Override // android.bluetooth.BluetoothGattCallback
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 != 2) {
                if (i2 == 0) {
                    BluetoothService.this.connectionState = 0;
                    Log.i(BluetoothService.TAG, "Disconnected from GATT server.");
                    BluetoothService.this.broadcastUpdate(BluetoothService.ACTION_GATT_DISCONNECTED);
                    return;
                }
                return;
            }
            BluetoothService.this.connectionState = 2;
            BluetoothService.this.broadcastUpdate(BluetoothService.ACTION_GATT_CONNECTED);
            Log.i(BluetoothService.TAG, "Connected to GATT server.");
            Log.i(BluetoothService.TAG, "Attempting to start service discovery:" + BluetoothService.this.bluetoothGatt.discoverServices());
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            if (i == 0) {
                BluetoothService.this.broadcastUpdate(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
                return;
            }
            Log.w(BluetoothService.TAG, "onServicesDiscovered received: " + i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (i == 0) {
                Log.d(BluetoothService.TAG, "status == BluetoothGatt.GATT_SUCCESS");
                BluetoothService.this.broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
            } else {
                Log.e(BluetoothService.TAG, "status != BluetoothGatt.GATT_SUCCESS");
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BluetoothService.this.broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
        }
    };
    private int _appType = 1;

    public BluetoothGatt GetBluetoothGatt() {
        return this.bluetoothGatt;
    }

    public int getConnectionState() {
        return this.connectionState;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastUpdate(String str) {
        sendBroadcast(new Intent(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastUpdate(String str, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Intent intent = new Intent(str);
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value != null && value.length > 0) {
            for (int i = 0; i < value.length; i++) {
                value[i] = (byte) (value[i] & UByte.MAX_VALUE);
            }
            intent.putExtra(EXTRA_DATA, value);
        }
        sendBroadcast(intent);
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if (this.bluetoothManager == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            this.bluetoothManager = bluetoothManager;
            if (bluetoothManager == null) {
                Log.e(TAG, "无法初始化BluetoothManager");
                return false;
            }
        }
        BluetoothAdapter adapter = this.bluetoothManager.getAdapter();
        this.bluetoothAdapter = adapter;
        if (adapter != null) {
            return true;
        }
        Log.e(TAG, "无法获得BluetoothAdapter");
        return false;
    }

    public boolean connect(String str) {
        if (this.bluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter没有初始化");
            initialize();
        }
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        if (bluetoothAdapter == null) {
            return false;
        }
        if (str == null) {
            Log.e(TAG, "BluetoothAdapter未指明的地址");
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            Log.e(TAG, "没有找到该设备，无法连接");
            return false;
        }
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            this.bluetoothGatt = null;
        }
        this.bluetoothGatt = remoteDevice.connectGatt(this, false, this.gattCallback);
        Log.d(TAG, "试图创建一个新的连接");
        this.bluetoothDeviceAddress = str;
        this.connectionState = 1;
        return true;
    }

    public void disconnect() {
        BluetoothGatt bluetoothGatt;
        if (this.bluetoothAdapter == null || (bluetoothGatt = this.bluetoothGatt) == null) {
            Log.w(TAG, "BluetoothAdapter没有初始化");
        } else {
            bluetoothGatt.disconnect();
        }
    }

    public void close() {
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        this.bluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt;
        if (this.bluetoothAdapter == null || (bluetoothGatt = this.bluetoothGatt) == null) {
            Log.d(TAG, "BluetoothAdapter没有初始化");
            return;
        }
        boolean characteristic = bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
        Log.d(TAG, "******** readCharacteristic: " + characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        BluetoothGatt bluetoothGatt;
        if (this.bluetoothAdapter == null || (bluetoothGatt = this.bluetoothGatt) == null) {
            Log.d(TAG, "BluetoothAdapter没有初始化");
        } else {
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    public void writeCharacteristic(byte[] bArr) {
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt == null) {
            return;
        }
        int i = this._appType;
        if (i == 2) {
            BluetoothGattService service = bluetoothGatt.getService(RX_SERVICE_UUID3);
            if (service == null) {
                Log.e(TAG, "RxService3 == null");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(RX_CHAR_UUID3);
            if (characteristic == null) {
                Log.e(TAG, "sendCharacteristic3 == null");
                return;
            }
            characteristic.setValue(bArr);
            if (!this.bluetoothGatt.writeCharacteristic(characteristic)) {
                Log.e(TAG, "写入数据失败");
                return;
            } else {
                Log.d(TAG, "写入数据成功");
                return;
            }
        }
        if (i == 3) {
            BluetoothGattService service2 = bluetoothGatt.getService(RX_SERVICE_UUID4);
            if (service2 == null) {
                Log.e(TAG, "RxService4 == null");
                return;
            }
            BluetoothGattCharacteristic characteristic2 = service2.getCharacteristic(RX_CHAR_UUID4);
            if (characteristic2 == null) {
                Log.e(TAG, "sendCharacteristic4 == null");
                return;
            }
            characteristic2.setValue(bArr);
            if (!this.bluetoothGatt.writeCharacteristic(characteristic2)) {
                Log.e(TAG, "写入数据失败");
                return;
            } else {
                Log.d(TAG, "写入数据成功");
                return;
            }
        }
        if (i == 4) {
            BluetoothGattService service3 = bluetoothGatt.getService(RX_SERVICE_UUID5);
            if (service3 == null) {
                Log.e(TAG, "RxService4 == null");
                return;
            }
            BluetoothGattCharacteristic characteristic3 = service3.getCharacteristic(RX_CHAR_UUID5);
            if (characteristic3 == null) {
                Log.e(TAG, "sendCharacteristic5 == null");
                return;
            }
            characteristic3.setValue(bArr);
            if (!this.bluetoothGatt.writeCharacteristic(characteristic3)) {
                Log.e(TAG, "写入数据失败");
                return;
            } else {
                Log.d(TAG, "写入数据成功");
                return;
            }
        }
        if (i == 5) {
            BluetoothGattService service4 = bluetoothGatt.getService(RX_SERVICE_UUID5);
            if (service4 == null) {
                Log.e(TAG, "RxService4 == null");
                return;
            }
            BluetoothGattCharacteristic characteristic4 = service4.getCharacteristic(RX_CHAR_UUID6);
            if (characteristic4 == null) {
                Log.e(TAG, "sendCharacteristic5 == null");
                return;
            }
            characteristic4.setValue(bArr);
            if (!this.bluetoothGatt.writeCharacteristic(characteristic4)) {
                Log.e(TAG, "写入数据失败");
                return;
            } else {
                Log.d(TAG, "写入数据成功");
                return;
            }
        }
        BluetoothGattService service5 = bluetoothGatt.getService(RX_SERVICE_UUID);
        if (service5 == null) {
            Log.e(TAG, "RxService == null");
            return;
        }
        BluetoothGattCharacteristic characteristic5 = service5.getCharacteristic(RX_CHAR_UUID);
        if (characteristic5 == null) {
            Log.e(TAG, "sendCharacteristic == null");
            return;
        }
        characteristic5.setValue(bArr);
        if (!this.bluetoothGatt.writeCharacteristic(characteristic5)) {
            Log.e(TAG, "写入数据失败");
        } else {
            Log.d(TAG, "写入数据成功");
        }
    }

    public void SetAppType(int i) {
        this._appType = i;
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z, int i) {
        BluetoothGatt bluetoothGatt;
        if (this.bluetoothAdapter == null || (bluetoothGatt = this.bluetoothGatt) == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
        } else {
            Log.e(TAG, "bluetoothGatt == null");
        }
        if (i == 0) {
            return;
        }
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(CCCD);
        if (descriptor == null) {
            Log.e(TAG, "clientConfig == null");
        } else if (this.bluetoothGatt != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt == null) {
            Log.e(TAG, "bluetoothGatt == null");
            return new ArrayList();
        }
        return bluetoothGatt.getServices();
    }

    public void enableTXNotification() {
        String str = TAG;
        Log.d(str, "enableTXNotification");
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt == null) {
            return;
        }
        int i = this._appType;
        if (i == 2) {
            BluetoothGattService service = bluetoothGatt.getService(RX_SERVICE_UUID3);
            if (service == null) {
                Log.e(str, "RxService == null");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(TX_CHAR_UUID3);
            if (characteristic == null) {
                Log.e(str, "TxChar == null");
                return;
            }
            this.bluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.bluetoothGatt.writeDescriptor(descriptor);
                return;
            }
            return;
        }
        if (i == 3) {
            BluetoothGattService service2 = bluetoothGatt.getService(RX_SERVICE_UUID4);
            if (service2 == null) {
                Log.e(str, "RxService == null");
                return;
            }
            BluetoothGattCharacteristic characteristic2 = service2.getCharacteristic(TX_CHAR_UUID4);
            if (characteristic2 == null) {
                Log.e(str, "TxChar == null");
                return;
            }
            this.bluetoothGatt.setCharacteristicNotification(characteristic2, true);
            BluetoothGattDescriptor descriptor2 = characteristic2.getDescriptor(CCCD);
            if (descriptor2 != null) {
                descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.bluetoothGatt.writeDescriptor(descriptor2);
                return;
            }
            return;
        }
        if (i == 4) {
            BluetoothGattService service3 = bluetoothGatt.getService(RX_SERVICE_UUID5);
            if (service3 == null) {
                Log.e(str, "RxService == null");
                return;
            }
            BluetoothGattCharacteristic characteristic3 = service3.getCharacteristic(TX_CHAR_UUID5);
            if (characteristic3 == null) {
                Log.e(str, "TxChar5 == null");
                return;
            }
            this.bluetoothGatt.setCharacteristicNotification(characteristic3, true);
            BluetoothGattDescriptor descriptor3 = characteristic3.getDescriptor(CCCD);
            if (descriptor3 != null) {
                descriptor3.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.bluetoothGatt.writeDescriptor(descriptor3);
                return;
            }
            return;
        }
        if (i == 5) {
            BluetoothGattService service4 = bluetoothGatt.getService(RX_SERVICE_UUID5);
            if (service4 == null) {
                Log.e(str, "RxService == null");
                return;
            }
            BluetoothGattCharacteristic characteristic4 = service4.getCharacteristic(TX_CHAR_UUID6);
            if (characteristic4 == null) {
                Log.e(str, "TxChar5 == null");
                return;
            }
            this.bluetoothGatt.setCharacteristicNotification(characteristic4, true);
            BluetoothGattDescriptor descriptor4 = characteristic4.getDescriptor(CCCD);
            if (descriptor4 != null) {
                descriptor4.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.bluetoothGatt.writeDescriptor(descriptor4);
                return;
            }
            return;
        }
        BluetoothGattService service5 = bluetoothGatt.getService(RX_SERVICE_UUID);
        if (service5 == null) {
            Log.e(str, "RxService == null");
            return;
        }
        BluetoothGattCharacteristic characteristic5 = service5.getCharacteristic(TX_CHAR_UUID);
        if (characteristic5 == null) {
            Log.e(str, "TxChar == null");
            return;
        }
        this.bluetoothGatt.setCharacteristicNotification(characteristic5, true);
        BluetoothGattDescriptor descriptor5 = characteristic5.getDescriptor(CCCD);
        if (descriptor5 != null) {
            descriptor5.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.bluetoothGatt.writeDescriptor(descriptor5);
        }
    }
}
