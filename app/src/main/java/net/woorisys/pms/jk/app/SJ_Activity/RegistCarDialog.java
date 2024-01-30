package net.woorisys.pms.jk.app.SJ_Activity;

import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import net.woorisys.pms.jk.R;
import net.woorisys.pms.jk.app.SJ_Server.ServerData;
import net.woorisys.pms.jk.app.SJ_Singleton.LocationDataSingleton;


public class RegistCarDialog extends AppCompatActivity {

    ListView Regist_Car;
    EditText ET_Regist_Car;

    ArrayList<String> carData;

    LocationDataSingleton locationDataSingleton;

    Button Regist_Btn,Cancle_Btn;

    ImageView Regist_Info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_interestcar);

        Bundle bundle=getIntent().getExtras();

        Log.d("TAG_REGISTCAR","CHANGE : "+bundle.getString("LOCATIONCHANGE"));

        {
            Regist_Info=findViewById(R.id.img_regist_car);
            Regist_Info.setImageResource(R.drawable.interestcar_change);
        }

        locationDataSingleton= LocationDataSingleton.getInstance();

        Regist_Car=findViewById(R.id.List_Regist_Car);
        ET_Regist_Car=findViewById(R.id.ET_Select_Car);

        Regist_Btn=findViewById(R.id.btn_Regist);
        Cancle_Btn=findViewById(R.id.btn_Regist_Cancle);

        Cancle_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Regist_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(ET_Regist_Car.getText().toString()==null || ET_Regist_Car.getText().toString().equals(null) ||ET_Regist_Car.getText().toString()=="" || ET_Regist_Car.getText().toString().equals("") )
                {
                    Toast.makeText(getApplicationContext(),"차량 정보가 입력되지 않았습니다.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    ServerData serverData=new ServerData(getApplicationContext());
                    serverData.RegistInterestCar(ET_Regist_Car.getText().toString());
                }
            }
        });

        carData=new ArrayList<>();

        AddCarData();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, carData)
        {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view=super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };

        Regist_Car.setAdapter(arrayAdapter);
        Regist_Car.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String CarNumber=Regist_Car.getItemAtPosition(position).toString();

                ET_Regist_Car.setText(CarNumber);
            }
        });
    }

    private void AddCarData()
    {
        for(int i=0; i <locationDataSingleton.getParkingResults().size();i++)
        {
            carData.add(locationDataSingleton.getParkingResults().get(i).getCarNumber());
        }
    }


}
