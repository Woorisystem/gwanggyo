package net.woorisys.pms.jk.app.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class GateInfo {

    @Getter
    @Setter
    @SerializedName("userId")
    @Expose
    private String userId;

    @Getter
    @Setter
    @SerializedName("major")
    @Expose
    private String major;

    @Getter
    @Setter
    @SerializedName("minor")
    @Expose
    private String minor;
}
