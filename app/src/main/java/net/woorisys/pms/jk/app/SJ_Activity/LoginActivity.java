package net.woorisys.pms.jk.app.SJ_Activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;

import org.altbeacon.beacon.service.BeaconService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.woorisys.pms.jk.app.PublicValue;
import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_BroadCast.NetworkCheck;
import net.woorisys.pms.jk.app.SJ_SensorOperation.Event;
import net.woorisys.pms.jk.app.SJ_Server.ServerData;
import net.woorisys.pms.jk.app.SJ_Service.Beacon26Service;
import net.woorisys.pms.jk.app.SJ_Service.SensorService;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;

import lombok.NonNull;

/**
 * 로그인 Activity
 *
 * --------------          설정            --------------
 * 소정 코드 :  맨뒤에 W 붙음
 **/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG="LOGIN_ACTIVITY";

    SharedPreferencesSingleton sharedPreferencesSingleton;

    private final int REQUEST_BLUETOOTH_PERMISSION_CODE_W = 1;            //  Bluetooth Permission Request Code
    private final int REQUEST_BACKGROUND_PERMISSION_CODE_W =2;
    private final int REQUEST_STORAGE_PERMISSION_CODE_W =3;
    private final int REQUEST_BLE_PERMISSION_CODE_W =4;

    private final int REQUEST_BLUETOOTH_ENABLED_CODE_W = 1;          //  Bluetooth Enabled Check Code
    private final long FINISH_INTERVAL_TIME_W = 2000;
    private long   backPressedTime_W = 0;

    //private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    //private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    //요청할 권한 배열 저장
    private String[] permissions = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    };
    private List permissionList;

    //권한 요청시 발생하는 창에 대한 결과값을 받기 위해 지정해주는 int 형
    //원하는 임의의 숫자 지정
    private final int MULTIPLE_PERMISSIONS = 1023;

    /** UI **/
    EditText ET_ID_W;           //  ID 값을 입력받는 곳
    EditText ET_PASSWORD_W;     //  PASSWORD 값을 입력받는 곳
    Button BTN_LOGIN_W;         //  로그인 버튼
    Button BTN_SIGNUP_W;        //  회원가입 버튼
    ImageButton BTN_DOWNLOAD_W; //  매뉴얼 다운로드 버튼
    CheckBox CHK_REMEMBER_W;    //  ID , PASSWORD 기억하기 CheckBox
    TextView TXT_RESULT_W;      //  로그인시 결과

    NotificationManager NotificationManager_W;
    NotificationChannel Channel_W;

    String ID_W;
    String NAME_W;
    String DESCRIPTION_W;

    private boolean BeaconServiceStart=false;
    private boolean SensorServiceStart=false;

    private WorkManager workManager = WorkManager.getInstance(this);
    //private DownloadManager downloadManager;

    private boolean startFlag = true;

    private String filepath = "http://211.52.72.27:4000/pms-server-web/app/userManual";
    private URL url = null;
    private String fileName = "guide.pdf";

    String message = "정상적인 앱 사용을 위해 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다. \n\n[확인] 버튼을 누른 후 시스템 알림 대화 상자가 뜨면 [허용] 을 선택해 주세요";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferencesSingleton=SharedPreferencesSingleton.getInstance(getApplicationContext());

//        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission == PackageManager.PERMISSION_DENIED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
//            }
//            //return;
//        }


        if(IsServiceRunning())
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("서비스 이미 실행중");
            builder.setMessage("서비스가 이미 실행중입니다.\n종료를 원하시면 상단의 노티피케이션을 통하여 어플을 종료하여 주십시오.");
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

            if (!checkPermission()){
                //권한 요청
                requestPermission();
            }
        }
    }

    //배열로 선언한 권한 중 허용되지 않은 권한 있는지 체크
    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<>();

        for(String pm : permissions){
            result = ContextCompat.checkSelfPermission(this, pm);
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(pm);
            }
        }
        if(!permissionList.isEmpty()){
            return false;
        }
        return true;
    }

    //배열로 선언한 권한에 대해 사용자에게 허용 요청
    public void requestPermission(){

//        Lod.d();

        //ActivityCompat.requestPermissions(this, (String[]) permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
//        ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION_CODE_W);
    }

    //요청한 권한에 대한 결과값 판단 및 처리
    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //우선 requestCode가 아까 위에 final로 선언하였던 숫자와 맞는지, 결과값의 길이가 0보다는 큰지 먼저 체크
        if(requestCode == MULTIPLE_PERMISSIONS && (grantResults.length >0)) {
            for(int i=0; i< grantResults.length; i++){
                //grantResults 가 0이면 사용자가 허용한 것 / -1이면 거부한 것
                //-1이 있는지 체크하여 하나라도 -1이 나온다면 false를 리턴
                if(grantResults[i] == -1){
                    return false;
                }
            }
        }
        return true;
    }


//    private void Permission()
//    {
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
//        {
//
//            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION_CODE_W);
//            }
//
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLE_PERMISSION_CODE_W);
//            }
//
//            if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_BACKGROUND_PERMISSION_CODE_W);
//            }
//
//            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH_PERMISSION_CODE_W);
//            }
//        }
//
//        PowerPermisson();
//
//    }

    private void PowerPermisson() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager.isIgnoringBatteryOptimizations(getPackageName()) == false) {

                // 화이트 리스트 등록 안됨.
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent,0);

                AlertDialog.Builder builder=new AlertDialog.Builder(this);

                builder.setMessage(message);
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent intent;
                        intent = getPermissionIntent();
                        startActivity(intent);
                    }
                });
                builder.create().show();
            }
        }
    }

    private Intent getPermissionIntent() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        return intent;
    }

    private Intent getSelfIntent() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        return intent;
    }

    // UI 설정
    private void UISetting()
    {
        ET_ID_W=findViewById(R.id.et_id_login);
        ET_PASSWORD_W=findViewById(R.id.et_password_login);
        BTN_LOGIN_W=findViewById(R.id.btn_login);
        BTN_SIGNUP_W=findViewById(R.id.btn_signup);
        BTN_DOWNLOAD_W = findViewById(R.id.btn_downloadPDF);

        CHK_REMEMBER_W=findViewById(R.id.chk_remember_login);
        TXT_RESULT_W=findViewById(R.id.txt_result_login);

        ET_ID_W.setText(sharedPreferencesSingleton.getID_W());
        ET_PASSWORD_W.setText(sharedPreferencesSingleton.getPASS0WORD_W());
        CHK_REMEMBER_W.setChecked(sharedPreferencesSingleton.isREMEMBER_W());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ET_ID_W.setBackgroundResource(R.drawable.ic_edit_background_round_rectangle);
            ET_PASSWORD_W.setBackgroundResource(R.drawable.ic_edit_background_round_rectangle);
        }

        BTN_LOGIN_W.setOnClickListener(this);
        BTN_SIGNUP_W.setOnClickListener(this);
        BTN_DOWNLOAD_W.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btn_login:
                if((ET_ID_W.getText().toString()==null || ET_PASSWORD_W.getText().toString()==null) || (ET_ID_W.getText().toString().equals("") || ET_PASSWORD_W.getText().toString().equals("")))
                {
                    //TXT_RESULT_W.setTextColor(getResources().getColor(R.color.colorError));
                    //TXT_RESULT_W.setText(getResources().getString(R.string.txtview_txt_error1_login_main));

                    Toast.makeText(getApplicationContext(), R.string.txtview_txt_error1_login_main, Toast.LENGTH_LONG).show();

                }
                else
                {
                    ServerData serverData=new ServerData(this);
                    Log.d(TAG,"ID : "+ET_ID_W.getText().toString()+", PW :"+ET_PASSWORD_W.getText().toString());
                    serverData.Login(ET_ID_W.getText().toString(),ET_PASSWORD_W.getText().toString(),TXT_RESULT_W,CHK_REMEMBER_W.isChecked());
                }
                break;

            case R.id.btn_signup:
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_downloadPDF:

                NetworkCheck networkCheck = new NetworkCheck();

                boolean networkResult = networkCheck.getNetwork(getApplicationContext());

                if (networkResult) {
                    try {
                        url = new URL(filepath);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url + ""));
                    request.setTitle(fileName);
                    request.setMimeType("application/pdf");
                    request.allowScanningByMediaScanner();
                    request.setAllowedOverMetered(true);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                }

                else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결이 안 되어있습니다. 인터넷 연결 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG,"REQUEST : "+requestCode+"/"+permissions+"/"+grantResults);

        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        switch (requestCode) {
//            case REQUEST_BLUETOOTH_PERMISSION_CODE_W:
//
//                Log.d(TAG,"PERMISSION FIND");
//                if(requestCode==-1)
//                    android.os.Process.killProcess(android.os.Process.myPid());
//
//            case REQUEST_BACKGROUND_PERMISSION_CODE_W:
//                Log.d(TAG,"BACKGROUND PERMISSION FIND");
//                if(requestCode==-1)
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                else
//                {
//                    Log.d(TAG,"항상허용으로 변경해주세요");
//                }
//
//            case REQUEST_STORAGE_PERMISSION_CODE_W:
//                if (requestCode == -1)
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                else
//                {
//                    Log.d(TAG,"허용으로 변경해주세요");
//                }
//            case REQUEST_BLE_PERMISSION_CODE_W:
//                if (requestCode == -1)
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                else
//                {
//                    Log.d(TAG,"허용으로 변경해주세요");
//                }
//        }
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
            sharedPreferencesSingleton.SharedPreferenceWrite(CHK_REMEMBER_W.isChecked(),ET_ID_W.getText().toString(),ET_PASSWORD_W.getText().toString());
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
    private boolean IsServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(Beacon26Service.class.getName().equals(service.service.getClassName()))
            {
                Log.d(TAG,"ALREADY BEACON 26 SERVICE");
                return true;
            }

            if(BeaconService.class.getName().equals(service.service.getClassName()))
            {
                Log.d(TAG,"ALREADY BEACON SERVICE");
                return true;
            }

            if(SensorService.class.getName().equals(service.service.getClassName()))
            {
                Log.d(TAG,"ALREADY SENSOR SERVICE");
                return true;
            }
        }
         return false;
    }

    private boolean isAppRunning(){
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if(procInfos.get(i).processName.equals(this.getPackageName())){
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

                        // 최초 로그인 아닐 경우 로그인
                        if(!SharedLogin)
                        {
                            NotificationStart();
                        }
                        // 최초 로그인시 센서테스트 기능
                        else
                        {
                            Event event=new Event(context);
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
                        //TXT_RESULT_W.setText(message);
                        //TXT_RESULT_W.setTextColor(context.getResources().getColor(R.color.colorError));
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


                    Log.d(TAG,"ACTION BEACON SERVICE START");
                    if(!SensorServiceStart)
                    {
                        BeaconServiceStart=true;
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
    private void CreateNotificationChannel()
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
//        Intent Intent_Location=new Intent(this, BroadCastManager.class);
//        Intent Intent_Exit=new Intent(this,BroadCastManager.class);
//
//        Intent_Location.putExtra("REQUESTCODE",4);      //  위치 확인
//        Intent_Exit.putExtra("REQUESTCODE",99);          //  종료
//
//        PendingIntent PendingIntent_Location=PendingIntent.getBroadcast(this,12,Intent_Location,PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent PendingIntent_Exit=PendingIntent.getBroadcast(this,99,Intent_Exit,PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Notification notification = new NotificationService(this).createForegroundNotification(this, PendingIntent_Location, PendingIntent_Exit);

        StartService();
        EndApplication();

//        NotificationManager_W=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//
//        CreateNotificationChannel();
//
//        Notification CustomNotification;
//
//        CustomNotification=new NotificationCompat.Builder(this,ID_W)
//                .setSmallIcon(R.mipmap.car)
//                .setStyle(new NotificationCompat.BigTextStyle())
//                .setContentTitle("[더샵 Smart Parking] 스마트 원패스")
//                .setContentText("스마트 원패스 앱이 실행중입니다.\n이 메시지는 앱을 항상 실행하도록 합니다.\n메시지를 삭제하지마세요!")
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .addAction(R.drawable.car,"주차위치확인",PendingIntent_Location)
//                .addAction(R.drawable.car,"종료",PendingIntent_Exit)
//                .build();
//
//        CustomNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE | Notification.COLOR_DEFAULT;
//        NotificationManager_W.notify(1,CustomNotification);
//
//        StartService(CustomNotification);
    }

    // Bluetooth Service
    private void StartService()
    {
        Intent BeaconService=new Intent(this, BeaconService.class);
        Intent BeaconService26=new Intent(this, Beacon26Service.class);
        Intent SensorService=new Intent(this, SensorService.class);

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
}
