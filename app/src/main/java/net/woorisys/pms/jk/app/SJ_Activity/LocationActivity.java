package net.woorisys.pms.jk.app.SJ_Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Server.ParkingResult;
import net.woorisys.pms.jk.app.SJ_Singleton.LocationDataSingleton;


/** 위치 확인 VIew 와 연결된 클래스 **/
public class LocationActivity extends AppCompatActivity {

    //region UI Object : 위치 확인 Activity 의 Object
    ImageView locationImageView_W,locationlogoImageVIew_W,faviorteIconImageView_W;
    TextView locationTextView_W,location_floorTextView_W,parkingTextView_W,carNumberTextView_W;
    Button CloseButton_W;
    //endregion

    /** 위치 정보를 가져올 수 있도록 처리해주는 Singletonaa **/
    LocationDataSingleton locationDataSingleton;

    ArrayList<String> cardata;
    Menu menu;

    /** 이미지의 원래크기 **/
    double OrginWidth=1572;
    double OrginHeight=1146;

    /** 현제 ImageVIew 의 크기 **/
    double CurrentWidth;
    double CurrentHeight;

    /** 감소 하는 Percent **/
    double PercentWidth;
    double PercentHeight;

    /** 이미지 Location X,Y 좌표 **/
    double ImgLocationX;
    double ImgLocationY;

    double ImgLocationIconWidth;
    double ImgLocationIconHeight;

    boolean InitStart=true;

    String InterestCar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_location);

        ActionBarSetting();

        locationDataSingleton= LocationDataSingleton.getInstance();

        locationImageView_W=findViewById(R.id.Img_Location);
        locationTextView_W=findViewById(R.id.txt_Location);
        CloseButton_W=findViewById(R.id.btn_Close);
        location_floorTextView_W=findViewById(R.id.txt_Location_Floor);
        locationlogoImageVIew_W=findViewById(R.id.img_location_logo);
        parkingTextView_W=findViewById(R.id.txt_parking_time);
        carNumberTextView_W=findViewById(R.id.Txt_CarNumber_Input);
        faviorteIconImageView_W=findViewById(R.id.img_faviorte_icon);
        CloseButton_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cardata = new ArrayList<String>();
        AddListCarInfo();
    }

    /** Action Bar 에 메뉴 button 생성 **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.menu=menu;

        // 차량 등록
        AddMenuItem();
        // 관심차량 변경 기능
        menu.add(2,1,1,"").setIcon(R.drawable.plusicon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /** Action Bar 에 메뉴의 item 선택 이벤트 **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d("TAG_Location","ITEM ID : "+item.getItemId());

        if(item.getGroupId()==1)
        {
            ShowFaviorteIcon(cardata.get(item.getItemId()).toString());
            LocationCheck(getParkingData(cardata.get(item.getItemId()).toString())) ;
        }

        else if(item.getGroupId()==2)
        {
            switch (item.getItemId())
            {
                case 1:
                    Intent intent=new Intent(getApplicationContext(),RegistCarDialog.class);
                    intent.putExtra("LOCATIONCHANGE","CHANGE");
                    intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);

                    finish();
                    break;
            }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        CurrentWidth = locationImageView_W.getWidth();
        CurrentHeight = locationImageView_W.getHeight();

        ImgLocationX=locationImageView_W.getX();
        ImgLocationY=locationImageView_W.getY();

        ImgLocationIconWidth=locationlogoImageVIew_W.getWidth();
        ImgLocationIconHeight=locationlogoImageVIew_W.getHeight();

        CurrentLocation();
    }

    /** 차량 정보 Action Bar 에 등록하는 함수 **/
    private void AddMenuItem()
    {
        for(int i=0; i<cardata.size(); i++)
        {
            Log.d("TAG_Location","Add Menu ITEM : "+cardata.get(i).toString());
            menu.add(1,i,i,cardata.get(i).toString());
        }
    }

    /** Action Bar 에 관심차량 기준으로 차량 정보 등록 **/
    private void AddListCarInfo()
    {
        if(locationDataSingleton.getParkingResults().size()==0)
        {
            Toast.makeText(getApplicationContext(),"등록된 차량이 없습니다.\n 차량을 등록하여 주세요.",Toast.LENGTH_SHORT).show();
        }

        for(int i=0; i<locationDataSingleton.getParkingResults().size(); i++)
        {
            String CarNumber=locationDataSingleton.getParkingResults().get(i).getCarNumber();
            InterestCar=locationDataSingleton.getInterestCar();

            if(CarNumber==InterestCar || CarNumber.equals(InterestCar))
            {
                cardata.add(CarNumber);
                for(int x=0; x<locationDataSingleton.getParkingResults().size();x++)
                {
                    String CarNumber2=locationDataSingleton.getParkingResults().get(x).getCarNumber();

                    if(CarNumber2==InterestCar || CarNumber2.equals(InterestCar))
                    {
                    }
                    else
                    {
                        cardata.add(CarNumber2);
                    }
                }
            }
        }
    }

    /** Action Bar 에서 선택한 차량에 대한 정보를 가져옴 **/
    private ParkingResult getParkingData(String CarNumber)
    {
        ParkingResult parkingResult=null;

        if(cardata.size()==0)
        {
            Log.d("TAG_Location","차량 정보가 없습니다.");
        }

        else
        {
            for(int i=0; i<cardata.size();i++)
            {

                Log.d("TAG_Location","차량 대수 : "+i);
                String carData=locationDataSingleton.getParkingResults().get(i).getCarNumber();
                if(carData==CarNumber || carData.equals(CarNumber))
                {
                    parkingResult=locationDataSingleton.getParkingResults().get(i);

                    String LogData=String.format("AREA : %s , CAR Number : %s , Last Parking Time : %s , Map ID : %s ,X : %s , Y : %s",parkingResult.getArea(),parkingResult.getCarNumber(),parkingResult.getLastParkingTime(),parkingResult.getMapId(),parkingResult.getX(),parkingResult.getY());
                    Log.d("TAG_Location",LogData);
                }
            }

        }
        return parkingResult;
    }

    /** 화면에 선택한 차량 정보 표시 **/
    private void LocationCheck(ParkingResult parkingResult)
    {
        String Area=parkingResult.getArea();                        //  위치
        String CarNumber=parkingResult.getCarNumber();              //  차량 번
        String LastTImer=parkingResult.getLastParkingTime();
        String MapId=parkingResult.getMapId();
        float X=0;
        float Y=0;

        if(Area==null || Area.equals(null))
        {
            Area="0-0";
        }

        if (LastTImer==null||LastTImer.equals(null))
        {
            LastTImer="NO TIME";
        }

        if(MapId==null || MapId.equals(null))
        {
            MapId="NO VALUE";
        }

        if(parkingResult.getX()==null || parkingResult.getX().equals(null))
        {
            X=0;
        }
        else
        {
            X=Float.valueOf(parkingResult.getX());
        }

        if(parkingResult.getY()==null || parkingResult.getY().equals(null))
        {
            Y=0;
        }
        else {
            Y=Float.valueOf(parkingResult.getY());
        }
        parkingTextView_W.setText(LastTImer);
        location_floorTextView_W.setText(MapId);
        carNumberTextView_W.setText(CarNumber);

        int Areaidx=Area.indexOf("-");
        if(Areaidx!=-1)
        {
            String AreaSub=Area.substring(0,Areaidx);
            locationTextView_W.setText(AreaSub);
        }
       else
        {
            locationTextView_W.setText(Area);
        }

        switch (MapId)
        {
            case "B1-103":
                locationImageView_W.setImageResource(R.drawable.b1_103);
                break;

            case "B1-104":
                locationImageView_W.setImageResource(R.drawable.b1_104);
                break;

            case "B2-101":
                locationImageView_W.setImageResource(R.drawable.b2_101);
                break;

            case "B2-102":
                locationImageView_W.setImageResource(R.drawable.b2_102);
                break;

            case "B2-103":
                locationImageView_W.setImageResource(R.drawable.b2_103);
                break;

            case "B2-104":
                locationImageView_W.setImageResource(R.drawable.b2_104);
                break;

            case "B2-G":
                locationImageView_W.setImageResource(R.drawable.b2_g);
                break;

            case "B3-101":
                locationImageView_W.setImageResource(R.drawable.b3_1);
                break;

            case "B3-102":
                locationImageView_W.setImageResource(R.drawable.b3_102);
                break;

            case "B3-103":
                locationImageView_W.setImageResource(R.drawable.b3_103);
                break;

            case "B3-104":
                locationImageView_W.setImageResource(R.drawable.b3_104);
                break;

                default:

                    break;
        }

        Log.d("TAG_Location","이동해야할 좌표 X/Y : "+X*PercentWidth+" / "+Y*PercentHeight);

        float ChangeX=X*Float.valueOf((float)PercentWidth)+(float)ImgLocationX-(float)ImgLocationIconWidth/2;
        float ChangeY=Y*Float.valueOf((float) PercentHeight)+(float)ImgLocationY-(float)ImgLocationIconHeight;

        locationlogoImageVIew_W.setX(ChangeX);
        locationlogoImageVIew_W.setY(ChangeY);

    }

    /**
     * Action Bar 설정
     * 사용자 차량 정보 입력
     **/
    private void ActionBarSetting()
    {
        ActionBar actionBar=this.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBackgroundColorYellow)));
        actionBar.setDisplayShowTitleEnabled(false);
    }

    /**
     * 현재 위치
     * Server 로 부터 받은 Location
     * 화면크기에 따라 계산하여 위치 표출
     **/
    private void CurrentLocation()
    {

        PercentWidth=CurrentWidth/OrginWidth;
        PercentHeight=CurrentHeight/OrginHeight;

        if(InitStart==true)
        {
            InitStart=false;

            for(int i=0; i<cardata.size();i++)
            {
                if(cardata.get(i)==InterestCar || cardata.get(i).equals(InterestCar))
                {
                    ShowFaviorteIcon(cardata.get(i).toString());
                    LocationCheck(getParkingData(cardata.get(i).toString())) ;
                }
            }


        }

    }

    private void ShowFaviorteIcon(String CarNumber)
    {
        if(InterestCar==CarNumber || InterestCar.equals(CarNumber))
        {
            faviorteIconImageView_W.setVisibility(View.VISIBLE);
        }
        else
        {
            faviorteIconImageView_W.setVisibility(View.INVISIBLE);
        }
    }
}
