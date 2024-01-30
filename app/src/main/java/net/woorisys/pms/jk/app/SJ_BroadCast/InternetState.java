package net.woorisys.pms.jk.app.SJ_BroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InternetState extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String status = NetworkUtil.getConnectivityStatusString(context);
        Log.d("TAG_TEST","STATUS : "+status);
    }
}
