package io.patrickjasonlim.easyposlib.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import io.patrickjasonlim.easyposlib.base.BitImage;
import io.patrickjasonlim.easyposlib.base.EscPosPrinter;
import io.patrickjasonlim.easyposlib.base.EscPosPrinterException;
import io.patrickjasonlim.easyposlib.base.PrintCommandSet;
import io.patrickjasonlim.easyposlib.base.PrinterCommand;
import io.patrickjasonlim.easyposlib.base.TextInput;

public abstract class BasePrinterHandler extends Handler {

    public static final int MSG_PRINT = 0x1;
    public static final int MSG_PRINT_IMG = 0x2;
    public static final int MSG_SEND_COMMAND = 0x3;
    public static final int MSG_CLOSE_PRINTER = 0x4;
    public static final int MSG_PRINT_CMD_SET = 0x5;

    private static final String TAG = BasePrinterHandler.class.getSimpleName();

    private EscPosPrinter mPrinter;

    public BasePrinterHandler(Looper looper) {
        super(looper);
    }

    protected abstract void doClosePrinter();

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "handle message");
        switch (msg.what) {
            case MSG_PRINT:
                String str = msg.getData().getString(String.valueOf(msg.what));
                boolean printLine = msg.getData().getBoolean("printline");

                doPrintText(str, printLine);
                break;
            case MSG_PRINT_IMG:
                Log.d(TAG, "msg print");
                Bitmap b = msg.getData().getParcelable(String.valueOf(msg.what));
                boolean invert = msg.getData().getBoolean("invert");
                doPrintBitmap(b, invert);
                break;
            case MSG_SEND_COMMAND:
                PrinterCommand cmd = (PrinterCommand) msg.obj;
                try {
                    mPrinter.sendCommand(cmd);
                } catch (EscPosPrinterException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_PRINT_CMD_SET:
                PrintCommandSet cmdSet = (PrintCommandSet) msg.obj;
                doPrintSet(cmdSet);

                break;
            case MSG_CLOSE_PRINTER:
                doClosePrinter();
                break;
        }
    }

    public void printSet(PrintCommandSet set) {
        Message msg = Message.obtain();
        msg.what = MSG_PRINT_CMD_SET;
        msg.obj = set;
        this.sendMessage(msg);
    }

    public void printImage(Bitmap bitmap, boolean invert) {
        Message msg = generateMessageWithParcelableData(bitmap, MSG_PRINT_IMG);
        msg.peekData().putBoolean("invert", invert);
        this.sendMessage(msg);
    }

    public void printText(String textToPrint) {
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        msg.setData(b);
        b.putString(String.valueOf(MSG_PRINT), textToPrint);
        msg.what = MSG_PRINT;
        this.sendMessage(msg);
    }

    public void printTextLine(String textToPrint) {
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        msg.setData(b);
        b.putString(String.valueOf(MSG_PRINT), textToPrint);
        b.putBoolean("printline", true);
        msg.what = MSG_PRINT;
        this.sendMessage(msg);
    }

    public void sendPrinterCommand(PrinterCommand cmd) {
        Message msg = Message.obtain();
        msg.what = MSG_SEND_COMMAND;
        msg.obj = cmd;
        this.sendMessage(msg);
    }

    public void closePrinter() {
        Message msg = Message.obtain();
        msg.what = MSG_CLOSE_PRINTER;
        this.sendMessage(msg);
    }

    protected Message generateMessageWithParcelableData(Parcelable p, int what) {
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putParcelable(String.valueOf(what), p);
        msg.setData(b);
        msg.what = what;
        return msg;
    }

    protected void doPrintBitmap(Bitmap b, boolean invert) {
        Log.d(TAG, "doPrint this bitmap");
        Bitmap binaryBm = BitmapUtils.createBinarizedBitmap(b, invert);
        Bitmap toPrint = BitmapUtils.resizeBitmapForThermalPrinter(binaryBm, BitmapUtils.WIDTH_THERMAL_PRINTER);

        int width = toPrint.getWidth();
        int height = toPrint.getHeight();
        int[][] pixels = BitmapUtils.getBitmapPixels(toPrint);

        BitImage img = new BitImage(pixels, width, height);
        PrinterCommand lfCmd = new PrinterCommand(PrinterCommand.COMMAND_BYTES_LF);

        try {
            mPrinter.printBitmap(img);
            mPrinter.sendCommand(lfCmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    protected void doPrintText(String text, boolean printLine) {
        if (!printLine) {
            TextInput input = new TextInput(text);
            // prepare...
            mPrinter.setCharacterFont(1);
            try {
                mPrinter.print(input);
            } catch (EscPosPrinterException e) {
                e.printStackTrace();
            }
        } else {
            TextInput input = new TextInput(text);
            mPrinter.setCharacterFont(1);
            try {
                mPrinter.printLine(input);
            } catch (EscPosPrinterException e) {
                e.printStackTrace();
            }
        }
    }

    protected void doPrintSet(PrintCommandSet set) {
        try {
            mPrinter.printSet(set);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public EscPosPrinter getEscPosPrinter() {
        return mPrinter;
    }

    public void setEscPosPrinter(EscPosPrinter printer) {
        mPrinter = printer;
    }
}
