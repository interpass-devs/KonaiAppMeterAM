package com.konai.appmeter.driver_am.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
            case R.id.nbtn_call:
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
//                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.nbtn_receipt:  //영수 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("receipt_bntn","ttttt");
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.nbtn_attendance:  //출근 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.nbtn_reserve:  //예약 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.nbtn_dayoff://휴무 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.nbtn_addpayment:  //추가 btn
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;


                /*메뉴 숫자버튼*/
            case R.id.close_menu_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.back_menu_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_0:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_1:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_2:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_3:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_4:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_5:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_6:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_7:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_8:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_9:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_clear:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_close:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.m_btn_ok:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;




                /*수기결제 숫자버튼*/
            case R.id.btn_0:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_1:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_2:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_3:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_4:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_5:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_6:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_7:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_8:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_9:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_clear:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_close:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
            case R.id.btn_ok:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.grey_selected_btn);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                }
                break;
        }

        return false;
    }



}
