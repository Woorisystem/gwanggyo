package net.woorisys.pms.jk.app.SJ_Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import net.woorisys.pms.jk.R;


/**
 * 가장 먼저 나오는 화면
 * 자동으로 일정 시간 이후 종료되고 로그인 창으로 이동
 * 해당 어플 이름을 알 수 있게 해준다.
 * Smart Parking
 * 기본 Default = 3 초 CountDownTime 을 통해 시간을 조절할 수 있다.
 *
 * --------------            설정              --------------
 * 소정 코드 :  맨뒤에 W 붙음
 *
 **/
public class SplashActivity extends AppCompatActivity {

    private static final String TAG_W="TAG_SplashActivity";

    private static final int CountDownTime_W=3;       //  몇초간 Timer 를 돌릴지 지정하는 값
    private static final int DownTime_W=1000;         //  1마다 한번씩 감소하도록 지정하는 값

    private CountDownTimer SplashTimer_W;             //  Count Down 을 해주는 Timer
    private Intent NextIntent_W;                      //  다음 화면으로 넘어가기 위한 Intent

    String location_message = "이 앱은 [주차 위치 확인]을 활성화하기 위해 [위치데이터]를 수집 합니다.\n이 앱은 [앱이 닫혀 있을 때]에도 [항상 사용됨]을 알려드립니다." +
            "\n [백그라운드]기능 사용은 앱이 자동으로 다시 켜졌을 때 비컨을 정상적으로 스케닝하여 [주차 위치 확인]을 사용하기 위함입니다. 수집된 위치 정보는 스마트주치에 사용됩니다.\n";

    String storage_message = "이 앱은 [고객용 매뉴얼]을 제공하기 위해 [저장소 권한]을 요구합니다.\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AlertDialog.Builder loc_permission_builder=new AlertDialog.Builder(this);
        loc_permission_builder.setCancelable(false);
        loc_permission_builder.setTitle("위치권한 사용 정보 알림");
        loc_permission_builder.setMessage(location_message);
        loc_permission_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NextIntent_W=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(NextIntent_W);
                finish();
            }
        });
        loc_permission_builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        loc_permission_builder.create().show();

        Log.d(TAG_W,"onCreate 실행");

       // CountDown();
    }

    /** 지정된 시간 이후에 Intent 가 넘어가도록 처리해주는 CountDown Timer 함수**/
    private void CountDown()
    {
        SplashTimer_W=new CountDownTimer(CountDownTime_W*DownTime_W,DownTime_W) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {


                NextIntent_W=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(NextIntent_W);

                finish();
                Log.d(TAG_W,"CountDown Finish");
            }
        }.start();
    }
}
