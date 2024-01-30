package net.woorisys.pms.jk.app.SJ_BroadCast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothStatus extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        switch (state)
        {
            case BluetoothAdapter.STATE_ON:

                Log.d("TAG_ACTION"  , String.valueOf(state));
                break;

            case BluetoothAdapter.STATE_OFF:
                Log.d("TAG_ACTION"  , String.valueOf(state));
                break;
        }
    }
}
