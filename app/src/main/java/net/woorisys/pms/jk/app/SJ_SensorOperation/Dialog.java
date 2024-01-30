package net.woorisys.pms.jk.app.SJ_SensorOperation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import net.woorisys.pms.jk.app.PublicValue;
import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Singleton.SharedPreferencesSingleton;


/** 센서 정삭 작동 확인 UI 부분 클래스
 *
 * --------------       함수 종류       --------------
 * Start()          -   시작
 * Stay()           -   세로로 세우기
 * Right()          -   오른쪽으로 기울이기
 * Left()           -   왼쪽으로 기울이기
 * Finish()         -   완료
 *
 * Calc 에 있는 STATECalc 의 값을 변경해주며 진행한다.
 *
 * --------------       STATE 종류        --------------
 * START            -   시작 : 시작 버튼이 존재하는 Dialog 가 있을 경우
 * STAY             -   세로 : 세로로 세우기 Dialog 가 있을 경우 ->  Right 가 진행 되었는지 Check 를 통해 다음 단계가 Right / Left 판별
 * RIGHT            -   우측 : 우측으로 기울이기 Dialog 가 있을 경우
 * LEFT             -   좌측 : 좌측으로 기울이기 Dialog 가 있을 경우
 * END              -   완료 : 모든 측정을 통과 할경우 진행가능 -> 자동으로 종료되며 로그인을 시도하고 최초 로그인 값을 false 로 만들어 준다.
 *
 * --------------            설정              --------------
 * 소정 코드 :  맨뒤에 W 붙음
 *
 * **/
public class Dialog {

    String TAG_W="SensorOperation_Dialog_TAG";

    Context context_W;

    Calc calc_W;

    /** 각 Dialog 의 Animation 효과 취소 됐을 경우 재시도 할경우 2개가 실행되는 것을 방지 시키기 위해 null Check를 해준다. **/
    Animation animation_W;
    RotateAnimation rotateAnimationR_W;   //  오른 쪽으로 돌기 Animation
    RotateAnimation rotateAnimationL_W;   //  왼 쪽으로 돌기 Animation

    /** Sensor Test 를 위해 사용되어지는 Dialog 들 **/
    android.app.Dialog StartDialog_W;     //  시작 Dialog
    android.app.Dialog StayDialog_W;      //  세로로 세우기 Dialog
    android.app.Dialog RightDialog_W;     //  가로로(우측) 눕히기 Dialog
    android.app.Dialog LeftDialog_W;      //  가로로(좌측) 눕히기 Dialog
    android.app.Dialog FinishDialog_W;    //  끝 Dialog

    CountDownTimer stayCountDown_W;
    CountDownTimer rightCountDown_W;
    CountDownTimer leftCountDown_W;
    CountDownTimer endCountDown_W;

    AlertDialog.Builder alert;

    boolean IsDriver;

    public Dialog(Context context)
    {
        this.context_W=context;
        calc_W=Calc.getInstance();

        this.IsDriver=IsDriver;

        Log.d(TAG_W,"생성자 Dialog");
    }

    /** 시작 버튼이 있는 Dialog 를 띄우는 함수 **/
    public void Start()
    {
        calc_W.setSTATECalc_W(context_W.getResources().getString(R.string.START));

        //  Dialog Setting
        StartDialog_W=new android.app.Dialog(context_W);
        StartDialog_W.setContentView(R.layout.activity_sensortest_start);
        StartDialog_W.setCancelable(false);

        // Button Event
        Button btn_Start=StartDialog_W.findViewById(R.id.BTN_START);

        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Stay();
                StartDialog_W.dismiss();

            }
        });

        StartDialog_W.show();

        Log.d(TAG_W,"Dialog Start UI");
    }

    /** 세로로 세우기 Dialog UI **/
    private void Stay()
    {
        calc_W.setSTATECalc_W(context_W.getResources().getString(R.string.STAY));


        if(StayDialog_W==null)
        {
            StayDialog_W=new android.app.Dialog(context_W);
            StayDialog_W.setContentView(R.layout.activity_sensortest_stay);
            StayDialog_W.setCancelable(false);
        }


        if(animation_W==null)
        {
            animation_W = new AlphaAnimation(1, 0);
            animation_W.setDuration(1000);
            animation_W.setInterpolator(new LinearInterpolator());
            animation_W.setRepeatCount(Animation.INFINITE);
            animation_W.setRepeatMode(Animation.REVERSE);
        }

        animation_W.start();

        final ImageView stayimage=StayDialog_W.findViewById(R.id.IMG_START);
        stayimage.setImageResource(R.mipmap.phone_stay);
        stayimage.setAnimation(animation_W);

        StayDialog_W.show();


        if(stayCountDown_W==null)
        {
            stayCountDown_W=new CountDownTimer(5000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if(calc_W.isStayMatch_W())
                    {

                        ComplitDialog(stayimage,R.mipmap.phone_complit);
                        animation_W.cancel();

                        CountDownTimer nextCountDown=new CountDownTimer(2000,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {

                                if(!calc_W.isRightMatch_W())
                                    Right();
                                else
                                    Left();

                                StayDialog_W.dismiss();
                                StartDialog_W.cancel();
                            }
                        }.start();
                    }
                    else
                    {
                        FailAlertDialog(StayDialog_W);
                        stayCountDown_W.cancel();
                    }
                }
            };
        }

        stayCountDown_W.start();

        Log.d(TAG_W,"Dialog Stay UI");
    }

    /** 오른쪽으로 기울이기 Dialog UI **/
    private void Right()
    {
        calc_W.setSTATECalc_W(context_W.getResources().getString(R.string.RIGHT));

        if(RightDialog_W==null)
        {
            RightDialog_W=new android.app.Dialog(context_W);
            RightDialog_W.setContentView(R.layout.activity_sensortest_right);
            RightDialog_W.setCancelable(false);
        }


        if(rotateAnimationR_W==null)
        {
            rotateAnimationR_W=new RotateAnimation(0.0f,90.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimationR_W.setInterpolator(new LinearInterpolator());
            rotateAnimationR_W.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
            rotateAnimationR_W.setDuration(2000); //P
        }

        rotateAnimationR_W.start();

        final ImageView rightimage=RightDialog_W.findViewById(R.id.IMG_RIGHT);
        rightimage.setImageResource(R.mipmap.phone_stay);
        rightimage.setAnimation(rotateAnimationR_W);

        RightDialog_W.show();

        if(rightCountDown_W==null)
        {
            rightCountDown_W=new CountDownTimer(5000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if(calc_W.isRightMatch_W())
                    {
                        ComplitDialog(rightimage,R.mipmap.phone_complit_right);
                        rotateAnimationR_W.cancel();

                        CountDownTimer nextCountDown=new CountDownTimer(2000,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {

                                calc_W.setStayMatch_W(false);
                                Stay();

                                RightDialog_W.dismiss();
                                RightDialog_W.cancel();
                            }
                        }.start();
                    }
                    else
                    {
                        FailAlertDialog(RightDialog_W);
                    }
                }
            };
        }

        rightCountDown_W.start();

        Log.d(TAG_W,"Dialog Right UI");
    }

    /** 왼쪽으로 기울이기 Dialog UI **/
    private void Left()
    {
        calc_W.setSTATECalc_W(context_W.getResources().getString(R.string.LEFT));

        if(LeftDialog_W==null)
        {
            LeftDialog_W=new android.app.Dialog(context_W);
            LeftDialog_W.setContentView(R.layout.activity_sensortest_left);
            LeftDialog_W.setCancelable(false);
        }

        if(rotateAnimationL_W==null)
        {
            rotateAnimationL_W=new RotateAnimation(0.0f,-90.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimationL_W.setInterpolator(new LinearInterpolator());
            rotateAnimationL_W.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
            rotateAnimationL_W.setDuration(2000); //P
        }

        rotateAnimationL_W.start();

        final ImageView leftImage=LeftDialog_W.findViewById(R.id.IMG_LEFT);
        leftImage.setImageResource(R.mipmap.phone_stay);
        leftImage.setAnimation(rotateAnimationL_W);

        LeftDialog_W.show();

        if(leftCountDown_W==null)
        {
            leftCountDown_W=new CountDownTimer(5000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if(calc_W.isLeftMatch_W())
                    {
                        ComplitDialog(leftImage,R.mipmap.phone_complit_left);
                        rotateAnimationL_W.cancel();

                        CountDownTimer nextCountDown=new CountDownTimer(2000,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {

                                Finish();

                                LeftDialog_W.dismiss();
                                LeftDialog_W.cancel();
                            }
                        }.start();
                    }
                    else
                    {
                        FailAlertDialog(LeftDialog_W);
                        leftCountDown_W.cancel();
                    }
                }
            };
        }

        leftCountDown_W.start();

        Log.d(TAG_W,"Dialog Left UI");
    }

    /** 완료 Dialog UI **/
    private void Finish()
    {
        calc_W.setSTATECalc_W(context_W.getResources().getString(R.string.END));

        if(FinishDialog_W==null)
        {
            FinishDialog_W=new android.app.Dialog(context_W);
            FinishDialog_W.setContentView(R.layout.activity_sensortest_end);
            FinishDialog_W.setCancelable(false);
        }

        FinishDialog_W.show();


        if(endCountDown_W==null)
        {
            endCountDown_W=new CountDownTimer(5000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {

                    calc_W.setStayMatch_W(false);
                    calc_W.setRightMatch_W(false);
                    calc_W.setLeftMatch_W(false);

                    SaveInitLogin();

                    FinishDialog_W.dismiss();
                    FinishDialog_W.cancel();

                    Intent FinishTest=new Intent(PublicValue.ACTION_LOGIN_SENSOR_TEST_FINISH);
                    context_W.sendBroadcast(FinishTest);

                }
            };
        }
        endCountDown_W.start();

        Log.d(TAG_W,"Dialog Finish UI");
    }

    /** 테스트 성공할 경우 나오는 Dialog **/
    private void ComplitDialog(ImageView changeImage,int Resources)
    {
        changeImage.setImageResource(Resources);

        Log.d(TAG_W,"ComplitDialog");
    }

    /** 테스트를 실패할 경우 나오는 Dialog **/
    private void FailAlertDialog(final android.app.Dialog dialogdata)
    {
        alert=new AlertDialog.Builder(context_W);
        alert.setMessage(" 측정에 실해하였습니다. ");
        alert.setTitle(" 측정 실패 ");
        alert.setPositiveButton("재시도", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
                dialog.dismiss();

                dialogdata.cancel();
                dialogdata.dismiss();

                switch (calc_W.getSTATECalc_W())
                {
                    case "STAY":
                        stayCountDown_W=null;
                        Stay();
                        break;
                    case "RIGHT":
                        rightCountDown_W=null;
                        Right();
                        break;
                    case "LEFT":
                        leftCountDown_W=null;
                        Left();
                        break;

                }
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialogdata.cancel();
                dialogdata.dismiss();

                dialog.cancel();
                dialog.dismiss();

                stayCountDown_W=null;

                calc_W.setStayMatch_W(false);
                calc_W.setRightMatch_W(false);
                calc_W.setLeftMatch_W(false);

            }
        });
        alert.setCancelable(false);
        alert.create().show();

        Log.d(TAG_W,"FailAlertDialog");
    }


    /** SharedPreference 파일 데이터 업데이트 **/
    private void SaveInitLogin()
    {
        SharedPreferencesSingleton sharedPreference=SharedPreferencesSingleton.getInstance(context_W);
        sharedPreference.SaveInitLogin();

        Log.d(TAG_W,"SaveInitLogin");
    }

}
