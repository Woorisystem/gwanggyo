package net.woorisys.pms.jk.app.SJ_BroadCast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkCheck {

    public static boolean getNetwork(Context context) {
        boolean enable = false;

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        NetworkInfo ethernet = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        if (wifi != null) {
            if (wifi.isAvailable() && wifi.isConnected()) {
                enable = true;
            }
        }

        if (mobile != null) {
            if (mobile.isAvailable() && mobile.isConnected()) {
                enable = true;
            }
        }

        if (wimax != null) {
            if (wimax.isAvailable() && wimax.isConnected()) {
                enable = true;
            }
        }

        if (ethernet != null) {
            if (ethernet.isAvailable() && ethernet.isConnected()) {
                enable = true;
            }
        }

        return enable;

    }


}
