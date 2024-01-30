package net.woorisys.pms.jk.app.SJ_Server;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import net.woorisys.pms.jk.app.PublicValue;
import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Activity.LocationActivity;
import net.woorisys.pms.jk.app.SJ_Activity.RegistCarDialog;
import net.woorisys.pms.jk.app.SJ_Activity.SignupActivity;
import net.woorisys.pms.jk.app.SJ_BroadCast.NetworkCheck;
import net.woorisys.pms.jk.app.SJ_Domain.Total;
import net.woorisys.pms.jk.app.SJ_ETC.Encryption;
import net.woorisys.pms.jk.app.SJ_Interface.NetworkService;
import net.woorisys.pms.jk.app.SJ_SensorOperation.Dialog;
import net.woorisys.pms.jk.app.SJ_Singleton.DataManagerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.LocationDataSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.TimerSingleton;
import net.woorisys.pms.jk.app.SJ_Singleton.UserDataSingleton;
import net.woorisys.pms.jk.app.SJ_Util.CallClassValue;

import java.util.regex.Pattern;


/** 서버 데이터 관련 클래스
 *
 * --------------       서버 처리 함수        --------------
 * Login()                  : Context,ID,PASSWORD,Init_Login    -   로그인 처리
 *
 * --------------       서버 Return Code      --------------
 * response                 : Server 로부터 받은 Data return 값
 * RETURN_CODE_SUCCESS      : 0     - return success
 * RETURN_CODE_FAIL         : -1    - return fail
 * onFailure                : 서버에 접속을 못했을 경우
 *
 * **/
public class ServerData {

    Context context;

    String TAG="";

    String IP=null;
    String PORT=null;

    ApplicationController applicationController=null;
    NetworkService networkService=null;

    CallClassValue callClassValue;

    public ServerData(Context context)
    {
        this.context=context;

        IP=context.getResources().getString(R.string.IP);
        PORT=context.getResources().getString(R.string.PORT);


        applicationController=ApplicationController.getInstance();
        networkService=applicationController.startApplication(IP,PORT);

        callClassValue=new CallClassValue(context);
        TAG="TAG_SERVER_DATA";

    }
//http://183.99.76.19:8884
    //region 로그인
    /** 로그인 요청
     *
     * 필요한 Data
     * - ID             :   사용자 계정을 구분하기 위한 ID
     * - PASSWORD       :   사용자 계정을 구분하기 위한 PASSWORD
     * - TEXTVIEW       :   사용자에게 로그인 요청시 결과를 알려주기 위한 TextView
     * - DRIVER CHECK   :   운전자 / 비운전자 구분하여 필요한 서비스만 구동시켜 베터리 사용량 감소
     * - REMEMBER       :   계정 기억하기로 가장 마지막에 사용한 유저 정보 기억하여 로그인 요청을 좀더 쉽게 해줌
     *
     * **/
    public void Login(final String ID, final String PASSWORD, final TextView ResultText, final Boolean Remember)
    {
        Encryption encryption=new Encryption();
        byte[] utf16=encryption.UTF16LE(PASSWORD);                  //  String -> UTF 16 Litle Endian
        final byte[] sha256=encryption.SHA256PASSWORD(utf16);       //  UTF 16 -> SHA256
        String base64=encryption.BASE64PASSWORD(sha256);            //  SHA256 -> BASE64
        String sub=base64.replace("\n","");    //  BASE64 에 임의로 \n이 생성 되어 제거

        // 로그인 요청을 서버로 RESTAPI 를 사용하여 요청 - 구조 FormData 형식으로 전달
        Log.d(TAG,"ID : "+ID+", PW :"+PASSWORD);

        Call<UserDataResult> getCall=networkService.LoginForm(ID,sub,"lakeedutown");
        getCall.enqueue(new Callback<UserDataResult>() {
            @Override
            public void onResponse(final Call<UserDataResult> call, Response<UserDataResult> response) {

                // body NULL 처리
                // 안해줄 경우 어플 자체가 터지는 문제가 생기기 때문에 확인 필수
                // NULL 일 경우 return 시켜준다.
                if(response.body()==null)
                {
                    Intent failed_login=new Intent(PublicValue.ACTION_LOGIN_FAILED);
                    failed_login.putExtra(PublicValue.LOGIN_FAILED_MESSAGE,"서버에서 응답을 보냈으나 값이 없습니다. 관리자에게 문의하여 주세요.");
                    context.sendBroadcast(failed_login);
                    return;
                }

                int ack=response.body().getReturnCode();                // Return Code 를 받는 변수
                String message=response.body().getMessage();            // Return Message 를 받는 변수

                switch (ack)
                {
                    // RETURN CODE : 0
                    case AppResult.RETURN_CODE_SUCCESS:

                        // 시작 비컨을 못받았을 때 시작을 하기 위한 비컨 수집타이머 실행
                        if(!TimerSingleton.getInstance().isNotStartBeaconStart())
                        {
                            TimerSingleton.getInstance().NotStartBeacon();
                        }

                        String dong=response.body().getResult().getDong();              //  사용자의 거주 동
                        String ho=response.body().getResult().getHo();                  //  사용자의 거주 호
                        String UserName=response.body().getResult().getName();          //  사용자의 이름

                        String phoneInfo = "android";

                        //String Cel=response.body().getResult().getCel();                //  사용자의 전화번호
                        //Object minorList=response.body().getResult().getMinorList();    //  로비 열기 기능을 위한 MINOR , RSSI   -   JSON 형태로 넘어오기 때문에 따로 Parsing 이 필요하다

                        // Json Parsing
//                        Gson gson=new Gson();
//                        String JsonValue=gson.toJson(minorList);
//                        JSONArray jsonArray= null;
//                        ArrayList<LobbyOpenData> MINORNUMBER=new ArrayList<>();
//                        try {
//                            jsonArray = new JSONArray(JsonValue);
//
//                            for(int i=0 ;i<jsonArray.length();i++)
//                            {
//                                JSONObject jsonObject=jsonArray.getJSONObject(i);
//                                String MINOR=jsonObject.getString("minor");
//                                String RSSI=jsonObject.getString("rssi");
//
//                                LobbyOpenData lobbyOpenData=new LobbyOpenData();
//                                lobbyOpenData.setMinor(MINOR);
//                                lobbyOpenData.setRssi(RSSI);
//
//                                MINORNUMBER.add(lobbyOpenData);
//                            }
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }

                        // 기능을 수행하기 위해 필요한 UserData 를 Singleton 형식으로 저장해 둔다.
                        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
                        userDataSingleton.setDong(dong);
                        userDataSingleton.setHo(ho);
                        userDataSingleton.setUserName(UserName);
                        userDataSingleton.setCel(phoneInfo);
                        userDataSingleton.setID(ID);
//                        userDataSingleton.setIsDriver(IsDriver);
//                        userDataSingleton.setOpenMINOR(MINORNUMBER);

                        Log.d(TAG,dong+" / "+ho+" / "+UserName+" / "+ID+" / ");

                        SharedPreferencesSingleton sharedPreferencesSingleton=SharedPreferencesSingleton.getInstance(context);
                        sharedPreferencesSingleton.SharedPreferenceWrite(Remember,ID,PASSWORD);

                        Intent success_login=new Intent(PublicValue.ACTION_LOGIN_SUCCESS);
                        success_login.putExtra(PublicValue.LOGIN_SUCCESS_MESSAGE,message);
                        context.sendBroadcast(success_login);
                        break;


                    case AppResult.RETURN_CODE_FAIL:
                        Intent failed_login=new Intent(PublicValue.ACTION_LOGIN_FAILED);
                        failed_login.putExtra(PublicValue.LOGIN_FAILED_MESSAGE,message);
                        context.sendBroadcast(failed_login);

                        if (message.equals("LEVEL ERROR")) {
                            Toast.makeText(context, "관리자의 승인이 필요합니다. 관리자에게 문의해 주세요.", Toast.LENGTH_LONG).show();
                        }

                        else if (message.equals("Password Wrong")){
                            Toast.makeText(context, "아이디 혹은 패스워드가 틀렸습니다. 다시 입력해 주세요.", Toast.LENGTH_LONG).show();
                        }

                        else {
                            Toast.makeText(context, "알 수 없는 오류로 인해 로그인에 실패하였습니다. 관리자에게 문의해 주세요.", Toast.LENGTH_LONG).show();
                        }

                        break;
                }
            }

            @Override
            public void onFailure(Call<UserDataResult> call, Throwable t) {

                Intent failed_login=new Intent(PublicValue.ACTION_LOGIN_FAILED);
                failed_login.putExtra(PublicValue.LOGIN_FAILED_MESSAGE,t.getMessage());
                context.sendBroadcast(failed_login);

            }
        });
    }
    // endregion 로그인 끝

    /** 주차 완료 **/
    public void Send(final Context context, final Total total)
    {
        Log.d("TOTAL TEST","TOTAL : "+total);

        networkService=applicationController.startApplication(IP, PORT);

            // 주차 시작 이후 진행 시간이 1분 이하일 경우 데이터를 전송하지 않는다.
            if(DataManagerSingleton.getInstance().getSAVEDELAY()>=5)
            {
                //Toast.makeText(context,"SEND SERVER",Toast.LENGTH_SHORT).show();

                UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
                String ID=userDataSingleton.getID();
                String Dong=userDataSingleton.getDong();
                String Ho=userDataSingleton.getHo();

            Call<AppResult> getCall=networkService.Result(ID,Dong,Ho,total);
            getCall.enqueue(new Callback<AppResult>() {
                @Override
                public void onResponse(Call<AppResult> call, Response<AppResult> response) {

                    if(response.body()==null)
                    {
                        Handler handler=new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                DataManagerSingleton.getInstance().Reset();
                            }
                        });
                        return;
                    }

                    DataManagerSingleton.getInstance().Reset();

                }

                @Override
                public void onFailure(Call<AppResult> call, Throwable t) {

                    DataManagerSingleton.getInstance().setCANNOTSENDTOTALSAVE(total);

                    if(!TimerSingleton.getInstance().isTimeoutTimerStart())
                    {
                        if(DataManagerSingleton.getInstance().getTimeoutCount()<4)
                        {

                            TimerSingleton.getInstance().SENDTIMEOUT(context);
                        }
                        else
                        {

                            DataManagerSingleton.getInstance().Reset();
                        }
                    }
                }

            });
        }
        else
        {
            DataManagerSingleton.getInstance().Reset();
        }

    }

    /** 주차 위치 확인 요청 **/
    public void LOCATION_DATA()
    {
        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
        String ID=userDataSingleton.getID();
        String Dong=userDataSingleton.getDong();
        String Ho=userDataSingleton.getHo();

        //String ID="dongtan-edu-psj";
        //String Dong ="2504";
        //String Ho ="903";

        Log.d("TAG_VALUE_LOCATION","위치확인 시도 : "+ID);

        Call<LocationResult> getCall=networkService.Location(ID,Dong,Ho);
        getCall.enqueue(new Callback<LocationResult>() {

            @Override
            public void onResponse(Call<LocationResult> call, Response<LocationResult> response) {

                // Body 에 Data 가 없을 경우 Return 처리 해주어야함
                // 안해주면 어플이 터짐
                if(response.body()==null)
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    return;

                }

                // 결과 Code
                int ack=response.body().getReturnCode();

                // 값전달 성공
                if(ack==LocationResult.RETURN_CODE_SUCCESS)
                {
                    String InterestCar=response.body().getInterestCar();

                    // 관심 차량이 없을 경우
                    if((InterestCar=="none" || InterestCar.equals("none")) ||(InterestCar==null || InterestCar.equals(null)))
                    {
                        LocationDataSingleton locationDataSingleton=LocationDataSingleton.getInstance();
                        locationDataSingleton.setParkingResults(response.body().getResult());

                        Intent intent=new Intent(context, RegistCarDialog.class);
                        intent.putExtra("LOCATIONCHANGE","NULL");
                        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
                        context.startActivity(intent);
                    }
                    // 관심 차량이 있을 경우
                    else
                    {

                        LocationDataSingleton locationDataSingleton=LocationDataSingleton.getInstance();
                        locationDataSingleton.setInterestCar(InterestCar);
                        locationDataSingleton.setParkingResults(response.body().getResult());

                        Intent intent=new Intent(context, LocationActivity.class);
                        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);


                    }

                }
                // 값 전달 실패
                else
                {
                    Intent intent=new Intent(context, LocationActivity.class);
                    intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    Intent failed_Location = new Intent(PublicValue.ACTION_LOCATION_FAILED);
                    failed_Location.putExtra(PublicValue.ACTION_LOCATION_FAILED,"등록된 정기권 차량이 없습니다.");
                    context.sendBroadcast(failed_Location);

                }

            }

            @Override
            public void onFailure(Call<LocationResult> call, Throwable t) {
            }
        });
    }

    /** 관심 차량 등록 요청 **/
    public void RegistInterestCar(final String CarData)
    {
        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
        String ID=userDataSingleton.getID();
        String Dong=userDataSingleton.getDong();
        String Ho=userDataSingleton.getHo();

        Call<AppResult> getCall=networkService.InterestCar(ID,Dong,Ho,CarData);
        getCall.enqueue(new Callback<AppResult>() {
            @Override
            public void onResponse(Call<AppResult> call, Response<AppResult> response) {

                if(response.body()==null)
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    return;
                }

                int ack=response.body().getReturnCode();
                String message=response.body().getMessage();


                switch (ack)
                {
                    case AppResult.RETURN_CODE_SUCCESS:

                        ServerData serverData=new ServerData(context);
                        serverData.LOCATION_DATA();
                        break;

                    case AppResult.RETURN_CODE_FAIL:
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppResult> call, Throwable t) {
            }
        });

    }

    public void OutParking()
    {
        Log.d("TAG_TEST_SERVER","OUTPARKING");
        applicationController=ApplicationController.getInstance();
        networkService=applicationController.startApplication("211.52.72.27","4000");

        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
        String ID=userDataSingleton.getID();
        String Dong=userDataSingleton.getDong();
        String Ho=userDataSingleton.getHo();

        Call<AppResult> getCall=networkService.OutParking(ID,Dong,Ho);
        getCall.enqueue(new Callback<AppResult>() {
            @Override
            public void onResponse(Call<AppResult> call, Response<AppResult> response) {

                if(response.body()==null)
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    DataManagerSingleton.getInstance().Reset();
                    return;
                }

                int ack=response.body().getReturnCode();
                String message=response.body().getMessage();

                switch (ack)
                {
                    case AppResult.RETURN_CODE_SUCCESS:
                        DataManagerSingleton.getInstance().Reset();
                        break;

                    case AppResult.RETURN_CODE_FAIL:
                        DataManagerSingleton.getInstance().Reset();
                        break;
                }
            }

            @Override
            public void onFailure(Call<AppResult> call, Throwable t) {
                DataManagerSingleton.getInstance().Reset();
            }
        });
    }

    public void GateInfo(String minor, final String major)
    {
        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();

        Call<GateInfoResult> getCall=networkService.GateInfo(userDataSingleton.getID(),major,minor);
        getCall.enqueue(new Callback<GateInfoResult>() {
            @Override
            public void onResponse(Call<GateInfoResult> call, Response<GateInfoResult> response) {

                if(response.body()==null)
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    return;
                }

                int ack=response.body().getReturnCode();

                switch (ack)
                {
                    case GateInfoResult.RETURN_CODE_SUCCESS:
                        break;
                    case GateInfoResult.RETURN_CODE_FAILED:
                        break;
                }
            }

            @Override
            public void onFailure(Call<GateInfoResult> call, Throwable t) {
            }
        });


    }

    public void PhoneInfo(String Version,String Info)
    {
        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();

        Call<PhoneInfoResult> getCall=networkService.PhoneInfo(userDataSingleton.getID(),Version,Info);
        getCall.enqueue(new Callback<PhoneInfoResult>() {
            @Override
            public void onResponse(Call<PhoneInfoResult> call, Response<PhoneInfoResult> response) {
                if(response.body()==null)
                {

                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    DataManagerSingleton.getInstance().Reset();
                    return;
                }

                int ack=response.body().getReturnCode();

                switch (ack)
                {
                    case GateInfoResult.RETURN_CODE_SUCCESS:
                        break;
                    case GateInfoResult.RETURN_CODE_FAILED:
                        break;
                }
            }

            @Override
            public void onFailure(Call<PhoneInfoResult> call, Throwable t) {
            }
        });
    }

    public void GyroSend(String Count)
    {
        UserDataSingleton userDataSingleton=UserDataSingleton.getInstance();
        Call<AppResult> getCall=networkService.GyroInfo(userDataSingleton.getID(),Count);
        getCall.enqueue(new Callback<AppResult>() {
            @Override
            public void onResponse(Call<AppResult> call, Response<AppResult> response) {
                Log.d(TAG,"GYRO 전송 성공");
            }

            @Override
            public void onFailure(Call<AppResult> call, Throwable t) {
//                Toast.makeText(context,"GYRO 전송 실패",Toast.LENGTH_SHORT).show();
            }
        });


    }

    public  void SignUp(final String userId, final String password,
                        final String userName, final String Dong, final String Ho) {

        boolean nameCheck = Pattern.matches("^[가-힣]*$", userName);
        boolean pwCheck = Pattern.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#$%^&*])(?=.*[0-9!@#$%^&*]).{8,15}$", password);
        boolean idCheck = Pattern.matches("^[a-zA-Z0-9]*$", userId);
        boolean dongCheck = Pattern.matches("^[0-9]*$", Dong);
        boolean hoCheck = Pattern.matches("^[0-9]*$", Ho);

        if (userName.equals("") || !nameCheck) {
            Toast.makeText(context, "사용자 이름을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (Ho.equals("") || !hoCheck) {
            Toast.makeText(context, "입주호수를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (Dong.equals("") || !dongCheck) {
            Toast.makeText(context, "입주동을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (userId.equals("") || !idCheck) {
            Toast.makeText(context, "아이디를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (password.equals("") || !pwCheck) {
            Toast.makeText(context, "패스워드를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nameCheck && pwCheck && idCheck && dongCheck && hoCheck) {
            Encryption encryption=new Encryption();
            byte[] utf16=encryption.UTF16LE(password);                  //  String -> UTF 16 Litle Endian
            final byte[] sha256=encryption.SHA256PASSWORD(utf16);       //  UTF 16 -> SHA256
            String base64=encryption.BASE64PASSWORD(sha256);            //  SHA256 -> BASE64
            String sub=base64.replace("\n","");    //  BASE64 에 임의로 \n이 생성 되어 제거

            // 회원가입 요청을 서버로 RESTAPI 를 사용하여 요청 - 구조 FormData 형식으로 전달
            Log.d(TAG,"SIGNUP ID : "+userId+", SIGNUP PW :"+password);

            Call<AppResult> getCall=networkService.SignUp(userId, sub, userName, Dong, Ho);
            getCall.enqueue(new Callback<AppResult>() {
                @Override
                public void onResponse(final Call<AppResult> call, Response<AppResult> response) {

                    if(response.body()==null)
                    {
                        Intent failed_signUp = new Intent(PublicValue.ACTION_SIGNUP_FAILED);
                        failed_signUp.putExtra(PublicValue.ACTION_SIGNUP_FAILED,"서버에서 응답을 보냈으나 값이 없습니다. 관리자에게 문의하여 주세요.");
                        context.sendBroadcast(failed_signUp);
                        return;
                    }

                    Log.d(TAG,"Signup DATA 전송 성공");

                    int ack = response.body().getReturnCode();                // Return Code 를 받는 변수
                    String message = response.body().getMessage();            // Return Message 를 받는 변수

                    Log.d(TAG,"return code : " + ack);
                    Log.d(TAG,"message : " + message);

                    // 유저 정보 중복일 경우 -1
                    // 회원가입 성공 시 0


                    switch (ack)
                    {
                        // RETURN CODE : 0
                        case AppResult.RETURN_CODE_SUCCESS:

                            new AlertDialog.Builder(context)
                                    .setTitle("회원가입 성공")
                                    .setMessage("회원가입에 성공하였습니다. 관리자의 승인 후 서비스를 이용하실 수 있습니다.")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            return;
                                        }
                                    })
                                    .create()
                                    .show();

                            break;


                        case AppResult.RETURN_CODE_FAIL:

                            if (message.equals("User Exist")) {
                                new AlertDialog.Builder(context)
                                        .setTitle("회원가입 실패")
                                        .setMessage("해당 아이디는 이미 존재합니다. 다른 아이디로 생성해 주세요.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                return;
                                            }
                                        })
                                        .create()
                                        .show();
                            }



                            break;
                    }

                }

                @Override
                public void onFailure(Call<AppResult> call, Throwable t) {

                    Intent failed_signUp = new Intent(PublicValue.ACTION_SIGNUP_FAILED);
                    failed_signUp.putExtra(PublicValue.ACTION_SIGNUP_FAILED,t.getMessage());
                    context.sendBroadcast(failed_signUp);

                    Log.d(TAG,"server 연결 실패");
                    Toast.makeText(context, "서버 연결에 실패했습니다. 관리자에게 문의해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }

    }

    public void DownloadPDF()
    {

        Call<AppResult> getCall=networkService.UserManual();
        getCall.enqueue(new Callback<AppResult>() {
            @Override
            public void onResponse(final Call<AppResult> call, Response<AppResult> response) {

                if(response.body()==null)
                {
                    Intent failed_Download = new Intent(PublicValue.ACTION_DOWNLOAD_FAILED);
                    failed_Download.putExtra(PublicValue.ACTION_DOWNLOAD_FAILED,"서버에서 응답을 보냈으나 값이 없습니다. 관리자에게 문의하여 주세요.");
                    context.sendBroadcast(failed_Download);
                    return;
                }

                //Log.d(TAG,"Signup DATA 전송 성공");

                int ack = response.body().getReturnCode();                // Return Code 를 받는 변수
                String message = response.body().getMessage();            // Return Message 를 받는 변수

                Log.d(TAG,"return code : " + ack);
                Log.d(TAG,"message : " + message);


                switch (ack)
                {
                    // RETURN CODE : 0
                    case AppResult.RETURN_CODE_SUCCESS:
                        // http://192.168.0.19:9082/pms-server-web/app/userManual
                        //manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        //Uri uri = Uri.parse("http://192.168.0.19:9082/pms-server-web/app/userManual/guide.pdf");
                        //DownloadManager.Request request = new DownloadManager.Request(uri);
                        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        //long reference = manager.enqueue(request);


                        break;


                    case AppResult.RETURN_CODE_FAIL:
//                        Intent failed_login=new Intent(PublicValue.ACTION_LOGIN_FAILED);
//                        failed_login.putExtra(PublicValue.LOGIN_FAILED_MESSAGE,message);
//                        context.sendBroadcast(failed_login);
                        break;
                }


            }

            @Override
            public void onFailure(Call<AppResult> call, Throwable t) {

                Intent failed_Download = new Intent(PublicValue.ACTION_DOWNLOAD_FAILED);
                failed_Download.putExtra(PublicValue.ACTION_DOWNLOAD_FAILED,t.getMessage());
                context.sendBroadcast(failed_Download);


            }
        });

    }

}
