package io.patrickjasonlim.easyposlib.base;

public class EscPosPrinterException extends Exception {

    public EscPosPrinterException() {
        this("There was an error in the printer.");
    }

    public EscPosPrinterException(String message) {
        super(message);
    }
}
