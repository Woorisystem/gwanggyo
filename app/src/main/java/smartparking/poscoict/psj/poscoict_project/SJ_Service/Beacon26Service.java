package smartparking.poscoict.psj.poscoict_project.SJ_Service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.service.scanner.ScanFilterUtils;

import java.util.ArrayList;
import java.util.List;

import smartparking.poscoict.psj.poscoict_project.PublicValue;
import smartparking.poscoict.psj.poscoict_project.R;
import smartparking.poscoict.psj.poscoict_project.SJ_BroadCast.BluetoothStatus;
import smartparking.poscoict.psj.poscoict_project.SJ_Util.CallClassValue;


public class Beacon26Service extends Service {

    private boolean isStartScanning=false;

    Handler handler;
    Runnable runnable;

    CallClassValue callClassValue;

    private BeaconParser beaconParser;
    private List<BeaconParser> beaconParsers;
    private ScanFilterUtils scanFilterUtils=new ScanFilterUtils();
    private ScanSettings settings;
    List<ScanFilter> filters;
    BluetoothAdapter BluetoothAdapter_W;
    BluetoothLeScanner LeScanner_W;

    BluetoothStatus bluetoothStatus;

    String TAG="TAG_BEACON26SERVICE";

    public Beacon26Service() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        if(handler==null)
            StartBeaconCollect();
        IntentFilterValue();
        // Singleton , 공용 함수를 불러오기위한 Class 모음
        callClassValue=new CallClassValue(Beacon26Service.this);

        //Bluetooth Scann 관련 부분
        beaconParser=new BeaconParser();
        beaconParser.setBeaconLayout(getResources().getString(R.string.beacon_parser));
        beaconParsers=new ArrayList<>();
        beaconParsers.add(beaconParser);
        settings=(new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)).build();      //  RequiresApi 가 필요 - Oreo 버전에서만 사용할 예정이기 때문에 Oreo 만 잡아준다
        filters=scanFilterUtils.createScanFiltersForBeaconParsers(beaconParsers);
        BluetoothAdapter_W= BluetoothAdapter.getDefaultAdapter();
        LeScanner_W=BluetoothAdapter_W.getBluetoothLeScanner();

        StartBluetoothScanning();

        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle=intent.getExtras();

        Notification notification=bundle.getParcelable(PublicValue.PUTEXTRA_SET_NOTIFICATION);
        startForeground(1,notification);

        Intent send=new Intent(PublicValue.ACTION_BEACON_SERVICE_START);
        sendBroadcast(send);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api= Build.VERSION_CODES.O)
    ScanCallback scanCallback=new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);
            Log.e("RUNTIMEEXCEPTION","COLLECT LOBBY TIMER ON FINISH ");
            ScanRecord scanRecord = result.getScanRecord();
            SparseArray<byte[]> sparseArray = scanRecord.getManufacturerSpecificData();
            byte[] bytevalue = sparseArray.valueAt(0);

            if(bytevalue!=null)
            {
                if(bytevalue.length>=23)
                {
                    String Address= String.format("%02x", bytevalue[2] & 0xff) + String.format("%02x", bytevalue[3] & 0xff)
                            + String.format("%02x", bytevalue[4] & 0xff)+ String.format("%02x", bytevalue[5] & 0xff)
                            + String.format("%02x", bytevalue[6] & 0xff)+ String.format("%02x", bytevalue[7] & 0xff)
                            + String.format("%02x", bytevalue[8] & 0xff)+ String.format("%02x", bytevalue[9] & 0xff)
                            + String.format("%02x", bytevalue[10] & 0xff)+ String.format("%02x", bytevalue[11] & 0xff)
                            + String.format("%02x", bytevalue[12] & 0xff)+ String.format("%02x", bytevalue[13] & 0xff)
                            + String.format("%02x", bytevalue[14] & 0xff)+ String.format("%02x", bytevalue[15] & 0xff)
                            + String.format("%02x", bytevalue[16] & 0xff)+ String.format("%02x", bytevalue[17] & 0xff);



                    if(Address==getResources().getString(R.string.beacon_id_4) || Address.equals(getResources().getString(R.string.beacon_id_4)))
                    {


                        final double rssi = result.getRssi();
                        String MajorValue= String.format("%02X",bytevalue[18])+ String.format("%02X",bytevalue[19]);
                        String MinorValue= String.format("%02X",bytevalue[20])+ String.format("%02X",bytevalue[21]);

                        final int major = Integer.valueOf(MajorValue,16);
                        int minor = Integer.valueOf(MinorValue,16);


                        Handler handler;
                        if(rssi>=-90)
                        {
                            switch (major)
                            {
                                // 로비 비컨
                                case 1:
                                    Log.d(TAG,"LOBBY INSERT");
                                    callClassValue.getBeaconFunction().LOBBYBEACON(rssi,minor,major);
                                    break;
                                // 주차 출입 - 시작 비컨 1
                                case 2:
                                    callClassValue.getBeaconFunction().ENTRANCEBEACON(rssi,minor,major);
                                    break;
                                // 엘리베이터 3 - 엘리베이터
                                case 3:
                                    callClassValue.getBeaconFunction().ELEVATORBEACON(rssi,major,minor);
                                    break;
                                //주차면 상태 평시
                                case 4:
                                    callClassValue.getBeaconFunction().StayBeacon(minor,major,rssi,callClassValue.getSaveArrayListValue());
                                    break;
                                // 주차면 상태 - 변화가 있을 때
                                case 5:
                                    callClassValue.getBeaconFunction().ChangeBeacon(minor,major,rssi,callClassValue.getSaveArrayListValue());
                                    break;
                                // 시작 비컨 2
                                case 6:
                                    callClassValue.getBeaconFunction().PARKINGBEACON(rssi,minor,major);
                                    break;
                            }
                        }
                    }
            }

            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            Log.d(TAG,"ERROR : "+errorCode);
            LeScanner_W.stopScan(scanCallback);
            LeScanner_W.startScan(scanCallback);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LeScanner_W.stopScan(scanCallback);
        }
        stopSelf();

        unregisterReceiver(mBluetoothStateReceiver);
        unregisterReceiver(broadcastReceiver);
    }

    // 5분 측정 시작하여 Bluetooth Bind 를 껏다 킨다.
    private void StartBeaconCollect()
    {
        if(handler==null)
            handler=new Handler();
        handler.postDelayed(StartBeaconCollectRunnable(),300000);
    }

    private Runnable StartBeaconCollectRunnable()
    {
        runnable=new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                handler=null;

                StopBluetoothScanning();
                StartBluetoothScanning();

                handler=null;

                if(handler==null)
                    StartBeaconCollect();
            }
        };
        return runnable;
    }


    private void IntentFilterValue()
    {
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        registerReceiver(mBluetoothStateReceiver,stateFilter);
    }

    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch(state) {
                case BluetoothAdapter.STATE_OFF:

                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:

                    break;
                case BluetoothAdapter.STATE_ON:
                    StopBluetoothScanning();
                    StartBluetoothScanning();

                    if(handler==null)
                        StartBeaconCollect();

                    break;
                case BluetoothAdapter.STATE_TURNING_ON:

                    break;
            }


        }
    };

    //==============================================================================================

    // Bluetooth Low Energy Scanning Start
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StartBluetoothScanning()
    {
        if(!isStartScanning)
        {
            if(LeScanner_W!=null)
            {

                Log.d(TAG,"START LOW ENERGY SCANNING");
                LeScanner_W.startScan(filters,settings,scanCallback);
                isStartScanning=true;
            }
        }
    }

    // Bluetooth Low Energy Scanning Stop
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StopBluetoothScanning()
    {
        if(isStartScanning)
        {
            if(LeScanner_W!=null)
            {
                Log.d(TAG,"STOP LOW ENERGY SCANNING");
                LeScanner_W.stopScan(scanCallback);
                isStartScanning=false;
            }
        }
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state)
            {
                case BluetoothAdapter.STATE_ON:
//                    Toast.makeText(getApplicationContext(),"'",Toast.LENGTH_SHORT).show();
                    StopBluetoothScanning();
                    StartBluetoothScanning();
                    Log.d("TAG_ACTION"  , String.valueOf(state));
                    break;

                case BluetoothAdapter.STATE_OFF:
                    Log.d("TAG_ACTION"  , String.valueOf(state));
                    Toast.makeText(getApplicationContext(),"블루투스가 꺼졌습니다. " +
                            "'SmartParking' 어플리케이션이 정상적으로 동작할 수 없습니다.\n정상 작동을 위하여 블루투스를 다시 켜주세요.", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };


}
