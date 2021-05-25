package io.patrickjasonlim.easyposlib.base;

public class PrinterEncoding {

    public static final int ENCODING_UTF = 1;
    public static final int ENCODING_THAI = 2;
    public static final int ENCODING_VIET = 4;
    public static final int ENCODING_CN = 8;

    public static int getCharEncodingType(String... str) {
        int type = ENCODING_UTF;
        for (String s: str) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                Character.UnicodeBlock codeBlock = Character.UnicodeBlock.of(ch);
                boolean isThai = Character.UnicodeBlock.THAI.equals(codeBlock);
                if (isThai) {
                    return ENCODING_THAI;
                }
            }
        }
        return type;
    }

    public static String getCharsetByEncodingType(int encoding) {
        // TODO handle other character encoding/char set
        if (encoding == ENCODING_UTF) {
            return "ISO-8859-1";
        } else if (encoding == ENCODING_THAI) {
            return "ISO-8859-11";
        }
        return "ISO-8859-1";
    }

    public static byte getCharCodePageByEncodingType(int encoding) {
        // TODO handle other character encoding/char set
        if (encoding == ENCODING_UTF) {
            return 0;
        } else if (encoding == ENCODING_THAI) {
            return PrinterCommand.CHAR_CODE_PAGE_EPSON_THAI_3;
        }
        return 0;
    }
}
