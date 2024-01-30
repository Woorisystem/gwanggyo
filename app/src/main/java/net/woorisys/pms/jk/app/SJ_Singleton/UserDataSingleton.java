package net.woorisys.pms.jk.app.SJ_Singleton;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.woorisys.pms.jk.app.SJ_Domain.LobbyOpenData;

@Data
public class UserDataSingleton {

    @Getter
    @Setter
    private String Dong;
    private String Ho;
    private String UserName;
    private String Cel;
    private String ID;
    private ArrayList<LobbyOpenData> OpenMINOR;

    private static final UserDataSingleton ourInstance = new UserDataSingleton();

    public static UserDataSingleton getInstance() {
        return ourInstance;
    }

    private UserDataSingleton() {
    }
}
