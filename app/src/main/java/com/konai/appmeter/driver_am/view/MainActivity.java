package com.konai.appmeter.driver_am.view;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver_am.adapter.MenuAdapter;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.dialog.BasicDialog;
import com.konai.appmeter.driver_am.handler.BackPressedKeyHandler;
import com.konai.appmeter.driver_am.service.AwindowService;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.PermissionSupoort;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.util.MyTouchListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BackPressedKeyHandler backKeyHandler = new BackPressedKeyHandler(this, windowService);
    private PermissionSupoort mPermission;
    private AMBluetoothManager amBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice = null;
    private String mBluetoothAddress = "";
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int ACCESS_FINE_LOCATION_DENIED = 10;
    private static final int BLUETOOTH_CONNECT_DENIED = 50;
    private static final int REQUEST_ENABLE_BT = 30;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    public static AwindowService windowService = null;
    private String log_ = "log_mainActivity";
    private Context mContext;
    int icon_x = -1, icon_y = -1, icon_w = 300, icon_h = 138;
    private MyTouchListener mTouchListener;
    private View viewframe1, viewframe2, viewframe3, viewframe4, viewframe5;
    private FrameLayout frame1, frame2, frame3, frame4, frame5;
    private LinearLayout main_layout, menu_layout, menuNumberPadLayout, menu_main_layout, main_all_layout, menu_info_layout, main_btn_layout, number_pad_layout, add_fare_frame_layout, number_pad_frame_layout, menu_list_layout;
    /* ???????????? ???????????? */
    private int mlength = 0;
    ArrayList<String> addFareList; //???????????? ?????????
    private String addFareVal = ""; //???????????? val
    ArrayList<String> attendanceList; //???????????? ?????????
    private String attendanceVal = ""; //???????????? val
    private ButtonFitText btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_clear, btn_close, btn_ok;
    /* ?????? ???????????? */
    ArrayList<String> menuInputList = new ArrayList<>();
    private String menuInputVal = "";
    private ButtonFitText m_btn_0, m_btn_1, m_btn_2, m_btn_3, m_btn_4, m_btn_5, m_btn_6, m_btn_7, m_btn_8, m_btn_9, m_btn_clear, m_btn_close, m_btn_ok;
    private FontFitTextView menu_input_text, add_fare_text, add_fare_title, add_fare_value_text;
    private EditText menu_input_edit_text, add_fare_edit_text;
    private MenuAdapter menuAdapter;
    private RecyclerView menuRecyclerView;
    private TextView menu_text;
    ArrayList<String> menuList;
    private ButtonFitText btn_menu, btn_receipt, btn_reserve, btn_attendance, btn_dayoff, btn_add_pay, close_menu_btn, back_menu_btn;
    private ButtonFitText btn_empty, btn_drive, btn_call, btn_pay;
    private ImageView iv_ble;
    private FontFitTextView fareRate, tv_night_status, tv_complex, tv_suburb_rate, emptybox, tv_add_pay, tv_rescall_pay, tv_total_pay, driver_id_text, btn_main_status, btn_sub_status, menu_title;
    private Boolean isDrivedClicked = false, isPayClicked = false, isCloseBtnCliked = false;


    private AwindowService.mainCallBack mCallback = new AwindowService.mainCallBack() {
        @Override
        public void changedDriverId(String driverID) {
            if (driverID != null) {
                if (!driverID.equals("")) {
                    driver_id_text.setText(driverID);
                }
            }
        }

        @Override
        public void serviceBleStatus(boolean bleStatus) {
            //???????????? ????????? ??????
            setBluetoothIconChanged(bleStatus);
        }


        @Override
        public void serviceMeterState( String driverId
                                    , int btnType
                                    , int mFare
                                    , int startFare
                                    , int callFare
                                    , int etcFare
                                    , int nightFare
                                    , int complexFare
                                    , int suburbFare
                                    , int suburbFareRate) {

            AMBlestruct.AMReceiveFare.M_CALL_FARE = callFare + "";

            if (!driverId.equals("")) {
                driver_id_text.setText(driverId);
            }

            //????????????
            if (!AMBlestruct.AMReceiveFare.M_CALL_FARE.equals("0")) {
                tv_rescall_pay.setVisibility(View.VISIBLE);
                tv_rescall_pay.setText("?????? " + AMBlestruct.AMReceiveFare.M_CALL_FARE);
            } else {
                tv_rescall_pay.setVisibility(View.GONE);
            }

            //????????????
            if (etcFare > 0) {
                tv_add_pay.setVisibility(View.VISIBLE);

                if (btnType == 02) {
                    tv_add_pay.setText("?????? "+etcFare);
                }else {
                    tv_add_pay.setText("?????? "+etcFare);
                }

//                Log.d("addfarelist", addFareList.toString());
            }else {
                tv_add_pay.setVisibility(View.GONE);
            }


            //????????????
            if (suburbFare == 0) {
                tv_suburb_rate.setVisibility(View.GONE);
                emptybox.setVisibility(View.VISIBLE);
            }else {
                tv_suburb_rate.setVisibility(View.VISIBLE);
                emptybox.setVisibility(View.GONE);
            }

            //????????????
            if (nightFare == 0) {
                tv_night_status.setVisibility(View.GONE);
                emptybox.setVisibility(View.VISIBLE);
            }else {
                tv_night_status.setVisibility(View.VISIBLE);
                emptybox.setVisibility(View.GONE);
            }


            //????????????
            if (complexFare == 0) {
                tv_complex.setVisibility(View.GONE);
                emptybox.setVisibility(View.VISIBLE);
            }else {
                tv_complex.setVisibility(View.VISIBLE);
                emptybox.setVisibility(View.GONE);
            }

            //?????????
            if (suburbFareRate > 0) {
                fareRate.setVisibility(View.VISIBLE);
                fareRate.setText(suburbFareRate+"%");
            }else {
                fareRate.setVisibility(View.GONE);
            }


            long value = Long.parseLong(mFare +"");
            DecimalFormat format = new DecimalFormat("###,###");

            //me: ????????? ?????? ??????  ***************************
            display_Runstate(format.format(value));


            if (btnType == 01) {
                isPayClicked = true;
                btn_dayoff.setText("????????????");
                btn_main_status.setText("??????");
                btn_main_status.setTextColor(getResources().getColor(R.color.orange));

            } else if (btnType == 05) {
                isDrivedClicked = false;
                isPayClicked = false;
//                Log.d("buttonType!!",btnType+"");
                main_all_layout.setVisibility(View.VISIBLE);
                main_layout.setVisibility(View.VISIBLE);
                btn_main_status.setText("??????");
                btn_main_status.setTextColor(getResources().getColor(R.color.white));
                tv_add_pay.setVisibility(View.GONE);
                menu_main_layout.setVisibility(View.GONE);
                menuNumberPadLayout.setVisibility(View.GONE);
                menuInputList.removeAll(menuInputList);
                tv_night_status.setVisibility(View.GONE);
                tv_complex.setVisibility(View.GONE);
                btn_dayoff.setText("??????");

            } else if (btnType == 20) {
                isDrivedClicked = true;
                btn_main_status.setText("??????");
                btn_main_status.setTextColor(getResources().getColor(R.color.yellow));
            } else if (btnType == AMBlestruct.MeterState.CALL) {  //???????????? ????????? ?????????.. i don't know why.
                btn_main_status.setText("??????");
                tv_rescall_pay.setVisibility(View.VISIBLE);
                tv_rescall_pay.setText(AMBlestruct.AMReceiveFare.M_CALL_FARE);
            } else if (btnType == 6) { //??????
                btn_main_status.setText("??????");
                btn_main_status.setTextColor(getResources().getColor(R.color.blue));
            } else if (btnType == 7) { //??????
                btn_main_status.setText("??????");
                btn_main_status.setTextColor(getResources().getColor(R.color.green));
            }
        }

        //?????? ????????? ????????????.. -> windowService??? ?????????
        @Override
        public void serviceMeterMenuState(String menuMsg, int menuType) {
            
            //??????????????? ?????? -> ?????? ????????? = ???????????????
            if (menuType == 0) {   //0-??????/1-??????/2-????????????/3-????????????+??????
                Log.d("menuType", menuType+"");
                menuNumberPadLayout.setVisibility(View.GONE);
                menuInputList.removeAll(menuInputList);
                menu_layout.setVisibility(View.GONE);
                main_all_layout.setVisibility(View.VISIBLE);
                main_layout.setVisibility(View.VISIBLE); //????????????
                menu_main_layout.setVisibility(View.GONE);
                return;
            }

            //????????? ????????? ?????? ?????????
            menuList = new ArrayList<>(Arrays.asList(menuMsg.split("\n")));
            Log.d("menuType_menuList", menuList.toString());

            menuAdapter = new MenuAdapter(MainActivity.this, menuList);

            menuAdapter.menuTitleTouch(menuList.get(0));

            menuRecyclerView.setAdapter(menuAdapter);

            if (menuList.size() == 1) {
                //?????? ???????????? 1?????? -> ???????????? (ex; [????????????])
                //?????? ????????? ??????
                //?????????????????? ?????????
                menuInputList.removeAll(menuInputList);  //?????????
                menu_input_edit_text.setText("");
                menuNumberPadLayout.setVisibility(View.VISIBLE);


//                ???????????? ????????? ??????????????? ????????? "47" --> ?????????(menuNumberPadLayout)?????? ?????????...
            }else {

                menuNumberPadLayout.setVisibility(View.GONE);
                menuInputList.removeAll(menuInputList);

                //??????????????? ???????????????
                menuAdapter.setmClickListener(new MenuAdapter.onItemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos, String numType) {
                        //me: ??? -> ?????????
                        // ???????????? ????????? ?????????????????????..

                        AMBlestruct.MenuType.MENU_CONTENT = pos + "";
                        AMBlestruct.MenuType.MENU_NUMTYPE = numType + "";


                        switch (numType) {
                            case "0":  //????????? ?????????
                                AMBlestruct.MenuType.MENU_CONTENT = pos + "";
                                windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE, "0", AMBlestruct.MenuType.MENU_CONTENT, AMBlestruct.MenuType.MENU_NUMTYPE);  //"43", ??????????????????
                                break;
                            case "1":  //????????????
                                AMBlestruct.MenuType.MENU_CONTENT = numType + "";
                                windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE, "0", AMBlestruct.MenuType.MENU_CONTENT, AMBlestruct.MenuType.MENU_NUMTYPE);  //"43", ??????????????????
                                break;
                            case "2":  //????????????
                                AMBlestruct.MenuType.MENU_CONTENT = numType + "";
                                windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE, "0", AMBlestruct.MenuType.MENU_CONTENT, AMBlestruct.MenuType.MENU_NUMTYPE);  //"43", ??????????????????
                                break;
                        }


                    }
                });
                //??????????????? ???????????????
                menuAdapter.setmTouchListener(new MenuAdapter.onItemTouchListener() {
                    @Override
                    public void onItemTouch(View v, int pos, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            v.setBackgroundResource(R.drawable.yellow_selected_btn);
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            v.setBackgroundColor(getResources().getColor(R.color.main_background));
                        }
                    }
                });
            }


        }
    };



    public void setBluetoothIconChanged(boolean ble) {
        if (ble == true) {
//            Log.d("???????????????", "ble connected");
            iv_ble.setBackgroundResource(R.drawable.bluetooth_green);
            Toast.makeText(MainActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();

        } else {
            iv_ble.setBackgroundResource(R.drawable.bluetooth_blue);
            Toast.makeText(MainActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
            iv_ble.setBackgroundResource(R.drawable.bluetooth_blue);
        }
    }


    //??? ??????
    public void display_Runstate(String mFare) {
//        Log.d("tv_total_pay:??????", mFare);
        tv_total_pay.setText(mFare);
    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(log_, "service connected");
            AwindowService.ServiceBinder binder = (AwindowService.ServiceBinder) iBinder;
            windowService = binder.getService();
            windowService.registerCallback(mCallback);

            if (windowService == null) {
                Log.d(log_, "service null");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(log_, "service disconnected");
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //???????????? ??????????????? ??????
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        goMain();

//        loadOverlayIconInfo();

    }//onCreate



    private void goMain() {
        //????????????
        locationPermission();

        //?????????????????? ???
        oerlayPermission();

        //??? ????????? ??????
        viewFrameVariablesConfiguration();
    }


    private void loadOverlayIconInfo() {
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        icon_x = pref.getInt("icon_x", -1);
        icon_y = pref.getInt("icon_y", -1);
        icon_w = pref.getInt("icon_w", 300);
        icon_h = pref.getInt("icon_h", 138);

    }


    @Override
    public void onBackPressed() {
        if (menu_main_layout.getVisibility() == View.VISIBLE) {
            Toast.makeText(MainActivity.this, "??????????????? ?????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
        }else if (number_pad_frame_layout.getVisibility() == View.VISIBLE){
            Toast.makeText(MainActivity.this, "??????????????? ?????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
        }else {
            backKeyHandler.onBackPressed();
            stopService(new Intent(MainActivity.this, AwindowService.class));
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        setting.OVERLAY = true;

        if (windowService != null) {
            Log.d("overlaycheck","not null");
            windowService.set_meterhandler.sendEmptyMessage(100);
        }else {
            Log.d("overlaycheck","null");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();


        //31 ??????
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            Log.d("versionCheck","31??????");
//            if (mBluetoothAdapter.isEnabled()) {
//                Log.d("versionCheck","bluetooth enable");
//                bleConnPermission();
//            }else {
//                Log.d("versionCheck","bluetooth not enable");
//                bleConnPermission();
//            }
//
//        }else {
//            Log.d("versionCheck","31?????? ??????");
//            if (mBluetoothAdapter.isEnabled()) {
//                Log.d("versionCheck","bluetooth enable");
//                //do nothing
//
//            }else {
//                Log.d("versionCheck","bluetooth not enable");
//                enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//            }
//        }


        bleConnPermission();




            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (windowService != null) {
            Log.d(log_, "windowService not NULL!!");
            setting.OVERLAY = false;
            windowService.set_meterhandler.sendEmptyMessage(100);
//            windowService.connectAM();
        }else {
            Log.d(log_, "windowService NULL!!"); //null
        }


    }

    //??????????????? ?????? ??????????????? ?????? (Location permission)
    public void locationPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //23
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{permission}, ACCESS_FINE_LOCATION_DENIED);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void bleConnPermission() {
        Log.d("bleconn", "bleconn");
        String ble_conn_permission = Manifest.permission.BLUETOOTH_CONNECT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //31
            Log.d("bleconn--",Build.VERSION.SDK_INT+"");
            Log.d("bleconn", "bleconn_lollipop");
            if (checkSelfPermission(ble_conn_permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{ble_conn_permission}, BLUETOOTH_CONNECT_DENIED);
            }
        }else {
            //if the api version is lower than 31 --> do nothing
        }
    }

    public void oerlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // ??????????????? ????????? ??????
            if (!Settings.canDrawOverlays(this)) {              // ??????

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.info_permission_overlay))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.setting_dialog_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + getPackageName()));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                startWindowService();
            }
        } else
            startWindowService();
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            switch (requestCode) {

                //???????????? ????????? ???????????????
                case ACCESS_FINE_LOCATION_DENIED:
                    Toast.makeText(mContext, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    break;

                case BLUETOOTH_CONNECT_DENIED:
                    Toast.makeText(mContext, "???????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    break;

                //???????????? ?????????
                case REQUEST_ENABLE_BT:
                    if (resultCode == RESULT_OK) {
//                        Toast.makeText(mContext, "???????????? ?????????", Toast.LENGTH_SHORT).show();
                        if (windowService != null) {
                            windowService.setBleScan();
                        }

//                        IntentFilter filter = new IntentFilter();
//                        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//                        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//                        registerReceiver(mReceiver, filter);


                    } else if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(mContext, "???????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                //?????????????????? ????????? ???????????? ???
                case ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE:
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        finish();
                    } else {
                        startWindowService();
                    }
                    break;
            }
        }


    }


    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d("mReceiver", "connected");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("mReceiver_device", device.getName() + "> " + device.getAddress());

//                connectAM();

            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d("mReceiver", "disconnected");
            }
        }
    };


    //me: ????????????????????? ??????????????? ??? ???????????? ????????????..
    void startWindowService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(windowService == null) {
                bindService(new Intent(getApplicationContext(),
                        AwindowService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }

            startForegroundService(new Intent(MainActivity.this, AwindowService.class));
        } else {
            startService(new Intent(MainActivity.this, AwindowService.class));
        }
    }





    private void viewFrameVariablesConfiguration() {

        viewframe1 = null;
        viewframe2 = null;
        viewframe3 = null;
        viewframe4 = null;
        viewframe5 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1);
        frame2 = (FrameLayout) findViewById(R.id.frame2);
        frame3 = (FrameLayout) findViewById(R.id.frame3);
        frame4 = (FrameLayout) findViewById(R.id.frame4);
        frame5 = (FrameLayout) findViewById(R.id.frame5);

        LayoutInflater inflater = null;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewframe1 = inflater.inflate(R.layout.mainframe1, frame1,true);  //????????????
        viewframe2 = inflater.inflate(R.layout.mainbuttonframe2, frame2, true); //??????????????????
        viewframe3 = inflater.inflate(R.layout.mainbuttonframe3, frame3, true); //??????????????????

        viewframe4 = inflater.inflate(R.layout.mainframe1, frame4);   //???????????? ??????
        viewframe5 = inflater.inflate(R.layout.numberpadframe5, frame5, true);  //??????????????????

        /*?????????????????? frame1*/
        iv_ble = (ImageView) viewframe1.findViewById(R.id.nbtn_connectble);
        driver_id_text = (FontFitTextView) viewframe1.findViewById(R.id.dirver_id_Text);
        btn_main_status = (FontFitTextView) viewframe1.findViewById(R.id.nbtn_main_status);
        btn_sub_status  = (FontFitTextView) viewframe1.findViewById(R.id.nbtn_main_sub_status);
        tv_night_status = (FontFitTextView) viewframe1.findViewById(R.id.ntv_status);
        fareRate = (FontFitTextView) viewframe1.findViewById(R.id.fareRate);
        tv_complex = (FontFitTextView) viewframe1.findViewById(R.id.ntv_complex);
        emptybox = (FontFitTextView) viewframe1.findViewById(R.id.emptybox);
        tv_add_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_addpayment);
        tv_rescall_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_rescallpay);
        tv_suburb_rate = (FontFitTextView) viewframe1.findViewById(R.id.ntv_suburb_rate);
        tv_total_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_totalpay);
        menu_title = (FontFitTextView) viewframe1.findViewById(R.id.menu_title);
        menu_list_layout = (LinearLayout) viewframe1.findViewById(R.id.layoutlist);
//        menuRecyclerView = (RecyclerView) viewframe1.findViewById(R.id.menuRecyclerView);
        menuRecyclerView = (RecyclerView) findViewById(R.id.menuRecyclerView);
        menu_text = (TextView) findViewById(R.id.menu_text);
        main_layout = viewframe1.findViewById(R.id.mainframe1_layout);
        menu_layout = viewframe1.findViewById(R.id.menu_frame_layout);
        menu_main_layout = (LinearLayout) findViewById(R.id.menu_main_layout);
//        radio_button_layout = (LinearLayout) findViewById(R.id.radio_button_layout);
        main_all_layout = (LinearLayout) findViewById(R.id.main_all_layout);
        menu_info_layout = (LinearLayout) findViewById(R.id.main_all_layout);


        /*???????????? frame2*/
        btn_empty = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_emptycar);
        btn_drive = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_drivestart);
        btn_call = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_call);
        btn_pay = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_driveend);
        main_btn_layout = (LinearLayout) viewframe2.findViewById(R.id.main_btn_layout);

        /*???????????? frame3*/
        btn_menu = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_menu);
        btn_receipt = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_receipt);
        btn_attendance = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_attendance);
        btn_reserve = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_reserve);
        btn_add_pay = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_addpayment);
        btn_dayoff = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_dayoff);

        /*???????????? frame4 & frame5*/
        number_pad_layout = (LinearLayout) findViewById(R.id.number_pad_layout);
        number_pad_frame_layout = (LinearLayout) findViewById(R.id.number_pad_frame_layout);
        add_fare_frame_layout = viewframe4.findViewById(R.id.add_fare_frame_layout);
//        add_fare_text = (FontFitTextView) viewframe4.findViewById(R.id.add_fare_text);
//        add_fare_edit_text = (EditText) viewframe4.findViewById(R.id.add_fare_edit_text);
        add_fare_value_text = (FontFitTextView) viewframe4.findViewById(R.id.add_fare_value_text);
        add_fare_title = (FontFitTextView) viewframe4.findViewById(R.id.add_fare_title);
        btn_0 = (ButtonFitText) viewframe5.findViewById(R.id.btn_0);
        btn_1 = (ButtonFitText) viewframe5.findViewById(R.id.btn_1);
        btn_2 = (ButtonFitText) viewframe5.findViewById(R.id.btn_2);
        btn_3 = (ButtonFitText) viewframe5.findViewById(R.id.btn_3);
        btn_4 = (ButtonFitText) viewframe5.findViewById(R.id.btn_4);
        btn_5 = (ButtonFitText) viewframe5.findViewById(R.id.btn_5);
        btn_6 = (ButtonFitText) viewframe5.findViewById(R.id.btn_6);
        btn_7 = (ButtonFitText) viewframe5.findViewById(R.id.btn_7);
        btn_8 = (ButtonFitText) viewframe5.findViewById(R.id.btn_8);
        btn_9 = (ButtonFitText) viewframe5.findViewById(R.id.btn_9);
        btn_clear = (ButtonFitText) viewframe5.findViewById(R.id.btn_clear);
        btn_close = (ButtonFitText) viewframe5.findViewById(R.id.btn_close);
        btn_ok = (ButtonFitText) viewframe5.findViewById(R.id.btn_ok);

        btn_0.setOnClickListener(numberPadClickListener);
        btn_1.setOnClickListener(numberPadClickListener);
        btn_2.setOnClickListener(numberPadClickListener);
        btn_3.setOnClickListener(numberPadClickListener);
        btn_4.setOnClickListener(numberPadClickListener);
        btn_5.setOnClickListener(numberPadClickListener);
        btn_6.setOnClickListener(numberPadClickListener);
        btn_7.setOnClickListener(numberPadClickListener);
        btn_8.setOnClickListener(numberPadClickListener);
        btn_9.setOnClickListener(numberPadClickListener);
        btn_clear.setOnClickListener(numberPadClickListener);
        btn_close.setOnClickListener(numberPadClickListener);
        btn_ok.setOnClickListener(numberPadClickListener);

        mTouchListener = new MyTouchListener();

        btn_0.setOnTouchListener(mTouchListener);
        btn_1.setOnTouchListener(mTouchListener);
        btn_2.setOnTouchListener(mTouchListener);
        btn_3.setOnTouchListener(mTouchListener);
        btn_4.setOnTouchListener(mTouchListener);
        btn_5.setOnTouchListener(mTouchListener);
        btn_6.setOnTouchListener(mTouchListener);
        btn_7.setOnTouchListener(mTouchListener);
        btn_8.setOnTouchListener(mTouchListener);
        btn_9.setOnTouchListener(mTouchListener);
        btn_clear.setOnTouchListener(mTouchListener);
        btn_close.setOnTouchListener(mTouchListener);
        btn_ok.setOnTouchListener(mTouchListener);



        iv_ble.setOnClickListener(mainBtnClickListener);
        btn_main_status.setOnClickListener(mainBtnClickListener);
        tv_night_status.setOnClickListener(mainBtnClickListener);
        tv_add_pay.setOnClickListener(mainBtnClickListener);
        tv_rescall_pay.setOnClickListener(mainBtnClickListener);
        tv_total_pay.setOnClickListener(mainBtnClickListener);

        mTouchListener = new MyTouchListener();
        btn_empty.setOnTouchListener(mTouchListener);
        btn_empty.setOnTouchListener(mTouchListener);
        btn_drive.setOnTouchListener(mTouchListener);
        btn_call.setOnTouchListener(mTouchListener);
        btn_pay.setOnTouchListener(mTouchListener);
        btn_empty.setOnClickListener(mainBtnClickListener);
        btn_drive.setOnClickListener(mainBtnClickListener);
        btn_call.setOnClickListener(mainBtnClickListener);
        btn_pay.setOnClickListener(mainBtnClickListener);
        btn_receipt.setOnClickListener(mainBtnClickListener);

        btn_menu.setOnTouchListener(mTouchListener);
        btn_receipt.setOnTouchListener(mTouchListener);
        btn_reserve.setOnTouchListener(mTouchListener);
        btn_attendance.setOnTouchListener(mTouchListener);
        btn_add_pay.setOnTouchListener(mTouchListener);
        btn_dayoff.setOnTouchListener(mTouchListener);
        btn_menu.setOnClickListener(mainBtnClickListener);
        btn_reserve.setOnClickListener(mainBtnClickListener);
        btn_attendance.setOnClickListener(mainBtnClickListener);
        btn_add_pay.setOnClickListener(mainBtnClickListener);
        btn_dayoff.setOnClickListener(mainBtnClickListener);


        /* menu_main_layout ?????? */
        /* ????????? ?????? */
        close_menu_btn = (ButtonFitText) findViewById(R.id.close_menu_btn);
        back_menu_btn = (ButtonFitText) findViewById(R.id.back_menu_btn);
        close_menu_btn.setOnClickListener(menuBtnClickListener);
        back_menu_btn.setOnClickListener(menuBtnClickListener);

        /*activity_main ??? ??????
        ?????? ???????????? ????????????*/
        menuNumberPadLayout = (LinearLayout) findViewById(R.id.menuNumberPadLayout);
        menu_input_text = (FontFitTextView) findViewById(R.id.menu_input_text);
        menu_input_edit_text = (EditText) findViewById(R.id.menu_input_edit_text);
        m_btn_0 = (ButtonFitText) findViewById(R.id.m_btn_0);
        m_btn_1 = (ButtonFitText) findViewById(R.id.m_btn_1);
        m_btn_2 = (ButtonFitText) findViewById(R.id.m_btn_2);
        m_btn_3 = (ButtonFitText) findViewById(R.id.m_btn_3);
        m_btn_4 = (ButtonFitText) findViewById(R.id.m_btn_4);
        m_btn_5 = (ButtonFitText) findViewById(R.id.m_btn_5);
        m_btn_6 = (ButtonFitText) findViewById(R.id.m_btn_6);
        m_btn_7 = (ButtonFitText) findViewById(R.id.m_btn_7);
        m_btn_8 = (ButtonFitText) findViewById(R.id.m_btn_8);
        m_btn_9 = (ButtonFitText) findViewById(R.id.m_btn_9);
        m_btn_clear = (ButtonFitText) findViewById(R.id.m_btn_clear);
        m_btn_close = (ButtonFitText) findViewById(R.id.m_btn_close);
        m_btn_ok = (ButtonFitText) findViewById(R.id.m_btn_ok);

        m_btn_0.setOnClickListener(menuBtnClickListener);
        m_btn_1.setOnClickListener(menuBtnClickListener);
        m_btn_2.setOnClickListener(menuBtnClickListener);
        m_btn_3.setOnClickListener(menuBtnClickListener);
        m_btn_4.setOnClickListener(menuBtnClickListener);
        m_btn_5.setOnClickListener(menuBtnClickListener);
        m_btn_6.setOnClickListener(menuBtnClickListener);
        m_btn_7.setOnClickListener(menuBtnClickListener);
        m_btn_8.setOnClickListener(menuBtnClickListener);
        m_btn_9.setOnClickListener(menuBtnClickListener);
        m_btn_clear.setOnClickListener(menuBtnClickListener);
        m_btn_close.setOnClickListener(menuBtnClickListener);
        m_btn_ok.setOnClickListener(menuBtnClickListener);

        close_menu_btn.setOnTouchListener(mTouchListener);
        back_menu_btn.setOnTouchListener(mTouchListener);
        m_btn_0.setOnTouchListener(mTouchListener);
        m_btn_1.setOnTouchListener(mTouchListener);
        m_btn_2.setOnTouchListener(mTouchListener);
        m_btn_3.setOnTouchListener(mTouchListener);
        m_btn_4.setOnTouchListener(mTouchListener);
        m_btn_5.setOnTouchListener(mTouchListener);
        m_btn_6.setOnTouchListener(mTouchListener);
        m_btn_7.setOnTouchListener(mTouchListener);
        m_btn_8.setOnTouchListener(mTouchListener);
        m_btn_9.setOnTouchListener(mTouchListener);
        m_btn_clear.setOnTouchListener(mTouchListener);
        m_btn_close.setOnTouchListener(mTouchListener);
        m_btn_ok.setOnTouchListener(mTouchListener);



    }//viewFrameVariablesConfiguration




    //Activity ????????? ????????? ??????
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

    }


    private void add_num(String val) {

        if (add_fare_title.getText().toString().equals("????????? ???????????????")) {
            mlength = 4;
            if (addFareList.size() >= mlength){  //?????????
                return;
            }
            addFareList.add(val);

        }else if (add_fare_title.getText().toString().equals("???????????? ??????")){

            mlength = 6;
            if (addFareList.size() >= mlength){  //????????????
                return;
            }
            addFareList.add(val);
        }
    }


    private View.OnClickListener numberPadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i=0; i<1; i++) {

                switch (v.getId()) {

                    case R.id.btn_0:
                        add_num("0");
                        break;
                    case R.id.btn_1:
                        add_num("1");
                        break;
                    case R.id.btn_2:
                        add_num("2");
                        break;
                    case R.id.btn_3:
                        add_num("3");
                        break;
                    case R.id.btn_4:
                        add_num("4");
                        break;
                    case R.id.btn_5:
                        add_num("5");
                        break;
                    case R.id.btn_6:
                        add_num("6");
                        break;
                    case R.id.btn_7:
                        add_num("7");
                        break;
                    case R.id.btn_8:
                        add_num("8");
                        break;
                    case R.id.btn_9:
                        add_num("9");
                        break;
                    case R.id.btn_clear:  //??????
                        try {
                            isCloseBtnCliked = false;
                            int lastIndex = addFareList.size() - 1;
                            if (lastIndex <= 0) {
                                addFareList.removeAll(addFareList);
                                add_fare_edit_text.setText("");
                                add_fare_value_text.setText("");
                            }else {
                                addFareList.remove(lastIndex);
                            }
                        }catch (Exception e){

                        }
                        break;
                    case R.id.btn_close:
//                        addFareList = new ArrayList<>();
//                        addFareVal = "";
//                        if (addFareList.size() >= 0) {
//                            addFareList.clear();
//                        }
//                        tv_add_pay.setText(addFareVal);
                        isCloseBtnCliked = true;

                        Toast.makeText(mContext, "close", Toast.LENGTH_SHORT).show();

                        number_pad_layout.setVisibility(View.VISIBLE);
                        main_layout.setVisibility(View.VISIBLE);
                        menu_layout.setVisibility(View.GONE);
                        number_pad_frame_layout.setVisibility(View.GONE);
                        add_fare_frame_layout.setVisibility(View.GONE);
                        add_fare_value_text.setVisibility(View.VISIBLE);
                        break;

                    case R.id.btn_ok: // ??????
                        isCloseBtnCliked = false;
                        if (add_fare_title.getText().toString().equals("???????????? ??????")) {
                            if (addFareVal.length() < 6) {
                                if (addFareVal.length() > 1) {
                                    if (addFareVal.length() == 2) {  //??????????????? ??? ????????? ???????????? 0 ??????.. ??????...
                                        addFareVal = "0000"+addFareVal;
                                    }else if (addFareVal.length() == 3) {
                                        addFareVal = "000"+addFareVal;
                                    }else if (addFareVal.length() == 4) {
                                        addFareVal = "00"+addFareVal;
                                    }else if (addFareVal.length() == 5) {
                                        addFareVal = "0"+addFareVal;
                                    }
                                    windowService.add_fareState("20",addFareVal);
                                    number_pad_layout.setVisibility(View.VISIBLE);
                                    main_layout.setVisibility(View.VISIBLE);
                                    menu_layout.setVisibility(View.GONE);
                                    number_pad_frame_layout.setVisibility(View.GONE);
                                    add_fare_frame_layout.setVisibility(View.GONE);
                                    add_fare_value_text.setVisibility(View.VISIBLE);
                                }else {
                                    Toast.makeText(mContext, "1???????????? ???????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                windowService.add_fareState("20",addFareVal);
                                number_pad_layout.setVisibility(View.VISIBLE);
                                main_layout.setVisibility(View.VISIBLE);
                                menu_layout.setVisibility(View.GONE);
                                number_pad_frame_layout.setVisibility(View.GONE);
                                add_fare_frame_layout.setVisibility(View.GONE);
                                add_fare_value_text.setVisibility(View.VISIBLE);
                            }

                            addFareVal = "";
                            addFareList.removeAll(addFareList);
                            addFareList.clear();


                        }else if (add_fare_title.getText().toString().equals("????????? ???????????????")) {
                            if (add_fare_value_text.getText().toString().length() < 4) {
                                Toast.makeText(mContext, "4???????????? ???????????????", Toast.LENGTH_SHORT).show();
                            }else {
                                number_pad_layout.setVisibility(View.VISIBLE);
                                main_layout.setVisibility(View.VISIBLE);
                                menu_layout.setVisibility(View.GONE);
                                number_pad_frame_layout.setVisibility(View.GONE);
                                add_fare_frame_layout.setVisibility(View.GONE);
                                add_fare_value_text.setVisibility(View.VISIBLE);
                                windowService.check_attendance("20", addFareVal);
                            }
                        }
                        break;
                }//switch

            }//for

            Log.d("keypad_list_final", addFareList.toString()+",   ?????????: "+addFareList.size());


            try {
                if (addFareList.size() == 0){
                    addFareVal = "";
                    add_fare_value_text.setText("0");
                }else {


                    addFareVal = TextUtils.join("", addFareList);


                    if (add_fare_title.getText().toString().equals("???????????? ??????")) {
                        if (addFareList.get(0).equals("0")) {
                            Toast.makeText(mContext, "?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                            addFareList.clear();
                        }else {

                            add_fare_value_text.setText(addFareVal);

//                            tv_add_pay.setText(addFareVal);

                            if (addFareVal.length() >= 0){
//                                tv_add_pay.setText("^_^");
                            }else {
                                Log.d("??????_1", addFareVal);
                                tv_add_pay.setText(addFareVal);
                            }
                        }
                    }else {

                        Log.d("??????_2", addFareVal);
                        add_fare_value_text.setText(addFareVal);

                        if (addFareVal.length() >= 0){
                            tv_add_pay.setText("");
                        }else {
                            tv_add_pay.setText(addFareVal);
                        }
                    }


                }

                AMBlestruct.AMReceiveFare.M_DRIVER_ID = addFareVal;
                Log.d("????????????_", addFareVal);

                if (addFareVal.equals(null) || addFareVal.equals("") || Integer.parseInt(addFareVal) < 0) {

                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    };



    //???????????? ???????????????
    private View.OnClickListener menuBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (int i=0; i<1; i++) {

                switch (v.getId()) {

                    case R.id.close_menu_btn:  //??????-????????????
                        //me: ??? -> ?????????
                        AMBlestruct.MenuType.MENU_CONTENT = "2";
                        menuNumberPadLayout.setVisibility(View.GONE);
                        menuInputList.removeAll(menuInputList);
                        windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE,  AMBlestruct.MenuType.MENU_CONTENT,"",""); //??????
                        main_all_layout.setVisibility(View.VISIBLE);
                        menu_main_layout.setVisibility(View.GONE);  //status:  ?????? ??????????????? ??? ?????? ??? ??????..  !!!!!!!!!!!!
                        main_layout.setVisibility(View.VISIBLE);  //????????????
                        break;
                    case R.id.back_menu_btn:
                        //me: ??? -> ?????????
                        AMBlestruct.MenuType.MENU_CONTENT = "1";
                        menuNumberPadLayout.setVisibility(View.GONE);
                        menuInputList.removeAll(menuInputList);
                        windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE,  AMBlestruct.MenuType.MENU_CONTENT,"", ""); //??????(??????)
                        break;
                    /*??????- ???????????? ??????*/
                    case R.id.m_btn_0:
                        menuInputList.add("0");
                        break;
                    case R.id.m_btn_1:
                        menuInputList.add("1");
                        break;
                    case R.id.m_btn_2:
                        menuInputList.add("2");
                        break;
                    case R.id.m_btn_3:
                        menuInputList.add("3");
                        break;
                    case R.id.m_btn_4:
                        menuInputList.add("4");
                        break;
                    case R.id.m_btn_5:
                        menuInputList.add("5");
                        break;
                    case R.id.m_btn_6:
                        menuInputList.add("6");
                        break;
                    case R.id.m_btn_7:
                        menuInputList.add("7");
                        break;
                    case R.id.m_btn_8:
                        menuInputList.add("8");
                        break;
                    case R.id.m_btn_9:
                        menuInputList.add("9");
                        break;
                    case R.id.m_btn_clear:  //??????
                        try {
                            int lastIndex = menuInputList.size() - 1;

                            if (lastIndex <= 0) {
                                menuInputList.removeAll(menuInputList);
                                menu_input_edit_text.setText("");
//                                menu_input_text.setText("");
                            }else {
                                menuInputList.remove(lastIndex);
                            }

                        }catch (Exception e) {

                        }
                        break;
                    case R.id.m_btn_close:
                        menuInputList.removeAll(menuInputList);
                        menuInputVal = "";
//                        menu_input_text.setText("");
                        menu_input_edit_text.setText("");

                        //me: ??? -> ?????????
                        windowService.menu_input_meterState("47", "0", menuInputVal.length(), menuInputVal); //??????/ ??????????????????(0-??????/1-??????)/ ??????????????? ??????/ ????????????

                        //?????? ??????
                        windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE,  "2", "",""); //2-??????

                        menuNumberPadLayout.setVisibility(View.GONE);
                        menuInputList.removeAll(menuInputList);
                        menu_main_layout.setVisibility(View.GONE);
                        main_layout.setVisibility(View.VISIBLE);
                        main_all_layout.setVisibility(View.VISIBLE);

                        break;
                    case R.id.m_btn_ok:
//                        menu_input_text.setText(menuInputVal);
                        if (menuList.contains("12??????")) {
                            if (menuInputVal.length() > 15) {
                                Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                            }
                        }

                        menu_input_edit_text.setText(menuInputVal);
//                        menu_input_text.setText(menuInputVal);
                        //me: ??? -> ?????????
                        windowService.menu_input_meterState("47", "1", menuInputVal.length(), menu_input_text.getText().toString()); //??????/ ??????????????????(0-??????/1-??????)/ ??????????????? ??????/ ????????????
//                        windowService.menu_input_responseState("92");
                        break;
                }//switch
            }//for

//            Log.d("menuInputList", menuInputList.toString());

            try {
                if (menuInputList.size() == 0) {
                    //0
                }else {
                    menuInputVal = TextUtils.join("", menuInputList);
//                    Log.d("menuInputVal", menuInputVal);
//                    menu_input_text.setText(menuInputVal);
                    menu_input_edit_text.setText(menuInputVal);
                    //???
                }
            }catch (Exception e) {
                e.printStackTrace();
            }


        }
    };


    //???????????? ???????????????
    private View.OnClickListener mainBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.nbtn_main_status:  //?????? status?????????
                    break;

                /*frame2 ??????????????????*/
                case R.id.nbtn_emptycar:  //????????????
                    isDrivedClicked = false;
                    isPayClicked = false;
                    btn_dayoff.setText("??????");
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    btn_menu.setClickable(true);
                    btn_menu.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                    //me: ?????? -> ?????????
                    windowService.update_BtnMeterstate(AMBlestruct.B_EMPTY);
                    break;
                case R.id.nbtn_drivestart:  //????????????
                    isDrivedClicked = true;
                    btn_drive.setTextColor(getResources().getColor(R.color.black));
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    //me: ?????? -> ?????????
                    windowService.update_BtnMeterstate(AMBlestruct.B_DRIVE);
                    break;

                case R.id.nbtn_call:    //????????????
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    //me: ?????? -> ?????????
                    windowService.update_BtnMeterstate(AMBlestruct.B_CALL);
                    break;

                case R.id.nbtn_driveend:  //????????????
                    isPayClicked = true;
                    isDrivedClicked = true;
                    btn_dayoff.setText("????????????");
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_main_layout.setVisibility(View.GONE);
                    //me: ?????? -> ?????????
                    windowService.update_BtnMeterstate(AMBlestruct.B_PAY);
                    break;

                /*frame3 ??????????????????*/
                case R.id.nbtn_menu:  //?????? btn

                    if (isDrivedClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else if (isPayClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else {
                        main_all_layout.setVisibility(View.GONE);
                        main_layout.setVisibility(View.GONE);
                        menu_main_layout.setVisibility(View.VISIBLE);
                        add_fare_frame_layout.setVisibility(View.GONE);
                        setEmptyStatus(btn_empty, btn_drive, btn_call, btn_pay);
                        //me: ?????? -> ?????????
                        windowService.menu_meterState(AMBlestruct.APP_MENU_REQUEST_CODE, AMBlestruct.MenuType.OPEN,"","");
                    }
                    break;

                case R.id.nbtn_receipt:  //?????? btn
                    if (isDrivedClicked == true && isPayClicked == true) {

                        windowService.update_BtnMeterstate("34");

                    }else if (isDrivedClicked == true) {

                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();

                    }else {
                        btn_dayoff.setText("??????");
                        windowService.update_BtnMeterstate(AMBlestruct.B_RECEIPT);
                    }
                    break;
                case R.id.nbtn_attendance:  //?????? btn

                    if (isDrivedClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else if (isPayClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else {
                        addFareList = new ArrayList<>();
                        add_fare_title.setText("????????? ???????????????");
                        add_fare_value_text.setText("");
                        main_layout.setVisibility(View.GONE);
                        menu_layout.setVisibility(View.GONE);
                        number_pad_frame_layout.setVisibility(View.VISIBLE);
                        add_fare_frame_layout.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.nbtn_reserve:  //?????? btn

                    if (isDrivedClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else if (isPayClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else {
                        //me: ?????? -> ?????????
                        windowService.update_BtnMeterstate("51");
                    }
                    break;
                case R.id.nbtn_dayoff:  //?????? btn
                    if (btn_dayoff.getText().toString().equals("????????????")) {
                        btn_main_status.setTextColor(getResources().getColor(R.color.orange));
                        windowService.update_BtnMeterstate("52");
                    }else {
                        btn_sub_status.setVisibility(View.GONE);
                        if (isDrivedClicked == true) {
                            Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                        }else {
                            windowService.update_BtnMeterstate("50");
                        }
                    }
                    break;

                case R.id.nbtn_addpayment:  //???????????? btn
                    if (isDrivedClicked == false){
                        //???????????? ???????????? ?????????
                        btn_dayoff.setText("??????");
                        addFareVal = "";
                        add_fare_title.setText("???????????? ??????");
//                        add_fare_edit_text.setText("");
                        add_fare_value_text.setText("");
                        addFareList = new ArrayList<>();
                        if (addFareList != null) {
                            if (addFareList.size() > 0) {
                                addFareList.clear();
                            }
                        }else {
                            addFareList = new ArrayList<>();
                        }
                        main_layout.setVisibility(View.GONE);
                        menu_layout.setVisibility(View.GONE);
                        number_pad_frame_layout.setVisibility(View.VISIBLE);
                        add_fare_frame_layout.setVisibility(View.VISIBLE);
                    }else if (isDrivedClicked == true && isPayClicked == true) {
                        btn_dayoff.setText("????????????");
                        add_fare_title.setText("???????????? ??????");
//                        add_fare_edit_text.setText("");
                        add_fare_value_text.setText("");
                        if (addFareList != null) {
                            if (addFareList.size() > 0) {
                                addFareList.clear();
                            }
                        }else {
                            addFareList = new ArrayList<>();
                        }
                        addFareVal = "";
                        main_layout.setVisibility(View.GONE);
                        menu_layout.setVisibility(View.GONE);
                        number_pad_frame_layout.setVisibility(View.VISIBLE);
                        add_fare_frame_layout.setVisibility(View.VISIBLE);
                    }else if (isDrivedClicked = true){
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else {}
                    break;

            }
        }
    };


    private void setEmptyStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnEmpty.setText("??????");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.white));
        btnReserve.setTextColor(getResources().getColor(R.color.white));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setDriveStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnDrive.setText("??????");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.grey_light));
        btnReserve.setTextColor(getResources().getColor(R.color.white));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_selected_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setReserveStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnReserve.setText("??????");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.white));
        btnReserve.setTextColor(getResources().getColor(R.color.grey_light));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_selected_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setPayStatus(ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnPay.setText("??????");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.white));
        btnReserve.setTextColor(getResources().getColor(R.color.white));
        btnPay.setTextColor(getResources().getColor(R.color.grey_light));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_selected_btn);
    }

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



}//MainActivity..