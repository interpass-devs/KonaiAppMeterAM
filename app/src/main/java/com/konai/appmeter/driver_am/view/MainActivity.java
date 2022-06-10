package com.konai.appmeter.driver_am.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver_am.R;
//import com.konai.appmeter.driver_am.databinding.ActivityMainBinding;
import com.konai.appmeter.driver_am.dialog.EnvSettingDialog;
import com.konai.appmeter.driver_am.dialog.InputFareDialog;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

//    ActivityMainBinding activityMainBinding;

    private Context mContext;
    private View viewframe1, viewframe2;
    private FrameLayout frame1, frame2;
    private LinearLayout view1_emptyLayout, view1_driveLayout, view1_paymentLayout;
    private Button btn_connect_ble, btn_menu, btn_close_menu;
    private DrawerLayout menuLayout;
    private View drawerView;
    private FontFitTextView tv_nowDate, tv_nowTime;
    private TextView menuVersion;
    private LinearLayout menuInfo, menualPay;

    /** viewframe1 **/
    /* 빈차화면 */
    private TextView showDetail;

    /* 빈차 상세화면 */
    private LinearLayout view1_detailEmptyLayout;

    /* 주행화면 */

    /* 결제화면 */

    /** viewframe2 **/
    private LinearLayout view2_emptyLayout, view2_driveLayout, view2_paymentLayout;
    private ButtonFitText btn_driveStart_e, btn_emptyCar_e, btn_menualPay_e, btn_call_e;
    private ButtonFitText btn_driveStart_d, btn_emptyCar_d, btn_complex_d, btn_suburb_d, btn_nbtn_suburb_d, btn_nbtn_extra_d;
    private ButtonFitText btn_cashPay_p, btn_addPay_p, btn_callPay_p, btn_cancelPay_p;
    private FontFitTextView tv_show_detail_e, tv_transaction_report_e, tv_back_empty_e, tv_resettfare_e, tv_daytoPay_e, tv_ntv_boardkm_d, tv_status_d, tv_callfare_d, tv_callfare_p, tv_addfare_p;
    private Boolean btnSubClicked = true;
    private Boolean btnAddFareClicked = true;
    private Boolean btnCallFareClicked = true;
    private Boolean btnCallClicked = true;

    private int cnt = 0;

    EnvSettingDialog envSettingDialog;
    InputFareDialog inputFareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //데이터 초기화 현상
        //저장되어있는 데이터 가져오기
        if (savedInstanceState != null) {
            cnt = savedInstanceState.getInt("num");
        }

        mainVariablesConfiguration();

        viewFrameVariablesConfiguration();

        dataSetConfiguration();





    }//onCreate


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //여기서도 setContentView를 호출해야 orientation 전환시 가로 레이아웃까지 잘 보여짐
        setContentView(R.layout.activity_main);

        mainVariablesConfiguration();

        //각 (viewFrame1, viewFrame2) orientation 화면의 변수들 define
        viewFrameVariablesConfiguration();

        //me: for 데이터 초기화 현상
        //dataSet
        //ex.... tvDistance.setText(ed.getText.toStrig);
        dataSetConfiguration();
    }

    public Configuration getOriConfig(Configuration newConfig) {
        return newConfig;
    }


    private void mainVariablesConfiguration() {

        mContext = this;
        tv_nowDate = (FontFitTextView) findViewById(R.id.ntv_nowdate);
        tv_nowTime = (FontFitTextView)findViewById(R.id.ntv_nowtime);
        menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawer_menu);
        btn_close_menu = (Button) findViewById(R.id.menu_btnclose);
        menuVersion = (TextView) findViewById(R.id.menuversion);  //메뉴버전
        btn_menu = findViewById(R.id.nbtn_menu);
        btn_connect_ble = (Button) findViewById(R.id.nbtn_connectble);
        menuInfo = (LinearLayout) findViewById(R.id.menu_info);
        menualPay = (LinearLayout) findViewById(R.id.menu_menualpay);

        //블루투스버튼
        btn_connect_ble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "블루투스 페어링", Toast.LENGTH_SHORT).show();
            }
        });

        //메뉴버튼
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //주행 일 경우 메뉴 못씀
                if (view1_driveLayout.getVisibility() == View.VISIBLE) {
                    //don't do anything
                }else {
                    menuLayout.openDrawer(drawerView);
                }
            }
        });

        //메뉴닫기버튼
        btn_close_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuLayout.closeDrawer(drawerView);
            }
        });

        //메뉴-정보
        menuInfo.setOnClickListener(menuClickListener);

        //메뉴-수기결제
        menualPay.setOnClickListener(menuClickListener);

    }


    private void viewFrameVariablesConfiguration() {

        viewframe1 = null;
        viewframe2 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1);
        frame2 = (FrameLayout) findViewById(R.id.frame2);

        LayoutInflater inflater = null;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewframe1 = inflater.inflate(R.layout.newmainframe1, frame1,true);  //상단화면
        viewframe2 = inflater.inflate(R.layout.newmainframe2, frame2, true); //하단 버튼화면


        //me: viewframe1
        view1_emptyLayout = (LinearLayout) viewframe1.findViewById(R.id.empty_layout);
        view1_detailEmptyLayout = (LinearLayout) viewframe1.findViewById(R.id.empty_detail_layout);
        view1_driveLayout = (LinearLayout) viewframe1.findViewById(R.id.driving_layout);
        view1_paymentLayout = (LinearLayout) viewframe1.findViewById(R.id.payment_layout);

        //me: viewframe2
        view2_emptyLayout = (LinearLayout) viewframe2.findViewById(R.id.emptylayouts);
        view2_driveLayout = (LinearLayout) viewframe2.findViewById(R.id.drivelayouts);
        view2_paymentLayout = (LinearLayout) viewframe2.findViewById(R.id.paymentlayouts);

        btn_driveStart_e = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_drivestart);
        btn_emptyCar_e = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_emptycar_e);
        btn_menualPay_e = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_manualpay_e);
        btn_call_e = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_reserv_e);
        tv_show_detail_e = (FontFitTextView) viewframe1.findViewById(R.id.tv_show_detail);
        tv_back_empty_e = (FontFitTextView) viewframe1.findViewById(R.id.tv_back_empty_e);
        tv_transaction_report_e = (FontFitTextView) viewframe1.findViewById(R.id.tv_transaction_report);
        tv_resettfare_e = (FontFitTextView) viewframe1.findViewById(R.id.tv_resettfare); //금액마감
        tv_daytoPay_e = (FontFitTextView) viewframe1.findViewById(R.id.ntv_daytotpay);  //금액

        btn_driveStart_d = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_driveend);
        btn_emptyCar_d = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_emptycar_d);
        btn_complex_d = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_complex_d);
        btn_suburb_d = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_surburb_d);
        btn_nbtn_suburb_d = (ButtonFitText) viewframe1.findViewById(R.id.nbtn_suburb);
        btn_nbtn_extra_d = (ButtonFitText) viewframe1.findViewById(R.id.nbtn_extra);
        tv_ntv_boardkm_d = (FontFitTextView) viewframe1.findViewById(R.id.ntv_boardkm);  //주행거리
        tv_callfare_d = (FontFitTextView) viewframe1.findViewById(R.id.ntv_callfare);
        tv_status_d = (FontFitTextView) viewframe1.findViewById(R.id.ntv_status);

        btn_cashPay_p = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_cashpayment);
        btn_addPay_p = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_addpayment);
        btn_callPay_p = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_callpayment);
        btn_cancelPay_p = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_cancelpayment);
        tv_callfare_p = (FontFitTextView) viewframe1.findViewById(R.id.ntv_rescallpay);
        tv_addfare_p = (FontFitTextView) viewframe1.findViewById(R.id.nedt_addpayment);

        btn_driveStart_e.setOnClickListener(mainBtnClickListener);
        btn_emptyCar_e.setOnClickListener(mainBtnClickListener);
        btn_menualPay_e.setOnClickListener(mainBtnClickListener);
        btn_call_e.setOnClickListener(mainBtnClickListener);
        tv_show_detail_e.setOnClickListener(clickListener);
        tv_back_empty_e.setOnClickListener(clickListener);
        tv_transaction_report_e.setOnClickListener(clickListener);
        tv_resettfare_e.setOnClickListener(clickListener);
        btn_nbtn_suburb_d.setOnClickListener(clickListener);
        btn_nbtn_extra_d.setOnClickListener(clickListener);

        btn_driveStart_d.setOnClickListener(mainBtnClickListener);
        btn_emptyCar_d.setOnClickListener(mainBtnClickListener);
        btn_complex_d.setOnClickListener(mainBtnClickListener);
        btn_suburb_d.setOnClickListener(mainBtnClickListener);
        tv_callfare_d.setOnClickListener(mainBtnClickListener);
        tv_status_d.setOnClickListener(mainBtnClickListener);

        btn_cashPay_p.setOnClickListener(mainBtnClickListener);
        btn_addPay_p.setOnClickListener(mainBtnClickListener);
        btn_callPay_p.setOnClickListener(mainBtnClickListener);
        btn_cancelPay_p.setOnClickListener(mainBtnClickListener);
        tv_callfare_p.setOnClickListener(mainBtnClickListener);
        tv_callfare_d.setOnClickListener(mainBtnClickListener);

    }


    private void dataSetConfiguration() {

        Date time = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("a hh:mm");

        //현재날짜
        tv_nowDate.setText(dateFormat.format(time));
        //현재시간
        tv_nowTime.setText(timeFormat.format(time));

    }


    //Activity 파괴시 데이터 저장
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("num", cnt);
    }



    //메인버튼 클릭리스너
    private View.OnClickListener mainBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                    //빈차화면 버튼
                case R.id.nbtn_drivestart: //손님탑승
                    view1_driveLayout.setVisibility(View.VISIBLE);
                    view1_emptyLayout.setVisibility(View.GONE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    view2_driveLayout.setVisibility(View.VISIBLE);
                    view2_emptyLayout.setVisibility(View.GONE);
                    view2_paymentLayout.setVisibility(View.GONE);
                    break;
                case R.id.nbtn_emptycar_e:  //빈차
                    view1_emptyLayout.setVisibility(View.VISIBLE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_driveLayout.setVisibility(View.GONE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    view2_emptyLayout.setVisibility(View.VISIBLE);
                    view2_driveLayout.setVisibility(View.GONE);
                    view2_paymentLayout.setVisibility(View.GONE);
                    break;
                case R.id.nbtn_manualpay_e: //수기

                    //수기결제 다이얼로그 띄우기
                    inputFareDialog = new InputFareDialog(mContext, setting.gOrient
                            , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //ok btn
                            String value= inputFareDialog.returnMenualValue();
//                            Log.d("getMenualPay_1", value);
                            inputFareDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //cancel btn
                            inputFareDialog.dismiss();
                        }
                    });

                    inputFareDialog.setCancelable(true);
                    inputFareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    inputFareDialog.show();
                    setDisplayMetrics(mContext, inputFareDialog);
                    break;

                case R.id.nbtn_reserv_e:    //호출
                    // +1000
                    if (btnCallClicked) {
                        btn_call_e.setBackgroundResource(R.drawable.yellow_selected_btn);
                        btnCallClicked = false;
                    }else {
                        btn_call_e.setBackgroundResource(R.drawable.grey_gradi_btn);
                        btnCallClicked = true;
                    }
                    break;

                    //주행화면 버튼
                case R.id.nbtn_driveend:   //지불
                    view1_paymentLayout.setVisibility(View.VISIBLE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_emptyLayout.setVisibility(View.GONE);
                    view1_driveLayout.setVisibility(View.GONE);
                    view2_emptyLayout.setVisibility(View.GONE);
                    view2_driveLayout.setVisibility(View.GONE);
                    view2_paymentLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.nbtn_emptycar_d: //빈차
                    view1_emptyLayout.setVisibility(View.VISIBLE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_driveLayout.setVisibility(View.GONE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    view2_emptyLayout.setVisibility(View.VISIBLE);
                    view2_driveLayout.setVisibility(View.GONE);
                    view2_paymentLayout.setVisibility(View.GONE);
                    btn_suburb_d.setBackgroundResource(R.drawable.grey_gradi_btn);
                    btn_suburb_d.setTextColor(getResources().getColor(R.color.white));
                    btn_nbtn_suburb_d.setBackgroundResource(R.drawable.white_line_background);
                    tv_status_d.setVisibility(View.GONE);
                    break;
                case R.id.nbtn_complex_d:  //복합
                    //
                    break;
                case R.id.nbtn_surburb_d:  //시외
                    if (btnSubClicked) {
                        tv_status_d.setVisibility(View.VISIBLE);
                        btn_suburb_d.setBackgroundResource(R.drawable.yellow_gradi_btn);
                        btn_suburb_d.setTextColor(getResources().getColor(R.color.black));
                        btn_nbtn_suburb_d.setText("시외 켜짐");
                        btn_nbtn_suburb_d.setBackgroundResource(R.drawable.pink_btn_background);
                        btnSubClicked = false;
                    }else {
                        tv_status_d.setVisibility(View.GONE);
                        btn_suburb_d.setBackgroundResource(R.drawable.grey_gradi_btn);
                        btn_suburb_d.setTextColor(getResources().getColor(R.color.white));
                        btn_nbtn_suburb_d.setText("시외 꺼짐");
                        btn_nbtn_suburb_d.setBackgroundResource(R.drawable.white_line_background);
                        btnSubClicked = true;
                    }
                    break;
                    //결제화면 버튼
                case R.id.nbtn_cashpayment:
                    break;
                case R.id.nbtn_addpayment:   //추가요금
                    if (btnAddFareClicked) {
                        //다이얼로그 띄우기
//                    tv_addfare_p.
                        btnAddFareClicked = false;
                    }else {
                        btnAddFareClicked = true;
                    }
                    break;
                case R.id.nbtn_callpayment:  //호출요금
                    if (btnCallFareClicked){
                        tv_callfare_p.setText("1000 원");
                        btnCallFareClicked = false;
                    }else {
                        tv_callfare_p.setText("0 원");
                        btnCallFareClicked = true;
                    }
                    break;
                case R.id.nbtn_cancelpayment:
                    view1_emptyLayout.setVisibility(View.GONE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_driveLayout.setVisibility(View.VISIBLE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    view2_emptyLayout.setVisibility(View.GONE);
                    view2_driveLayout.setVisibility(View.VISIBLE);
                    view2_paymentLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

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


    //기타 클릭리스너
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                /* 빈차화면 */
                case R.id.tv_show_detail:      //상세보기
                    view1_emptyLayout.setVisibility(View.GONE);
                    view1_detailEmptyLayout.setVisibility(View.VISIBLE);
                    view1_driveLayout.setVisibility(View.GONE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    break;
                case R.id.tv_back_empty_e:     //돌아가기
                    view1_emptyLayout.setVisibility(View.VISIBLE);
                    view1_detailEmptyLayout.setVisibility(View.GONE);
                    view1_driveLayout.setVisibility(View.GONE);
                    view1_paymentLayout.setVisibility(View.GONE);
                    break;
                case R.id.tv_transaction_report:  //거래집계
                    Toast.makeText(MainActivity.this, "빈차등 메뉴 연결", Toast.LENGTH_SHORT).show();
                    //Intent = new Intent
                    break;
                case R.id.tv_resettfare:       //금액마감
                    break;

                    /*주행화면*/
                case R.id.nbtn_suburb:         //시외꺼짐
                    break;

                case R.id.nbtn_extra:    //할증꺼짐
                    break;
            }

        }
    };


    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_info:
                    Intent i = new Intent(mContext, InfoActivity.class);
                    startActivity(i);
                    break;
                case R.id.menu_menualpay:
                    //수기결제 다이얼로그
                    inputFareDialog = new InputFareDialog(mContext, setting.gOrient
                            , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //ok btn
                            String value= inputFareDialog.returnMenualValue();
//                            Log.d("getMenualPay", value);
                            inputFareDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //cancel btn
                            inputFareDialog.dismiss();
                        }
                    });

                    inputFareDialog.setCancelable(true);
                    inputFareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    inputFareDialog.show();
                    setDisplayMetrics(mContext, inputFareDialog);
                    break;
            }
        }
    };


    private void frameLayoutChange(int whichLayout) {   //1: 빈차화면/  2: 주행화면/ 3: 결제화면

    }


}//MainActivity..