package io.patrickjasonlim.easyposlib.base;

public class Utils {
    private static final byte[] HEX_CHAR = new byte[]{'0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String dumpBytes(byte[] buffer) {

        if (buffer == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        for (int i = 0; i < buffer.length; i++) {
            sb.append((char) (HEX_CHAR[(buffer[i] & 0x00F0) >> 4])).append(
                    (char) (HEX_CHAR[buffer[i] & 0x000F]));
        }
        return sb.toString();
    }
}
