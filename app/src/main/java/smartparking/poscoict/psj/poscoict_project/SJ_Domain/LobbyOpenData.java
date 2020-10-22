package smartparking.poscoict.psj.poscoict_project.SJ_Domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LobbyOpenData {

    @Getter
    @Setter
    private String Minor;
    private String Rssi;

    public LobbyOpenData(){}
}
