package net.woorisys.pms.jk.app.SJ_ETC;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Item {

    @Getter @Setter
    private String address;
    private double rssi;
    private int txPower;
    private double distance;
    private int major;
    private int minor;

    public Item(String address, double rssi, int txPower, double distance, int major, int minor) {
        this.address = address;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = distance;
        this.major = major;
        this.minor = minor;
    }
}
