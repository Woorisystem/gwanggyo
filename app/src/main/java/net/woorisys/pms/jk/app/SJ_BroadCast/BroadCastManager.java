package net.woorisys.pms.jk.app.SJ_BroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import org.altbeacon.beacon.service.BeaconService;

import net.woorisys.pms.jk.app.SJ_Server.ServerData;
import net.woorisys.pms.jk.app.SJ_Service.Beacon26Service;
import net.woorisys.pms.jk.app.SJ_Service.SensorService;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;


public class BroadCastManager extends BroadcastReceiver {

    private static final String TAG = "BroadCast";

    public static final int INTENT_LOCATION_NUM = 10;
    public static final int INTENT_EXIT_NUM = 11;
    public static final int INTENT_PERMISSION_NUM = 12;

    int REQUEST_CODE;
    int REQUEST_CODE_P;

    String Request="REQUEST_CODE";
    String LOCATION="LOCATION";

    SharedPreferencesSingleton sharedPreferencesSingleton;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        sharedPreferencesSingleton= SharedPreferencesSingleton.getInstance(context);
        sharedPreferencesSingleton.SharedPreferenceRead();

        REQUEST_CODE=intent.getIntExtra(Request,0);

        Log.d(TAG,"REQUESTCODE :"+REQUEST_CODE);

//        IntentDataSingleton intentDataSingleton=IntentDataSingleton.getInstance();
//
//        Intent sensor_service=intentDataSingleton.getSensor_service();
//        if(sensor_service==null)
//        {
//            sensor_service=new Intent(context, SensorService.class);
//            intentDataSingleton.setSensor_service(sensor_service);
//        }
//
//        Intent beacon_service=intentDataSingleton.getBeacon_service();
//        if(beacon_service==null)
//        {
//            Log.d(TAG,"CREATE BROADCAST BEACON RECEIVER");
//            beacon_service=new Intent(context, BeaconService.class);
//            intentDataSingleton.setBeacon_service(beacon_service);
//        }

        //boolean REQUESTDRIVER;
        switch (REQUEST_CODE)
        {
            // 위치확인
            case INTENT_LOCATION_NUM:
                //Log.d(TAG,"위치 확인");
//                CharSequence text = "리셋 Count : " + sharedPreferencesSingleton.getRESET_W();
//                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
//                toast.show();

                ServerData serverData=new ServerData(context);
                serverData.LOCATION_DATA();
                break;

            // 어플 종료
            case INTENT_EXIT_NUM:
                Log.d(TAG,"어플 종료");
                context.stopService(new Intent(context, SensorService.class));
                context.stopService(new Intent(context, Beacon26Service.class));
                context.stopService(new Intent(context, BeaconService.class));

                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            case INTENT_PERMISSION_NUM:
                Log.d(TAG,"권한 요청");
                Intent intentTest = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+context.getPackageName()));
                intentTest.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity(intentTest);
                break;

        }
    }
}
