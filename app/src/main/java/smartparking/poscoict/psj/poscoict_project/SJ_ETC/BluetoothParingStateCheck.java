package smartparking.poscoict.psj.poscoict_project.SJ_ETC;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;

import smartparking.poscoict.psj.poscoict_project.SJ_BroadCast.ParingStateCheck;


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
