package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.LobbyOpenData;

@Data
public class UserDataSingleton {

    @Getter
    @Setter
    private String Dong;
    private String Ho;
    private String UserName;
    private String Cel;
    private String ID;
    private boolean IsDriver;
    private ArrayList<LobbyOpenData> OpenMINOR;


    private static final UserDataSingleton ourInstance = new UserDataSingleton();

    public static UserDataSingleton getInstance() {
        return ourInstance;
    }

    private UserDataSingleton() {
        OpenMINOR=new ArrayList<>();
    }
}
