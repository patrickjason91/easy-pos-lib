package io.patrickjasonlim.easyposlib.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrinterCommand {

    public static final byte ESC = 0x1B;
    public static final byte FF = 0x0C;
    public static final byte LF = 0x0A;
    public static final byte HT = 0x09;
    public static final byte GS = 0x1D;
    public static final byte CR = 0x0D;
    public static final byte FS = 0x1C;
    public static final byte DLE = 0x10;

    public static final byte[] COMMAND_BYTES_LF = { LF };
    public static final byte[] COMMAND_RESET_PRINTER = { ESC, 0x40 };
    public static final byte[] COMMAND_PRINT_RASTER_BIT_IMG = { GS, (byte) 'v', (byte) '0' };
    public static final byte[] COMMAND_FEED_N_DOTS = { ESC, (byte) 'J' };
    public static final byte[] COMMAND_SET_ALIGNMENT = { ESC, 0x61 };
    public static final byte[] COMMAND_SET_CHARACTER_FONT = { ESC, (byte) 'M' };
    public static final byte[] COMMAND_SELECT_PAGE_MODE = { ESC, 0x4C };
    public static final byte[] COMMAND_SELECT_STD_MODE = { ESC, 0x53 };
    public static final byte[] COMMAND_FEED_PAPER_N_LINES = { ESC, (byte) 'd' };
    public static final byte[] COMMAND_SELECT_CHARACTER_SIZE = { GS, (byte) '!' };
    public static final byte[] COMMAND_CUT_PAPER = { GS, (byte) 'V' };
    public static final byte[] COMMAND_TRANSMIT_REALTIME_STATUS = { DLE, 0x04 };
    public static final byte[] COMMAND_ENABLE_AUTO_STATUS_BACK = { GS, 0x61 };
    public static final byte[] COMMAND_EMPHASIZE = { ESC, 'E' };

    public static final byte[] COMMAND_SELECT_CHARACTER_CODE = { ESC, 't' };
    public static final byte[] COMMAND_SELECT_CHARACTER_SET = { ESC, 'R'};

    public static final byte FONT_A = 0;
    public static final byte FONT_B = 1;
    public static final byte FONT_C = 2;

    public static final byte ALIGN_LEFT = 0;
    public static final byte ALIGN_CENTER = 1;
    public static final byte ALIGN_RIGHT = 2;

    public static final byte CHAR_CODE_PAGE_EPSON_THAI_1 = 20;
    public static final byte CHAR_CODE_PAGE_EPSON_THAI_2 = 21;
    public static final byte CHAR_CODE_PAGE_EPSON_THAI_3 = 26;
    public static final byte CHAR_CODE_PAGE_EPSON_VIET_1 = 30;
    public static final byte CHAR_CODE_PAGE_EPSON_VIET_2 = 31;

    private byte[] mCmdBytes;
    private byte[] mParamBytes = new byte[0];

    public PrinterCommand(byte... cmdBytes) {
        mCmdBytes = cmdBytes;
    }

    public PrinterCommand(byte[] cmdBytes, byte... paramBytes) {
        mCmdBytes = cmdBytes;
        addParams(paramBytes);
    }

    public byte[] getFullCommandBytes() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            byteStream.write(mCmdBytes);
            byteStream.write(mParamBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toByteArray();
    }

    public void addParams(byte... paramBytes) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            byteStream.write(mParamBytes);
            byteStream.write(paramBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mParamBytes = byteStream.toByteArray();
    }

    public static PrinterCommand fromBytes(byte[] cmdBytes) {
        PrinterCommand cmd = new PrinterCommand(cmdBytes);
        return cmd;
    }
}
