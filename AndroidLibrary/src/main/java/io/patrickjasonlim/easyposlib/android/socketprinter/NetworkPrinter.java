package io.patrickjasonlim.easyposlib.android.socketprinter;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkPrinter implements Parcelable {

    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE_INVALID = -1;

    public long id;
    public String name = "";
    public String ipAddress = "";
    public String location = "";

    public int printerStatus;

    public NetworkPrinter() {}

    protected NetworkPrinter(Parcel in) {
        id = in.readLong();
        name = in.readString();
        ipAddress = in.readString();
        location = in.readString();
        printerStatus = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(ipAddress);
        dest.writeString(location);
        dest.writeInt(printerStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NetworkPrinter> CREATOR = new Creator<NetworkPrinter>() {
        @Override
        public NetworkPrinter createFromParcel(Parcel in) {
            return new NetworkPrinter(in);
        }

        @Override
        public NetworkPrinter[] newArray(int size) {
            return new NetworkPrinter[size];
        }
    };
}
