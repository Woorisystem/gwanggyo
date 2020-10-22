package smartparking.poscoict.psj.poscoict_project.SJ_SensorOperation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import smartparking.poscoict.psj.poscoict_project.SJ_Activity.LoginActivity;


/** 센서 기능 정상 작동 확인
 *
 * 센서에 칼만 필터를 사용하지 않음 -> 평균 값을 내서 측정하는 것이기 때문에 센서 테스트에는 적합하지 않음
 *
 * Senaor Event Listener 를 통해 Sensor Value 를 받는 클래스
 * Accelometer , Gyroscope 사용
 * Sensor 속도 : Sensor Delay normal
 * onSensorChange 의 event 를 통해 값을 받아올 수 있다
 *
 * dialog_W.Start(); - 해당 함수를 호출하여 UI 부분적 처리 시작 -> 진행 넘기는 부분이 포함되어있다.
 *
 **/
public class Event implements SensorEventListener {

    Context context;

    // 다른 클래스 들
    Dialog dialog_W;
    Calc calc_W;

    // 사용자 지정 TAG
    String TAG="SensorOperation_Event";

    // 센서 동작 관련 변수
    SensorManager _SensorManager;
    Sensor        _AccelSensor;
    Sensor        _GyroSensor;

    /*Sensor variables*/
    private float[] _GyroValue=new float[3];
    private float[] _AccValue=new float[3];
    private double _AccPitch,_AccRoll,_AccYaw;

    private boolean gyroRunning=false;
    private boolean accRunning=false;

    /*for unsing complementary fliter*/
    private float a = 0.2f;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double pitch = 0, roll = 0,yaw=0;
    private double timestamp;
    private double dt;
    private double temp;



    public Event(Context context)
    {
        this.context=context;

        LoginActivity loginActivity=(LoginActivity)context;

        _SensorManager=(SensorManager)loginActivity.getSystemService(Context.SENSOR_SERVICE);
        _AccelSensor=_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _GyroSensor=_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        _SensorManager.registerListener(this,_AccelSensor,SensorManager.SENSOR_DELAY_NORMAL);
        _SensorManager.registerListener(this,_GyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        dialog_W=new Dialog(context);
        calc_W=Calc.getInstance();


        // Dialog 를 띄워 진행 시작
        dialog_W.Start();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_GYROSCOPE:
                _GyroValue=event.values;
                if(!gyroRunning)
                    gyroRunning = true;

                break;
            case Sensor.TYPE_ACCELEROMETER:
                _AccValue=event.values;
                if(!accRunning)
                    accRunning = true;
                break;
        }

        /**두 센서 새로운 값을 받으면 상보필터 적용*/
        if(gyroRunning && accRunning){
            complementaty(event.timestamp);

            Log.d(TAG,"TIME STAMP : "+event.timestamp);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void complementaty(double new_ts)
    {
        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }

        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        _AccPitch = -Math.atan2(_AccValue[0], _AccValue[2]) * 180.0 / Math.PI; // Y 축 기준
        _AccRoll= Math.atan2(_AccValue[1], _AccValue[2]) * 180.0 / Math.PI; // X 축 기준
        _AccYaw=Math.atan2(_AccValue[2],_AccValue[2])* 180.0 / Math.PI;     // Z 축 기준

        temp = (1/a) * (_AccPitch - pitch) + _GyroValue[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (_AccRoll - roll) + _GyroValue[0];
        roll = roll + (temp*dt);

        temp=  (1/a) * (_AccYaw  -  yaw)  + _GyroValue[2];
        yaw=yaw+ (temp*dt);

        float updateFreq = 30; // match this to your update speed
        float cutOffFreq = 0.9f;
        float RC = 1.0f / cutOffFreq;
        float dt = 1.0f / updateFreq;
        float filterConstant = RC / (dt + RC);
        float alpha = filterConstant;

        double lastAccel[] = new double[3];
        double Filter[] = new double[3];

        Filter[0] =  (alpha * (Filter[0] + roll - lastAccel[0]));
        Filter[1] =  (alpha * (Filter[1] + pitch - lastAccel[1]));
        Filter[2] =  (alpha * (Filter[2] + yaw - lastAccel[2]));

        lastAccel[0] = roll;
        lastAccel[1] = pitch;
        lastAccel[2] = yaw;


        switch (calc_W.getSTATECalc_W())
        {
            case "START":

                break;
            case "STAY":
                calc_W.STAY( Filter[0]);
                break;
            case "RIGHT":
                calc_W.RIGHT( Filter[0],Filter[1]);
                break;
            case "LEFT":
                calc_W.LEFT( Filter[0],Filter[1]);
                break;
            case "END":
                _SensorManager.unregisterListener(this);
                break;
        }
    }
}
