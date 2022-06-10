package com.konai.appmeter.driver_am.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.util.ButtonFitText;

public class DriverRegisterActivity extends AppCompatActivity {

    private Context mContext;
    private EditText driverName, driverLicenseNum;
    private TextView driverMemberNum;
    private ButtonFitText registerBtn, editCompleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        defineVariables();


    }//onCreate


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_driver_register);
    }

    private void defineVariables() {
        mContext = this;
        driverName = (EditText) findViewById(R.id.et_driver_name);
        driverLicenseNum = (EditText) findViewById(R.id.et_driver_license_num);
        driverMemberNum = (TextView) findViewById(R.id.et_driver_identi_num);
        registerBtn = (ButtonFitText) findViewById(R.id.register_btn);
        editCompleteBtn = (ButtonFitText) findViewById(R.id.edit_btn);

        registerBtn.setVisibility(View.VISIBLE);
        registerBtn.setOnClickListener(clickListener);
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.et_driver_name:  //성함
                    break;
                case R.id.et_driver_license_num: //자격번호
                    break;
                case R.id.et_driver_identi_num:  //사원번호
                    break;
                case R.id.register_btn:  //등록버튼
                    finish();
                    break;
                case R.id.edit_btn:     //수정완료버튼
                    break;
            }

        }
    };

}