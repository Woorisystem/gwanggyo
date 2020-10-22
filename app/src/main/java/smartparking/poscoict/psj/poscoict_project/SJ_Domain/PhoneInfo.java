package smartparking.poscoict.psj.poscoict_project.SJ_Domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class PhoneInfo {
    @Getter
    @Setter
    @SerializedName("userId")
    @Expose
    private String userId;

    @Getter
    @Setter
    @SerializedName("version")
    @Expose
    private String version;

    @Getter
    @Setter
    @SerializedName("info")
    @Expose
    private String info;
}
