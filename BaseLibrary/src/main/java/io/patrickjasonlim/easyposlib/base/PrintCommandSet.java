package io.patrickjasonlim.easyposlib.base;

import java.util.ArrayDeque;
import java.util.Queue;

public class PrintCommandSet {

    private Queue<PrintBuffer> mBufferQueue;

    public PrintCommandSet() {
        mBufferQueue = new ArrayDeque<>();
    }

    /**
     * @param text
     * @return
     */
    public PrintCommandSet addText(String text) {
        TextInput textInput = new TextInput(text);
        return addPrinterInput(textInput);
    }

    public PrintCommandSet addText(String text, int charType) {
        String encoding = PrinterEncoding.getCharsetByEncodingType(charType);
        setCharacterCode(PrinterEncoding.getCharCodePageByEncodingType(charType));
        TextInput textInput = new TextInput(text, encoding);
        addPrinterInput(textInput);
        resetCharacterCode();
        return this;
    }

    public PrintCommandSet addText(String text, boolean autoHandleEncoding) {
        if (autoHandleEncoding) {
            return addText(text, PrinterEncoding.getCharEncodingType(text));
        } else {
            return addText(text);
        }
    }

    /**
     * @param text
     * @return
     */
    public PrintCommandSet addTextLine(String text) {
        return addText(text).addLineFeedCommand();
    }

    public PrintCommandSet addTextLine(String text, int charType) {
        return addText(text, charType).addLineFeedCommand();
    }

    public PrintCommandSet addTextLine(String text, boolean autoHandleEncoding) {
        if (autoHandleEncoding) {
            return addText(text, PrinterEncoding.getCharEncodingType(text)).addLineFeedCommand();
        } else {
            return addText(text).addLineFeedCommand();
        }
    }

    /**
     * @param fontType
     * @return
     */
    public PrintCommandSet addCharacterFont(int fontType) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SET_CHARACTER_FONT);
        cmd.addParams((byte) fontType);
        return addPrinterCommand(cmd);
    }

    /**
     * @param command
     * @param input
     * @return
     */
    public PrintCommandSet addPrinterCommandInput(PrinterCommand command, PrinterInput input) {
        PrintBuffer buff = new PrintBuffer.Builder()
                .command(command)
                .input(input)
                .build();
        return addPrintBuffer(buff);
    }

    /**
     * @param command
     * @return
     */
    public PrintCommandSet addPrinterCommand(PrinterCommand command) {
        PrintBuffer buff = new PrintBuffer.Builder()
                .command(command)
                .build();

        return addPrintBuffer(buff);
    }

    /**
     * @param input
     * @return
     */
    public PrintCommandSet addPrinterInput(PrinterInput input) {
        PrintBuffer buff = new PrintBuffer.Builder()
                .input(input)
                .build();
        return addPrintBuffer(buff);
    }

    /**
     * @return
     */
    public PrintCommandSet addLineFeedCommand() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_BYTES_LF);
        return addPrinterCommand(cmd);
    }

    /**
     * @param buffer
     * @return
     */
    public PrintCommandSet addPrintBuffer(PrintBuffer buffer) {
        mBufferQueue.add(buffer);
        return this;
    }

    public PrintCommandSet addRawCommand(byte[] cmd) {
        return addPrinterCommand(PrinterCommand.fromBytes(cmd));
    }

    public PrintCommandSet addLineFeedCommand(int lines) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_FEED_PAPER_N_LINES);
        cmd.addParams((byte) lines);
        return addPrinterCommand(cmd);
    }

    public PrintCommandSet setAlignment(byte alignment) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SET_ALIGNMENT);
        cmd.addParams(alignment);
        return addPrinterCommand(cmd);
    }

    public PrintCommandSet addTab() {
        return addPrinterCommand(PrinterCommand.fromBytes(new byte[] { PrinterCommand.HT }));
    }

    public PrintCommandSet paperCut(int lines) {
        PrinterCommand cmd = PrinterCommand.fromBytes(PrinterCommand.COMMAND_CUT_PAPER);
        cmd.addParams((byte) 65, (byte) lines);
        return addPrinterCommand(cmd);
    }

    public PrintCommandSet setCharacterCode(byte characterCode) {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SELECT_CHARACTER_CODE, characterCode);

        return addPrinterCommand(cmd);
    }

    public PrintCommandSet resetCharacterCode() {
        PrinterCommand cmd = new PrinterCommand(PrinterCommand.COMMAND_SELECT_CHARACTER_CODE, (byte) 0);

        return addPrinterCommand(cmd);
    }

    public PrintCommandSet resetPrinter() {
        return addPrinterCommand(PrinterCommand.fromBytes(PrinterCommand.COMMAND_RESET_PRINTER));
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return mBufferQueue.peek() != null;
    }

    /**
     * @return
     */
    public PrintBuffer getNext() {
        PrintBuffer buff = mBufferQueue.poll();
        return buff;
    }

    public void clear() {
        mBufferQueue.clear();
    }
}
