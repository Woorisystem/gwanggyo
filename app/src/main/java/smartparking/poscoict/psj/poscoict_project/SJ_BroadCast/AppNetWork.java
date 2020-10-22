package smartparking.poscoict.psj.poscoict_project.SJ_BroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import smartparking.poscoict.psj.poscoict_project.SJ_Server.ServerData;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.DataManagerSingleton;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.TimerSingleton;


public class AppNetWork extends BroadcastReceiver {

    public static final String EVENT_NETWORK_CHAGED = "network_changed";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if(isConnected) {

                    Toast.makeText(context,"인터넷 재연결.",Toast.LENGTH_SHORT).show();

                    if(!TimerSingleton.getInstance().isWholeTimerStart())
                    {
                        ServerData serverData=new ServerData(context);
                        serverData.Send(context, DataManagerSingleton.getInstance().getTotalArrayList().get(DataManagerSingleton.getInstance().getTotalArrayList().size()-1));
                    }

                    context.unregisterReceiver(this);

                    }
                else{
                    Toast.makeText(context,"인터넷 연결 끊김.",Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e("ULNetworkReceiver", e.getMessage());
            }
        }
    }

}
