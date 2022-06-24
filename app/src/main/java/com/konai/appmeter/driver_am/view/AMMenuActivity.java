package com.konai.appmeter.driver_am.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.konai.appmeter.driver_am.adapter.MenuAdapter;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.service.AwindowService;

import java.util.ArrayList;

public class AMMenuActivity extends AppCompatActivity {

    private Context mContext;
    public static AwindowService windowService = null;
    private boolean mIsBound;
    private String log_ = "menuList";

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private ArrayList<String> menuListItems = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ammenu);

        mContext = this;


//        Intent intent = getIntent();
//
//        if (intent.getStringExtra("menuListString") != null) {
//            Log.d("메뉴내용>", intent.getStringExtra("menuListString"));
//        }


        setStartService();

//        windowService.menu_meterState(AMBlestruct.APP_MENU_REQUEST_CODE);

//        menuRecyclerView = findViewById(R.id.menu_recyclerview);
//        menuAdapter = new MenuAdapter(mContext, menuListItems);
    }




    private void setStartService() {
        startService(new Intent(AMMenuActivity.this, AwindowService.class));
        bindService(new Intent(this, AwindowService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void setStopService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        stopService(new Intent(AMMenuActivity.this, AwindowService.class));
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(log_,"onServiceConnected"); //y

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };






}