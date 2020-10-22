package smartparking.poscoict.psj.poscoict_project.SJ_Server;

import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.PhoneInfo;

public class PhoneInfoResult {

    public static final int RETURN_CODE_SUCCESS=0;
    public static final int RETURN_CODE_FAILED=-1;

    @Getter
    @Setter
    private int returnCode;
    private String message;
    private PhoneInfo result=null;

    public PhoneInfoResult(){this.returnCode=RETURN_CODE_SUCCESS;}
    public PhoneInfoResult(int returnCode){this.returnCode=returnCode;}
    public PhoneInfoResult(PhoneInfo object){this.result=object;}

    public PhoneInfoResult(int returnCode,PhoneInfo object)
    {
        this(returnCode);
        this.result=object;
    }
}
