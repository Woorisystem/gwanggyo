package smartparking.poscoict.psj.poscoict_project.SJ_Interface;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import smartparking.poscoict.psj.poscoict_project.SJ_Domain.Total;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.AppResult;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.GateInfoResult;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.LocationResult;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.PhoneInfoResult;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.UserDataResult;


public interface NetworkService {

    String pms="pms-server-web";

    @FormUrlEncoded
    @POST("/"+pms+"/app//locationCheck")
    Call<LocationResult>Location
            (
                    @Field("userId") String UserId,
                    @Field("dong") String Dong,
                    @Field("ho") String Ho
            );

    @POST("/"+pms+"/app/calcLocation")
    Call<AppResult> Result(@Query("userId") String userId, @Query("dong") String dong, @Query("ho") String ho, @Body Total location);

    @FormUrlEncoded
    @POST("/"+pms+"/app/login")
    Call<UserDataResult>LoginForm(
            @Field("id") String ID,
            @Field("pass") String PASSWORD,
            @Field("site") String SITE
    );

    @FormUrlEncoded
    @POST("/"+pms+"/app/openLobby")
    Call<AppResult>OpenLobby(
            @Field("id") String ID,
            @Field("dong") String DONG,
            @Field("ho") String HO,
            @Field("minor") String MINOR,
            @Field("rssi") String RSSI
    );

    @FormUrlEncoded
    @POST("/"+pms+"/app/outParking")
    Call<AppResult>OutParking
            (
                    @Field("userId") String ID,
                    @Field("dong") String DONG,
                    @Field("ho") String HO
            );

    @FormUrlEncoded
    @POST("/"+pms+"/app/interestCar")
    Call<AppResult>InterestCar
            (
                    @Field("userId") String UserID,
                    @Field("dong") String Dong,
                    @Field("ho") String Ho,
                    @Field("car") String Car
            );

    @FormUrlEncoded
    @POST("/"+pms+"/app/gateInfo")
    Call<GateInfoResult>GateInfo
            (
                    @Field("userId") String UserID,
                    @Field("major") String major,
                    @Field("minor") String minor
            );

    @FormUrlEncoded
    @POST("/"+pms+"/app/phoneInfo")
    Call<PhoneInfoResult>PhoneInfo
            (
                    @Field("userId") String UserID,
                    @Field("version") String Dong,
                    @Field("info") String Ho
            );

    @FormUrlEncoded
    @POST("/"+pms+"/app/gyroInfo")
    Call<AppResult>GyroInfo(
            @Field("userId") String USERID,
            @Field("count") String Count
    );

}
