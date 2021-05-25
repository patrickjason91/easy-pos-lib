package io.patrickjasonlim.easyposlib.base;

public class EscPosPrinter {

    private PrinterInterface mPrinterInterface;

    public EscPosPrinter(PrinterInterface printerInterface) {
        mPrinterInterface = printerInterface;
    }

    public void selectPageMode() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SELECT_PAGE_MODE);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void selectStdMode() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SELECT_STD_MODE);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void resetPrinter() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_RESET_PRINTER);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void printBitmapEsc(PrinterInput input) {
        // TODO create a print method using ESC * command printing one row of bit image
    }

    public void printBitmap(BitImage bitImage) throws EscPosPrinterException {
        // GS v 0 m xL xH yL yH d1 … dk
        // 1D  76 30 m xL xH yL yH [d]k

        // actually code for the bitmap to be printed
//        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//        try {
//            byteStream.write(new byte[] {GS, 0x76, 0x30, 0x0});
//            byteStream.write(params);
//            byteStream.write(bitmapBytes);
//            byteStream.write(LF);
//            byteStream.write(LF);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        byte[] printBytes = {
//                        GS, 0x76, 0x30,
//                        0x30,0x4,0x0,0x05,0x0,
//                (byte)0xC3, (byte)0xC3, (byte)0xC3, (byte)0xC3,
//                (byte)0xC3, (byte)0xC3, (byte)0xC3, (byte)0xC3,
//                (byte)0xC3, (byte)0xC3, (byte)0xC3, (byte)0xC3,
//                (byte)0xC3, (byte)0xC3, (byte)0xC3, (byte)0xC3,
//                (byte)0xC3, (byte)0xC3, (byte)0xC3, (byte)0xC3,
//                };
        //byte[] cmd = byteStream.toByteArray();

        //Utils.logDebug(TAG, "Hex dump: " + Utils.dumpBytes(cmd));

        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_PRINT_RASTER_BIT_IMG);
        cmd.addParams((byte) 0x0);

        send(cmd, bitImage);
    }

    public void print(TextInput input) throws EscPosPrinterException {
        sendInput(input);
    }

    public void printLine(TextInput input) throws EscPosPrinterException {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_BYTES_LF);
        sendInput(input);
        sendCommand(cmd);
    }

    public void printMultipleLines(TextInput... input) throws EscPosPrinterException {
        for (TextInput i : input) {
            printLine(i);
        }
    }

    public void addSpaces(int n) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_FEED_PAPER_N_LINES);
        cmd.addParams((byte) n);

        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    /*
        General methods for sending commands and inputs
     */

    public void sendCommand(PrinterCommand cmd) throws EscPosPrinterException {
        PrintBuffer buff = new PrintBuffer.Builder()
                .command(cmd)
                .build();
        mPrinterInterface.sendToPrinter(buff);
    }

    public void sendInput(PrinterInput input) throws EscPosPrinterException {
        PrintBuffer buff = new PrintBuffer.Builder()
                .input(input)
                .build();
        mPrinterInterface.sendToPrinter(buff);
    }

    public void send(PrinterCommand cmd, PrinterInput input) throws EscPosPrinterException {
        PrintBuffer buff = new PrintBuffer.Builder()
                .command(cmd)
                .input(input)
                .build();
        mPrinterInterface.sendToPrinter(buff);
    }

    public void printSet(PrintCommandSet set) throws EscPosPrinterException {
        while (set.hasNext()) {
            PrintBuffer buff = set.getNext();
            mPrinterInterface.sendToPrinter(buff);
        }
    }

    public void setAlignment(int alignment) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SET_ALIGNMENT);
        cmd.addParams((byte) alignment);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void setCharacterFont(int fontType) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SET_CHARACTER_FONT);
        cmd.addParams((byte) fontType);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void setCharacterSize(int charSize) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SELECT_CHARACTER_SIZE);
        cmd.addParams((byte) charSize);
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    public void cutPaper() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_CUT_PAPER);
        cmd.addParams((byte) '0');
        try {
            sendCommand(cmd);
        } catch (EscPosPrinterException e) {
            e.printStackTrace();
        }
    }

    private void setFontMode() {
//        buffer.write(ESC);
//        buffer.write((byte) 0x4D);
//        buffer.write((byte) 0x01);

    }
}
