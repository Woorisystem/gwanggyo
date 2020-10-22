package smartparking.poscoict.psj.poscoict_project.SJ_Activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import smartparking.poscoict.psj.poscoict_project.R;
import smartparking.poscoict.psj.poscoict_project.SJ_Server.ParkingResult;
import smartparking.poscoict.psj.poscoict_project.SJ_Singleton.LocationDataSingleton;

public class LocationActivity_Select extends AppCompatActivity {

    SlidingUpPanelLayout slidingUpPanelLayout;
    LocationDataSingleton locationDataSingleton;
    LinearLayout linearLayout;
    ArrayList<ParkingResult> array;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        locationDataSingleton=LocationDataSingleton.getInstance();
        slidingUpPanelLayout=(SlidingUpPanelLayout)findViewById(R.id.const_layout);

        array=new ArrayList<>();

        array=locationDataSingleton.getParkingResults();

        linearLayout=findViewById(R.id.scroll_layout);
        final int inPixels= (int) getResources().getDimension(R.dimen.button_height);

        for(int i=0; i < array.size();i++)
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,inPixels);
            params.setMargins(40,20,40,30);
            params.weight=1;

            LinearLayout buttonLayout =new LinearLayout(this);
            buttonLayout.setLayoutParams(params);
            buttonLayout.setOrientation(LinearLayout.VERTICAL);
            buttonLayout.setClickable(true);
            buttonLayout.setBackgroundResource(R.drawable.bg_rectangle);
            buttonLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            buttonLayout.setBackgroundResource(R.drawable.bg_rectangle_click);
                            slidingUpPanelLayout.setAnchorPoint(1f);
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                            break;
                        case MotionEvent.ACTION_UP:
                            buttonLayout.setBackgroundResource(R.drawable.bg_rectangle);

                            break;
                    }
                    return true;
                }
            });


            linearLayout.addView(buttonLayout);

            LinearLayout.LayoutParams textparams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textparams.weight=1;

            TextView CarNumberText=new TextView(this);
            CarNumberText.setText(array.get(i).getCarNumber());
            CarNumberText.setLayoutParams(textparams);
            CarNumberText.setGravity(Gravity.CENTER|Gravity.BOTTOM);
            CarNumberText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            CarNumberText.setTypeface(Typeface.DEFAULT_BOLD);
            CarNumberText.setTextColor(getResources().getColor(R.color.colorTheSharpDarkNavyBlue));

            TextView Area=new TextView(this);
            Area.setText("지도: "+array.get(i).getMapId()+"기둥번호: "+array.get(i).getArea());
            Area.setLayoutParams(textparams);
            Area.setGravity(Gravity.CENTER|Gravity.TOP);
            Area.setTextColor(getResources().getColor(R.color.colorTheSharpDarkNavyBlue));

            buttonLayout.addView(CarNumberText);
            buttonLayout.addView(Area);

        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

    }
}
