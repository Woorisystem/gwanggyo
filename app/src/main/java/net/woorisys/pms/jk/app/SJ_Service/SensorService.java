package net.woorisys.pms.jk.app.SJ_Service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import net.woorisys.pms.jk.app.PublicValue;
import net.woorisys.pms.jk.app.SJ_ETC.KalmanFilter;
import net.woorisys.pms.jk.app.SJ_ETC.SaveArrayListValue;
import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.TimerSingleton;

import static android.view.KeyCharacterMap.ALPHA;

public class SensorService extends Service implements SensorEventListener{

    SensorManager SensorManager_W;
    Sensor AccelSensor_W;
    Sensor GyroSensor_W;

    int RollResultCount=0,PitchResultCount=0,YawResultCount=0;

    int PreRollCount=0;
    int PrePitchCount=0;
    int PreYawCount=0;

    float NextValue=0;      //현재 CVA 값
    float PreValue=0;       //이전 CVA 값
    int DefaultAbsValue=4;  //기본 Default Prevalue-NextValue 절대값

    boolean BMatchValue=true;
    int IMatchValue=0;

    private KalmanFilter KalmanX;
    private KalmanFilter KalmanY;
    private KalmanFilter KalmanZ;

    boolean SR=false;
    boolean SP=false;
    boolean SY=false;

    private double mX=0, mY=0,mZ=0;

    int limitvalue=100;

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SensorManager_W=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        AccelSensor_W=SensorManager_W.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroSensor_W=SensorManager_W.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        SensorManager_W.registerListener(this,AccelSensor_W,SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        SensorManager_W.registerListener(this,GyroSensor_W,SensorManager.SENSOR_STATUS_ACCURACY_LOW);

        KalmanX=new KalmanFilter(0.0f);
        KalmanY=new KalmanFilter(0.0f);
        KalmanZ=new KalmanFilter(0.0f);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Notification notification = new NotificationService(this).createForegroundNotification(this);
        startForeground(NotificationService.FOREGROUND_NOTIFICATION_ID,notification);

        Intent send=new Intent(PublicValue.ACTION_SENSOR_SERVICE_START);
        sendBroadcast(send);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType()== Sensor.TYPE_ACCELEROMETER)
        {
            float X=event.values[0];
            float Y=event.values[1];
            float Z=event.values[2];

            float CVA;

            // CVA 값 계산
            float[] accelData = new float[3];
            accelData = filter(event.values.clone(), accelData);
            CVA = (float) Math.sqrt(accelData[0] * accelData[0] + accelData[1] * accelData[1] + accelData[2] * accelData[2]);

            // 이전 값이 있을 경우에는 계산을 진행한다.
            if(PreValue!=0)
            {
                NextValue=CVA;

                float ABSValue=Math.abs(PreValue-NextValue);

                if(ABSValue>=DefaultAbsValue)
                {
                    int accel_count= DataManagerSingleton.getInstance().getAccelCount()+1;
                    DataManagerSingleton.getInstance().setAccelCount(accel_count);
                }
                PreValue=NextValue;
            }
            else
                PreValue=CVA;

            if(!TimerSingleton.getInstance().isAccelTimerStart())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // only for gingerbread and newer versions
                    TimerSingleton.getInstance().AccelTimer();
                }
                else
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TimerSingleton.getInstance().AccelTimer();

                        }
                    });
                }
            }
        }
        if(event.sensor.getType()== Sensor.TYPE_GYROSCOPE)
        {
            float LIMIT_MAX=0.5f;
            float LIMIT_MIN=-0.5f;

//            float LIMIT_MAX=5.0f;
//            float LIMIT_MIN=-5.0f;

            double Roll=event.values[0];
            double Pitch=event.values[1];
            double Yaw=event.values[2];

//            Log.d("test", "Roll : "+Roll + " Pitch : "+ Pitch+" YaW"+Yaw);

            if(!TimerSingleton.getInstance().isGyroTimerStart())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // only for gingerbread and newer versions
                    TimerSingleton.getInstance().GyroTimer();
                }
                else
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TimerSingleton.getInstance().GyroTimer();


                        }
                    });
                }
            }
            else
            {
                if((Roll>LIMIT_MAX || Roll<LIMIT_MIN) || (Pitch>LIMIT_MAX || Pitch<LIMIT_MIN) || (Yaw>LIMIT_MAX || Yaw<LIMIT_MIN))
                {
                    KalmanX.Init();
                    KalmanY.Init();
                    KalmanZ.Init();
                }
                else
                {
                    double FilterX=KalmanX.Update(Roll);
                    double FilterY=KalmanY.Update(Pitch);
                    double FilterZ=KalmanZ.Update(Yaw);

                    GyroSensorResult((FilterX),(FilterY),(FilterZ));

                    mX=FilterX;
                    mY=FilterY;
                    mZ=FilterZ;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Sensor X,Y,Z 값 을 CVA 로 사용할 Data 변환 시켜주는 코드
    private float[] filter(float[] input, float[] output)
    {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void GyroSensorResult(double Roll,double Pitch,double Yaw)
    {
        float LimitMinus=-0.025f;
        float LimitPlus=0.025f;

        if(DataManagerSingleton.getInstance().ROLLQ.size()==4)
        {
            Object[] RollA=DataManagerSingleton.getInstance().ROLLQ.toArray();

            if(!(((double)RollA[0]<LimitPlus && (double)RollA[0]>LimitMinus) || ((double)RollA[1]<LimitPlus && (double)RollA[1]>LimitMinus) || ((double)RollA[2]<LimitPlus && (double)RollA[2]>LimitMinus) || ((double)RollA[3]<LimitPlus && (double)RollA[3]>LimitMinus)))
            {
                RollResultCount++;

            }
            else
            {
                if(RollResultCount>=limitvalue)
                {

                    SR=true;
                    PreRollCount=RollResultCount;
                }
                RollResultCount=0;
            }

            double FIRSTR=DataManagerSingleton.getInstance().ROLLQ.poll();
            double SECONDR=DataManagerSingleton.getInstance().ROLLQ.poll();
            double THIRDR=DataManagerSingleton.getInstance().ROLLQ.poll();
            double FORTHR=DataManagerSingleton.getInstance().ROLLQ.poll();

            DataManagerSingleton.getInstance().ROLLQ.offer(SECONDR);
            DataManagerSingleton.getInstance().ROLLQ.offer(THIRDR);
            DataManagerSingleton.getInstance().ROLLQ.offer(FORTHR);
            DataManagerSingleton.getInstance().ROLLQ.offer(Roll);
        }
        else
        {
            DataManagerSingleton.getInstance().ROLLQ.offer(Roll);

        }

        if(DataManagerSingleton.getInstance().PITCHQ.size()==4)
        {


            Object[] PITCHA=DataManagerSingleton.getInstance().PITCHQ.toArray();

            if(!(((double)PITCHA[0]<LimitPlus && (double)PITCHA[0]>LimitMinus) || ((double)PITCHA[1]<LimitPlus && (double)PITCHA[1]>LimitMinus) || ((double)PITCHA[2]<LimitPlus && (double)PITCHA[2]>LimitMinus) || ((double)PITCHA[3]<LimitPlus && (double)PITCHA[3]>LimitMinus)))
            {
                PitchResultCount++;
            }
            else
            {
                if( PitchResultCount>=limitvalue)
                {
                    SP=true;
                    PrePitchCount=PitchResultCount;

                }
                PitchResultCount=0;
            }

            double FIRSTP=DataManagerSingleton.getInstance().PITCHQ.poll();
            double SECONDP=DataManagerSingleton.getInstance().PITCHQ.poll();
            double THIRDP=DataManagerSingleton.getInstance().PITCHQ.poll();
            double FORTHP=DataManagerSingleton.getInstance().PITCHQ.poll();

            DataManagerSingleton.getInstance().PITCHQ.offer(SECONDP);
            DataManagerSingleton.getInstance().PITCHQ.offer(THIRDP);
            DataManagerSingleton.getInstance().PITCHQ.offer(FORTHP);
            DataManagerSingleton.getInstance().PITCHQ.offer(Pitch);
        }
        else
        {
            DataManagerSingleton.getInstance().PITCHQ.offer(Pitch);
        }

        if(DataManagerSingleton.getInstance().YAWQ.size()==4)
        {
            Object[] YAWA=DataManagerSingleton.getInstance().YAWQ.toArray();

            if(!(
                    ((double)YAWA[0]<LimitPlus && (double)YAWA[0]>LimitMinus)
                            || ((double)YAWA[1]<LimitPlus && (double)YAWA[1]>LimitMinus)
                            || ((double)YAWA[2]<LimitPlus && (double)YAWA[2]>LimitMinus) || ((double)YAWA[3]<LimitPlus && (double)YAWA[3]>LimitMinus)))
            {
                YawResultCount++;
            }
            else
            {
                if(YawResultCount>=limitvalue)
                {
                    SY=true;
                    PreYawCount=YawResultCount;
                }
                YawResultCount=0;
            }

            double FIRSTY=DataManagerSingleton.getInstance().YAWQ.poll();
            double SECONDY=DataManagerSingleton.getInstance().YAWQ.poll();
            double THIRDY=DataManagerSingleton.getInstance().YAWQ.poll();
            double FORTH=DataManagerSingleton.getInstance().YAWQ.poll();


            DataManagerSingleton.getInstance().YAWQ.offer(SECONDY);
            DataManagerSingleton.getInstance().YAWQ.offer(THIRDY);
            DataManagerSingleton.getInstance().YAWQ.offer(FORTH);
            DataManagerSingleton.getInstance().YAWQ.offer(Yaw);
        }
        else
        {
            DataManagerSingleton.getInstance().YAWQ.offer(Yaw);
        }

        if((PreRollCount>=limitvalue || PrePitchCount >=limitvalue || PreYawCount >=limitvalue) && (RollResultCount==0 && PreRollCount!=0) || (PitchResultCount==0 &&PrePitchCount!=0) || (YawResultCount==0 && PreYawCount!=0))
        {
            IMatchValue++;
            BMatchValue=true;


            DataManagerSingleton.getInstance().setSaveCountRoll(PreRollCount);
            DataManagerSingleton.getInstance().setSaveCountPitch(PrePitchCount);
            DataManagerSingleton.getInstance().setSaveCountYaw(PreYawCount);

            SR=false;
            SP=false;
            SY=false;
            PreRollCount=0;
            PrePitchCount=0;
            PreYawCount=0;
        }
        else
        {
            BMatchValue=false;
            if(!BMatchValue && IMatchValue!=0)
            {
                 if(DataManagerSingleton.getInstance().isRESTARTBEACON() && !TimerSingleton.getInstance().isWholeTimerStart() && (DataManagerSingleton.getInstance().getPreState()=="T"|| DataManagerSingleton.getInstance().getPreState().equals("T")) && TimerSingleton.getInstance().isStayRestartStart())
                 {

                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                         // only for gingerbread and newer versions
                         TimerSingleton.getInstance().StartWholeTimer(getApplicationContext());

                     }
                     else
                     {
                         Handler handler=new Handler(Looper.getMainLooper());
                         handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 TimerSingleton.getInstance().StartWholeTimer(getApplicationContext());

                             }
                         });
                     }

                     SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     String currentDate=simpleDateFormat.format(Calendar.getInstance().getTime());
                     DataManagerSingleton.getInstance().setInputTime("move_"+currentDate);

                     if(TimerSingleton.getInstance().isStayRestartStart())
                     {
                         try
                         {
                             TimerSingleton.getInstance().getStayRestartStartTimer().onFinish();
                             TimerSingleton.getInstance().getStayRestartStartTimer().cancel();
                         }
                         catch (RuntimeException e)
                         {
                             Log.e("RUNTIMEEXCEPTION","STY RESTART START ONFINISH : "+e.getMessage());
                         }
                     }
                 }

                 if(!TimerSingleton.getInstance().isWholeTimerStart())
                 {
                     if(!TimerSingleton.getInstance().isAFTERGYROSTARTCALC())
                     {
                        TimerSingleton.getInstance().AFTERGYROSTARTTIMER(getApplicationContext());
                     }
                 }

                if(TimerSingleton.getInstance().isWholeTimerStart())
                {
                    SaveArrayListValue saveArrayListValue=new SaveArrayListValue();
                    saveArrayListValue.SaveGyro(getApplicationContext());

                }

                BMatchValue=false;
                IMatchValue=0;

                PitchResultCount=0;
                RollResultCount=0;
                YawResultCount=0;
            }
        }
    }
}
