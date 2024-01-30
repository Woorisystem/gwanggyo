package net.woorisys.pms.jk.app.SJ_Service;

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
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.service.scanner.ScanFilterUtils;

import java.util.ArrayList;
import java.util.List;

import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;
import net.woorisys.pms.jk.app.SJ_Util.CallClassValue;


public class Beacon26Service extends Service {

    private boolean isStartScanning=false;

    SharedPreferencesSingleton sharedPreferencesSingleton;

    CallClassValue callClassValue;

    private BeaconParser beaconParser;
    private List<BeaconParser> beaconParsers;
    private ScanFilterUtils scanFilterUtils=new ScanFilterUtils();
    private ScanSettings settings;
    List<ScanFilter> filters;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner LeScanner_W;

    private BLEScanCallback mScanCallback = null;

    private static Beacon26Service mBeaconServiceContext = null;

    private static String TAG="TAG_BLUETOOTH_SERVICE_26";

    private BLEBroadcastReceiver mBroadcastReceiver = null;

    public static Beacon26Service getInstance() {
        return mBeaconServiceContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mBeaconServiceContext = null;

//        sharedPreferencesSingleton= SharedPreferencesSingleton.getInstance(getApplicationContext());
//        sharedPreferencesSingleton.SharedPreferenceRead();
//
//        ServerData serverData=new ServerData(this);
//        TextView TXT_RESULT_W = null;      //  로그인시 결과
//        serverData.Login(sharedPreferencesSingleton.getID_W(),sharedPreferencesSingleton.getPASS0WORD_W()
//                ,TXT_RESULT_W,sharedPreferencesSingleton.isDRIVER_W(),sharedPreferencesSingleton.isREMEMBER_W());
//
//        sharedPreferencesSingleton.ResetUpdate(sharedPreferencesSingleton.getRESET_W()+1);

        callClassValue=new CallClassValue(Beacon26Service.this);

        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        beaconParser=new BeaconParser();
        beaconParser.setBeaconLayout(getResources().getString(R.string.beacon_parser));
        beaconParsers=new ArrayList<>();
        beaconParsers.add(beaconParser);
        settings=(new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)).build();      //  RequiresApi 가 필요 - Oreo 버전에서만 사용할 예정이기 때문에 Oreo 만 잡아준다
        filters=scanFilterUtils.createScanFiltersForBeaconParsers(beaconParsers);

        LeScanner_W=mBluetoothAdapter.getBluetoothLeScanner();

        mScanCallback = new BLEScanCallback();

        IntentFilterValue();

        if (NotificationService.getInstance() == null) {
            new NotificationService(getApplicationContext());
        }
        startForeground(NotificationService.FOREGROUND_NOTIFICATION_ID,NotificationService.getInstance().createForegroundNotification(this));

        if (mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (this.isStartScanning) {
                    StopBluetoothScanning();
                }
                StartBluetoothScanning();
            }
        }

        Log.d(TAG,"OnStartCommand ");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class BLEScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);

            //Log.e("RUNTIMEEXCEPTION","COLLECT LOBBY TIMER ON FINISH ");
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

                    if(Address==getResources().getString(R.string.beacon_id) || Address.equals(getResources().getString(R.string.beacon_id)))
                    {
                        final double rssi = result.getRssi();
                        String MajorValue= String.format("%02X",bytevalue[18])+ String.format("%02X",bytevalue[19]);
                        String MinorValue= String.format("%02X",bytevalue[20])+ String.format("%02X",bytevalue[21]);

                        final int major = Integer.valueOf(MajorValue,16);
                        int minor = Integer.valueOf(MinorValue,16);

                        if(rssi>=-90)
                        {
                            switch (major)
                            {
                                // 로비 비컨
                                case 1:
                                    //callClassValue.getBeaconFunction().LOBBYBEACON(rssi,minor,major);
                                    break;
                                // 주차 출입 - 시작 비컨 1
                                case 2:
                                    //Log.d(TAG,"START 1");
                                    //callClassValue.getBeaconFunction().ENTRANCEBEACON(rssi,minor,major);
                                    break;
                                // 엘리베이터 3 - 엘리베이터
                                case 3:
                                    Log.d(TAG,"START 3");
                                    callClassValue.getBeaconFunction().ELEVATORBEACON(rssi,major,minor);
                                    break;
                                //주차면 상태 평시
                                case 4:
                                    Log.d(TAG,"START 4");

//                                    Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                        vibrator.vibrate(VibrationEffect.createOneShot(1000,100));
//                                    }

                                    callClassValue.getBeaconFunction().StayBeacon(minor,major,rssi,callClassValue.getSaveArrayListValue());
                                    break;
                                // 주차면 상태 - 변화가 있을 때
                                case 5:
                                    callClassValue.getBeaconFunction().ChangeBeacon(minor,major,rssi,callClassValue.getSaveArrayListValue());
                                    break;
                                // 시작 비컨 2
                                case 6:
                                    //Log.d(TAG,"START 2");
                                    //callClassValue.getBeaconFunction().PARKINGBEACON(rssi,minor,major);
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

            //LeScanner_W.stopScan(scanCallback);
            //LeScanner_W.startScan(scanCallback);
        }
    };

    @Override
    public void onDestroy() {
        // Unregister Bluetooth state change
        Log.d("BLUETOOTH_SERVICE","Service onDestroy");
        getApplicationContext().unregisterReceiver(mBroadcastReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StopBluetoothScanning();
        }
        mBeaconServiceContext = null;
        super.onDestroy();
    }


    // Bluetooth Low Energy Scanning Start
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StartBluetoothScanning()
    {
        if(!isStartScanning)
        {
            if(LeScanner_W!=null)
            {
                Log.d(TAG,"START LOW ENERGY SCANNING");
                LeScanner_W.startScan(filters,settings,mScanCallback);
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
                LeScanner_W.stopScan(mScanCallback);
                isStartScanning=false;
            }
        }
    }

    private void IntentFilterValue()
    {
        mBroadcastReceiver = new BLEBroadcastReceiver();
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        registerReceiver(mBroadcastReceiver,stateFilter);
    }

    private class BLEBroadcastReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch(state) {
                case BluetoothAdapter.STATE_OFF:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        StopBluetoothScanning();
                    }

                    // Notification Bluetooth off
                    if (NotificationService.getInstance() != null) {
                        NotificationService.getInstance().updateNotification(Beacon26Service.this);
                    }

                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:

                    break;
                case BluetoothAdapter.STATE_ON:
                    // Notification Bluetooth off
                    Context appContext = Beacon26Service.this.getApplicationContext();
                    if (appContext != null) {
                        Intent beaconServiceIntent = new Intent(appContext, Beacon26Service.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appContext.startForegroundService(beaconServiceIntent);
                        } else {
                            appContext.startService(beaconServiceIntent);
                        }
                    } else {
                        Log.d(TAG, "BeaconService.this.getApplicationContext() is null........");
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
            }
        }
    };
}
