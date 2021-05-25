package io.patrickjasonlim.easyposlib.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.patrickjasonlim.easyposlib.android.BasePrinterHandler;
import io.patrickjasonlim.easyposlib.android.Constants;
import io.patrickjasonlim.easyposlib.base.EscPosPrinter;

public class BluetoothPrinterHandler extends BasePrinterHandler {

    public static final int MSG_CONNECT_DEVICE = 0x6;

    private static final String TAG = BluetoothPrinterHandler.class.getSimpleName();

    private BluetoothDevice mDevice;
    private BluetoothSocket clientSocket;
    private InputStream mClientInputStream;
    private OutputStream mClientOutputStream;

    private SubscriptionResultCallback mCallback;

    private Handler mResultHandler;

    public BluetoothPrinterHandler(BluetoothDevice device, Looper looper) {
        super(looper);
        mDevice = device;
        mResultHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.d(TAG, "handle message");
        if (msg.what == MSG_CONNECT_DEVICE) {
            connectToDevice();
        }
    }

    private void connectToDevice() {
        try {
            if (!isConnected()) {
                clientSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constants.APP_UUID));

                clientSocket.connect();
                mClientInputStream = clientSocket.getInputStream();
                mClientOutputStream = clientSocket.getOutputStream();

            }

            BTPouchPrinterInterface btPouchPrinter = new BTPouchPrinterInterface(mClientInputStream, mClientOutputStream);
            EscPosPrinter printer = new EscPosPrinter(btPouchPrinter);
            setEscPosPrinter(printer);

            Log.d(TAG, "Bluetooth Socket connected! Input stream is " + mClientInputStream + ", output stream is : " + mClientOutputStream);


            publishResult(MSG_CONNECT_DEVICE, true);

//            if (clientSocket == null) {
//                clientSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constants.APP_UUID));
//            }
//
//            clientSocket.connect();
//            mClientInputStream = clientSocket.getInputStream();
//            mClientOutputStream = clientSocket.getOutputStream();
//
//            BTPouchPrinterInterface btPouchPrinter = new BTPouchPrinterInterface(mClientInputStream, mClientOutputStream);
//            printer = new EscPosPrinter(btPouchPrinter);
//
//            Log.d(TAG, "Bluetooth Socket connected! Input stream is " + mClientInputStream + ", output stream is : " + mClientOutputStream);
//
//
//            publishResult(MSG_CONNECT_DEVICE, true);
        } catch (IOException e) {
            e.printStackTrace();
            publishResult(MSG_CONNECT_DEVICE, false);
        }
    }

    public boolean isConnected() {
        if (clientSocket != null) {
            return clientSocket.isConnected();
        } else {
            return false;
        }
    }

    @Override
    protected void doClosePrinter() {
        if (clientSocket != null) {
            Log.d(TAG, "handleMessage() -> CLOSE PRINTER");
            try {
                clientSocket.close();
                clientSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.removeCallbacksAndMessages(null);
                getLooper().quit();
            }

        }
    }

    public void setSubscriptionCallback(SubscriptionResultCallback callback) {
        mCallback = callback;
    }

    private void publishResult(final int operationType, final boolean success) {
        mResultHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onOperationFinished(BluetoothPrinterHandler.this, operationType, success);
                }
            }
        });
    }

    public interface SubscriptionResultCallback<T> {
        void onOperationFinished(BluetoothPrinterHandler handlerInstance, int operationType, boolean success);
    }
}
