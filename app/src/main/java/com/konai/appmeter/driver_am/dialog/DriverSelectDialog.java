package com.konai.appmeter.driver_am.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.konai.appmeter.driver_am.R;

public class DriverSelectDialog extends Dialog {

    ImageView addDriverBtn;
    RecyclerView recycler;
    View.OnClickListener addListener;

    public DriverSelectDialog(@NonNull Context context) {
        super(context);
    }

    public DriverSelectDialog(Context context
                            , View.OnClickListener addListener) {
        super(context);
        this.addListener = addListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_driver_check);

        defineVariables();

    }


    private void defineVariables() {

        addDriverBtn = (ImageView) findViewById(R.id.add_driver);
        recycler = (RecyclerView) findViewById(R.id.recycler);

        addDriverBtn.setOnClickListener(addListener);
        recycler.setOnClickListener(clickListener);



    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.add_driver:
                    break;
                case R.id.recycler:
                    break;
            }

        }
    };



}
