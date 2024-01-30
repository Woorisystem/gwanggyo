package net.woorisys.pms.jk.app.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** 통합 클래스  -   전체 타이머 종료시 데이터를 모아 서버로 보내기 위한 클래스
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * phoneInfo        :   핸드폰 고유의 PhoneID                                    :   PhoneInfo
 * sensorList       :   Accel Sensor 값 ArrayList 로 모은것                      :   Sensors
 * beaconList       :   Beacon 값 ArrayList 로 모은것                            :   Beacons
 * gyroList         :   Gyro Sensor 값 ArrayList 로 모은것                       :   Gyros
 * AccelBeacons     :   가장 높은 Rssi 값을 가진 Beacon 값 ArrayList 로 모은것   :   AccelBeacons
 *
 * **/
@Data
public class Total {

    @Getter @Setter
    @SerializedName("PhoneInfo")
    @Expose
    private String phoneInfo;

    @Getter @Setter
    @SerializedName("InputDate")
    @Expose
    private String InputDate;

    /**
     * 게이트웨이 보드 설정 정보 리스트
     */
    @Getter @Setter
    @SerializedName("Sensors")
    @Expose
    private List<AccelSensor> sensorList;

    /**
     * 게이트웨이 보드 설정 정보 리스트
     */
    @Getter @Setter
    @SerializedName("Beacons")
    @Expose
    private List<Beacon> beaconList;

    @Getter @Setter
    @SerializedName("Gyros")
    @Expose
    private List<GyroSensor> gyroList;

    @Getter @Setter
    @SerializedName("AccelBeacons")
    @Expose
    private List<AccelBeacon> accelBeaconList;

    @Getter @Setter
    @SerializedName("ParingState")
    @Expose
    private String ParingState;
}
