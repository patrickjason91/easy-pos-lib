package io.patrickjasonlim.easyposlib.android.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.patrickjasonlim.easyposlib.base.PrinterInfo;

public class BTDevicesListAdapter extends RecyclerView.Adapter<BTDevicesListAdapter.BTDevicesViewHolder> {

    private static final String TAG = BTDevicesListAdapter.class.getSimpleName();
    private Context mContext;
    private LinkedHashSet<BluetoothDevice> mDevicesSet = new LinkedHashSet<>();
    private List<BluetoothDevice> mDevices = new ArrayList<>();

    private EventListener mListener;

    public BTDevicesListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public BTDevicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null; // LayoutInflater.from(mContext).inflate(R.layout.item_devices_list, parent, false);
        BTDevicesViewHolder holder = new BTDevicesViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BTDevicesViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);
        Log.d(TAG, "Device: " + device.getName() + ", device class: " + device.getBluetoothClass().getMajorDeviceClass() + ", MAC address: " + device.getAddress());
        holder.bindItem(device);
    }

    @Override
    public int getItemCount() {
        if (mDevices != null) {
            return mDevices.size();
        }
        return 0;
    }

    public void addBluetoothDevice(BluetoothDevice device) {
        if (BTDeviceManager.isPrinter(device)) {
            mDevicesSet.add(device);
            toList();
            this.notifyDataSetChanged();
        }
    }

    public void addBluetoothDevices(Set<BluetoothDevice> devices) {
        Iterator<BluetoothDevice> iter = devices.iterator();
        while (iter.hasNext()) {
            BluetoothDevice device = iter.next();
            if (BTDeviceManager.isPrinter(device)) {
                mDevicesSet.add(device);
            }
        }
        toList();
        this.notifyDataSetChanged();
    }

    private void toList() {
        mDevices.clear();
        mDevices.addAll(mDevicesSet);
    }

    public void setItems(Set<BluetoothDevice> devices) {
//        mDevices.addAll(devices);
//        this.notifyDataSetChanged();
    }

    public void resetDevicesList() {
//        mDevices.clear();
//        this.notifyDataSetChanged();
        mDevicesSet.clear();
        this.notifyDataSetChanged();
    }

    public void setEventListener(EventListener listener) {
        mListener = listener;
    }

    public interface EventListener {
        void onItemClick(BluetoothDevice device);
    }

    class BTDevicesViewHolder extends RecyclerView.ViewHolder {

        TextView tvDeviceName, tvDeviceAddr;
        BluetoothDevice device;

        public BTDevicesViewHolder(View itemView) {
            super(itemView);

//            tvDeviceName = (TextView) itemView.findViewById(R.id.tv_device_name);
//            tvDeviceAddr = (TextView) itemView.findViewById(R.id.tv_device_addr);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onItemClick(device);
                    }
                }
            });
        }

        public void bindItem(BluetoothDevice device) {
            this.device = device;
            PrinterInfo info = null;  // SQLiteHelper.getInstance(mContext).getPrinterInfoByAddress(device.getAddress());

            if (info != null) {
                tvDeviceName.setText(info.name);
            } else {
                tvDeviceName.setText(device.getName());
            }
            tvDeviceAddr.setText(device.getAddress());
        }
    }
}
