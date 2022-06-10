package com.konai.appmeter.driver_am.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.databinding.ActivityInfoBinding;

public class InfoActivity extends DrawerBaseActivity {

    ActivityInfoBinding activityInfoBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_info);
        activityInfoBinding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(activityInfoBinding.getRoot());
    }

}


