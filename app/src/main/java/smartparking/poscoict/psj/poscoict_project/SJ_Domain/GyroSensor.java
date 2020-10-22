package smartparking.poscoict.psj.poscoict_project.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/** 자이로 센서 클래스  -   조건을 만족하는 자이로 센서를 수집하는 Class
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * X        :   Gyro 값의 event.value[0]의 값       :   X
 * Y        :   Gyro 값의 event.value[1]의 값       :   Y
 * Z        :   Gyro 값의 event.value[2]의 값       :   Z
 * Delay    :   조건을 만족한 Gyro 를 받은 시간     :   Delay
 *
 * **/
@Data
public class GyroSensor {

    @Getter @Setter
    @SerializedName("X")
    @Expose
    private String X;

    @Getter @Setter
    @SerializedName("Y")
    @Expose
    private String Y;

    @Getter @Setter
    @SerializedName("Z")
    @Expose
    private String Z;

    @Getter @Setter
    @SerializedName("Delay")
    @Expose
    private String Delay;
}
