package io.patrickjasonlim.easyposlib.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrintBuffer {

    private PrinterCommand cmd;
    private PrinterInput input;

    private PrintBuffer() {
    }

    public byte[] getBuffBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            if (cmd != null) {
                os.write(cmd.getFullCommandBytes());
            }
            if (input != null) {
                os.write(input.getByteArrayValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public static class Builder {

        private static PrintBuffer buff;

        public Builder() {
            buff = new PrintBuffer();
        }

        public Builder command(PrinterCommand cmd) {
            buff.cmd = cmd;
            return this;
        }

        public Builder input(PrinterInput input) {
            buff.input = input;
            return this;
        }

        public PrintBuffer build() {
            return buff;
        }
    }
}
