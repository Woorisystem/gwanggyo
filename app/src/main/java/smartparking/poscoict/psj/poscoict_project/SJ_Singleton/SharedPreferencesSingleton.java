package smartparking.poscoict.psj.poscoict_project.SJ_Singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import smartparking.poscoict.psj.poscoict_project.R;
@Data
public class SharedPreferencesSingleton {

    private final String TAG_W="TAG_SharedSingleton";
    private Context context;

    @Getter
    @Setter
    private String ID_W;
    private String PASS0WORD_W;
    private boolean INITLOGIN_W;
    private boolean REMEMBER_W;
    private boolean DRIVER_W;

    private String IDName;
    private String PASSWORDName;
    private String INITLOGINName;
    private String REMEMBERName;
    private String DRIVERName;

    private static SharedPreferencesSingleton ourInstance;

    public static SharedPreferencesSingleton getInstance(Context context) {
        if(ourInstance==null)
            ourInstance=new SharedPreferencesSingleton(context);

        return ourInstance;
    }

    private SharedPreferencesSingleton(Context context) {

        this.context=context;

        IDName=context.getString(R.string.sp_id);
        PASSWORDName=context.getString(R.string.sp_password);
        INITLOGINName=context.getString(R.string.sp_initlogin);
        REMEMBERName=context.getString(R.string.sp_remember);
        DRIVERName=context.getString(R.string.sp_driver);
    }

    public void SharedPreferenceRead()
    {

        Log.e(TAG_W,"SharedPreferenceRead 실행");
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getResources().getString(R.string.sp_login_value),Context.MODE_PRIVATE);

        ID_W=sharedPreferences.getString(IDName,"");
        PASS0WORD_W=sharedPreferences.getString(PASSWORDName,"");
        INITLOGIN_W=sharedPreferences.getBoolean(INITLOGINName,true);
        REMEMBER_W=sharedPreferences.getBoolean(REMEMBERName,true);
        DRIVER_W=sharedPreferences.getBoolean(DRIVERName,false);
    }

    public void SharedPreferenceWrite(boolean remember,boolean driver,String id,String password)
    {
        Log.e(TAG_W,"SharedPreferenceWrite 실행");
        SharedPreferences SharedPre_S=context.getSharedPreferences(context.getResources().getString(R.string.sp_login_value),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=SharedPre_S.edit();

        if(remember)
        {
            editor.putString(IDName,id);
            editor.putString(PASSWORDName,password);
        }
        else
        {
            editor.putString(IDName,null);
            editor.putString(PASSWORDName,null);
        }

        editor.putBoolean(REMEMBERName,remember);
        editor.putBoolean(DRIVERName,driver);

        editor.commit();
    }

    public void SaveInitLogin()
    {
        SharedPreferences SharedPre=context.getSharedPreferences(context.getResources().getString(R.string.sp_login_value),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=SharedPre.edit();

        editor.putBoolean(INITLOGINName,false);
        editor.apply();
        editor.commit();
    }
}
