package com.example.widgets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.os.Bundle;

public class Dashboard extends AppCompatActivity {
    private PagerAdapter pagerAdapter;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ViewPager viewPager = findViewById(R.id.vp);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        if (shp == null) {
            shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        }
        String userName = shp.getString("name", "");

        if (userName.equals("her")){
            pagerAdapter.add(new him(), "Him");
            pagerAdapter.add(new her(), "Her");
        }else{
            pagerAdapter.add(new her(), "Her");
            pagerAdapter.add(new him(), "Him");
        }
        viewPager.setAdapter(pagerAdapter);
    }
}