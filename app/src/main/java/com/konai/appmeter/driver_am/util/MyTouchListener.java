package com.konai.appmeter.driver_am.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.konai.appmeter.driver_am.R;

public class MyTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()) {
            /*frame2 메인하단버튼*/
            case R.id.nbtn_emptycar:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
            case R.id.nbtn_drivestart:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.yellow_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.yellow_gradi_btn);
                }
                break;
            case R.id.nbtn_reserve:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.green_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.green_gradi_btn);
                }
                break;
            case R.id.nbtn_driveend:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.orange_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.orange_gradi_btn);
                }
                break;
            /*frame3 메인상단버튼*/
            case R.id.nbtn_menu:  //메뉴 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
            case R.id.nbtn_complex:  //복합 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
            case R.id.nbtn_login:  //로그인 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
            case R.id.nbtn_arrived:  //도착 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
            case R.id.nbtn_addpayment:  //추가 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn);
                }
                break;
        }

        return false;
    }



}
