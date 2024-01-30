package net.woorisys.pms.jk.app.SJ_Server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserResult {

    @Getter
    @Setter
    @SerializedName("dong")
    @Expose
    private String dong;

    @Getter
    @Setter
    @SerializedName("ho")
    @Expose
    private String ho;

    @Getter
    @Setter
    @SerializedName("name")
    @Expose
    private String name;

    @Getter
    @Setter
    @SerializedName("cel")
    @Expose
    private String cel;

    @Getter
    @Setter
    @SerializedName("minorList")
    @Expose
    private Object minorList;
}
