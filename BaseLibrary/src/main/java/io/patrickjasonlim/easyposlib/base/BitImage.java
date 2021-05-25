package io.patrickjasonlim.easyposlib.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitImage extends PrinterInput {

    public static final int COLOR_BLACK = 0xFF000000;
    public static final int COLOR_WHITE = 0xFFFFFFFF;

    private int[][] mBitmapArr;
    private int mWidth;
    private int mHeight;

    private byte[] bitImageParams;
    private byte[] bitImageArray;

    public BitImage(int[][] bitmapArr, int width, int height) {
        mBitmapArr = bitmapArr;
        mWidth = width;
        mHeight = height;
        init();
        initParams();
    }

    public static PrintBuffer getImagePrintBuffer(BitImage img) {

        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_PRINT_RASTER_BIT_IMG, (byte) 0x0);

        PrintBuffer buff = new PrintBuffer.Builder()
                .command(cmd)
                .input(img)
                .build();

        return buff;
    }

    private void init() {

        int divWidth = mWidth / 8;
        int modWidth = mWidth % 8;
        divWidth = divWidth + (modWidth > 0 ? 1 : 0);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        for (int y = 0; y < mHeight; ++y) {
            for (int x = 0; x < divWidth; ++x) {
                byte slice = 0;
                int startCol = x * 8;
                for (int bits = 0; bits < 8; bits++) {
                    int col = startCol + bits;
                    if (col >= mWidth) {

                        break;
                    }
                    int pix = mBitmapArr[y][col];
                    int black = pix == COLOR_BLACK ? 1 : 0;

                    slice |= (byte) (black << (7 - bits));
                }
                byteStream.write(slice);
            }
        }
        bitImageArray = byteStream.toByteArray();
    }

    private void initParams() {
        // xL, xH, yL, yH
        byte[] params = new byte[4];
        int width = mWidth;
        int height = mHeight;

        int divWidth = width / 8;
        int modWidth = width % 8;

        divWidth = divWidth + (modWidth > 0 ? 1 : 0);

        params[0] = (byte) (0x00ff & divWidth);
        params[1] = (byte) ((0xff00 & divWidth) >> 8); // value should be computed, for now use 0
        params[2] = (byte) (0x00ff & height);
        params[3] = (byte) ((0xff00 & height) >> 8); // value should be computed, for now use 0

        bitImageParams = params;
    }

    @Override
    public byte[] getByteArrayValue() {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            byteStream.write(bitImageParams);
            byteStream.write(bitImageArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toByteArray();
    }
}
