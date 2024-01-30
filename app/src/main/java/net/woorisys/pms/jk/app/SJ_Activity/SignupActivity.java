package net.woorisys.pms.jk.app.SJ_Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Server.ServerData;
import net.woorisys.pms.jk.app.SJ_Singleton.SignupDataSingleton;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG="SIGNUP_ACTIVITY";

    SignupDataSingleton signupDataSingleton;

    EditText SIGNUP_ID_W;
    EditText SIGNUP_PW_W;
    EditText SIGNUP_DONG_W;
    EditText SIGNUP_HO_W;
    EditText SIGNUP_NAME_W;
    Button BTN_SIGNUP_MAIN_W;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupDataSingleton = SignupDataSingleton.getInstance();
        UISetting();

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("회원가입 주의사항");
        builder.setMessage("회원 정보 수정 및 가입 정보 찾기는 관리사무소로 문의바랍니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();

    }

    private void UISetting() {
        SIGNUP_ID_W = findViewById(R.id.join_id);
        SIGNUP_PW_W = findViewById(R.id.join_password);
        SIGNUP_DONG_W = findViewById(R.id.join_dong);
        SIGNUP_HO_W = findViewById(R.id.join_ho);
        SIGNUP_NAME_W = findViewById(R.id.join_name);

        BTN_SIGNUP_MAIN_W = findViewById(R.id.btn_signup_main);

        BTN_SIGNUP_MAIN_W.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_signup_main) {

        ServerData serverData = new ServerData(this);
        serverData.SignUp(SIGNUP_ID_W.getText().toString(),SIGNUP_PW_W.getText().toString(),
            SIGNUP_NAME_W.getText().toString(), SIGNUP_DONG_W.getText().toString(), SIGNUP_HO_W.getText().toString());

        }

    }



}
