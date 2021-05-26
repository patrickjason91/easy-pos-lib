package io.patrickjasonlim.easyposlib.android;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import io.patrickjasonlim.easyposlib.base.BitImage;
import io.patrickjasonlim.easyposlib.base.Utils;

public class BitmapUtils {

    public static final int WIDTH_THERMAL_PRINTER = 280; // 240 as of now, ISSUE: if bitmap height exceeds 256, error in printer
    private static final String TAG = BitmapUtils.class.getSimpleName();

    /*

    Operations needed for printing of graphic
    - convert to binary image (b/w)
      - from colored bitmap,
    - resize image, according to the width of the pos receipt
    -
     */
    public static Bitmap createBinarizedBitmap(Bitmap bmpOriginal, boolean isInverted) {

//        Bitmap b = Bitmap.createBitmap(origBitmap);
//        Bitmap bmpMonochrome = Bitmap.createBitmap(origBitmap.getWidth(), origBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bmpMonochrome);
//        ColorMatrix ma = new ColorMatrix();
//        ma.setSaturation(0);
//        Paint paint = new Paint();
//        paint.setColorFilter(new ColorMatrixColorFilter(ma));
//        canvas.drawBitmap(origBitmap, 0, 0, paint);
//
//        for (int h = 0; h < b.getHeight(); h++) {
//
//            for (int w = 0; w < b.getHeight(); w++) {
//                int pix = b.getPixel(w, h);
//
//                pix
//            }
//        }
        int width, height, threshold;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        threshold = 127;
        Bitmap bmpBinary = bmpOriginal.copy(bmpOriginal.getConfig(), true);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get one pixel color
                int pixel = bmpOriginal.getPixel(x, y);
                int gray = (int) (Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11);
                //get binary value
                if (gray < threshold) {
                    bmpBinary.setPixel(x, y, isInverted ? Color.BLACK : Color.WHITE);
                } else {
                    bmpBinary.setPixel(x, y, isInverted ? Color.WHITE : Color.BLACK);
                }

            }
        }
        return bmpBinary;
    }

    public static Bitmap resizeBitmapForThermalPrinter(Bitmap bitmap, int thermalPrintWidth) {
        /*
        Thermal printer has
        - 384dot/line
        - 58mm
        Convert the image to
        - width of < 384 pixels
        - aspect ratio preserve
         */

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap resized = null;

        if (width > thermalPrintWidth) {
            float ratio = (float) height / (float) width;
            int widthFactored = thermalPrintWidth;
            int heightFactored = (int) (widthFactored * ratio);
            Log.d(TAG, "resizeBitmap size: " + widthFactored + "x" + heightFactored);
            resized = Bitmap.createScaledBitmap(bitmap, widthFactored, heightFactored, false);
        } else {
            resized = bitmap;
        }

        return resized;
    }

    public static int[] getBitmapArray(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixelArr = new int[w * h];
        bitmap.getPixels(pixelArr, 0, w, 0, 0, w, h); // the pixel array, the start pos, the stride, x start, y start, width, height

        return pixelArr;
    }

    public static byte[] convertBitmapToBinaryArray(Bitmap bitmap) {
        byte[] imgBytes = null;
        // TODO convert the pixels to byte array, suitable for the thermal printer GS v 0 command
        // convert the pixels to individual bits, then pack to bytes

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int divWidth = width / 8;
        int modWidth = width % 8;
        divWidth = divWidth + (modWidth > 0 ? 1 : 0);

        int totalBytes = divWidth * height;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        // iterate the rows
        // from rows, iterate the height
        // from height, iterate the width
        // slice the width to multiples of 8 (one byte is 8 bits), each bit is one color
        //
        for (int y = 0; y < height; ++y) {
            int b = 0;
            for (int x = 0; x < divWidth; ++x) {
                byte slice = 0;
                int startCol = x * 8;
                for (int bits = 0; bits < 8; bits++) {
                    int col = startCol + bits;
                    if (col >= width) {

                        break;
                    }
                    int pix = bitmap.getPixel(col, y);
                    int black = pix == Color.BLACK ? 1 : 0;

                    slice |= (byte) (black << (7 - bits));
                }
                byteStream.write(slice);


//                int pix = bitmap.getPixel(y, x);
//                int black = pix == Color.BLACK ? 1 : 0;
//
//                int mod8 = (y + 1) % 8;
//                slice |= (byte) ( black << (7 - b));
//                b++;
//                if (( y + 1 ) == width) {
//                    byteStream.write(slice);
//                    break;
//                }
//                if (mod8 == 0) {
//                    byteStream.write(slice);
//                    byteCt++;
//                    slice = 0;
//                }
            }
        }
        imgBytes = byteStream.toByteArray();

        Log.d(TAG, "byte ct: " + imgBytes.length + ", totalbytes computed: " + totalBytes + ", byte dump: " + Utils.dumpBytes(imgBytes));

        return imgBytes;
    }

    public static byte[] getRasterCommandDimensionParams(Bitmap bitmap) {
        // xL, xH, yL, yH
        byte[] params = new byte[4];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int divWidth = width / 8;
        int modWidth = width % 8;

        divWidth = divWidth + (modWidth > 0 ? 1 : 0);

        params[0] = (byte) (0x00ff & divWidth);
        params[1] = (byte) ((0xff00 & divWidth) >> 8); // value should be computed, for now use 0
        params[2] = (byte) (0x00ff & height);
        params[3] = (byte) ((0xff00 & height) >> 8); // value should be computed, for now use 0

        Log.d(TAG, "Width div 8: " + divWidth);
        Log.d(TAG, "params: " + Utils.dumpBytes(params));
        return params;
    }

    public static int[][] getBitmapPixels(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[][] pixelsArr = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pix = bitmap.getPixel(x, y);
                pixelsArr[y][x] = pix;
            }
        }
        return pixelsArr;
    }

    public static BitImage convertToOptimalBitImage(Bitmap b, boolean invert) {
        Bitmap binaryBm = BitmapUtils.createBinarizedBitmap(b, invert);
        Bitmap toPrint = BitmapUtils.resizeBitmapForThermalPrinter(binaryBm, BitmapUtils.WIDTH_THERMAL_PRINTER);

        int width = toPrint.getWidth();
        int height = toPrint.getHeight();
        int[][] pixels = BitmapUtils.getBitmapPixels(toPrint);

        BitImage img = new BitImage(pixels, width, height);

        return img;
    }
}
