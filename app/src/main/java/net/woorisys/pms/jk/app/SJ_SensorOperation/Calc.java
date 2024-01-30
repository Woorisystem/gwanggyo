package net.woorisys.pms.jk.app.SJ_SensorOperation;

import android.util.Log;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** Sensor 정상 작동 확인을 위한 클래스
 * Singleton 으로 구현
 * 각 단계별로 정상 작동하였는지 판별+
 *
 *  --------------            설정              --------------
 * 소정 코드 :  맨뒤에 W 붙음
 *
 * **/
@Data
public class Calc {

    static String TAG_W="SensorOperation_Calc_TAG";                //      Android Calc 부분 TAG 명칭

    //region Singleton 선언 부분

    private static Calc calc_W;
    public synchronized static Calc getInstance()
    {
        if (calc_W==null)
        {
            Log.d(TAG_W,"Calc Singleton 생성 - getInstance");
            calc_W=new Calc();
        }
        return calc_W;
    }

    //endregion

    //region 변수

    @Getter @Setter
    private String STATECalc_W="START";                     //      계산 진행을 위한 State 값 -> 다음 어떤 진행을 할 것 인지 확인하기 위해서 필요한 변수
    private boolean StayMatch_W=false;                      //      세우기의 조건을 만족 하였는지 Check , 2번 확인 해야됨 오른쪽 -> 세우기 / 왼쪽 -> 세우기
    private boolean RightMatch_W=false;                     //      오른쪽으로 기울이기의 조건을 만족 하였는지 Check
    private boolean LeftMatch_W=false;                      //      왼쪽으로 기울이기의 조건을 만족 하였는지 Check

    //endregion

    //region 함수 - 해당 상태 값이 조건을 만족 했는지 확인 하는 부분

    // 세우기 기능 함수 ROLL 값을 받아온다
    public void STAY(double ROLL)
    {
        // 해당 조건을 만족 하였는지 Check
        if(ROLL >=78 && ROLL <=82)
        {
            // 만족할 경우 StayMatch_W 를 True 로 변경
            StayMatch_W=true;

            Log.d(TAG_W,"STAY 값 만족 : "+StayMatch_W);
        }
    }

    // 오른쪽으로 기울이기 기능 함수 ROLL , PITCH 값을 받아온다.
    public void RIGHT(double ROLL,double PITCH)
    {
        // 해당 조건을 만족 하였는지 Check
        if(ROLL<-20 && PITCH>0)
        {
            // 만족할 경우 RightMatch_W 를 True 로 변경
            RightMatch_W=true;

            Log.d(TAG_W,"STAY 값 만족 : "+RightMatch_W);
        }
    }

    // 왼쪽으로 기울이기 기능 함수 ROLL , PITCH 값을 받아온다
    public void LEFT(double ROLL,double PITCH)
    {
        // 해당 조건을 만족 하였는지 Check
        if(PITCH<0 && ROLL<=-20)
        {
            // 만족할 경우 LeftMatch_W 를 True 로 변경
            LeftMatch_W=true;

            Log.d(TAG_W,"Left 값 만족 : "+LeftMatch_W);
        }
    }

    // endregion

}
