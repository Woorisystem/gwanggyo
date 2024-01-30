package net.woorisys.pms.jk.app.SJ_Util;

import android.content.Context;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.woorisys.pms.jk.app.SJ_ETC.BeaconFunction;
import net.woorisys.pms.jk.app.SJ_ETC.SaveArrayListValue;
import net.woorisys.pms.jk.app.SJ_Singleton.BluetoothSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.IntentDataSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.LocationDataSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.NotificationSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.TimerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.UserDataSingleton;


@Data
public class CallClassValue {

    @Getter
    @Setter
    BluetoothSingleton bluetoothSingleton;                  //  Bluetooth 관련 Singleton
    DataManagerSingleton dataManagerSingleton;              //  서버에 전송할 데이터를 관리하는 Singleton
    IntentDataSingleton intentDataSingleton;                //  Intent 를 관리하는 Singleton
    LocationDataSingleton locationDataSingleton;            //  위치확인을 위한 차량 정보를 관리하는 Singleton
    NotificationSingleton notificationSingleton;            //  startforeground 를 위해 필요한 notification 을 관리하는 Singleton
    SharedPreferencesSingleton sharedPreferences;           //  회원 아이디 , 비밀번호를 저장해두는 SharedPreferenced를 관리하는 Singleton
    TimerSingleton timerSingleton;                          //  Timer 를 총괄적으로 관리하는 Singleton
    UserDataSingleton userDataSingleton;                    //  로그인 성공후 받아오는 사용자의 Data를 관리하는 Singleton

    BeaconFunction beaconFunction;
    SaveArrayListValue saveArrayListValue;

    public CallClassValue(Context context)
    {
        bluetoothSingleton= BluetoothSingleton.getInstance();
        dataManagerSingleton=DataManagerSingleton.getInstance();
        intentDataSingleton=IntentDataSingleton.getInstance();
        locationDataSingleton=LocationDataSingleton.getInstance();
        notificationSingleton=NotificationSingleton.getInstance();
        sharedPreferences= SharedPreferencesSingleton.getInstance(context);
        timerSingleton=TimerSingleton.getInstance();
        userDataSingleton=UserDataSingleton.getInstance();

        beaconFunction=new BeaconFunction(context);
        saveArrayListValue=new SaveArrayListValue();
    }
}
