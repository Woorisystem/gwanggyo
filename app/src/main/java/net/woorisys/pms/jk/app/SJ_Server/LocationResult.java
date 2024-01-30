package net.woorisys.pms.jk.app.SJ_Server;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LocationResult {
    public static final int RETURN_CODE_SUCCESS = 0;

    @Getter
    @Setter
    private int returnCode;
    private String message;
    private String interestCar;
    private ArrayList<ParkingResult> result;

    private LocationResult()
    {
        this.returnCode = RETURN_CODE_SUCCESS;
        result=new ArrayList<>();
    }

    public LocationResult(int returnCode) {
        this.returnCode = returnCode;
    }

    public LocationResult(ArrayList<ParkingResult> object) {
        this.result = object;
    }
    public LocationResult(int returnCode, ArrayList<ParkingResult> object) {
        this(returnCode);
        this.result = object;
    }

}
