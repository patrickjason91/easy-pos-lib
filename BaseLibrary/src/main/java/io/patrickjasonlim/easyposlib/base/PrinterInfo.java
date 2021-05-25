package io.patrickjasonlim.easyposlib.base;

public class PrinterInfo {

    public String macAddress;
    public String name;

    @Override
    public boolean equals(Object obj) {
        return (macAddress != null) && (obj instanceof PrinterInfo)
                && this.macAddress.equalsIgnoreCase(((PrinterInfo) obj).macAddress);
    }
}
