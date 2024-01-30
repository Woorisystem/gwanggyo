package net.woorisys.pms.jk.app.SJ_Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Activity.LoginActivity;
import net.woorisys.pms.jk.app.SJ_BroadCast.BroadCastManager;

public class NotificationService {
    private static final String TAG = "Notification";

    private static final String FOREGROUND_CHANNEL_ID = "smart-parking-bluetooth";
    public static final int FOREGROUND_NOTIFICATION_ID = 821;

    private final BluetoothAdapter mBluetoothAdapter;
    private static NotificationService mNotificationServiceInstance;
    private static NotificationManager mNotificationManager = null;

    public NotificationService(@NonNull Context context) {
        Log.d(TAG,"Notification TEST !!!!");
        mNotificationServiceInstance = this;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static NotificationService getInstance() {
        return mNotificationServiceInstance;
    }

    // ---------------------------------------------------------------------------------------------
    // createForegroundNotification
    // ---------------------------------------------------------------------------------------------
    public Notification createForegroundNotification(Context context)
    {
        boolean pemissionState = true;

        Intent Intent_Location=new Intent(context, BroadCastManager.class);
        Intent Intent_Exit=new Intent(context,BroadCastManager.class);
        Intent Intent_Permission=new Intent(context,BroadCastManager.class);
        Intent Intent_Restart=new Intent(context,BroadCastManager.class);


        Intent_Location.putExtra("REQUEST_CODE",BroadCastManager.INTENT_LOCATION_NUM);      //위치 확인
        Intent_Exit.putExtra("REQUEST_CODE",BroadCastManager.INTENT_EXIT_NUM);              //종료
        Intent_Permission.putExtra("REQUEST_CODE",BroadCastManager.INTENT_PERMISSION_NUM);  //권한요청



        PendingIntent Location=PendingIntent.getBroadcast(context,BroadCastManager.INTENT_LOCATION_NUM,Intent_Location,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent Exit=PendingIntent.getBroadcast(context,BroadCastManager.INTENT_EXIT_NUM,Intent_Exit,PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent Permission=PendingIntent.getBroadcast(context,BroadCastManager.INTENT_PERMISSION_NUM,Intent_Permission,PendingIntent.FLAG_UPDATE_CURRENT);
        

        Resources resources = context.getResources();
        boolean locationGranted = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        String channelName;
        String title;
        String content;
        String description;
        Drawable drawable = null;
        int backgroundColor = resources.getColor(R.color.colorBackground);

        channelName = resources.getString(R.string.notification_parking_name);
        title = resources.getString(R.string.notification_parking_title);

        if (mBluetoothAdapter.isEnabled() && locationGranted) {
            //-1이면 앱 실행시 권한요청
            //0이면 항상 권한 요청
            //안드로이드 9버전 이하(포함)에서는 실행시 권한 요청이 없음
            int bPermissionApproved = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                bPermissionApproved = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }

            Log.d(TAG,"권한 확인 "+bPermissionApproved);

            if(bPermissionApproved == -1){
                content = resources.getString(R.string.notification_parking_permission_text);
                description = resources.getString(R.string.notification_parking_permission_description);
                pemissionState = false;
            }else {
                content = resources.getString(R.string.notification_parking_text);
                description = resources.getString(R.string.notification_parking_description);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                backgroundColor = resources.getColor(R.color.colorBackground, null);
            }
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                content = resources.getString(R.string.notification_parking_text);
                description = resources.getString(R.string.notification_bluetooth_off_description);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    backgroundColor = resources.getColor(R.color.colorBackground, null);
                }
            } else {
                content = resources.getString(R.string.notification_location_permission_off);
                description = resources.getString(R.string.notification_location_permission_off_description);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    backgroundColor = resources.getColor(R.color.colorBackground, null);
                }
            }
        }

        // Create Notification Channel
        createNotificationChannel(channelName);

        // Activity Pending Intent
        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setOngoing(true);
        if(pemissionState == false){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // for Large Icon
                Bitmap largeIcon = getDefaultBitmap(context, drawable);
                builder.setSmallIcon(R.drawable.car)
                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .addAction(R.drawable.car,"위치 권한 설정",Permission)
                        .addAction(R.drawable.car,"종료",Exit)
                        .setLargeIcon(largeIcon)
                        .setColor(backgroundColor);
            } else {
                builder.setSmallIcon(R.drawable.car);
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // for Large Icon
                Bitmap largeIcon = getDefaultBitmap(context, drawable);
                builder.setSmallIcon(R.drawable.car)
                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .addAction(R.drawable.car,"주차위치확인",Location)
                        .addAction(R.drawable.car,"종료",Exit)
                        .setLargeIcon(largeIcon)
                        .setColor(backgroundColor);
            } else {

                builder.setSmallIcon(R.drawable.car);
            }
        }
        return builder.build();
    }

    // ---------------------------------------------------------------------------------------------
    // createNotificationChannel
    // ---------------------------------------------------------------------------------------------
    private void createNotificationChannel(String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create Notification Channel
            if (mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableVibration(false);

                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // getDefaultBitmap
    // ---------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap getDefaultBitmap(Context context, Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        } else if (d instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable icon = ((AdaptiveIconDrawable)d);
            int w = icon.getIntrinsicWidth();
            int h = icon.getIntrinsicHeight();
            Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            icon.setBounds(0, 0, w, h);
            icon.draw(canvas);
            return result;
        }

        float density = context.getResources().getDisplayMetrics().density;
        int defaultWidth = (int)(48* density);
        int defaultHeight = (int)(48* density);
        return Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888);
    }


    // ---------------------------------------------------------------------------------------------
    // updateNotification
    // ---------------------------------------------------------------------------------------------
    public void updateNotification(Context context) {
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, createForegroundNotification(context));
    }
}
