package net.woorisys.pms.jk.app.SJ_ETC;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import net.woorisys.pms.jk.app.SJ_Domain.AccelBeacon;
import net.woorisys.pms.jk.app.SJ_Domain.GyroSensor;
import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;


public class SaveArrayListValue {

    public void SaveAccelBeacon(String ID,String RSSI,String Delay)
    {
        AccelBeacon accelBeacon= DataManagerSingleton.getInstance().getAccelBeaconMap().get(ID);

        if(accelBeacon==null)
        {
            String LogValue=String.format("ID : %s , RSSI : %s , DELAY : %s",ID,RSSI,Delay);
            Log.d("TEST_LOG_190221_AB","엑셀 비컨 저장 - 값 없음 -> 추가 // "+LogValue);

            AccelBeacon saveaccelb=new AccelBeacon();
            saveaccelb.setBeaconId(ID);
            saveaccelb.setRssi(RSSI);
            saveaccelb.setDelay(Delay);
            saveaccelb.setCount("1");

            DataManagerSingleton.getInstance().getAccelBeaconMap().put(ID,saveaccelb);
        }
        else
        {
            if(Float.valueOf(RSSI)>= Float.valueOf(accelBeacon.getRssi()))
            {
                String LogValue=String.format("ID : %s , RSSI : %s , DELAY : %s",ID,RSSI,Delay);
                Log.d("TEST_LOG_190221_AB","엑셀 비컨 저장 - 값 있음 -> 변경 // "+LogValue);

                int accelcount=Integer.valueOf(accelBeacon.getCount());
                accelcount++;

                AccelBeacon saveaccelb=new AccelBeacon();
                saveaccelb.setBeaconId(ID);
                saveaccelb.setRssi(RSSI);
                saveaccelb.setDelay(Delay);
                saveaccelb.setCount(String.valueOf(accelcount));

                DataManagerSingleton.getInstance().getAccelBeaconMap().put(ID,saveaccelb);
            }
            else
            {
                int accelcount=Integer.valueOf(accelBeacon.getCount());
                accelcount++;

                String LogValue=String.format("ID : %s , RSSI : %s , DELAY : %s",accelBeacon.getBeaconId(),accelBeacon.getRssi(),accelBeacon.getDelay());
                Log.d("TEST_LOG_190221_AB","엑셀 비컨 저장 - 값 있음 -> 유지 // "+LogValue);

                AccelBeacon saveaccelb=new AccelBeacon();
                saveaccelb.setBeaconId(accelBeacon.getBeaconId());
                saveaccelb.setRssi(accelBeacon.getRssi());
                saveaccelb.setDelay(accelBeacon.getDelay());
                saveaccelb.setCount(String.valueOf(accelcount));

                DataManagerSingleton.getInstance().getAccelBeaconMap().put(ID,saveaccelb);
            }
        }

        SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS");
        Log.d("TAG_BYTE_CALC_ACCBEACON","ACCELBEACON BYTE 0 : "+ DataManagerSingleton.getInstance().getAccelBeaconMap().toString().length()+" , TIME : "+ full_sdf.format(System.currentTimeMillis()).toString());
    }

    public String AccelSensorResult()
    {
        int ResultCount=DataManagerSingleton.getInstance().getAccelCount();

        String Result;

        if(ResultCount<3 && ResultCount>=0)
        {
            Result="T";
        }
        else if(ResultCount<12 && ResultCount>=3)
        {
            Result="S";
        }
        else
        {
            Result="W";
        }

        return Result;
    }

    public void SaveGyro(Context context)
    {
        GyroSensor gyroSensor=new GyroSensor();
        gyroSensor.setDelay(String.valueOf(DataManagerSingleton.getInstance().getWholeTimerDelay()));
        gyroSensor.setX(String.valueOf(DataManagerSingleton.getInstance().getSaveCountRoll()));
        gyroSensor.setY(String.valueOf(DataManagerSingleton.getInstance().getSaveCountPitch()));
        gyroSensor.setZ(String.valueOf(DataManagerSingleton.getInstance().getSaveCountYaw()));

        DataManagerSingleton.getInstance().getGyroSensorArrayList().add(gyroSensor);

        SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS");
        Log.d("TAG_BYTE_CALC_GYRO","GYRO BYTE 0 : "+DataManagerSingleton.getInstance().getGyroSensorArrayList().toString().length()+" , TIME : "+ full_sdf.format(System.currentTimeMillis()).toString());
    }

    public void SaveAccelBeacon()
    {
        if(DataManagerSingleton.getInstance().getAccelBeaconMap()!=null)
        {
            Iterator AccelBeaconIterator=DataManagerSingleton.getInstance().getAccelBeaconMap().keySet().iterator();

            while (AccelBeaconIterator.hasNext())
            {
                String key=(String)AccelBeaconIterator.next();
                AccelBeacon value=(AccelBeacon)DataManagerSingleton.getInstance().getAccelBeaconMap().get(key);


                String ID=value.getBeaconId();
                String Rssi=value.getRssi();
                String Delay=value.getDelay();
                String Count=value.getCount();

                ArrayList<Integer> AccelDelay=DataManagerSingleton.getInstance().getAccelBeaconDelayMap().get(ID);
                Log.d("TAG_ACCELVALUE_INFORM2","ACCEL VALUE : "+AccelDelay+" , "+DataManagerSingleton.getInstance().getAccelBeaconDelayMap());

                AccelBeacon accelBeacon=new AccelBeacon();
                accelBeacon.setBeaconId(ID);
                accelBeacon.setRssi(Rssi);
                accelBeacon.setDelay(Delay);
                accelBeacon.setCount(Count);
                accelBeacon.setAccelDelay(AccelDelay);

                DataManagerSingleton.getInstance().getAccelBeaconArrayList().add(accelBeacon);

                Log.d("TAG_ACCELVALUE_INFORM",String.format("ID : %s , RSSI : %s , DELAY : %s , COUNT : %s",ID,Rssi,Delay,Count)+" , ACCELDELAY : "+AccelDelay);
            }
        }
    }
}
