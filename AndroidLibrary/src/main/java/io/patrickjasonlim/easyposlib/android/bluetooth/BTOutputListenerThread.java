package io.patrickjasonlim.easyposlib.android.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class BTOutputListenerThread extends Thread {

    private static final String TAG = BTOutputListenerThread.class.getSimpleName();
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private boolean stopped = false;

    public BTOutputListenerThread(BluetoothSocket socket, InputStream inputStream) {
        mSocket = socket;
        mInputStream = inputStream;
    }

    @Override
    public void run() {
        Log.d(TAG, "run executed");
        while (mSocket.isConnected()) {

            CharBuffer buff = CharBuffer.allocate(1024);
            InputStreamReader reader = new InputStreamReader(mInputStream);
            try {
                reader.read(buff);

                String str = new String(buff.array());

                Log.d(TAG, "Output from BT device: " + str);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (stopped) {
                break;
            }
        }
    }

    public void tryStop() {
        stopped = true;
    }
}
