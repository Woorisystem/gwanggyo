package net.woorisys.pms.jk.app.SJ_ETC;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import net.woorisys.pms.jk.app.SJ_Domain.Beacon;
import net.woorisys.pms.jk.app.SJ_Domain.LobbyOpenData;
import net.woorisys.pms.jk.app.SJ_Server.ServerData;
import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.TimerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.UserDataSingleton;


public class BeaconFunction {

    Context context;

    TimerSingleton timerSingleton;
    DataManagerSingleton dataManagerSingleton;


    public BeaconFunction(Context context)
    {
        this.context=context;

        timerSingleton= TimerSingleton.getInstance();
        dataManagerSingleton=DataManagerSingleton.getInstance();
    }

    // 입구 비컨
    public void ENTRANCEBEACON(double rssi, int minor ,int major)
    {

        if(timerSingleton.isAfterStart())
        {
            int count=dataManagerSingleton.getAfterStartCount();
            count++;
            dataManagerSingleton.setAfterStartCount(count);
        }

        if(timerSingleton.isAFTERGYROSTARTCALC())
        {
            int value=dataManagerSingleton.getAfterGyroCount();
            value++;
            dataManagerSingleton.setAfterGyroCount(value);

        }
        /** 주차 시작 조건
         * RSSI -90 이상
         * 입차 Beacon 이 들어온적이 없어야한다.
         * 주차장 Beacon 이 들어온적이 없어야한다.
         * 전체 타이머 (데이터를 수집하는 기준) 가 동작하지 않는다.
         **/
        if(rssi>=-90)
        {
            if(!dataManagerSingleton.isStart2Beacon())
            {
                if(!dataManagerSingleton.isStart1Beacon())
                {
                    if(!timerSingleton.isWholeTimerStart())
                    {
                        dataManagerSingleton.setStart1Beacon(true);

                        ServerData serverData=new ServerData(context);
                        serverData.GateInfo(String.valueOf(minor),String.valueOf(major));
                    }
                }
            }
            else
            {
                if(!dataManagerSingleton.isStart1Beacon())
                {

                    dataManagerSingleton.setStart1Beacon(true);

                    ServerData serverData=new ServerData(context);
                    serverData.GateInfo(String.valueOf(minor),String.valueOf(major));

                    //dataManagerSingleton.setOutParking(true);

                    //전체 타이머가 돌고 있을 경우
                    if(timerSingleton.isWholeTimerStart())
                    {
                        try
                        {
                            timerSingleton.getWholeTimer().onFinish();
                            timerSingleton.getWholeTimer().cancel();
                        }
                        catch (RuntimeException e)
                        {
                            Log.e("RUNTIMEEXCEPTION","Whole Timer Finish : "+e.getMessage());
                        }

                    }
//                    else
//                    {
//                        serverData.OutParking();
//                    }

                    if(timerSingleton.isCollectLobbyStart())
                    {
                        timerSingleton.setNotRestart(true);
                        try
                        {timerSingleton.getCollectLobbyTimer().onFinish();
                            timerSingleton.getCollectLobbyTimer().cancel();}
                        catch (RuntimeException e)
                        {
                            //Log.e("RUNTIMEEXCEPTION","COLLECT LOBBY TIMER ON FINISH : "+e.getMessage());
                        }

                        timerSingleton.setNotRestart(false);
                    }

                    if(timerSingleton.isStayRestartStart())
                    {
                        try
                        {
                            timerSingleton.getStayRestartStartTimer().onFinish();
                            timerSingleton.getStayRestartStartTimer().cancel();
                        }
                        catch (RuntimeException e)
                        {
                            Log.e("RUNTIMEEXCEPTION","STY RESTART START ONFINISH : "+e.getMessage());
                        }

                    }

                }
            }
        }
    }

    // 주차장 비컨
    public void PARKINGBEACON(double rssi, int minor ,int major)
    {

        if(timerSingleton.isAfterStart())
        {
            int count=dataManagerSingleton.getAfterStartCount();
            count++;
            dataManagerSingleton.setAfterStartCount(count);

            Log.d("TEST_AFTER_START","COUNT : "+count);
        }

//        if(timerSingleton. ())
//        {
//            int value=dataManagerSingleton.getAfterGyroCount();
//            value++;
//            Log.d("AFTERGYROSTARTCALC","COUNT : "+value);
//            dataManagerSingleton.setAfterGyroCount(value);
//        }

        if(rssi>=-80)
        {
            if(dataManagerSingleton.isStart1Beacon())
            {
                if(!dataManagerSingleton.isStart2Beacon())
                {
                    if(!timerSingleton.isWholeTimerStart())
                    {

                        dataManagerSingleton.setStart2Beacon(true);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            timerSingleton.StartWholeTimer(context);
                        }
                        else
                        {
                            Handler handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    timerSingleton.StartWholeTimer(context);
                                }
                            });
                        }
                        dataManagerSingleton.setEnd1Beacon(false);
                        dataManagerSingleton.setEnd2Beacon(false);

                        ServerData serverData=new ServerData(context);
                        serverData.GateInfo(String.valueOf(minor),String.valueOf(major));

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDate=simpleDateFormat.format(Calendar.getInstance().getTime());
                        dataManagerSingleton.setInputTime(currentDate);

                        serverData=new ServerData(context);
                        serverData.GateInfo("NORMAL_START","NORMAL_START");
                    }
                }
            }
            else
            {
                if(!dataManagerSingleton.isStart2Beacon())
                {
                    dataManagerSingleton.setStart2Beacon(true);

                    ServerData serverData=new ServerData(context);
                    serverData.GateInfo(String.valueOf(minor),String.valueOf(major));
                }
            }
        }
    }

    /**
     * ** 종료 준비 **
     * -> 전재 조건 : 3번 비컨(엘리베이터)이 컨저 들어와선 안된다
     *              : 1번 비컨(로비)을 받은 후 3번 비컨(엘리베이터)을 받을 수 있도록 처리하여야 한다.
     *              : 1번 비컨(로비)를 초기화 시키기 전까지는 한번 받은 이후로 받을 수 없다.
     *              : 들어온 이후로는 계속 받는지만 Check
     *
     * -> 종료
     *              : 로비 -> 엘리베이터 순으로 비컨이 들어와야한다.
     *              : 로비 들어온 이후 엘리베이터를 받으면 종료 모드 시작
     *              : 종료 모드 시작되면 일정 시간동안 로비.엘리베이터 비컨을 계속 받는지 Check -> 안받기 시작한 시점을 기준으로 3~5초간 안들어오면 종료
     *
     * -> 시작      : 엘리베이터 -> 로비 순으로 비컨이 들어와야한다.
     *              : 로비가 들어온 이후 T 상태로 Gyro 가 발생할 경우 시작
     *
     *  -> 1층 로비 : 3번째 자리가 1이다.            **/
    public void LOBBYBEACON(final double rssi , int minor,int major)
    {

        if(timerSingleton.isAFTERLOBBYELECHECK())
        {
            int value=dataManagerSingleton.getAfterLobbyEleCount();
            value++;
            dataManagerSingleton.setAfterLobbyEleCount(value);
        }

        if(timerSingleton.isCOLLECTSTARTBEACONCALC())
        {
            int value=dataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            dataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
        //ArrayList<LobbyOpenData> lobbyOpenData=userDataSingleton.getOpenMINOR();
        ArrayList<LobbyOpenData> lobbyOpenData = userDataSingleton.getOpenMINOR();

        if(timerSingleton.isCollectLobbyStart())
            dataManagerSingleton.getINOUTDATAMAJOR().add(major);

        //endregion

        if(rssi>=-80)
        {
            // region 종료 준비
            // 로비 , 엘리베이터 둘다 들어오지 않아야 한다.
            if(!dataManagerSingleton.isEnd2Beacon() && !dataManagerSingleton.isEnd1Beacon())
            {
                // 타이머가 돌고있어야 한다.
                if(timerSingleton.isWholeTimerStart())
                {

                    ServerData serverData=new ServerData(context);
                    serverData.GateInfo(String.valueOf(minor),String.valueOf(major));

                    dataManagerSingleton.setEnd1Beacon(true);
                    dataManagerSingleton.setINOUTSTATE("OUT");
                    if(!timerSingleton.isAFTERLOBBYELECHECK())
                    {
                        Log.d("TAG_BEACON_FUNCTION","TIMER LOBBY END START");
                        timerSingleton.AFTERLOBBYELETIMER();
                    }
                }
            }
            //endregion

            // region 시작
            if(!dataManagerSingleton.isEnd1Beacon() && dataManagerSingleton.isEnd2Beacon())
            {
                if(!timerSingleton.isWholeTimerStart())
                {
                    if(dataManagerSingleton.getINOUTSTATE()=="IN"|| dataManagerSingleton.getINOUTSTATE().equals("IN"))
                    {
                        dataManagerSingleton.setEnd1Beacon(true);
                        dataManagerSingleton.setRESTARTBEACON(true);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            timerSingleton.StartStayRestart();
                        }
                        else
                        {
                            Handler handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    timerSingleton.StartStayRestart();
                                }
                            });
                        }

                        ServerData serverData=new ServerData(context);
                        serverData.GateInfo(String.valueOf(minor),String.valueOf(major));
                    }
                }
            }
            // endregion
        }

    }

    // 엘리베이터 비컨
    public void ELEVATORBEACON(double rssi, int major,int minor)
    {
        dataManagerSingleton.setBeaconThree(true);

//        if(!dataManagerSingleton.isBeaconFour()) {
//            dataManagerSingleton.setOutParkingFlag(true);
//        }
//        else {
//            dataManagerSingleton.setOutParkingFlag(false);
//            //dataManagerSingleton.setABNORMALEND(true);
//        }

        if(timerSingleton.isCOLLECTSTARTBEACONCALC())
        {
            if(!dataManagerSingleton.isElebeaconGet())
            {
                dataManagerSingleton.setElebeaconGet(true);
            }

            int value=dataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            dataManagerSingleton.setCollectStartCalcBeacon(value);
        }


        if(rssi>=-75)
        {
            if(timerSingleton.isCollectLobbyStart()) {
                dataManagerSingleton.getINOUTDATAMAJOR().add(major);
            }

            // region 종료
            if(dataManagerSingleton.isEnd1Beacon() && !dataManagerSingleton.isEnd2Beacon())
            {
                if(timerSingleton.isWholeTimerStart())
                {
                    if(dataManagerSingleton.getINOUTSTATE()=="OUT" || dataManagerSingleton.getINOUTSTATE().equals("OUT"))
                    {
                        // 타이머 돌림 - 5초간 값 들어온 것을 Check -> 1,3 번 비컨 없을시 종료 , 있을시 재시작
                        dataManagerSingleton.setEnd2Beacon(true);

                        if(!timerSingleton.isCollectLobbyStart())
                        {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                // only for gingerbread and newer versions
                                timerSingleton.StartCollectLobby(context);
                            }
                            else
                            {
                                Handler handler=new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        timerSingleton.StartCollectLobby(context);
                                    }
                                });
                            }


                            ServerData serverData=new ServerData(context);
                            serverData.GateInfo(String.valueOf(minor),String.valueOf(major));
                        }
                    }
                }
            }
            //endregion

            //region 시작 준비
            if(!dataManagerSingleton.isEnd1Beacon() && !dataManagerSingleton.isEnd2Beacon())
            {
                if(!timerSingleton.isWholeTimerStart())
                {
                    dataManagerSingleton.setEnd2Beacon(true);
                    dataManagerSingleton.setINOUTSTATE("IN");

                    ServerData serverData=new ServerData(context);
                    serverData.GateInfo(String.valueOf(minor),String.valueOf(major));
                }
            }
            //endregion
        }
    }

    public void StayBeacon(int minor,int major,double rssi,SaveArrayListValue saveArrayListValue)
    {
        dataManagerSingleton.setBeaconFour(true);
//        if(!dataManagerSingleton.isBeaconThree()) {
//            dataManagerSingleton.setOutParkingFlag(false);
//        }else {
//            dataManagerSingleton.setOutParkingFlag(true);
//        }

        if(timerSingleton.isAFTERLOBBYELECHECK())
        {
            int value=dataManagerSingleton.getAfterLobbyEleCount();
            value++;
            dataManagerSingleton.setAfterLobbyEleCount(value);
        }


        if(timerSingleton.isAFTERGYROSTARTCALC())
        {
            int value=dataManagerSingleton.getAfterGyroCount();
            value++;
            dataManagerSingleton.setAfterGyroCount(value);

            Log.d("TAG_TEST_TIMER","GYRO COUNT VALUE : "+dataManagerSingleton.getAfterGyroCount());
        }

        if(timerSingleton.isAfterStart())
        {
            int count=dataManagerSingleton.getAfterStartCount();
            count++;
            dataManagerSingleton.setAfterStartCount(count);

//            Log.d("TEST_AFTER_START","COUNT : "+count);
        }

        if(timerSingleton.isCOLLECTSTARTBEACONCALC())
        {
            int value=dataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            dataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        if(timerSingleton.isNotStartBeaconStart())
        {
//            Log.d("TAG_NOTSTART","BEACON VALUE : "+minor);
            dataManagerSingleton.getNOSTARTBEACON().add(minor);
        }

        if(timerSingleton.isWholeTimerStart())
        {
            //dataManagerSingleton.setEnd1Beacon(true);
            //dataManagerSingleton.setINOUTSTATE("OUT");

            String ID="";

            if(minor>32768)
            {
                ID=String.valueOf(minor-32768);
            }
            else
            {
                ID=String.valueOf(minor);
            }

            if(timerSingleton.isCollectAccelBeaconStart())
            {
                String HexValue=String.format("%04X",Integer.valueOf(ID));
                saveArrayListValue.SaveAccelBeacon(HexValue,String.valueOf(rssi),String.valueOf(dataManagerSingleton.getWholeTimerDelay()));

                int value=dataManagerSingleton.getPARINGCOUNT();
                value++;
                dataManagerSingleton.setPARINGCOUNT(value);
                AddAccelDelay(HexValue,major);
            }

        }
    }

    public void ChangeBeacon(int minor,int major,double rssi,SaveArrayListValue saveArrayListValue)
    {

        if(timerSingleton.isAFTERLOBBYELECHECK())
        {
            int value=dataManagerSingleton.getAfterLobbyEleCount();
            value++;
            dataManagerSingleton.setAfterLobbyEleCount(value);
        }

        if(timerSingleton.isCOLLECTSTARTBEACONCALC())
        {
            int value=dataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            dataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        if(timerSingleton.isNotStartBeaconStart())
        {
//            Log.d("TAG_NOTSTART","BEACON VALUE : "+minor);
            dataManagerSingleton.getNOSTARTBEACON().add(minor);
        }

        if(timerSingleton.isWholeTimerStart())
        {
            String ID="";

            if(minor>32768)
            {
                ID=String.valueOf(minor-32768);
                String HexValue=String.format("%04X",Integer.valueOf(ID));

                Beacon beacon=new Beacon();
                beacon.setBeaconid(HexValue);
                beacon.setRssi(String.valueOf(rssi));
                beacon.setState(String.valueOf(major));
                beacon.setDelay(String.valueOf(dataManagerSingleton.getWholeTimerDelay()));
                beacon.setSeq(String.valueOf(dataManagerSingleton.getBeaconSequence()));

                dataManagerSingleton.getBeaconArrayList().add(beacon);

                SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS");
                Log.d("TAG_BEACON_CHANGE","BEACON VALUE 5 :"+HexValue+" / "+rssi);
                int beaconsequence=dataManagerSingleton.getBeaconSequence()+1;
                dataManagerSingleton.setBeaconSequence(beaconsequence);
            }
            else
            {
                ID=String.valueOf(minor);
            }

            if(timerSingleton.isCollectAccelBeaconStart())
            {
                String HexValue=String.format("%04X",Integer.valueOf(ID));
                saveArrayListValue.SaveAccelBeacon(HexValue,String.valueOf(rssi),String.valueOf(dataManagerSingleton.getWholeTimerDelay()));
                AddAccelDelay(HexValue,major);
            }
        }
    }

//    public void OnlyOpenLobby(int minor, int major,double rssi)
//    {
//        UserDataSingleton userDataSingleton =UserDataSingleton.getInstance();
//        ArrayList<LobbyOpenData> lobbyOpenData=userDataSingleton.getOpenMINOR();
//
//        final TimerSingleton timerSingleton=TimerSingleton.getInstance();
//
//        Log.d("TEST_LOBBY_OPEN","RSSI : "+rssi+" ,LOBBY TIMER : "+timerSingleton.isLobbyTimerStart());
//
//        for(int i=0; i<lobbyOpenData.size();i++)
//        {
//            String LobbyMinor=lobbyOpenData.get(i).getMinor();
//            double LobbyRssi=Double.valueOf(lobbyOpenData.get(i).getRssi());
//            String Minor=String.valueOf(minor);
//            String MinorHex=String.format("%04X",minor);
//
//            Log.d("TAG_LOBBYBEACON1","LOBBY 확인 : (LOBBY) : "+LobbyMinor+" , (MINOR HEX) : "+MinorHex);
//
//            if(LobbyMinor==MinorHex || LobbyMinor.equals(MinorHex))
//            {
//                Log.d("TAG_LOBBYBEACON2","RSSI 확인 : "+rssi);
//
//                if(rssi>=LobbyRssi)
//                {
//                    if(!timerSingleton.isLobbyTimerStart())
//                    {
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                            // only for gingerbread and newer versions
//                            timerSingleton.StartLobbyTimer();
//                        }
//                        else
//                        {
//                            Handler handler=new Handler(Looper.getMainLooper());
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    timerSingleton.StartLobbyTimer();
//                                }
//                            });
//                        }
//
//                        Log.d("TAG_LOBBYBEACON3","OPEN 요청 : "+timerSingleton.isLobbyTimerStart()+", rssi : "+rssi);
//                        String Rssi=String.valueOf((int)rssi);
//
//                        ServerData serverData=new ServerData(context);
//                        serverData.OpenLobby(Minor,Rssi);
//
//                        CharSequence text = "로비폰 열림 요청";
//                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
//                        toast.show();
//
//                    }
//                }
//
//
//            }
//        }
//        //endregion
//    }

    private void AddAccelDelay(String HexValue,int major)
    {
        ArrayList<Integer> ArrayValue=dataManagerSingleton.getAccelBeaconDelayMap().get(HexValue);
        if(ArrayValue==null)
        {
            ArrayValue=new ArrayList<>();
        }
        ArrayValue.add(dataManagerSingleton.getWholeTimerDelay());
        dataManagerSingleton.getAccelBeaconDelayMap().put(HexValue,ArrayValue);

        Log.d("TAG_BEACONFUNCTAION","HASH MAP ACCELBEACON DELAY VALUE "+major+" : "+ArrayValue);
    }

}
