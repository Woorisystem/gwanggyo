package smartparking.poscoict.psj.poscoict_project.SJ_Service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import smartparking.poscoict.psj.poscoict_project.PublicValue;
import smartparking.poscoict.psj.poscoict_project.R;
import smartparking.poscoict.psj.poscoict_project.SJ_ETC.BeaconFunction;
import smartparking.poscoict.psj.poscoict_project.SJ_ETC.Item;
import smartparking.poscoict.psj.poscoict_project.SJ_ETC.SaveArrayListValue;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.BluetoothSingleton;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.NotificationSingleton;


public class BeaconService extends Service implements BeaconConsumer {

    NotificationSingleton notificationSingleton;
    final String TAG="SJP_BeaconService";
    BeaconFunction beaconFunction;

    private Vector<Item> items;
    private DecimalFormat decimalFormat=new DecimalFormat("#.##");

    BeaconManager BeaconManager_W;
    BluetoothAdapter BluetoothAdapter_W;

    SaveArrayListValue saveArrayListValue;

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationSingleton=NotificationSingleton.getInstance();

        beaconFunction=new BeaconFunction(getApplicationContext());
        saveArrayListValue=new SaveArrayListValue();

        BluetoothSingleton bluetoothSingleton=BluetoothSingleton.getInstance();

        BluetoothAdapter_W= bluetoothSingleton.getBluetoothAdapterW();
        BeaconManager_W= BeaconManager.getInstanceForApplication(this);
        BeaconManager_W.getBeaconParsers().add(new BeaconParser().setBeaconLayout(getResources().getString(R.string.beacon_parser)));

        if(!BeaconManager_W.isBound(this))
            BeaconManager_W.bind(this);

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

    @Override
    public void onBeaconServiceConnect() {

        BeaconManager_W.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons.size()>0)
                {
                    Iterator<Beacon> iterator=beacons.iterator();
                    items=new Vector<>();

                    while (iterator.hasNext())
                    {
                        org.altbeacon.beacon.Beacon beacon=iterator.next();
                        String address=beacon.getBluetoothAddress();
                        Identifier id1=beacon.getId1();
                        double rssi=beacon.getRssi();
                        int txPower=beacon.getTxPower();
                        double distance= Double.parseDouble(decimalFormat.format(beacon.getDistance()));
                        int major=beacon.getId2().toInt();
                        int minor=beacon.getId3().toInt();

                        items.add(new Item(id1.toHexString(),rssi,txPower,distance,major,minor));
                    }

                    for (Item item:items)
                    {
                        String Address=item.getAddress();
                        final double rssi=item.getRssi();
                        final int major=item.getMajor();
                        int minor=item.getMinor();

                        // Address 가 일치하는 Beacon 만 가져온다 &&  RSSI 가 -90 이상
                        if((Address==getResources().getString(R.string.beacon_id) || Address.equals(getResources().getString(R.string.beacon_id))) && rssi >=- 90)
                        {
                            if(rssi>=-90)
                            {


                                switch (major) {
                                    // 로비 비컨
                                    case 1:
                                        beaconFunction.LOBBYBEACON(rssi,minor,major);
                                        break;
                                    // 주차 출입 - 시작 비컨 1
                                    case 2:
                                        beaconFunction.ENTRANCEBEACON(rssi,minor,major);
                                        break;
                                    // 로비 2 - 엘리베이터
                                    case 3:
                                        beaconFunction.ELEVATORBEACON(rssi,major,minor);

                                        break;
                                    //주차면 상태 평시
                                    case 4:
                                        beaconFunction.StayBeacon(minor,major,rssi,saveArrayListValue);
                                        break;
                                    // 주차면 상태 - 변화가 있을 때
                                    case 5:

                                        beaconFunction.ChangeBeacon(minor,major,rssi,saveArrayListValue);
                                        break;
                                    // 시작 비컨 2
                                    case 6:
                                        beaconFunction.PARKINGBEACON(rssi,minor,major);
                                        break;
                                }

                            }
                        }
                    }
                }
            }
        });

        //region 있어야 작동되는 것
        try {
            BeaconManager_W.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }
        catch (RemoteException e)
        {
            Log.e(TAG,"ERROR : "+e.getMessage());
        }

        BeaconManager_W.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        try {
            BeaconManager_W.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //endregion
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BeaconManager_W.unbind(this);
        stopSelf();

        unregisterReceiver(broadcastReceiver);
    }


    private void ScanningStart()
    {
        BeaconManager_W.bind(this);
    }

    private void ScanningStop()
    {
        BeaconManager_W.unbind(this);
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
                    ScanningStop();
                    ScanningStart();
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
