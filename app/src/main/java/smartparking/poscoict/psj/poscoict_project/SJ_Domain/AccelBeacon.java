package smartparking.poscoict.psj.poscoict_project.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** 비컨 클래스  -   조건 상관없이 들어오는 Beacon 의 Data 를 수집하여 RSSI 가 가장 큰 데이터만 가지고 있는다
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * BeaconId     :   Beacon ID                       :   ID
 * Rssi         :   Beacon 의 Rssi (강도)           :   Rssi
 * Delay        :   Beacon 을 받아온 소요 시간      :   Delay
 * Count        :   Beacon 을 받은 갯수             :   Count
 *
 * **/
@Data
public class AccelBeacon {

    @Getter
    @Setter
    @SerializedName("ID")
    @Expose
    private String BeaconId;

    @Getter
    @Setter
    @SerializedName("Rssi")
    @Expose
    private String Rssi;

    @Getter
    @Setter
    @SerializedName("Delay")
    @Expose
    private String Delay;

    @Getter
    @Setter
    @SerializedName("Count")
    @Expose
    private String Count;

    @Getter
    @Setter
    @SerializedName("DelayList")
    @Expose
    private ArrayList<Integer> AccelDelay;

}
