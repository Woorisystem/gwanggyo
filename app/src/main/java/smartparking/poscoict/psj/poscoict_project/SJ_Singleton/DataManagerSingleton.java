package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.AccelBeacon;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.AccelSensor;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.Beacon;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.GyroSensor;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.Total;


@Data
public class DataManagerSingleton {

    @Getter
    @Setter
    String InputTime="";

    private String PreState=null;

    private boolean Start1Beacon=false;     //  시작
    private boolean Start2Beacon=false;     //  시작2
    private boolean End1Beacon=false;       //  끝
    private boolean End2Beacon=false;       //  끝2
    private boolean OutParking=false;       //  타이머 종료시 출차 확인

    private boolean ParingState=false;      //  실시간 ParingStateCheck
    private boolean ParingStateSave=false;  //  한번이라도 Paring 되면 true 상태 유지

    private boolean ABNORMALEND=false;

    private ArrayList<Beacon> beaconArrayList;
    private ArrayList<GyroSensor> gyroSensorArrayList;
    private ArrayList<AccelSensor> accelSensorArrayList;
    private ArrayList<Total> totalArrayList;
    private ArrayList<AccelBeacon> accelBeaconArrayList;

    private ArrayList<Integer> INOUTDATAMAJOR;
    private ArrayList<Integer> NOSTARTBEACON;       //  시작 비컨을 못받았을 경우 -시작이 가능한지 일반 비컨이 많이 들어오는 경우를 측정

    private int CollectStartCalcBeacon=0;           //  시작 이후 비컨을 수집하여 일정시간동안 비컨이 안들어오는 상태일경우 종료 시켜버린다.

    private ArrayList<Integer> AccelBeaconDelayArray;

    private Map<String,AccelBeacon> accelBeaconMap;
    private Map<String,ArrayList<Integer>> AccelBeaconDelayMap;

    private int WholeTimerDelay=0;
    private int BeaconSequence=0;
    private int AccelSequence=0;

    private int AccelCount=0;

    private int SAVEDELAY=0;

    public Queue<Double> ROLLQ;
    public Queue<Double> PITCHQ;
    public Queue<Double> YAWQ;

    int SaveCountRoll=0,SaveCountPitch=0,SaveCountYaw=0;

    /** Timeout 시 시도횟수 Check **/
    private int TimeoutCount=0;
    private Total CANNOTSENDTOTALSAVE;

    //region Beacon Lobby
    private boolean LobbyGet=false;
    private boolean ElevatorGet=false;
    private String INOUTSTATE=null;
    private boolean RESTARTBEACON=false;
    private int LASTBEACON=0;
    //endregion

    private int PARINGCOUNT=0;

    private int AfterStartCount=0;

    private int AfterGyroCount=0;

    private int AfterLobbyEleCount=0;
    private boolean LobbyBeaconEnd=false;
    private boolean ElebeaconGet=false;

    private String ParingStateValue="non-paring";
    private static final DataManagerSingleton ourInstance = new DataManagerSingleton();

    public static DataManagerSingleton getInstance() {
        return ourInstance;
    }

    private DataManagerSingleton() {
        beaconArrayList=new ArrayList<>();
        gyroSensorArrayList=new ArrayList<>();
        accelSensorArrayList=new ArrayList<>();
        totalArrayList=new ArrayList<>();
        accelBeaconArrayList=new ArrayList<>();
        INOUTDATAMAJOR=new ArrayList<>();
        NOSTARTBEACON=new ArrayList<>();
        AccelBeaconDelayArray=new ArrayList<>();

        accelBeaconMap=new HashMap<>();
        AccelBeaconDelayMap=new HashMap<>();

        CANNOTSENDTOTALSAVE=new Total();

        ROLLQ=new LinkedList<>();
        PITCHQ=new LinkedList<>();
        YAWQ=new LinkedList<>();

        ROLLQ.clear();
        PITCHQ.clear();
        YAWQ.clear();
    }

    public void Reset(){

        Log.d("TAG_RESET","RESET 시도");
        InputTime="";

        PreState=null;

        Start1Beacon=false;     //  시작
        Start2Beacon=false;     //  시작2
        End1Beacon=false;       //  끝
        End2Beacon=false;       //  끝2
        OutParking=false;       //  타이머 종료시 출차 확인

        ABNORMALEND=false;
        ParingStateSave=false;

        beaconArrayList.clear();
        gyroSensorArrayList.clear();
        accelSensorArrayList.clear();
        totalArrayList.clear();
        accelBeaconArrayList.clear();

        INOUTDATAMAJOR.clear();
        NOSTARTBEACON.clear();

        AccelBeaconDelayArray.clear();

        AccelBeaconDelayMap.clear();
        accelBeaconMap.clear();

        PARINGCOUNT=0;

        WholeTimerDelay=0;
        BeaconSequence=0;
        AccelSequence=0;

        AccelCount=0;

        SAVEDELAY=0;

        ROLLQ.clear();
        PITCHQ.clear();
        YAWQ.clear();

        SaveCountRoll=0;
        SaveCountPitch=0;
        SaveCountYaw=0;

        //region Beacon Lobby
        LobbyGet=false;
        ElevatorGet=false;
        INOUTSTATE=null;
        RESTARTBEACON=false;
        LASTBEACON=0;

        TimeoutCount=0;
        TimerSingleton.getInstance().setAfterStart(false);
        AfterStartCount=0;
        AfterGyroCount=0;

        AfterLobbyEleCount=0;
        LobbyBeaconEnd=false;
        ElebeaconGet=false;
    }

}
