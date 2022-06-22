package com.konai.appmeter.driver_am.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;

public class BasicDialog extends Dialog {

    private FontFitTextView tvMsg;
    private ButtonFitText okBtn, cancelBtn;
    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;

    public BasicDialog(@NonNull Context context) {
        super(context);
    }

    public BasicDialog(@NonNull Context context
                    , View.OnClickListener okListener
                    , View.OnClickListener cancelListener) {
        super(context);
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_basic);

        tvMsg = findViewById(R.id.msg);
        okBtn = findViewById(R.id.okay_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        okBtn.setOnClickListener(clickListener);
        cancelBtn.setOnClickListener(clickListener);
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.okay_btn:
                    break;
                case R.id.cancel_btn:
                    break;
            }
        }
    };
}
