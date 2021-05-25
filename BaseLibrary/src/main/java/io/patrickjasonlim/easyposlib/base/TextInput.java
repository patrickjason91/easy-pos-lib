package io.patrickjasonlim.easyposlib.base;

public class TextInput extends PrinterInput {

    public String value;
    public byte[] byteVal;
    public String encoding = "UTF-8";

    public TextInput(String text) {
        this.value = text;
    }

    public TextInput(String text, String encoding) {
        this.value = text;
        this.encoding = encoding;
    }

    @Override
    public byte[] getByteArrayValue() {
        if (value != null) {
            try {
                return value.getBytes(encoding);
            } catch (Exception e) {
                e.printStackTrace();
                return new byte[0];
            }
        } else {
            return new byte[0];
        }
    }
}
