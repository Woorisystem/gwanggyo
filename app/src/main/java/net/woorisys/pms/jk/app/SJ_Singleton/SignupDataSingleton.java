package net.woorisys.pms.jk.app.SJ_Singleton;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SignupDataSingleton {

    @Getter
    @Setter
    private String SignupName;
    private String SignupDong;
    private String SignupHO;
    private String SignupID;
    private String SignupPassword;

    private static final SignupDataSingleton ourInstance = new SignupDataSingleton();

    public static SignupDataSingleton getInstance() {
        return ourInstance;
    }

}
