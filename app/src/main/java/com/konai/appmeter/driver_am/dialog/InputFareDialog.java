package com.konai.appmeter.driver_am.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.konai.appmeter.driver_am.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class InputFareDialog extends Dialog {

    ArrayList<String> list = new ArrayList<>();
    int index = 0;
    String logtag= "logtag_";
    private TextView title;
    private EditText edit_user, edit_password;
    private Button cancelBtn, okBtn;
    private RadioButton btn_0,
            btn_1,
            btn_2,
            btn_3,
            btn_4,
            btn_5,
            btn_6,
            btn_7,
            btn_8,
            btn_9,
            btn_clear,
            btn_back;

    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;
    private Configuration config;
    private int ori;
    private String calVal;

    public InputFareDialog(@NonNull Context context) {
        super(context);
    }

    public InputFareDialog(@NonNull Context context
                                    , int orientation
                                    , View.OnClickListener okListener
                                    , View.OnClickListener cancelListener) {
        super(context);
        this.ori = orientation;
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.dialog_inputfare_h);
        }else {
            setContentView(R.layout.dialog_inputfare);
        }


        defineVariables();


    }//onCreate

    private void defineVariables() {

        title = (TextView) findViewById(R.id.title);
        edit_user = (EditText) findViewById(R.id.edit_user);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        okBtn = (Button) findViewById(R.id.btn_ok);

        okBtn.setOnClickListener(okListener);
        cancelBtn.setOnClickListener(cancelListener);

        btn_0 = (RadioButton) findViewById(R.id.btn_0);
        btn_1 = (RadioButton) findViewById(R.id.btn_1);
        btn_2 = (RadioButton) findViewById(R.id.btn_2);
        btn_3 = (RadioButton) findViewById(R.id.btn_3);
        btn_4 = (RadioButton) findViewById(R.id.btn_4);
        btn_5 = (RadioButton) findViewById(R.id.btn_5);
        btn_6 = (RadioButton) findViewById(R.id.btn_6);
        btn_7 = (RadioButton) findViewById(R.id.btn_7);
        btn_8 = (RadioButton) findViewById(R.id.btn_8);
        btn_9 = (RadioButton) findViewById(R.id.btn_9);
        btn_clear = (RadioButton) findViewById(R.id.btn_clear);
        btn_back = (RadioButton) findViewById(R.id.btn_back);

        btn_0.setOnClickListener(mCalculatorListener);
        btn_1.setOnClickListener(mCalculatorListener);
        btn_2.setOnClickListener(mCalculatorListener);
        btn_3.setOnClickListener(mCalculatorListener);
        btn_4.setOnClickListener(mCalculatorListener);
        btn_5.setOnClickListener(mCalculatorListener);
        btn_6.setOnClickListener(mCalculatorListener);
        btn_7.setOnClickListener(mCalculatorListener);
        btn_8.setOnClickListener(mCalculatorListener);
        btn_9.setOnClickListener(mCalculatorListener);
        btn_back.setOnClickListener(mCalculatorListener);
        btn_clear.setOnClickListener(mCalculatorListener);

    }


    //계산기 버튼 클릭리스너
    private View.OnClickListener mCalculatorListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i = 0; i<1; i++){

                index = i;

                switch (v.getId()){
                    case R.id.btn_0:
//                        list.add(i, "0");  //거꾸로 삽입됌
                        list.add("0");
                        break;
                    case R.id.btn_1:
                        list.add("1");
                        break;
                    case R.id.btn_2:
                        list.add("2");
                        break;
                    case R.id.btn_3:
                        list.add("3");
                        break;
                    case R.id.btn_4:
                        list.add("4");
                        break;
                    case R.id.btn_5:
                        list.add("5");
                        break;
                    case R.id.btn_6:
                        list.add("6");
                        break;
                    case R.id.btn_7:
                        list.add("7");
                        break;
                    case R.id.btn_8:
                        list.add("8");
                        break;
                    case R.id.btn_9:
                        list.add("9");
                        break;
                    case R.id.btn_back: //지우기 버튼
                        try{
                            //마지막값 지우기
                            int lastIndex = list.size() - 1;
//                            Log.d(logtag+"remove_previous_index", lastIndex+"번: "+list.get(lastIndex));

                            if (lastIndex <= 0) //20220303
                            {
                                Log.d(logtag+"last_index", lastIndex+",  값: "+list.get(lastIndex));
                                list.removeAll(list);
                                edit_user.setText("");
                            }else {
                                list.remove(lastIndex);
                            }
                        }catch (Exception e){}
                        break;
                    case R.id.btn_clear: //모두 삭제버튼
//                        list.clear();
                        if (list.size() != 0){
                            list.removeAll(list);
                            Log.d(logtag+"clear", list.size()+"개,  "+list.toString());
                        }
                        break;
                }
            }//for..

            Log.d(logtag+"list_final", list.toString()+",   사이즈: "+list.size());

            try{
                calVal ="";
                int nmanulafare = 0;

                if (list.size() == 0){
                    Log.d("0000000","0000000");
                    edit_user.setText("");
                    calVal ="";
                }else {

                    calVal = TextUtils.join("",list);  //instead of String.join
                    Log.d(logtag+"calVal", calVal);
                    int calValInt = Integer.parseInt(calVal);
                    DecimalFormat format = new DecimalFormat("###,###");
                    String formatVal = format.format(calValInt);
                    edit_user.setText(formatVal);
                    Log.d(logtag+"get_edit_user_1", edit_user.getText().toString());
                }

                Log.d(logtag+"final_calVal!!", calVal+"_!");

                if (calVal.equals(null) || calVal.equals("") || Integer.parseInt(calVal) < 0){
                    nmanulafare = 0;
                }else {
                    nmanulafare = Integer.parseInt(calVal);
                }

            }catch (Exception e){}

        }//onClick..
    };


    public String returnMenualValue() {
        return calVal;
    }

}
