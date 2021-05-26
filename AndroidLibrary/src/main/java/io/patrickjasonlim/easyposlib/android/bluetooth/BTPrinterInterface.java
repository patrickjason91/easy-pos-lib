package io.patrickjasonlim.easyposlib.android.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.patrickjasonlim.easyposlib.base.EscPosPrinterException;
import io.patrickjasonlim.easyposlib.base.PrintBuffer;
import io.patrickjasonlim.easyposlib.base.PrinterInterface;
import io.patrickjasonlim.easyposlib.base.Utils;

public class BTPrinterInterface implements PrinterInterface {
    private static final String TAG = BTPrinterInterface.class.getSimpleName();
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public BTPrinterInterface(InputStream is, OutputStream os) {
        mInputStream = is;
        mOutputStream = os;
    }

    @Override
    public void sendToPrinter(PrintBuffer buffer) throws EscPosPrinterException {
        byte[] buffBytes = buffer.getBuffBytes();
        Log.d(TAG, "Buffer bytes:" + Utils.dumpBytes(buffBytes));
        try {
            mOutputStream.write(buffer.getBuffBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new EscPosPrinterException(e.getMessage());
        }
    }
}
