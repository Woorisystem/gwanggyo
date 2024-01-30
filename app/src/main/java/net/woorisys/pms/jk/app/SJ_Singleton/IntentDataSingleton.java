package net.woorisys.pms.jk.app.SJ_Singleton;

import android.content.Intent;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class IntentDataSingleton {

    @Getter
    @Setter
    Intent notification_service;
    Intent sensor_service;
    Intent beacon_service;
    Intent beacon26_service;

    private static final IntentDataSingleton ourInstance = new IntentDataSingleton();

    public static IntentDataSingleton getInstance() {
        return ourInstance;
    }

    private IntentDataSingleton() {
    }
}
