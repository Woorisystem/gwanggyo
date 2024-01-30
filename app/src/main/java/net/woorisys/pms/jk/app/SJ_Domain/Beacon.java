package net.woorisys.pms.jk.app.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** 비컨 클래스  -   MAJOR 번호가 5번인 Beacon Data 를 수집
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * beaconid     :   Beacon ID                       :   ID
 * State        :   Beacon MAJOR 값                 :   State
 * Rssi         :   Beacon 의 Rssi                  :   Rssi
 * Delay        :   Beacon 을 받은 시간             :   Delay
 * Seq          :   Beacon 을 받은 갯수             :   Seq
 *
 * **/
@Data
public class Beacon {

    @Getter @Setter
    @SerializedName("ID")
    @Expose
    private String beaconid;

    @Getter @Setter
    @SerializedName("State")
    @Expose
    private String state;

    @Getter @Setter
    @SerializedName("Rssi")
    @Expose
    private String rssi;

    @Getter @Setter
    @SerializedName("Delay")
    @Expose
    private String delay;

    @Getter @Setter
    @SerializedName("Seq")
    @Expose
    private String seq;
}
