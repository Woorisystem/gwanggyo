package smartparking.poscoict.psj.poscoict_project.SJ_Server;

import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.GateInfo;

public class GateInfoResult {

    public static final int RETURN_CODE_SUCCESS=0;
    public static final int RETURN_CODE_FAILED=-1;

    @Getter
    @Setter
    private int returnCode;
    private String message;
    private GateInfo result=null;

    public GateInfoResult(){this.returnCode=RETURN_CODE_SUCCESS;}
    public GateInfoResult(int returnCode){this.returnCode=returnCode;}
    public GateInfoResult(GateInfo object){this.result=object;}

    public GateInfoResult(int returnCode,GateInfo object)
    {
        this(returnCode);
        this.result=object;
    }
}
