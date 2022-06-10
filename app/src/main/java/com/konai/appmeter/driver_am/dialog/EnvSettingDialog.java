package com.konai.appmeter.driver_am.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.konai.appmeter.driver_am.R;


public class EnvSettingDialog extends Dialog {

    private RelativeLayout ble_layout;

    private RadioButton modem_normal
            , modem_woorinet
            , modem_am
            , ble
            , serial_inabi
            , serial_artview
            , serial_atlan
            , ori_horizontal
            , ori_vertical
            , gubun_personal
            , gubun_corporate;

    private String bleValue, oriValue, gubunValue, modemValue;

    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;





    public EnvSettingDialog(@NonNull Context context
            , View.OnClickListener okListener
            , View.OnClickListener cancelListener) {
        super(context);
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }

    public EnvSettingDialog(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_env_setting);


        modem_normal = (RadioButton)findViewById(R.id.modem_normal);
        modem_woorinet = (RadioButton)findViewById(R.id.modem_woorinet);
        modem_am = (RadioButton)findViewById(R.id.modem_am);
        ble_layout = (RelativeLayout)findViewById(R.id.ble_layout);
        ble = (RadioButton) findViewById(R.id.ble);
        serial_inabi = (RadioButton)findViewById(R.id.serial_inabi);
        serial_artview = (RadioButton)findViewById(R.id.serial_artview);
        serial_atlan = (RadioButton)findViewById(R.id.serial_atlan);
        ori_horizontal = (RadioButton)findViewById(R.id.ori_horizontal);
        ori_vertical = (RadioButton)findViewById(R.id.ori_vertical);
        gubun_personal = (RadioButton)findViewById(R.id.gubun_personal);
        gubun_corporate = (RadioButton)findViewById(R.id.gubun_corporate);

        Button okBtn = (Button)findViewById(R.id.ok_btn);
        Button cancelBtn = (Button)findViewById(R.id.cancel_btn);

        okBtn.setOnClickListener(okListener);  //확인
        cancelBtn.setOnClickListener(cancelListener);  //취소

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()){

                    case R.id.ble:   //블루투스
                        ble.setChecked(true);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(false);
                        ble_layout.setBackgroundResource(R.drawable.yellow_selected_btn);
                        serial_inabi.setBackgroundResource(R.drawable.grey_round_background);
                        serial_artview.setBackgroundResource(R.drawable.grey_round_background);
                        serial_atlan.setBackgroundResource(R.drawable.grey_round_background);
                        bleValue = "true";
                        break;
                    case R.id.serial_inabi:  //시리얼 아이나비
                        ble.setChecked(false);
                        serial_inabi.setChecked(true);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(false);
                        serial_inabi.setBackgroundResource(R.drawable.yellow_selected_btn);
                        ble_layout.setBackgroundResource(R.drawable.grey_round_background);
                        serial_artview.setBackgroundResource(R.drawable.grey_round_background);
                        serial_atlan.setBackgroundResource(R.drawable.grey_round_background);
                        bleValue = "1";
                        break;
                    case R.id.serial_artview:  //시리얼 아트뷰
                        ble.setChecked(false);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(true);
                        serial_atlan.setChecked(false);
                        serial_artview.setBackgroundResource(R.drawable.yellow_selected_btn);
                        ble_layout.setBackgroundResource(R.drawable.grey_round_background);
                        serial_inabi.setBackgroundResource(R.drawable.grey_round_background);
                        serial_atlan.setBackgroundResource(R.drawable.grey_round_background);
                        bleValue = "2";
                        break;
                    case R.id.serial_atlan:  //시리얼 아틀란
//                        Toast.makeText(getContext(), "아틀란 클릭", Toast.LENGTH_SHORT).show();
                        ble.setChecked(false);
                        serial_inabi.setChecked(false);
                        serial_artview.setChecked(false);
                        serial_atlan.setChecked(true);
                        serial_atlan.setBackgroundResource(R.drawable.yellow_selected_btn);
                        ble_layout.setBackgroundResource(R.drawable.grey_round_background);
                        serial_inabi.setBackgroundResource(R.drawable.grey_round_background);
                        serial_artview.setBackgroundResource(R.drawable.grey_round_background);
                        bleValue = "3";
                    case R.id.ori_horizontal:  //가로
                        ori_horizontal.setChecked(true);
                        ori_vertical.setChecked(false);
                        ori_horizontal.setBackgroundResource(R.drawable.grey_round_background);
                        ori_vertical.setBackgroundResource(R.drawable.grey_round_background);
                        oriValue = "1";
                        break;
                    case R.id.ori_vertical:   //세로
                        ori_vertical.setChecked(true);
                        ori_horizontal.setChecked(false);
                        ori_vertical.setBackgroundResource(R.drawable.yellow_selected_btn);
                        ori_horizontal.setBackgroundResource(R.drawable.grey_round_background);
                        oriValue = "2";
                        break;
                    case R.id.gubun_personal:  //개인
                        gubun_personal.setChecked(true);
                        gubun_corporate.setChecked(false);
                        gubun_personal.setBackgroundResource(R.drawable.yellow_selected_btn);
                        gubun_corporate.setBackgroundResource(R.drawable.grey_round_background);
                        gubunValue ="1";
                        break;
                    case R.id.gubun_corporate:  //법인
                        gubun_corporate.setChecked(true);
                        gubun_personal.setChecked(false);
                        gubun_corporate.setBackgroundResource(R.drawable.yellow_selected_btn);
                        gubun_personal.setBackgroundResource(R.drawable.grey_round_background);
                        gubunValue ="2";
                    case R.id.modem_normal:   //모뎀- 일반
                        modem_normal.setChecked(true);
                        modem_woorinet.setChecked(false);
                        modem_am.setChecked(false);
                        modem_normal.setBackgroundResource(R.drawable.yellow_selected_btn);
                        modem_woorinet.setBackgroundResource(R.drawable.grey_round_background);
                        modem_am.setBackgroundResource(R.drawable.grey_round_background);
                        modemValue = "1";
                        break;
                    case R.id.modem_woorinet:  //모뎀- 우리넷
                        modem_normal.setChecked(false);
                        modem_woorinet.setChecked(true);
                        modem_am.setChecked(false);
                        modem_woorinet.setBackgroundResource(R.drawable.yellow_selected_btn);
                        modem_normal.setBackgroundResource(R.drawable.grey_round_background);
                        modem_am.setBackgroundResource(R.drawable.grey_round_background);
                        modemValue = "2";
                        break;
                    case R.id.modem_am:        //모뎀- 에이엠
                        modem_normal.setChecked(false);
                        modem_woorinet.setChecked(false);
                        modem_am.setChecked(true);
                        modem_am.setBackgroundResource(R.drawable.yellow_selected_btn);
                        modem_woorinet.setBackgroundResource(R.drawable.grey_round_background);
                        modem_normal.setBackgroundResource(R.drawable.grey_round_background);
                        modemValue = "3";
                        break;

                }//switch..
            }//onclick..
        };

        modem_normal.setOnClickListener(onClickListener);
        modem_woorinet.setOnClickListener(onClickListener);
        modem_am.setOnClickListener(onClickListener);
        ble.setOnClickListener(onClickListener);
        serial_inabi.setOnClickListener(onClickListener);
        serial_artview.setOnClickListener(onClickListener);
        serial_atlan.setOnClickListener(onClickListener);
        ori_horizontal.setOnClickListener(onClickListener);
        ori_vertical.setOnClickListener(onClickListener);
        gubun_personal.setOnClickListener(onClickListener);
        gubun_corporate.setOnClickListener(onClickListener);
    }//onCreate..






    public String return_blueValue(){
        Log.d("final_bleValue", bleValue);
        return bleValue;
    }

    public String return_oriValue(){
        Log.d("final_orivalue", oriValue);
        return oriValue;
    }

    public String return_gubunValue(){
        Log.d("final_gubunValue", gubunValue);
        return gubunValue;
    }
}
