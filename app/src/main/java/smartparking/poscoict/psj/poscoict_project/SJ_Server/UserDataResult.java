package smartparking.poscoict.psj.poscoict_project.SJ_Server;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserDataResult {
    public static final int RETURN_CODE_SUCCESS=0;

    @Getter
    @Setter
    private int returnCode;
    private String message;
    private UserResult result=null;

    public UserDataResult(){this.returnCode=RETURN_CODE_SUCCESS;}
    public UserDataResult(int returnCode){this.returnCode=returnCode;}
    public UserDataResult(UserResult object){this.result=object;}

    public UserDataResult(int returnCode,UserResult object)
    {
        this(returnCode);
        this.result=object;
    }


}
