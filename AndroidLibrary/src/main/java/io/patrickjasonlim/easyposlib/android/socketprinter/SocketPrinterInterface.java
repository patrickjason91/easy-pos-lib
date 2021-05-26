package io.patrickjasonlim.easyposlib.android.socketprinter;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import io.patrickjasonlim.easyposlib.base.EscPosPrinterException;
import io.patrickjasonlim.easyposlib.base.PrintBuffer;
import io.patrickjasonlim.easyposlib.base.PrinterInterface;
import io.patrickjasonlim.easyposlib.base.Utils;

public class SocketPrinterInterface implements PrinterInterface {

    private static final String TAG = SocketPrinterInterface.class.getSimpleName();
    private OutputStream mSocketOutputStream;

    public SocketPrinterInterface(OutputStream outputStream) {
        mSocketOutputStream = outputStream;
    }

    @Override
    public void sendToPrinter(PrintBuffer buffer) throws EscPosPrinterException {
        byte[] buffBytes =buffer.getBuffBytes();
        Log.d(TAG,"Buffer bytes:" + Utils.dumpBytes(buffBytes));
        try {
            mSocketOutputStream.write(buffer.getBuffBytes());
            mSocketOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new EscPosPrinterException(e.getMessage());
        }
    }
}
