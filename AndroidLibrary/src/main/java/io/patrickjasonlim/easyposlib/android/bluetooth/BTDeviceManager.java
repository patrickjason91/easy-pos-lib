package io.patrickjasonlim.easyposlib.android.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Set;

import io.patrickjasonlim.easyposlib.android.Constants;
import io.patrickjasonlim.easyposlib.base.PrinterInfo;

public class BTDeviceManager {

    private static final String TAG = BTDeviceManager.class.getSimpleName();

    private BluetoothAdapter mBtAdapter;
    private Context mContext;
    private long durationMs;

    private OnBluetoothDeviceDiscoveryListener mListener;
    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    if (mListener != null) {
                        mListener.onDiscoveryStarted();
                    }
                    mDiscoveryTimer.sendMessageDelayed(Message.obtain(), durationMs);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "Device discovery finished..stopping discovery");
                    Toast.makeText(mContext, "Bluetooth device scanning finished", Toast.LENGTH_SHORT).show();
                    if (mListener != null) {
                        mListener.onDiscoveryFinished();
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice deviceFound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "Device found: " + deviceFound.getName());
                    boolean isPrinter = isPrinter(deviceFound);
                    if (isPrinter) {
                        if (mListener != null) {
                            mListener.onDeviceDiscovered(deviceFound);
                        }
                    }
                    //Toast.makeText(getApplicationContext(), "Device found: " + deviceFound.getName(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Handler mDiscoveryTimer = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // TODO stop discovery
            stopBluetoothDiscovery();
        }
    };

    public BTDeviceManager(Context context, BluetoothAdapter btAdapter) {
        mContext = context;
        mBtAdapter = btAdapter;
    }

    /**
     * Checks if the given {@code BluetoothDevice} is of a printer class, specifically checking if major device class
     * of the deivce is {@code BluetoothClass.Device.Major.IMAGING}
     *
     * @param device BT device to check for
     * @return if device is a possible instance of a printer
     */
    public static boolean isPrinter(BluetoothDevice device) {
        boolean check = false;
        BluetoothClass btClass = device.getBluetoothClass();
        if (btClass != null) {
            check = btClass.getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING;
        }
        return check;
    }

    public void setOnDeviceDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mListener = listener;
    }

    public void promptEnableBluetooth(Activity activity, Fragment fragment) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (fragment != null) {
            fragment.startActivityForResult(intent, Constants.REQ_CODE_BT_REQUEST_ENABLE);
        } else {
            activity.startActivityForResult(intent, Constants.REQ_CODE_BT_REQUEST_ENABLE);
        }
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (isBluetoothOn()) {
            return mBtAdapter.getBondedDevices();
        }
        return null;
    }

    public void startBluetoothDiscovery(long durationMs) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        try {
            mContext.registerReceiver(mBtReceiver, filter);
            Toast.makeText(mContext, "Bluetooth device scanning started", Toast.LENGTH_SHORT).show();
            this.durationMs = durationMs;
            mBtAdapter.startDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBluetoothDiscovery() {
        mDiscoveryTimer.removeCallbacksAndMessages(null);
        if (mBtAdapter.isDiscovering()) {
            try {
                mBtAdapter.cancelDiscovery();
                mContext.unregisterReceiver(mBtReceiver);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, "Receiver not registered, graceful execution...");
            }
        }
    }

    public boolean isBluetoothOn() {
        return mBtAdapter.isEnabled();
    }

    public void goToBluetoothSettings() {
        ComponentName cn = new ComponentName(Constants.COMPONENT_NAME_SETTINGS,
                Constants.COMPONENT_NAME_BLUETOOTH_SETTINGS);
        Intent i = new Intent();
        i.setComponent(cn);
        if (i.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(i);
        }
    }

    public BluetoothDevice getBluetoothDevice(PrinterInfo info) {
        return mBtAdapter.getRemoteDevice(info.macAddress);
    }

    public interface OnBluetoothDeviceDiscoveryListener {
        void onDiscoveryStarted();

        void onDiscoveryFinished();

        void onDeviceDiscovered(BluetoothDevice device);
    }
}
