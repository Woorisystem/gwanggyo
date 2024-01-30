package net.woorisys.pms.jk.app.SJ_BroadCast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;

public class ParingStateCheck extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.d("BroadcastActions", "Action "+action+", received");
        int state;
        BluetoothDevice bluetoothDevice;

        switch(action)
        {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF)
                {
                    Log.d("BroadcastActions", "Bluetooth is off");
                }
                else if (state == BluetoothAdapter.STATE_TURNING_OFF)
                {
                    Log.d("BroadcastActions", "Bluetooth is turning off");
                }
                else if(state == BluetoothAdapter.STATE_ON)
                {
                    Log.d("BroadcastActions", "Bluetooth is on");
                }
                break;

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DataManagerSingleton.getInstance().setParingStateSave(true);
                DataManagerSingleton.getInstance().setParingState(true);

                Log.d("BroadcastActions", "Connected to "+bluetoothDevice.getName()+" , SAVE VALUE : "+DataManagerSingleton.getInstance().isParingStateSave());
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("BroadcastActions", "DisConnected to "+bluetoothDevice.getName()+" , SAVE VALUE : "+DataManagerSingleton.getInstance().isParingStateSave());

                DataManagerSingleton.getInstance().setParingState(false);
                break;
        }
    }
}
