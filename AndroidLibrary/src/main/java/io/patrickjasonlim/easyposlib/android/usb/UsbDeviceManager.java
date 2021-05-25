package io.patrickjasonlim.easyposlib.android.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Collection;

public class UsbDeviceManager {

    private static final String TAG = UsbDeviceManager.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION";
    private static final int REQ_CODE_USB_PERMISSION = 100;

    private final UsbManager mManager;
    private final Context mContext;

    private final IntentFilter mFilter;
    private OnUsbDeviceDiscoveryListener mListener;

    private boolean mDiscoverStarted = false;
    private BroadcastReceiver mPrinterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.d(TAG, "onReceive() -> USB device: " + device.toString());
                    boolean hasPermission = hasPermission(device);
                    if (hasPermission) {
                        if (mListener != null) {
                            mListener.onDeviceAttached(device);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onPermissionAsked(device);
                        }
                    }
                }
            } else if (action.equals(ACTION_USB_PERMISSION)) {
                boolean permissionAllowed = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (mListener != null) {
                    mListener.onPermissionResult(permissionAllowed);
                }
                if (permissionAllowed) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.d(TAG, "onReceive() -> USB device: " + device.toString());
                    if (mListener != null) {
                        mListener.onDeviceAttached(device);
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (mListener != null) {
                    mListener.onDeviceDetached(device);
                }
            }
        }
    };

    public UsbDeviceManager(Context context, UsbManager manager) {
        mContext = context;
        mManager = manager;

        mFilter = new IntentFilter();
        mFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mFilter.addAction(ACTION_USB_PERMISSION);
    }

    /**
     * Determine if a {@link UsbDevice} is of a printer class
     *
     * @param device
     * @return
     */
    public static boolean isPrinter(UsbDevice device) {
        UsbInterface intf = getPrinterInterface(device);
        return intf != null; // no interface OR interface is not a printer..
    }

    public static UsbInterface getPrinterInterface(UsbDevice device) {
        UsbInterface intf = null;
        for (int i = 0, count = device.getInterfaceCount(); i < count; i++) {
            UsbInterface currIntf = device.getInterface(i);
            Log.d(TAG, "getPrinterInterface() -> UsbInterface + " + currIntf.toString() + ", interface class: " + currIntf.getInterfaceClass() + ", interface subclass " + currIntf.getInterfaceSubclass());
            boolean isPrinter = currIntf.getInterfaceSubclass() == UsbConstants.USB_CLASS_PRINTER
                    || currIntf.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER;
            if (isPrinter) {
                intf = currIntf;
                break;
            }
        }
        return intf;
    }

    public static UsbEndpoint getPrinterOutEndpoint(UsbInterface intf) {
        UsbEndpoint endpoint = null;
        for (int i = 0, count = intf.getEndpointCount(); i < count; i++) {
            UsbEndpoint currEndpoint = intf.getEndpoint(i);
            Log.d(TAG, "UsbENDPOINT + " + currEndpoint.toString() + ", endpoint type: " + currEndpoint.getType() + ", " + currEndpoint.getDirection());
            if (currEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                endpoint = currEndpoint;
                //break;
            }
        }
        return endpoint;
    }

    public UsbDevice getAttachedDevice() {
        Collection<UsbDevice> deviceList = mManager.getDeviceList().values();
        UsbDevice device = null;
        if (!deviceList.isEmpty()) {
            for (UsbDevice d :
                    deviceList) {
                device = d;
            }
        }
        return device;
    }

    public UsbDevice getAttachedPrinterDevice() {
        Collection<UsbDevice> deviceList = mManager.getDeviceList().values();
        UsbDevice device = null;
        if (!deviceList.isEmpty()) {
            for (UsbDevice d :
                    deviceList) {
                if (isPrinter(d)) {
                    device = d;
                    break;
                }
            }
        }
        return device;
    }

    public boolean hasUsbDeviceAttached() {
        return getAttachedDevice() != null;
    }

    public void setOnUsbDiscoveryListener(OnUsbDeviceDiscoveryListener listener) {
        mListener = listener;
    }

    public void startDeviceDiscovery() {
        mContext.registerReceiver(mPrinterReceiver, mFilter);
        mDiscoverStarted = true;
    }

    public void stopDiscovery() {
        try {
            mContext.unregisterReceiver(mPrinterReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        mDiscoverStarted = false;
    }

    public boolean isDiscoveryStarted() {
        return mDiscoverStarted;
    }

    public void requestUsbPermission(UsbDevice usbDevice) {
        Intent intent = new Intent(ACTION_USB_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, REQ_CODE_USB_PERMISSION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mManager.requestPermission(usbDevice, pendingIntent);
    }

    public boolean hasPermission(UsbDevice device) {
        return mManager.hasPermission(device);
    }

    public interface OnUsbDeviceDiscoveryListener {
        void onPermissionAsked(UsbDevice device);

        void onPermissionResult(boolean granted);

        void onDeviceAttached(UsbDevice device);

        void onDeviceDetached(UsbDevice device);
    }

}
