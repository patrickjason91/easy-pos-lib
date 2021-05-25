package io.patrickjasonlim.easyposlib.android.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import io.patrickjasonlim.easyposlib.base.EscPosPrinterException;
import io.patrickjasonlim.easyposlib.base.PrintBuffer;
import io.patrickjasonlim.easyposlib.base.PrinterInterface;

/**
 * Created by patricklim on 06/10/2017.
 */

public class UsbPrinterInterface implements PrinterInterface {

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final String TAG = UsbPrinterInterface.class.getSimpleName();
    private static final boolean FORCE_CLAIM = true;

    private UsbManager mManager;
    private UsbDevice mDevice;

    private UsbDeviceConnection mConn;
    private UsbInterface mIntf;
    private UsbEndpoint mEndpoint;

    private OnUsbPrinterListener mListener;

    public UsbPrinterInterface(UsbManager manager, UsbDevice device, UsbInterface intf, UsbEndpoint endpoint) {
        mManager = manager;
        mDevice = device;
        mIntf = intf;
        mEndpoint = endpoint;
    }

    public boolean open() {
        mConn = mManager.openDevice(mDevice);
        if (mConn != null) {
            boolean claimed = mConn.claimInterface(mIntf, FORCE_CLAIM);
            return claimed;
        }
        return false;
    }

    public void stop() {
        try {
            mConn.releaseInterface(mIntf);
            mConn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToPrinter(PrintBuffer buffer) throws EscPosPrinterException {
        byte[] buffBytes = buffer.getBuffBytes();
        int len = buffBytes.length;
        if (mListener != null) {
            mListener.onPrintStarted();
        }
        int transmittedLen = mConn.bulkTransfer(mEndpoint, buffBytes, len, DEFAULT_TIMEOUT);
        Log.d(TAG, "sendToPrinter() -> print buffer: " + buffBytes + ", transmitted length: " + transmittedLen);
        boolean success = transmittedLen >= 0; // success if transmittedlen is not negative
        if (mListener != null) {
            mListener.onPrinterFinished(transmittedLen, success);
        }
    }

    public void setOnUsbPrinterListener(OnUsbPrinterListener listener) {
        mListener = listener;
    }

    public interface OnUsbPrinterListener {
        void onPrintStarted();

        void onPrinterFinished(int transmittedLength, boolean success);
    }
}
