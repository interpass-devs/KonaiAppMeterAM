package com.konai.appmeter.driver_am.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.dialog.DriverSelectDialog;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemberCertActivity extends AppCompatActivity {

    private Context mContex;
    private TextView checkDriverBtn;
    private FontFitTextView driverName, driverNum, connResult;
    private EditText inputName, inputCarNo;
    private ButtonFitText okBtn;
    private CheckBox autoLoginCheckbox;

    private DriverSelectDialog driverSelectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_cert);

        mainVariableConfiguration();

        dataSetConfiguration();

    }//onCreate


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setting.gOrient = Configuration.ORIENTATION_LANDSCAPE;
        }else {
            setting.gOrient = Configuration.ORIENTATION_PORTRAIT;
        }

        setContentView(R.layout.activity_member_cert);

        mainVariableConfiguration();

        dataSetConfiguration();
    }



    private void mainVariableConfiguration() {

        mContex = this;
        checkDriverBtn = (TextView) findViewById(R.id.check_driver_info);
        driverName = (FontFitTextView) findViewById(R.id.driver_name);
        driverNum = (FontFitTextView) findViewById(R.id.driver_num);
        inputName = (EditText) findViewById(R.id.inpt_name);
        inputCarNo = (EditText) findViewById(R.id.inpt_carno);
        connResult = (FontFitTextView) findViewById(R.id.tv_conn_status);
        okBtn = (ButtonFitText) findViewById(R.id.btn_ok);
        autoLoginCheckbox = (CheckBox) findViewById(R.id.auto_login_checkbox);

        checkDriverBtn.setOnClickListener(clickListener);
        inputName.setOnClickListener(clickListener);
        inputCarNo.setOnClickListener(clickListener);
        okBtn.setOnClickListener(clickListener);

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.check_driver_info:
                    //운전자선택 다이얼로그
                    driverSelectDialog = new DriverSelectDialog(mContex
                            , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {  //운전자 등록

                            Intent i = new Intent(mContex, DriverRegisterActivity.class);
                            startActivity(i);
                            driverSelectDialog.dismiss();
                        }
                    });
                    driverSelectDialog.setCancelable(true);
                    driverSelectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    driverSelectDialog.show();
                    setDisplayMetrics(mContex, driverSelectDialog);
                    break;
                case R.id.inpt_name:
                    break;
                case R.id.inpt_carno:
                    break;
                case R.id.btn_ok:
                    Intent i = new Intent(mContex, MainActivity.class);
                    startActivity(i);
                    break;
            }
        }
    };

    private void dataSetConfiguration() {


    }

    private void setDisplayMetrics(Context context, Dialog dialog) {

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        width = (int) (width * 0.9);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = width;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        Window window = dialog.getWindow();
        window.setAttributes(lp);
    }

}