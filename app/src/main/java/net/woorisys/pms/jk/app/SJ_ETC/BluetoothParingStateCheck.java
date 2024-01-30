package net.woorisys.pms.jk.app.SJ_ETC;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;

import net.woorisys.pms.jk.app.SJ_BroadCast.ParingStateCheck;


public class BluetoothParingStateCheck {

    ParingStateCheck paringStateCheck;

    public void RegistParing(Context context)
    {
        ParingStateCheck paringStateCheck=new ParingStateCheck();
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(paringStateCheck, filter1);
    }

    public void UnRegistParing(Context context)
    {
        ParingStateCheck paringStateCheck=new ParingStateCheck();
        context.unregisterReceiver(paringStateCheck);
    }
}
