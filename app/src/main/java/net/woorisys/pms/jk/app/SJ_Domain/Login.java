package net.woorisys.pms.jk.app.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** 로그인 클래스  -   로그인을 하기 위한 Class
 *
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * ID         :   로그인을 위한 사용자에게 입력받은 ID 값           :   ID
 * PAWWSORD   :   로그인을 위한 사용자에게 입력받은 PASSWORD 값     :   PAWWSORD
 *
 * **/
@Data
public class Login {

    @Getter
    @Setter
    @SerializedName("ID")
    @Expose
    private String ID;

    @Getter
    @Setter
    @SerializedName("PAWWSORD")
    @Expose
    private String PASSWORD;

}
