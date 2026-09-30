package org.cocos2dx.cpp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class DeviceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BluetoothDevice> devices;

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        return view;
    }

    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> arrayList) {
        this.context = context;
        this.devices = arrayList;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        ArrayList<BluetoothDevice> arrayList = this.devices;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return this.devices.get(i);
    }

    private static class ViewHolder {
        private TextView deviceAddress;
        private TextView deviceName;

        public ViewHolder(View view) {
        }
    }
}
