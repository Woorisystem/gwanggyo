package smartparking.poscoict.psj.poscoict_project.SJ_Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.service.BeaconService;

import smartparking.poscoict.psj.poscoict_project.PublicValue;
import smartparking.poscoict.psj.poscoict_project.R;
import smartparking.poscoict.psj.poscoict_project.SJ_BroadCast.BroadCastManager;
import smartparking.poscoict.psj.poscoict_project.SJ_SensorOperation.Event;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.ServerData;
import smartparking.poscoict.psj.poscoict_project.SJ_Service.Beacon26Service;
import smartparking.poscoict.psj.poscoict_project.SJ_Service.Beacon26Service_NoDriver;
import smartparking.poscoict.psj.poscoict_project.SJ_Service.BeaconService_NoDriver;
import smartparking.poscoict.psj.poscoict_project.SJ_Service.SensorService;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.SharedPreferencesSingleton;


/**
 * 로그인 Activity
 *
 * --------------            설정              --------------
 * 소정 코드 :  맨뒤에 W 붙음
 **/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG="LOGINACTIVITYTAG";

    SharedPreferencesSingleton sharedPreferencesSingleton;

    private final int REQUEST_BLUETOOTH_PERMISSION_CODE_W=1;            //  Bluetooth Permission Request Code
    private final int REQUEST_BLUETOOTH_ENABLED_CODE_W = 1;          //  Bluetooth Enabled Check Code
    private final long FINISH_INTERVAL_TIME_W = 2000;
    private long   backPressedTime_W = 0;


    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    /** UI **/
    EditText ET_ID_W;           //  ID 값을 입력받는 곳
    EditText ET_PASSWORD_W;     //  PASSWORD 값을 입력받는 곳
    Button BTN_LOGIN_W;         //  로그인 버튼
    CheckBox CHK_REMEMBER_W;    //  ID , PASSWORD 기억하기 CheckBox
    CheckBox CHK_DRIVER_W;      //  운전자 , 비운전자 구분하여 어플 실행
    TextView TXT_RESULT_W;      //  로그인시 결과



    NotificationManager NotificationManager_W;
    NotificationChannel Channel_W;

    String ID_W;
    String NAME_W;
    String DESCRIPTION_W;

    private boolean BeaconServiceStart=false;
    private boolean SensorServiceStart=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferencesSingleton=SharedPreferencesSingleton.getInstance(getApplicationContext());

        if(IsServicRunning())
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("서비스 이미 실행중");
            builder.setMessage("서비스가 이비 실행중입니다.\n종료를 원하시면 상단의 노티피케이션을 통하여 어플을 종료하여 주십시오.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EndApplication();
                }
            });
            builder.create().show();
        }
        else
        {

            RegistServerResult_BroadcastReceiver();

            sharedPreferencesSingleton.SharedPreferenceRead();
            // UI 설정
            UISetting();
            Permsiion();
        }

    }


    private void Permsiion()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_BLUETOOTH_PERMISSION_CODE_W);
            }
            else
            {
                BluetoothBind();
            }
        }
        else
            BluetoothBind();

    }


    // UI 설정
    private void UISetting()
    {
        ET_ID_W=findViewById(R.id.et_id_login);
        ET_PASSWORD_W=findViewById(R.id.et_password_login);
        BTN_LOGIN_W=findViewById(R.id.btn_login);
        CHK_REMEMBER_W=findViewById(R.id.chk_remember_login);
        CHK_DRIVER_W=findViewById(R.id.chk_Open_Door);
        TXT_RESULT_W=findViewById(R.id.txt_result_login);

        ET_ID_W.setText(sharedPreferencesSingleton.getID_W());
        ET_PASSWORD_W.setText(sharedPreferencesSingleton.getPASS0WORD_W());
        CHK_REMEMBER_W.setChecked(sharedPreferencesSingleton.isREMEMBER_W());
        CHK_DRIVER_W.setChecked(sharedPreferencesSingleton.isDRIVER_W());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ET_ID_W.setBackgroundResource(R.drawable.ic_edit_background_round_rectangle);
            ET_PASSWORD_W.setBackgroundResource(R.drawable.ic_edit_background_round_rectangle);
        }

        BTN_LOGIN_W.setOnClickListener(this);
    }



    // Bluetooth 가 꺼져 있을 경우 키는 기능 -> 끌필요는 없기 떄문에 끄는 기능 구현 X
    private void BluetoothBind()
    {

        BluetoothAdapter.getDefaultAdapter().enable();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btn_login:

                if((ET_ID_W.getText().toString()==null || ET_PASSWORD_W.getText().toString()==null) || (ET_ID_W.getText().toString().equals("") || ET_PASSWORD_W.getText().toString().equals("")))
                {
                    TXT_RESULT_W.setTextColor(getResources().getColor(R.color.colorError));
                    TXT_RESULT_W.setText(getResources().getString(R.string.txtview_txt_error1_login_main));
                }
                else
                {
                    ServerData serverData=new ServerData(this);
                    serverData.Login(ET_ID_W.getText().toString(),ET_PASSWORD_W.getText().toString(),TXT_RESULT_W,CHK_DRIVER_W.isChecked(),CHK_REMEMBER_W.isChecked());
                }

            break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG,"REQUEST : "+requestCode+"/"+permissions+"/"+grantResults);

        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSION_CODE_W:

                Log.d(TAG,"PERMISSION FIND");
                if(requestCode==-1)
                    android.os.Process.killProcess(android.os.Process.myPid());
                else
                {
                    BluetoothBind();
                }

                break;

            case PERMISSION_REQUEST_BACKGROUND_LOCATION:

                if(requestCode==-1)
                    android.os.Process.killProcess(android.os.Process.myPid());
                else
                {
                    BluetoothBind();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            // result code 가 1 일 경우 비컨이 켜지지 않으므로 어플을 종료 시켜버린다.
            case REQUEST_BLUETOOTH_ENABLED_CODE_W:

                if(resultCode==-1)
                {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

                break;
        }
    }

    /** 뒤로가기 두번 클릭시 종료되도록 처리 **/
    @Override
    public void onBackPressed() {

        long tempTime= System.currentTimeMillis();
        long intervalTime=tempTime-backPressedTime_W;


        if (0 <= intervalTime && FINISH_INTERVAL_TIME_W >= intervalTime)
        {
            super.onBackPressed();
            sharedPreferencesSingleton.SharedPreferenceWrite(CHK_REMEMBER_W.isChecked(),CHK_DRIVER_W.isChecked(),ET_ID_W.getText().toString(),ET_PASSWORD_W.getText().toString());
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else
        {
            backPressedTime_W = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if(BroadcastReceiver_ServerResult!=null)
                unregisterReceiver(BroadcastReceiver_ServerResult);
        }
        catch (IllegalArgumentException e)
        {

        }

    }

    // Service 동작 확인 Bluetooth Service , Sensor Service 동작 확인
    private boolean IsServicRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {

            if(BeaconService.class.getName().equals(service.service.getClassName()))
            {
                Log.d(TAG,"ALREADY BEACON SERVICE");
                return true;
            }


            if(SensorService.class.getName().equals(service.service.getClassName()))
            {
                return true;
            }

        }
        return false;
    }

    // Application Activity 종료
    private void EndApplication()
    {
        this.finish();
    }


    // Broadcast Register Action Intent FIlter
    private void RegistServerResult_BroadcastReceiver()
    {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(PublicValue.ACTION_LOGIN_SUCCESS);
        intentFilter.addAction(PublicValue.ACTION_LOGIN_FAILED);
        intentFilter.addAction(PublicValue.ACTION_LOGIN_SENSOR_TEST_FINISH);
        intentFilter.addAction(PublicValue.ACTION_BEACON_SERVICE_START);
        intentFilter.addAction(PublicValue.ACTION_SENSOR_SERVICE_START);
        registerReceiver(BroadcastReceiver_ServerResult,intentFilter);
    }

    // Broadcast Receiver Server Result
    BroadcastReceiver BroadcastReceiver_ServerResult=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String ACTION=intent.getAction();
            Bundle bundle=intent.getExtras();

            String message;

            switch (ACTION)
            {
                case PublicValue.ACTION_LOGIN_SUCCESS:

                    message=bundle.getString(PublicValue.LOGIN_SUCCESS_MESSAGE);

                    if(message!=null) {
                        Log.d(TAG, "ACTION RESPONSE SUCCESS : " + message);
                        TXT_RESULT_W.setText(message);
                        TXT_RESULT_W.setTextColor(context.getResources().getColor(R.color.colorSuccess));


                        boolean SharedLogin= sharedPreferencesSingleton.isINITLOGIN_W();


                        // 최초 로그인시 센서테스트 기능
                        if(SharedLogin)
                        {
                            if(CHK_DRIVER_W.isChecked())
                            {
                                Event event=new Event(context);
                            }
                            else
                            {
                                NotificationStart();
                            }
                        }
                        // 아닐 경우 로그인
                        else
                        {
                            NotificationStart();
                        }

                    }
                    else
                    {
                        Log.e(TAG,"ACTION RESPONSE SUCCESS MESSAGE NULL");
                    }
                    break;

                case PublicValue.ACTION_LOGIN_FAILED:

                    message=bundle.getString(PublicValue.LOGIN_FAILED_MESSAGE);

                    if(message!=null)
                    {
                        Log.d(TAG,"ACTION RESPONSE FAILED : "+message);
                        TXT_RESULT_W.setText(message);
                        TXT_RESULT_W.setTextColor(context.getResources().getColor(R.color.colorError));
                    }
                    else
                    {
                        Log.e(TAG,"ACTION RESPONSE FAILED MESSAGE NULL");
                    }
                    break;

                case  PublicValue.ACTION_LOGIN_SENSOR_TEST_FINISH:

                    Log.d(TAG,"ACTION LOGIN SENSOR TEST FINISH");
                    NotificationStart();
                    break;

                case PublicValue.ACTION_BEACON_SERVICE_START:

                    if(CHK_DRIVER_W.isChecked())
                    {
                        Log.d(TAG,"ACTION BEACON SERVICE START");
                        if(SensorServiceStart)
                        {
                            EndApplication();
                        }
                        else
                        {
                            BeaconServiceStart=true;
                        }
                    }
                    else
                    {
                        EndApplication();
                    }

                    break;
                case PublicValue.ACTION_SENSOR_SERVICE_START:
                    Log.d(TAG,"ACTION SENSOR SERVICE START");

                    if(BeaconServiceStart)
                    {
                        EndApplication();
                    }
                    else
                        SensorServiceStart=true;
                    break;
            }
        }
    };


    /**Notification Channel 설정**/
    private void CreateChannel()
    {
        Log.d(TAG,"Notification Service CreateChannel");

        int importance= NotificationManager.IMPORTANCE_HIGH;

        ID_W=getResources().getString(R.string.N_ID);
        NAME_W=getResources().getString(R.string.N_NAME);
        DESCRIPTION_W=getResources().getString(R.string.N_DESCRIPTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Channel_W=new NotificationChannel(ID_W,NAME_W,importance);
            Channel_W.setDescription(DESCRIPTION_W);
            Channel_W.enableVibration(false);

            NotificationManager_W.createNotificationChannel(Channel_W);
        }

    }

    private void NotificationStart()
    {

        NotificationManager_W=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent Intent_Location=new Intent(this, BroadCastManager.class);
        Intent Intent_Exit=new Intent(this,BroadCastManager.class);

        Intent_Location.putExtra("REQUESTCODE",4);      //  위치 확인
        Intent_Exit.putExtra("REQUESTCODE",99);          //  종료

        PendingIntent PendingIntent_Location=PendingIntent.getBroadcast(this,12,Intent_Location,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent PendingIntent_Exit=PendingIntent.getBroadcast(this,99,Intent_Exit,PendingIntent.FLAG_UPDATE_CURRENT);

        CreateChannel();

        Notification CustomNotification;

        CustomNotification=new NotificationCompat.Builder(this,ID_W)
                .setSmallIcon(R.mipmap.car)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentTitle("[더샵 Smart Parking] 스마트 원패스")
                .setContentText("스마트 원패스 앱이 실행중입니다.\n이 메시지는 앱을 항상 실행하도록 합니다.\n메시지를 삭제하지마세요!")
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.car,"주차위치확인",PendingIntent_Location)
                .addAction(R.drawable.car,"종료",PendingIntent_Exit)
                .build();

        CustomNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE | Notification.COLOR_DEFAULT;
        NotificationManager_W.notify(1,CustomNotification);

        StartService(CustomNotification);
    }

    // Bluetooth Service
    private void StartService(Notification notification)
    {
//        Intent BeaconService=new Intent(this, Beacon_service.class);
//        Intent SensorService=new Intent(this, SensorService.class);
//
//        BeaconService.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
//        BeaconService.putExtra(PublicValue.PUTEXTRA_SET_ISDRIVING,CHK_DRIVER_W.isChecked());
//        SensorService.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
//
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
//        {
//            startForegroundService(SensorService);
//            startForegroundService(BeaconService);
//        }
//        else
//        {
//            startService(SensorService);
//            startService(BeaconService);
//        }
        Intent BeaconService=new Intent(this, BeaconService.class);
        Intent BeaconService_NoDrive=new Intent(this, BeaconService_NoDriver.class);
        Intent BeaconService26=new Intent(this, Beacon26Service.class);
        Intent BeaconService26_NoDrive=new Intent(this, Beacon26Service_NoDriver.class);
        Intent SensorService=new Intent(this, SensorService.class);

        BeaconService.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
        BeaconService_NoDrive.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
        BeaconService26.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
        BeaconService26_NoDrive.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);
        SensorService.putExtra(PublicValue.PUTEXTRA_SET_NOTIFICATION,notification);

        // 운전자
        if(CHK_DRIVER_W.isChecked())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(SensorService);
                startForegroundService(BeaconService26);
            }
            else
            {
                startService(SensorService);
                startService(BeaconService);
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(BeaconService26_NoDrive);
            }
            else
            {
                startService(BeaconService_NoDrive);
            }
        }
    }



}
