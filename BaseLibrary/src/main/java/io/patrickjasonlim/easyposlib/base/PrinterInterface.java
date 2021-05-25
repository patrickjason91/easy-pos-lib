package io.patrickjasonlim.easyposlib.base;

public interface PrinterInterface {

    void sendToPrinter(PrintBuffer buffer) throws EscPosPrinterException;
}
