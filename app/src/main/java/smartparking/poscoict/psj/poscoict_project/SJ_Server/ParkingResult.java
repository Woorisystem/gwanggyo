package smartparking.poscoict.psj.poscoict_project.SJ_Server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ParkingResult {

    //ex)A,B,C
    @Getter
    @Setter
    @SerializedName("carNumber")
    @Expose
    private String carNumber;

    //ex)A-1, A-2, A-3
    @Getter
    @Setter
    @SerializedName("mapId")
    @Expose
    private String mapId;

    @Getter
    @Setter
    @SerializedName("lastParkingTime")
    @Expose
    private String lastParkingTime;

    @Getter
    @Setter
    @SerializedName("area")
    @Expose
    private String area;

    @Getter
    @Setter
    @SerializedName("x")
    @Expose
    private String x;

    @Getter
    @Setter
    @SerializedName("y")
    @Expose
    private String y;
}
