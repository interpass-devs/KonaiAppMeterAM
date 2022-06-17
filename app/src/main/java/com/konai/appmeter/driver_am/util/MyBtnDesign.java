package com.konai.appmeter.driver_am.util;

import android.content.Context;
import android.view.View;

import com.konai.appmeter.driver_am.R;

public class MyBtnDesign {

    private Context mContext;
    private ButtonFitText btnMainStatus, btnEmpty, btnDrive, btnReserve, btnPay;

    public MyBtnDesign(Context context, ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        this.mContext = context;
        this.btnMainStatus = btnMainStatus;
        this.btnEmpty = btnEmpty;
        this.btnDrive = btnDrive;
        this.btnReserve = btnReserve;
        this.btnPay = btnPay;
    }

    private void setBtnEmpty(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("빈차");
        btnMainStatus.setTextColor(mContext.getResources().getColor(R.color.white));
        btnMainStatus.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnEmpty.setText("빈차");
        btnEmpty.setTextColor(mContext.getResources().getColor(R.color.white));
        btnDrive.setTextColor(mContext.getResources().getColor(R.color.white));
        btnReserve.setTextColor(mContext.getResources().getColor(R.color.white));
        btnPay.setTextColor(mContext.getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setDriveStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("주행");
        btnMainStatus.setTextColor(mContext.getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnDrive.setText("주행");
        btnEmpty.setTextColor(mContext.getResources().getColor(R.color.white));
        btnDrive.setTextColor(mContext.getResources().getColor(R.color.grey_light));
        btnReserve.setTextColor(mContext.getResources().getColor(R.color.white));
        btnPay.setTextColor(mContext.getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_selected_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setReserveStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("호출");
        btnMainStatus.setTextColor(mContext.getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.green_gradi_btn);
        btnReserve.setText("호출");
        btnEmpty.setTextColor(mContext.getResources().getColor(R.color.white));
        btnDrive.setTextColor(mContext.getResources().getColor(R.color.white));
        btnReserve.setTextColor(mContext.getResources().getColor(R.color.grey_light));
        btnPay.setTextColor(mContext.getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_selected_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setPayStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("지불");
        btnMainStatus.setTextColor(mContext.getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.orange_gradi_btn);
        btnPay.setText("지불");
        btnEmpty.setTextColor(mContext.getResources().getColor(R.color.white));
        btnDrive.setTextColor(mContext.getResources().getColor(R.color.white));
        btnReserve.setTextColor(mContext.getResources().getColor(R.color.white));
        btnPay.setTextColor(mContext.getResources().getColor(R.color.grey_light));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_selected_btn);
    }


}
