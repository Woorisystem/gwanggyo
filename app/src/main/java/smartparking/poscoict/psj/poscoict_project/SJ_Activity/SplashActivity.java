package smartparking.poscoict.psj.poscoict_project.SJ_Activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import smartparking.poscoict.psj.poscoict_project.R;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG_W,"onCreate 실행");

        CountDown();
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
