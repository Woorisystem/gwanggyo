package net.woorisys.pms.jk.app.SJ_Singleton;

import android.bluetooth.BluetoothAdapter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BluetoothSingleton {

    @Getter
    @Setter
    private BluetoothAdapter bluetoothAdapterW;

    private static final BluetoothSingleton ourInstance = new BluetoothSingleton();

    public static BluetoothSingleton getInstance() {
        return ourInstance;
    }

    private BluetoothSingleton()
    {
        bluetoothAdapterW=BluetoothAdapter.getDefaultAdapter();
    }
}
