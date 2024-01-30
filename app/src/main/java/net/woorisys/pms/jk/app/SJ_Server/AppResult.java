package net.woorisys.pms.jk.app.SJ_Server;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class AppResult {

    public static final int RETURN_CODE_SUCCESS = 0;
    public static final int RETURN_CODE_FAIL = -1;

    /**
     * 에러 코드, 0이면  성공
     */
    @Getter
    @Setter
    private int returnCode;
    private String message;
    private Object result = null;

    public AppResult() {
        this.returnCode = RETURN_CODE_SUCCESS;
    }

    public AppResult(int returnCode) {
        this.returnCode = returnCode;
    }

    public AppResult(Object object) {
        this.result = object;
    }

    public AppResult(int returnCode, Object object) {
        this(returnCode);
        this.result = object;
    }

}
