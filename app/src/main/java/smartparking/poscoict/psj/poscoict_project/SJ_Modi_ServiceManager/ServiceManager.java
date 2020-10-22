package smartparking.poscoict.psj.poscoict_project.SJ_Modi_ServiceManager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class ServiceManager {

    String TAG="TAG_SERVICE_MANAGER";

    public ServiceManager()
    {
    }

    /** Bluetooth Service 관리하는 부분
     * StartBluetooth : Bluetooth Service 실행 시키는 함수
     * EndBluetooth   : Bluetooth Service 종료 시키는 함수
     **/
    //region Bluetooth
     public void StartBluetooth(Context context,boolean isDriver)
     {

         if(isDriver)
         {}
         else
         {}
     }
     public void EndBluetooth()
     {}
    //endregion

    /** Sensor Service 관리하는 부분
     * StartSensor  : Sensor Service 실행 시키는 함수
     * EndSensor    : Sensor Service 종료 시키는 함수
     **/
    //region Sensor
     public void StartSensor()
     {}
     public void EndSensor()
     {}
    //endregion

    /** Notification Service 관리하는 부분
     * StartNotification  : Notification Service 실행 시키는 함수
     * EndNotification    : Notification Service 종료 시키는 함수
     **/
    //region Notification

    //endregion
}
