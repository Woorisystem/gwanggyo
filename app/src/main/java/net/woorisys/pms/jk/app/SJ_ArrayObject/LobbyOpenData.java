package net.woorisys.pms.jk.app.SJ_ArrayObject;

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
