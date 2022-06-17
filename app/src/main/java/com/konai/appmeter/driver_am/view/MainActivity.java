package com.konai.appmeter.driver_am.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.konai.appmeter.driver_am.BuildConfig;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.dialog.InputFareDialog;
import com.konai.appmeter.driver_am.service.AwindowService;
import com.konai.appmeter.driver_am.setting.PermissionSupoort;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.util.MyTouchListener;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private PermissionSupoort mPermission;

    private AMBluetoothManager mBluetoothLE;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice = null;
    private String mBluetoothAddress = "";
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int ACCESS_FINE_LOCATION_DENIED = 10;
    private static final int BLUETOOTH_SCAN_DENIED = 60;
    private static final int BLUETOOTH_CONNECT_DENIED = 50;
    private static final int REQUEST_ENABLE_BLE = 20;
    private static final int REQUEST_ENABLE_BT = 30;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private String mDrvnum = "";
    private Handler mHandler;

    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    String ble = "connectBle";

    //BluetoothGattCallback

    private BluetoothGatt mbluetoothGatt = null;
    public static BluetoothGattService m_gattService = null;
    private static BluetoothGattCharacteristic m_gattCharTrans = null;
    private static BluetoothGattCharacteristic m_gattCharConfig = null;
    private static BluetoothGattDescriptor m_descriptor = null;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public static class SampleGattAttributes {
        private static HashMap<String, String> attributes = new HashMap();
        public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
        static {
            // Sample Services.
            attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
            // Sample Characteristics.
            attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        }
    }


//    public static LocService m_Service;
    public static AwindowService windowService = null;

    int lbs_x = -1;
    int lbs_y = -1;
    int lbs_w = 300;
    int lbs_h = 138;

    private Context mContext;

    private MyTouchListener mTouchListener;

    private View viewframe1, viewframe2, viewframe3;
    private FrameLayout frame1, frame2, frame3;
    private LinearLayout main_layout, menu_layout;
    private ButtonFitText btn_menu, btn_complex, btn_login, btn_arrive, btn_add_pay;
    private ButtonFitText btn_empty, btn_drive, btn_reserve, btn_pay;
    private ImageView iv_ble, iv_wifi;
    private FontFitTextView tv_night_status, tv_add_pay, tv_rescall_pay, tv_total_pay;
    private ButtonFitText btn_main_status;
    private Boolean menuClicked = true;

    InputFareDialog inputFareDialog;
    private NotificationChannel nfChannel;
    private NotificationManager nfManager;
    private NotificationChannel nfGpsChannel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //서비스가 백그라운드에서 돌아가고 있는지 확인
//        m_Service.isLocServiceRunning(mContext);

        setting.APP_VERSION = Double.parseDouble(BuildConfig.VERSION_NAME);


        //me: 권한설정

        //위치권한
        locationPermission();

        //블루투스 스캔 권한
        bleScanPermission();

        //블루투스 연결 권한
        bleConnPermission();


        //앱위에그리기 권한
        oerlayPermission();

        //me:[저전력 블루투스 연결 설정]

        //블루투스 관리자객체 소환
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        mBluetoothAdapter = bluetoothManager.getAdapter();

        //블루투스 활성화 (블루투스가 켜져있는지 확인)
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "이 기기에는 블루투스가 없습니다.", Toast.LENGTH_SHORT).show();
            //블루투스 장치 On 하는 화면실행
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d("bluetooth_result", "ble on");

            //me: 기기검색 실행코드

            //status -- 안되면 되살리기
            //scan device
//            mHandler = new Handler();
//            scanLeDevice(true);
            //status -- 안되면 되살리기
            //status -- 블루투스는 AwindowService 에서 돌고있음

        }

        Log.d("ble_state_1", setting.BLE_STATE+"");

        viewFrameVariablesConfiguration();

        Log.d("ble_state_2", setting.BLE_STATE+"");

//        if (setting.BLE_STATE == true) {
//            Log.d("ble_state_true", setting.BLE_STATE+"");
//            iv_ble.setBackgroundResource(R.drawable.bluetooth_green);
////            Toast.makeText(mContext, "빈차등과 연결 되었습니다.", Toast.LENGTH_SHORT).show();
//        }else {
//            Log.d("ble_state_false", setting.BLE_STATE+"");
//            iv_ble.setBackgroundResource(R.drawable.bluetooth_blue);
//            Toast.makeText(mContext, "빈차등 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show();
//        }


    }//onCreate




    //권한요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d("mPermission", requestCode + ": \n" + permissions + ": \n" + grantResults);

        //리턴이 false 로 들어온다면 사용자가 권한 허용거부
        if (!mPermission.permissionResult(requestCode, permissions, grantResults)) {
            //다시 permission 요청
            mPermission.requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //위치권한데 대한 동적퍼미션 작업 (Location permission)
    public void locationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //23
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{permission}, ACCESS_FINE_LOCATION_DENIED);
            }
        }
    }

    public void bleScanPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_DENIED);
            }
        }
    }

    public void bleConnPermission() {
        String ble_conn_permission = Manifest.permission.BLUETOOTH_CONNECT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //31
            if (checkSelfPermission(ble_conn_permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{ble_conn_permission}, BLUETOOTH_CONNECT_DENIED);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //위치권한 설정을 거부했을때
            case ACCESS_FINE_LOCATION_DENIED:
                Toast.makeText(mContext, "위치권한을 거부하였습니다.", Toast.LENGTH_SHORT).show();
                break;

            case BLUETOOTH_SCAN_DENIED:
                Toast.makeText(mContext, "!!!!!!", Toast.LENGTH_SHORT).show();
                break;

            case BLUETOOTH_CONNECT_DENIED:
                Toast.makeText(mContext, "블루투스 연결 거부", Toast.LENGTH_SHORT).show();
                break;

            //블루투스 활성화
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(mContext, "블루투스 ON", Toast.LENGTH_SHORT).show();
//                    Log.d("REQUEST_ENABLE_BT", data.getStringExtra("Address"));
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(mContext, "블루투스 설정을 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            //앱그리기권한 설정을 거부했을 때
            case ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE:
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    finish();
                } else {
                    startWindowService();
//                startService();
                }
                break;
        }

    }

    public void oerlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크

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
//                startService();
                startWindowService();
            }
        } else
//            startService();
            startWindowService();
    }

    //me: 메인화면에서는 보이지말고 앱 밖에서만 보이도록..
    void startWindowService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(MainActivity.this, AwindowService.class));
        } else {
            startService(new Intent(MainActivity.this, AwindowService.class));
        }
    }

    /**
    public void startService() {

        if (windowService != null) {
            return;
        }
        if (setting.gUseBLE) {
            if (!mBluetoothAdapter.isEnabled()) {  //null object
                return;
            }
        }

        Intent service = new Intent(getApplicationContext(), AwindowService.class);
        service.setPackage("com.konai.appmeter.driver_am");

        try {
            unbindService(mServiceConnection);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        }

//        windowService = null;

        if (Build.VERSION.SDK_INT >= 26) {
            getApplicationContext().startForegroundService(service);
        } else {
            startService(service);
        }

        try {

            Thread.sleep(1000);

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        if (windowService == null) {
            bindService(new Intent(getApplicationContext(),
                    AwindowService.class), mServiceConnection, Context.BIND_AUTO_CREATE); //20180117
        }

    }

/**
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            AwindowService.ServiceBinder binder = (LocService.ServiceBinder) service;
//            m_Service = binder.getService();
//            m_Service.registerCallback(mCallback);

            Log.d("mServiceConnection", m_Service + "");

            if (m_Service == null) {
                Log.d("mServiceConnection", "null");
            } else {
                Log.d("mServiceConnection", "not null");
            }

            setting.m_Service = m_Service;

            windowService.lbs_initx = lbs_x;
            windowService.lbs_inity = lbs_y;
            windowService.lbs_initw = lbs_w;
            windowService.lbs_inith = lbs_h;

            if (setting.bOverlaymode) {
                windowService._overlaycarstate();
            }

//            afterServiceConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            windowService = null;
            setting.windowService = null;
        }
    };
    **/


    private void viewFrameVariablesConfiguration() {

        viewframe1 = null;
        viewframe2 = null;
        viewframe3 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1);
        frame2 = (FrameLayout) findViewById(R.id.frame2);
        frame3 = (FrameLayout) findViewById(R.id.frame3);

        LayoutInflater inflater = null;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewframe1 = inflater.inflate(R.layout.mainframe1, frame1,true);  //메인화면
        viewframe2 = inflater.inflate(R.layout.mainbuttonframe2, frame2, true); //메인하단버튼
        viewframe3 = inflater.inflate(R.layout.mainbuttonframe3, frame3, true); //메인상단버튼

        /*메인화면버튼 frame1*/
        iv_ble = (ImageView) viewframe1.findViewById(R.id.nbtn_connectble);
        btn_main_status = (ButtonFitText) viewframe1.findViewById(R.id.nbtn_main_status);
        tv_night_status = (FontFitTextView) viewframe1.findViewById(R.id.ntv_status);
        tv_add_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_addpayment);
        tv_rescall_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_rescallpay);
        tv_total_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_totalpay);

        /*메인버튼 frame2*/
        btn_empty = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_emptycar);
        btn_drive = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_drivestart);
        btn_reserve = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_reserve);
        btn_pay = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_driveend);
        main_layout = viewframe1.findViewById(R.id.mainframe1_layout);
        menu_layout = viewframe1.findViewById(R.id.menu_frame_layout);

        /*메인버튼 frame3*/
        btn_menu = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_menu);
        btn_complex = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_complex);
        btn_login = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_login);
        btn_arrive = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_arrived);
        btn_add_pay = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_addpayment);

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
        btn_reserve.setOnTouchListener(mTouchListener);
        btn_pay.setOnTouchListener(mTouchListener);
        btn_empty.setOnClickListener(mainBtnClickListener);
        btn_drive.setOnClickListener(mainBtnClickListener);
        btn_reserve.setOnClickListener(mainBtnClickListener);
        btn_pay.setOnClickListener(mainBtnClickListener);

        btn_menu.setOnTouchListener(mTouchListener);
        btn_complex.setOnTouchListener(mTouchListener);
        btn_login.setOnTouchListener(mTouchListener);
        btn_arrive.setOnTouchListener(mTouchListener);
        btn_add_pay.setOnTouchListener(mTouchListener);
        btn_menu.setOnClickListener(mainBtnClickListener);
        btn_complex.setOnClickListener(mainBtnClickListener);
        btn_login.setOnClickListener(mainBtnClickListener);
        btn_arrive.setOnClickListener(mainBtnClickListener);
        btn_add_pay.setOnClickListener(mainBtnClickListener);

    }



    //Activity 파괴시 데이터 저장
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

//        outState.putInt("num", cnt);
    }



    //메인버튼 클릭리스너
    private View.OnClickListener mainBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.nbtn_connectble:  //블루투스 iv
//                    setupBluetooth(1);
                    break;
                case R.id.nbtn_main_status:  //운행 status텍스트
                    break;
                case R.id.ntv_status:  //ex:심야20% tv
                    break;
                case R.id.ntv_addpayment:  //추가요금 tv
                    break;
                case R.id.ntv_rescallpay:  //ex:호출1000 tv
                    break;
                case R.id.ntv_totalpay:  //총 운행금액 tv
                    break;
                /*frame2 메인하단버튼*/
                case R.id.nbtn_emptycar:  //빈차버튼
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    setEmptyStatus(btn_main_status, btn_empty, btn_drive, btn_reserve, btn_pay);
//                    tv_night_status.setVisibility(View.GONE);
                    btn_menu.setClickable(true);
                    btn_menu.setBackgroundResource(R.drawable.grey_gradi_btn);
                    break;
                case R.id.nbtn_drivestart:  //주행버튼
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    setDriveStatus(btn_main_status, btn_empty, btn_drive, btn_reserve, btn_pay);
                    //주행버튼 클릭 -> 메뉴버튼 클릭 못하게
                    btn_menu.setClickable(false);
                    Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nbtn_reserve:    //호출버튼
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    setReserveStatus(btn_main_status, btn_empty, btn_drive, btn_reserve, btn_pay);
                    break;
                case R.id.nbtn_driveend:  //지불버튼
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    setPayStatus(btn_main_status, btn_empty, btn_drive, btn_reserve, btn_pay);
                    break;
                /*frame3 메인상단버튼*/
                case R.id.nbtn_menu:  //메뉴 btn
                    //빈차등메뉴
//                    setupBluetooth(2);
                    main_layout.setVisibility(View.GONE);
                    menu_layout.setVisibility(View.VISIBLE);
                    setEmptyStatus(btn_main_status, btn_empty, btn_drive, btn_reserve, btn_pay);
                    break;
                case R.id.nbtn_complex:  //복합 btn
                    break;
                case R.id.nbtn_login:  //로그인 btn
                    break;
                case R.id.nbtn_arrived:  //도착 btn
                    break;
                case R.id.nbtn_addpayment:  //추가 btn
                    inputFareDialog = new InputFareDialog(mContext, setting.gOrient
                            , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //ok btn
                            String value= inputFareDialog.returnMenualValue();
//                            Log.d("getMenualPay_1", value);
                            inputFareDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //cancel btn
                            inputFareDialog.dismiss();
                        }
                    });
                    inputFareDialog.setCancelable(true);
                    inputFareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    inputFareDialog.show();
                    setDisplayMetrics(mContext, inputFareDialog);
                    break;



            }
        }
    };


    private void setEmptyStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("빈차");
        btnMainStatus.setTextColor(getResources().getColor(R.color.white));
        btnMainStatus.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnEmpty.setText("빈차");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.white));
        btnReserve.setTextColor(getResources().getColor(R.color.white));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setDriveStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("주행");
        btnMainStatus.setTextColor(getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnDrive.setText("주행");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.grey_light));
        btnReserve.setTextColor(getResources().getColor(R.color.white));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_selected_btn);
        btnReserve.setBackgroundResource(R.drawable.green_gradi_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setReserveStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("호출");
        btnMainStatus.setTextColor(getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.green_gradi_btn);
        btnReserve.setText("호출");
        btnEmpty.setTextColor(getResources().getColor(R.color.white));
        btnDrive.setTextColor(getResources().getColor(R.color.white));
        btnReserve.setTextColor(getResources().getColor(R.color.grey_light));
        btnPay.setTextColor(getResources().getColor(R.color.white));
        btnEmpty.setBackgroundResource(R.drawable.grey_gradi_btn);
        btnDrive.setBackgroundResource(R.drawable.yellow_gradi_btn);
        btnReserve.setBackgroundResource(R.drawable.green_selected_btn);
        btnPay.setBackgroundResource(R.drawable.orange_gradi_btn);
    }

    private void setPayStatus(ButtonFitText btnMainStatus, ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
        btnMainStatus.setText("지불");
        btnMainStatus.setTextColor(getResources().getColor(R.color.black));
        btnMainStatus.setBackgroundResource(R.drawable.orange_gradi_btn);
        btnPay.setText("지불");
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