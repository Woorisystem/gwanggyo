package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import android.app.Notification;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class NotificationSingleton {

    @Getter
    @Setter
    Notification notification;


    private static final NotificationSingleton ourInstance = new NotificationSingleton();

    public static synchronized NotificationSingleton getInstance() {
        return ourInstance;
    }

    private NotificationSingleton()
    {
    }


}
