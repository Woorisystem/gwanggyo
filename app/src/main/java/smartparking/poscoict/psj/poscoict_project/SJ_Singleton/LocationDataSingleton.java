package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.ParkingResult;

@Data
public class LocationDataSingleton {

    @Getter
    @Setter
    ArrayList<ParkingResult> parkingResults;
    String InterestCar;

    private static final LocationDataSingleton ourInstance = new LocationDataSingleton();

    public static LocationDataSingleton getInstance() {
        return ourInstance;
    }

    private LocationDataSingleton() {

        parkingResults=new ArrayList<>();
    }
}
