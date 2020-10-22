package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_BroadCast.AppNetWork;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.AccelSensor;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.Total;
import smartparking.poscoict.psj.poscoict_project.SJ_ETC.SaveArrayListValue;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.ServerData;


@Data
public class TimerSingleton {

    DataManagerSingleton dataManagerSingleton=DataManagerSingleton.getInstance();

    @Getter
    @Setter
    private boolean LobbyTimerStart=false;
    private boolean WholeTimerStart=false;
    private boolean AccelTimerStart=false;
    private boolean GyroTimerStart=false;

    // 시작 이후 수집하는 타이머
    private boolean COLLECTSTARTBEACONCALC=false;



    private boolean NotRestart=false;
    private boolean CollectAccelBeaconStart=false;
    private boolean CollectLobbyStart=false;
    private boolean StayRestartStart=false;
    private boolean NotStartBeaconStart=false;

    /** Time out 관련 Data **/
    private boolean TimeoutTimerStart=false;
    private int FirstTimeout=60*1000;
    private int SecondTimeout=120 * 1000;
    private int ThirdTimeout=180*1000;
    private CountDownTimer TimeoutCountDownTimer;

    private CountDownTimer LobbyTimer;
    private CountDownTimer WholeTimer;
    private CountDownTimer AccelTimer;
    private CountDownTimer GyroTimer;
    private CountDownTimer CollectAccelBeaconTimer;
    private CountDownTimer CollectLobbyTimer;
    private CountDownTimer StayRestartStartTimer;

    // 시작 이후 10초간 일반 비컨이 몇개들어오는지 셀 Count
    private CountDownTimer AfterStartCountDownTimer;
    private boolean AfterStart=false;

    // 자이로 발생이후 일반 비컨을 수집하는 타이머
    private boolean AFTERGYROSTARTCALC=false;
    private CountDownTimer AFTERGYROSTARTCOUNTDOWNTIMER;

    // 시작을 하였으나 로비비컨만 받고 엘리베이터 비컨을 못받았을때 상황을 확인하는 타이머
    private boolean AFTERLOBBYELECHECK=false;
    private CountDownTimer AFTERLOBBYELECHECKCOUNTDOWNTIMER;

    private static final TimerSingleton ourInstance = new TimerSingleton();

    public static TimerSingleton getInstance() {
        return ourInstance;
    }

    private TimerSingleton() {
    }

    private TimerTask NotStartBeaconStartTimerTask;
    private Timer NotStartBeaconStartTimer;

    private TimerTask CollectSTARTCALCTIMERTASK;
    private Timer CollectSTARTCALCTIMER;

    /** 로비 시작 Timer
     * 엘리베이터 비컨 -> 로비 비컨 을 받은 후 자이로가 들어오기 전까지 대기하도록 하는 Timer
     * 15분간 동작하며 해당 시간안에 자이로가 발생 안할 시에 종료 시켜버린다.**/
    public void StartLobbyTimer()
    {
        int Delay=3*1000;

        if(LobbyTimer!=null)
            LobbyTimer=null;

        LobbyTimerStart=true;

        LobbyTimer=new CountDownTimer(Delay,1000)
        {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                LobbyTimerStart=false;
            }
        }.start();
    }

    /** 전체 Timer
     * 주차 시스템 측정 시작
     * 해당 타이머가 돌경우에 정보 수집을 한다.
     * 15분간 동작하며 타이머가 끝날시 서버로 데이터를 보낸다.**/
    public void StartWholeTimer(final Context context)
    {
        DataManagerSingleton.getInstance().Reset();

        Log.d("TAG_GYRO_START","START WHOLE TIMER");

        AfterStartTIMER();

        if(!COLLECTSTARTBEACONCALC)
        {
            STARTCALCTIMER();
        }

        if(NotStartBeaconStart)
        {
            NotStartBeaconStartTimerTask.cancel();
            dataManagerSingleton.getNOSTARTBEACON().clear();
        }

        int Delay=900*1000;
        if(WholeTimer!=null)
            WholeTimer=null;

        WholeTimerStart=true;

        WholeTimer=new CountDownTimer(Delay,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int Time=dataManagerSingleton.getWholeTimerDelay();

                Time++;

                dataManagerSingleton.setWholeTimerDelay(Time);
            }

            @Override
            public void onFinish() {

                if(COLLECTSTARTBEACONCALC)
                {
                    CollectSTARTCALCTIMERTASK.cancel();
                    CollectSTARTCALCTIMER.cancel();
                    dataManagerSingleton.setCollectStartCalcBeacon(0);
                    TimerSingleton.getInstance().setCOLLECTSTARTBEACONCALC(false);
                }

                WholeTimerStart=false;
                COLLECTSTARTBEACONCALC=false;

                if(AccelTimerStart)
                {
                    if(AccelTimer!=null)
                    {
                        try
                        { AccelTimer.onFinish();
                            AccelTimer.cancel();}
                        catch (RuntimeException e)
                        {
                            Log.d("RUNTIMEEXCEPTION","ACCEL TIMER FINISH : "+e.getMessage());

                        }

                    }
                }

                if(GyroTimerStart)
                {
                    if(GyroTimer!=null)
                    {

                        try
                        { GyroTimer.onFinish();
                            GyroTimer.cancel();}
                        catch (RuntimeException e)
                        {
                            Log.d("RUNTIMEEXCEPTION","GYRO TIMER FINISH : "+e.getMessage());

                        }

                    }

                }


                if(dataManagerSingleton.isOutParking() && !dataManagerSingleton.isABNORMALEND() )
                {
                    ServerData serverData=new ServerData(context);
                    serverData.OutParking();

                }
                else
                {

                    String ParingState=dataManagerSingleton.getParingStateValue();

                    if(dataManagerSingleton.isABNORMALEND())
                    {
                        ParingState+="-end";
                    }

                    if(dataManagerSingleton.isLobbyBeaconEnd())
                    {
                        ParingState+="-lobby";
                    }

                    Log.d("TEST_AFTER_START","COUNT : "+dataManagerSingleton.getAfterStartCount() +"PARING STATE : "+ParingState);

                    SaveArrayListValue saveArrayListValue=new SaveArrayListValue();
                    saveArrayListValue.SaveAccelBeacon();

                    Total total=new Total();
                    total.setPhoneInfo(UserDataSingleton.getInstance().getCel());
                    total.setBeaconList(dataManagerSingleton.getBeaconArrayList());
                    total.setGyroList(dataManagerSingleton.getGyroSensorArrayList());
                    total.setSensorList(dataManagerSingleton.getAccelSensorArrayList());
                    total.setAccelBeaconList(dataManagerSingleton.getAccelBeaconArrayList());
                    total.setInputDate(dataManagerSingleton.getInputTime());
                    total.setParingState(ParingState);


                    dataManagerSingleton.getTotalArrayList().add(total);
                    String LogValue = String.format("ARRAY LIST ---- BEACON : %d , GYRO : %d , ACCEL : %d , ACCELBEACON : %d",dataManagerSingleton.getBeaconArrayList().size(),dataManagerSingleton.getGyroSensorArrayList().size(),dataManagerSingleton.getAccelSensorArrayList().size(),dataManagerSingleton.getAccelBeaconArrayList().size());

                    ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if(isConnected)
                    {

                        DataManagerSingleton.getInstance().setSAVEDELAY(DataManagerSingleton.getInstance().getWholeTimerDelay());
                        ServerData serverData=new ServerData(context);
                        serverData.Send(context,dataManagerSingleton.getTotalArrayList().get(dataManagerSingleton.getTotalArrayList().size()-1));

                        dataManagerSingleton.setParingStateValue("non-paring");
                    }
                    else
                    {
                        DataManagerSingleton.getInstance().setSAVEDELAY(DataManagerSingleton.getInstance().getWholeTimerDelay());
                        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

                        AppNetWork receiver = new AppNetWork();
                        context.registerReceiver(receiver, filter);
                    }

                    if(dataManagerSingleton.isOutParking())
                    {
                        ServerData serverData=new ServerData(context);
                        serverData.OutParking();
                    }

                }


                dataManagerSingleton.setWholeTimerDelay(0);
            }
        }.start();
    }

    public void AccelTimer()
    {
        int Delay=2*1000;

        if(AccelTimer!=null)
            AccelTimer=null;

        AccelTimerStart=true;

        AccelTimer=new CountDownTimer(Delay,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                AccelTimerStart=false;

                SaveArrayListValue saveArrayListValue=new SaveArrayListValue();

                String AccelResult=saveArrayListValue.AccelSensorResult();

                String PreData=dataManagerSingleton.getPreState();

                if(PreData==null)
                {
                    dataManagerSingleton.setPreState(AccelResult);
                }
                else
                {
                    // 이전값이 T 이고 현재 값이 S/W 일경우 타이머를 돌린다.
                    if((PreData=="T" || PreData.equals("T")) && ((AccelResult=="S" || AccelResult.equals("S"))|| (AccelResult=="W" || AccelResult.equals("W"))))
                    {
                        if(!CollectAccelBeaconStart)
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // only for gingerbread and newer versions
                                StartCollectAccelBeacon();
                            }
                            else
                            {
                                Handler handler=new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        StartCollectAccelBeacon();
                                    }
                                });
                            }

                        }
                    }

                    dataManagerSingleton.setPreState(AccelResult);
                }

                if(TimerSingleton.getInstance().isWholeTimerStart())
                {
                    AccelSensor accelSensor=new AccelSensor();
                    accelSensor.setState(AccelResult);
                    accelSensor.setSeq(String.valueOf(dataManagerSingleton.getAccelSequence()));
                    accelSensor.setDelay(String.valueOf(dataManagerSingleton.getWholeTimerDelay()));

                    dataManagerSingleton.getAccelSensorArrayList().add(accelSensor);
                }

                int accelsensorsequence=dataManagerSingleton.getAccelSequence()+1;
                dataManagerSingleton.setAccelSequence(accelsensorsequence);

                dataManagerSingleton.setAccelCount(0);

            }
        }.start();
    }

    public void GyroTimer()
    {

        int Delay=1*1000;

        if(GyroTimer!=null)
            GyroTimer=null;

        GyroTimerStart=true;

        GyroTimer=new CountDownTimer(Delay,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                GyroTimerStart=false;
            }
        }.start();
    }

    public void StartCollectAccelBeacon()
    {
        CollectAccelBeaconStart=true;

        CollectAccelBeaconTimer=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                CollectAccelBeaconStart=false;
            }
        }.start();
    }

    public void StartCollectLobby(final Context context)
    {
        CollectLobbyStart=true;

        CollectLobbyTimer=new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                CollectLobbyStart=false;

                if(!NotRestart)
                {
                    if(dataManagerSingleton.getINOUTDATAMAJOR().size()==0)
                    {
                        Log.d("TAG_EST_BEACON_VALUE","END VALUE : "+dataManagerSingleton.getLASTBEACON());
                        if(dataManagerSingleton.getLASTBEACON()==3)
                        {
                            if(isWholeTimerStart())
                            {
                                try
                                {
                                   WholeTimer.onFinish();
                                    WholeTimer.cancel();
                                }
                                catch (RuntimeException e)
                                {
                                    Log.e("RUNTIMEEXCEPTION","Whole Timer Finish : "+e.getMessage());
                                }
                            }

                        }
                        else
                        {
                            dataManagerSingleton.getINOUTDATAMAJOR().clear();
                            StartCollectLobby(context);
                        }

                    }
                    else
                    {
                        int Last=dataManagerSingleton.getINOUTDATAMAJOR().get(dataManagerSingleton.getINOUTDATAMAJOR().size()-1);

                        dataManagerSingleton.setLASTBEACON(Last);
                        dataManagerSingleton.getINOUTDATAMAJOR().clear();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            StartCollectLobby(context);
                        }
                        else
                        {
                            Handler handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    StartCollectLobby(context);
                                }
                            });
                        }

                    }
                }
                else
                {
                    dataManagerSingleton.getINOUTDATAMAJOR().clear();
                }


            }
        }.start();
    }

    public void StartStayRestart()
    {
        StayRestartStart=true;

        StayRestartStartTimer=new CountDownTimer(900*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                StayRestartStart=false;

                dataManagerSingleton.setRESTARTBEACON(false);
                dataManagerSingleton.setEnd1Beacon(false);
                dataManagerSingleton.setEnd2Beacon(false);
            }
        }.start();
    }

    public void NotStartBeacon()
    {
        NotStartBeaconStart=true;

        NotStartBeaconStartTimerTask =new TimerTask() {
            @Override
            public void run() {
//                Log.d("TAG_TEST_NOTSTART","VALUE BEACON : "+dataManagerSingleton.getNOSTARTBEACON().size());
                dataManagerSingleton.getNOSTARTBEACON().clear();
            }
        };

        NotStartBeaconStartTimer=new Timer();
        NotStartBeaconStartTimer.schedule(NotStartBeaconStartTimerTask,10000,10000);
    }

    public void STARTCALCTIMER()
    {
        COLLECTSTARTBEACONCALC=true;

        CollectSTARTCALCTIMERTASK=new TimerTask() {
            @Override
            public void run() {

                Log.e("TEST_TIMERSINGLETON","30초");
                if(DataManagerSingleton.getInstance().getCollectStartCalcBeacon()!=0)
                {
                    Log.d("TEST_TIMERSINGLETON_VAL","ACCELBEACON 0개 아님 종료 처리 X : "+DataManagerSingleton.getInstance().getCollectStartCalcBeacon());
                    DataManagerSingleton.getInstance().setCollectStartCalcBeacon(0);
                }
                else
                {
                    // 전체 타이머가 돌고있을 경우 전체 타이머 종료
                    if(WholeTimerStart)
                    {
                        if(!CollectLobbyStart)
                        {
                            if(!dataManagerSingleton.isEnd1Beacon())
                            {
                                dataManagerSingleton.setABNORMALEND(true);

                                Log.d("TEST_TIMERSINGLETON_VAL","ACCELBEACON 0개 종료 처리");
                                dataManagerSingleton.setOutParking(true);

                                if(isWholeTimerStart())
                                {
                                    try
                                    {
                                        WholeTimer.onFinish();
                                        WholeTimer.cancel();
                                    }
                                    catch (RuntimeException e)
                                    {
                                        Log.e("RUNTIMEEXCEPTION","Whole Timer Finish : "+e.getMessage());
                                    }
                                }


                                CollectSTARTCALCTIMERTASK.cancel();
                                CollectSTARTCALCTIMER.cancel();
                            }

                        }
                    }
                    // 초기화
                    DataManagerSingleton.getInstance().setCollectStartCalcBeacon(0);
                }

            }
        };

        CollectSTARTCALCTIMER=new Timer();
        CollectSTARTCALCTIMER.schedule(CollectSTARTCALCTIMERTASK,30000,30000);
    }

    public void SENDTIMEOUT(final Context context)
    {
        int count=dataManagerSingleton.getTimeoutCount();

        int DelayTimer=FirstTimeout;

        if(count==1)
        {DelayTimer=SecondTimeout;}
        else if(count==2)
        {DelayTimer=ThirdTimeout;}

        if(!TimeoutTimerStart)
        {
            TimeoutTimerStart=true;
            count++;
            dataManagerSingleton.setTimeoutCount(count);

            TimeoutCountDownTimer=new CountDownTimer(DelayTimer,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {

                    TimeoutTimerStart=false;

                    ServerData serverData=new ServerData(context);
                    serverData.Send(context,DataManagerSingleton.getInstance().getCANNOTSENDTOTALSAVE());
                }
            }.start();
        }
    }

    public void AfterStartTIMER()
    {
        if(!AfterStart)
        {
            AfterStart=true;
            AfterStartCountDownTimer=new CountDownTimer(10000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    AfterStart=false;

                    String ParingState="non-paring";

                    if(dataManagerSingleton.getAfterStartCount()<10)
                    {
                        ParingState="paring";
                    }
                    else
                    {
                        ParingState="non-paring";
                    }

                    dataManagerSingleton.setParingStateValue(ParingState);
                }
            }.start();
        }
    }

    public void AFTERGYROSTARTTIMER(final Context context)
    {
        AFTERGYROSTARTCALC=true;
        AFTERGYROSTARTCOUNTDOWNTIMER=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {


                if(dataManagerSingleton.getAfterGyroCount()!=0)
                {

                    if(!TimerSingleton.getInstance().isWholeTimerStart())
                    {
                        Log.d("TAG_GYRO_START","IS WHOLE : "+TimerSingleton.getInstance().isWholeTimerStart());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            TimerSingleton.getInstance().StartWholeTimer(context);

                        }
                        else
                        {
                            Handler handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TimerSingleton.getInstance().StartWholeTimer(context);
                                }
                            });
                        }



                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDate=simpleDateFormat.format(Calendar.getInstance().getTime());
                        DataManagerSingleton.getInstance().setInputTime(currentDate);

                        ServerData serverData=new ServerData(context);
                        serverData.GateInfo("GYRO_START","GYRO_START");

                        try
                        {
                            AFTERGYROSTARTCOUNTDOWNTIMER.onFinish();
                            AFTERGYROSTARTCOUNTDOWNTIMER.cancel();
                        }
                        catch (RuntimeException e)
                        {
                            Log.e("RUNTIMEEXCEPTION","AFTER GYRO START TIME ON FINISH : "+e.getMessage());

                        }

                    }


                }

            }

            @Override
            public void onFinish() {

                AFTERGYROSTARTCALC=false;
                ServerData serverData=new ServerData(context);
                serverData.GyroSend(String.valueOf(dataManagerSingleton.getAfterGyroCount()));
            }
        }.start();
    }

    public void AFTERLOBBYELETIMER()
    {
        AFTERLOBBYELECHECK=true;
        AFTERLOBBYELECHECKCOUNTDOWNTIMER=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {

                if(dataManagerSingleton.getAfterLobbyEleCount()==0)
                {
                    if(WholeTimerStart && !dataManagerSingleton.isElebeaconGet())
                    {
                        Log.d("TAG_BEACON_FUNCTION","END LOBBY");
                        dataManagerSingleton.setLobbyBeaconEnd(true);
                        try
                        {
                            WholeTimer.onFinish();
                            WholeTimer.cancel();
                        }
                        catch (RuntimeException e)
                        {
                            Log.e("RUNTIMEEXCEPTION","Whole Timer Finish : "+e.getMessage());
                        }
                        AFTERLOBBYELECHECK=false;
                    }
                    else
                    {
                        AFTERLOBBYELECHECK=false;
                        Log.d("TAG_BEACON_FUNCTION","END LOBBY NOMATCH");
                    }
                }
                else
                {
                    AFTERLOBBYELECHECK=false;
                    AFTERLOBBYELETIMER();
                    Log.d("TAG_BEACON_FUNCTION","WHOLETIMER : "+WholeTimerStart+" , "+dataManagerSingleton.isElebeaconGet()+" , COUNT : "+dataManagerSingleton.getAfterLobbyEleCount());
                    dataManagerSingleton.setAfterLobbyEleCount(0);
                }

            }
        }.start();
    }
}
