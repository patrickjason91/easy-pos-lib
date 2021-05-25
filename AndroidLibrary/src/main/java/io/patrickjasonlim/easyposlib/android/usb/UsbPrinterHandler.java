package io.patrickjasonlim.easyposlib.android.usb;

import android.os.Looper;

import io.patrickjasonlim.easyposlib.android.BasePrinterHandler;
import io.patrickjasonlim.easyposlib.base.EscPosPrinter;

/**
 * Created by patricklim on 06/10/2017.
 */

public class UsbPrinterHandler extends BasePrinterHandler {

    private static final String TAG = UsbPrinterHandler.class.getSimpleName();
    private final UsbPrinterInterface mInterface;

    public UsbPrinterHandler(EscPosPrinter printer, UsbPrinterInterface printerInterface, Looper looper) {
        super(looper);
        setEscPosPrinter(printer);

        mInterface = printerInterface;
    }

    public void stop() {
        this.removeCallbacksAndMessages(null);
        try {
            mInterface.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doClosePrinter() {
        stop();
        try {
            getLooper().quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
