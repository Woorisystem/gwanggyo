package smartparking.poscoict.psj.poscoict_project.SJ_BroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.service.BeaconService;

import smartparking.poscoict.psj.poscoict_project.SJ_ETC.BluetoothParingStateCheck;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.ServerData;
import smartparking.poscoict.psj.poscoict_project.SJ_Service.SensorService;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.IntentDataSingleton;


public class BroadCastManager extends BroadcastReceiver {

    int REQUESTCODE;

    String Request="REQUESTCODE";
    String LOCATION="LOCATION";


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


        REQUESTCODE=intent.getIntExtra(Request,0);

        IntentDataSingleton intentDataSingleton=IntentDataSingleton.getInstance();

        Intent sensor_service=intentDataSingleton.getSensor_service();
        if(sensor_service==null)
        {
            sensor_service=new Intent(context, SensorService.class);
            intentDataSingleton.setSensor_service(sensor_service);
        }

        Intent beacon_service=intentDataSingleton.getBeacon_service();
        if(beacon_service==null)
        {
            Log.d("TAG_BEACON_SERVICE_BRO","CREATE BROADCAST BEACON RECEIVER");
            beacon_service=new Intent(context, BeaconService.class);
            intentDataSingleton.setBeacon_service(beacon_service);
        }




        boolean REQUESTDRIVER;
        switch (REQUESTCODE)
        {


            // 위치확인
            case 4:
                ServerData serverData=new ServerData(context);
                serverData.LOCATION_DATA();
                break;


            // 어플 종료
            case 99:
                android.os.Process.killProcess(android.os.Process.myPid());
                BluetoothParingStateCheck paringStateCheck=new BluetoothParingStateCheck();
                paringStateCheck.UnRegistParing(context);
                break;
        }

    }

}
