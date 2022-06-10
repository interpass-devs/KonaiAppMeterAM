package com.konai.appmeter.driver_am.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.konai.appmeter.driver_am.R;

public class DrawerBaseActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;

    LinearLayout menuInfo, menuDrvHistory, menuSetting, menuEnvSetting, menuMeualPay, menuPrintReceipt, menuCashReceipt, menuCancelCardPay;
    Button memuEndDrv, menuEndApp;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
        ImageView btn_menu = drawerLayout.findViewById(R.id.iv_menu);
        setSupportActionBar(toolbar);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        menuInfo = (LinearLayout) drawerLayout.findViewById(R.id.menu_info);   //정보
        menuDrvHistory = (LinearLayout) drawerLayout.findViewById(R.id.menu_drvhistory); //거래집계
        menuSetting = (LinearLayout) drawerLayout.findViewById(R.id.menu_setting);    //빈차등메뉴
        menuEnvSetting = (LinearLayout) drawerLayout.findViewById(R.id.menu_env_setting); //환경설정
        menuMeualPay = (LinearLayout) drawerLayout.findViewById(R.id.menu_menualpay);  //수기결제
        menuPrintReceipt = (LinearLayout) drawerLayout.findViewById(R.id.menu_getreceipt); //영수증출력
        menuCashReceipt = (LinearLayout) drawerLayout.findViewById(R.id.menu_cashreceipt); //현금영수증
        menuCancelCardPay = (LinearLayout) drawerLayout.findViewById(R.id.menu_cancelpay);   //카드결제취소
        memuEndDrv = (Button) drawerLayout.findViewById(R.id.menu_enddrv);     //금일영업마감
        menuEndApp = (Button) drawerLayout.findViewById(R.id.menu_endapp);     //앱종료


        menuInfo.setOnClickListener(menuClickListener);
        menuDrvHistory.setOnClickListener(menuClickListener);
        menuSetting.setOnClickListener(menuClickListener);
        menuEnvSetting.setOnClickListener(menuClickListener);
        menuMeualPay.setOnClickListener(menuClickListener);
        menuPrintReceipt.setOnClickListener(menuClickListener);
        menuCashReceipt.setOnClickListener(menuClickListener);
        menuCancelCardPay.setOnClickListener(menuClickListener);
        memuEndDrv.setOnClickListener(menuClickListener);
        menuEndApp.setOnClickListener(menuClickListener);
    }




    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.menu_info:
                    Toast.makeText(DrawerBaseActivity.this, "정보화면", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_drvhistory:
                    Toast.makeText(DrawerBaseActivity.this, "거래집계", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_setting:
                    Toast.makeText(DrawerBaseActivity.this, "빈차등메뉴", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_env_setting:
                    Toast.makeText(DrawerBaseActivity.this, "환경설정", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_menualpay:
                    Toast.makeText(DrawerBaseActivity.this, "수기결제", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_getreceipt:
                    Toast.makeText(DrawerBaseActivity.this, "영수증출력", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_cashreceipt:
                    Toast.makeText(DrawerBaseActivity.this, "현금영수증", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_cancelpay:
                    Toast.makeText(DrawerBaseActivity.this, "카드결제취소", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_enddrv:
                    Toast.makeText(DrawerBaseActivity.this, "금일영업마감", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_endapp:
                    Toast.makeText(DrawerBaseActivity.this, "앱종료", Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    };



    protected void allocateActivityTitle(String titleString) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleString);
        }
    }




}