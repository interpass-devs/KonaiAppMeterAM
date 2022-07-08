package com.konai.appmeter.driver_am.service;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.view.MainActivity;

import java.util.ArrayList;
import java.util.Set;

public class AwindowService extends Service {

    public static BluetoothConnectThread bluetoothConnectThread;
    String log = "log_WindowService";
    private Handler mHandler;
    private LocationManager locationManager;
    private static AMBluetoothManager amBluetoothManager = null;
    public Set<BluetoothDevice> bluetoothDeviceSet;
    public ArrayList<String> deviceList = new ArrayList<>();
    public WindowManager windowManager;
    private BluetoothAdapter mBluetoothAdapter = null;
    public View mView;
    public WindowManager.LayoutParams m_Params;
    private int MAX_X = -1, MAX_Y = -1, max_x = -1, max_y = -1;
    public ImageView launcher_icon;
    public TextView backgroundIcon, btn_type_status;
    public LinearLayout show_fare_layout;
    public float moveX = 1000f;
    public float moveY = 1000f;
    private boolean moveV = false;
    private float start_x, start_y, START_X, START_Y; //움직이기 위해 터치한 시작 점
    private int prev_x, prev_y, PREV_X, PREV_Y;
    public int view_w, view_h;
    private boolean move_view = false;
    private static Thread mainThread = null;
    private static Thread checkstateThread = null;
    private final IBinder m_ServiceBinder = new ServiceBinder();
    private int mfare = 0;
    private int startFare = 0;
    private int callFare = 0;
    private int etcFare = 0;  //추가 또는 기타요금
    private int nightFare = 0;
    private int complexFare = 0;
    private int suburbFare = 0;
    private int suburbFareRate = 0;
    private String mDriverId ="";


    // Activity 에서 정의해 해당 서비스와 통신 할 함수를 추상함수로 정의
    public interface mainCallBack {

        void changedDriverId(String driverID);

        //블루투스 수신상태
        void serviceBleStatus(boolean bleStatus);

        //미터기 버튼 수신상태
        void serviceMeterState( String driverId
                            , int btnType
                            , int mFare
                            , int startFare
                            , int callFare
                            , int etcFare
                            , int nightFare
                            , int complexFare
                            , int suburbFare
                            , int suburbFareRate);

        //미터기 메뉴 수신상태
        void serviceMeterMenuState(String menuMsg, int menuType);
    }


    // Activity 와 통신할 callback 객체
    public mainCallBack mCallback = null;

    // callback 객체 등록함수
    public void registerCallback(mainCallBack callBack) {
        Log.d("mainCallBack-> ", "registerCallback");
        mCallback = callBack;
    }


    @Override
    public void onCreate() {
        super.onCreate();

//        setBleScan();

//        bluetoothConnectThread = new BluetoothConnectThread();
//        bluetoothConnectThread.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); // Foreground service 종료
        }

        if (windowManager != null) {
            if (mView != null) {
                windowManager.removeView(mView); // View 초기화
                mView = null;
            }
            windowManager = null;
        }
    }

    public class ServiceBinder extends Binder {

        public AwindowService getService() {
            return AwindowService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_ServiceBinder;
    }

    public boolean onUnbind(Intent intent) {
//        close();
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        amBluetoothManager = new AMBluetoothManager(this, AwindowService.this);
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService();

            _overlaycarstate();
        }


        //status: 블루투스 연결/ 디바이스 찾기
        setBleScan();

//        bluetoothConnectThread = new BluetoothConnectThread();
//        bluetoothConnectThread.start();

        return super.onStartCommand(intent, flags, startId);
    }


    //status ---------- 앱위에 그리기 ----------

    void startForegroundService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "appmeter";
            final String TITLE = "AM100";
            final String strTitle = getString(R.string.app_name);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);

            if (channel == null) {
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_ID + " " + TITLE,
                        NotificationManager.IMPORTANCE_LOW);
                channel.setSound(null, null);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
            startForeground(1, notification);

        } else {


        }
    }

    public void showHideForgroundservice(boolean show, String fare, String buttonType) {
        if (mView == null) {
            return;
        }
        if (show == true) {
            mView.setVisibility(View.VISIBLE);
            backgroundIcon.setText(fare);
            switch (buttonType) {
                case "05":
                    btn_type_status.setText("빈\n차");
                    btn_type_status.setBackgroundResource(R.drawable.empty_bg);
                    break;
                case "20":
                    btn_type_status.setText("주\n행");
                    btn_type_status.setBackgroundResource(R.drawable.drive_bg);
                    break;
                case "01":
                    btn_type_status.setText("지\n불");
                    btn_type_status.setBackgroundResource(R.drawable.pay_bg);
                    break;
            }

        } else {
            mView.setVisibility(View.INVISIBLE);
            backgroundIcon.setText(fare);
        }
    }


    public void _overlaycarstate() {

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflater 를 사용하여 layout 을 가져오자
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 윈도우매니저 설정

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                // Android O 이상인 경우 TYPE_APPLICATION_OVERLAY 로 설정
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.TOP | Gravity.LEFT;
        // 위치 지정

        mView = inflate.inflate(R.layout.layout_show_mainactivity, null);
        // view_in_service.xml layout 불러오기
        // mView.setOnTouchListener(onTouchListener);
        // Android O 이상의 버전에서는 터치리스너가 동작하지 않는다. ( TYPE_APPLICATION_OVERLAY 터치 미지원)

        launcher_icon = (ImageView) mView.findViewById(R.id.launcher_icon);
        show_fare_layout = (LinearLayout) mView.findViewById(R.id.show_fare_layout);
        btn_type_status = (TextView) mView.findViewById(R.id.btn_type_text);
        backgroundIcon = (TextView) mView.findViewById(R.id.btn_img);
        show_fare_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something!
                //다시 메인 액티비티 열기
                show_mainActivity();
            }
        });

//        show_fare_layout.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                show_fare_layout.setVisibility(View.GONE);
//                launcher_icon.setVisibility(View.VISIBLE);
//                return true;
//            }
//        });
//
//
//        show_fare_layout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:if (MAX_X == -1) {
//                        DisplayMetrics matrix = new DisplayMetrics();
//                        windowManager.getDefaultDisplay().getMetrics(matrix);  //화면 정보를 가져와서
//
//                        MAX_X = matrix.widthPixels - mView.getWidth(); //x 최대값 설정
//                        MAX_Y = matrix.heightPixels - mView.getHeight(); //y 최대값 설정
//                    }
//                        moveV = false;
//
//                        START_X = event.getRawX();  //터치 시작 점
//                        START_Y = event.getRawY();  //터치 시작 점
//                        PREV_X = params.x;        //뷰의 시작 점
//                        PREV_Y = params.y;
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        moveV = true;
//
//                        int x = (int) (event.getRawX() - START_X);
//                        int y = (int) (event.getRawY() - START_Y);
//
//                        final int num = 10;
//                        if ((x > -num && x < num) && (y > -num && y < num)) {
//                            moveV = false;
//
//                        } else {
//
//                            //터치해서 이동한 만큼 이동 시킨다
//                            params.x = PREV_X + x;
//                            params.y = PREV_Y + y;
//
//                            //최대값 넘어가지 않게 설정
//                            if (params.x > MAX_X) params.x = MAX_X;
//                            if (params.y > MAX_Y) params.y = MAX_Y;
//                            if (params.x < 0) params.x = 0;
//                            if (params.y < 0) params.y = 0;
//
//                            windowManager.updateViewLayout(mView, params);    //뷰 업데이트
//                        }
//                        break;
//                }
//                return false;
//            }
//        });

//        launcher_icon.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                show_fare_layout.setVisibility(View.VISIBLE);
//                launcher_icon.setVisibility(View.GONE);
//                return true;
//            }
//        });

        launcher_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_mainActivity();
            }
        });


        launcher_icon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:if (max_x == -1) {
                        DisplayMetrics matrix = new DisplayMetrics();
                        windowManager.getDefaultDisplay().getMetrics(matrix);  //화면 정보를 가져와서

                        max_x = matrix.widthPixels - mView.getWidth(); //x 최대값 설정
                        max_y = matrix.heightPixels - mView.getHeight(); //y 최대값 설정
                    }
                        moveV = false;

                        start_x = event.getRawX();  //터치 시작 점
                        start_y = event.getRawY();  //터치 시작 점
                        prev_x = params.x;        //뷰의 시작 점
                        prev_y = params.y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        moveV = true;

                        int x = (int) (event.getRawX() - start_x);
                        int y = (int) (event.getRawY() - start_y);

                        final int num = 10;
                        if ((x > -num && x < num) && (y > -num && y < num)) {
                            moveV = false;

                        } else {

                            //터치해서 이동한 만큼 이동 시킨다
                            params.x = prev_x + x;
                            params.y = prev_y + y;

                            //최대값 넘어가지 않게 설정
                            if (params.x > max_x) params.x = max_x;
                            if (params.y > max_y) params.y = max_y;
                            if (params.x < 0) params.x = 0;
                            if (params.y < 0) params.y = 0;

                            windowManager.updateViewLayout(mView, params);    //뷰 업데이트
                        }
                        break;
                }
                return false;
            }
        });


        windowManager.addView(mView, params); // 윈도우에 layout 을 추가 한다.

        return;
    }

    private void show_mainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }//show_mainActivity


    private void showhideCallBtn(boolean show) {

        if (mView == null) {
            return;
        }

        if (show == true) {
            mView.setVisibility(View.VISIBLE);
            windowManager.updateViewLayout(mView, mView.getLayoutParams());
        } else {
            mView.setVisibility(View.INVISIBLE);
            windowManager.updateViewLayout(mView, mView.getLayoutParams());
        }
    }

    public class BluetoothConnectThread extends Thread {

        boolean isRun = true;

        @Override
        public void run() {
//            super.run();  //아무의미 없음

            while (true) {

                Log.d(log, "bleThread start..");

                //이 스레드가 해야할 작업 수행
                //기기찾기 및 연결
            if (isRun)
                setBleScan();

                //5초 동안 잠시대기
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }//while

        }//run


        //스레드를 종료하는 메소드
        void startThread() {
            isRun = true;
            Log.d(log, "bleThread restarted.. ");
//            mCallback.serviceBleStatus(true);   //status: 여기서 앱이 꺼짐
        }

        //스레드를 종료하는 메소드
        void stopThread() {
            isRun = false;
            Log.d(log, "bleThread stopped.. ");
//            mCallback.serviceBleStatus(true);   //status: 여기서 앱이 꺼짐
        }
    }//bluetoothConnectThread


    //status --- 블루투스 -------------

    public void setBleScan() {

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {

            Log.d("start_scan_device", "isEnabled");  //y
            Log.d("start_scan_device", setting.BLUETOOTH_DEVICE_NAME); //null

            //me: new
//            startPairingBluetooth();
            try {
                for (BluetoothDevice device : bluetoothDeviceSet)
                {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    deviceList.add(deviceName + ": " + deviceAddress);
//                Log.d("deviceList", deviceList + "");   //status - 기기들이 없을때 여기서 에러남

                    if (deviceName.contains("AM101"))
                    {
                        Log.d("deviceList_tobePaired", deviceName+":  " +deviceAddress);

                        // gatt 서버에 연결
                        if (amBluetoothManager != null)
                        {
                            //amBluetoothManager.connectAM(deivceName, deviceAddress);
                            amBluetoothManager.connectBLE(deviceName, deviceAddress);
                        }
                        else
                        {
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    //페어링을 확인하는 브로드캐스트리시버
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

//                Log.d("mReceiver", "ACTION_STATE_CHANGED");

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {

                    case BluetoothAdapter.STATE_OFF:
                        Log.d("mReceiver", "STATE_OFF");
                        //31이하
                        //다시 연결..
//                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        intent.putExtra("enableBle","enableBle");
//                        sendEnableBLEOrder(enableIntent);
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("mReceiver", "STATE_TURNING_OFF");
                        bluetoothConnectThread = new BluetoothConnectThread();
                        bluetoothConnectThread.start();
                        break;

                    case  BluetoothAdapter.STATE_ON:
                        Log.d("mReceiver", "STATE_ON");
                        bluetoothConnectThread.stopThread();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("mReceiver", "STATE_TURNING_ON");
                        break;
                }
            }

        }
    };

    public void bluetoothConnState(boolean isConnected) {
        if (isConnected == false) {
            Log.d("isConnected!", isConnected+"");
            //스레드 다시시작 & 아이콘 색 변경
//            bluetoothConnectThread = new BluetoothConnectThread();
//            bluetoothConnectThread.start();
            bluetoothConnectThread.startThread();
        }else {
            Log.d("isConnected!", isConnected+"");
            //스레드 멈춤 & 아이콘 색 변경
            bluetoothConnectThread.stopThread();
        }
    }


    public Handler set_meterhandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
//            Log.d("meterHandler_service", msg.what+"");


            //status: 다른 앱 위에 그리기 상태설정
            if (msg.what == 100) {

                if (setting.OVERLAY == false) {
                    showHideForgroundservice(false, mfare+"", AMBlestruct.AMReceiveFare.M_STATE);
                }else {
                    showHideForgroundservice(true, mfare+"", AMBlestruct.AMReceiveFare.M_STATE);
                }
            }else if (msg.what == 101) {
                mCallback.changedDriverId(AMBlestruct.AMReceiveFare.M_DRIVER_ID);
            }

            //status: 블루투스 상태값
            else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE) {

                try{
                    if (setting.BLE_STATE == true) {
                        Log.d(log, "ble_conn- " + setting.BLE_STATE);
                    mCallback.serviceBleStatus(true); //null obj??


                    }else {
                        Log.d(log, "ble_conn- " + setting.BLE_STATE);
                    mCallback.serviceBleStatus(false);


                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }


                //status: 빈차등 현재상태 수신값
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE) {

                startFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_START_FARE);   //승차요금   //status: 여기서 앱 꺼짐
                callFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_CALL_FARE);     //호출요금
                etcFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_ETC_FARE);       //기타요금/추가요금
                complexFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
                suburbFareRate = Integer.parseInt(AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);
                Log.d("rateCheck_1", suburbFareRate+"");

//                backgroundIcon.setText(startFare);

                if (AMBlestruct.AMReceiveFare.M_DRIVER_ID != null) {
                    mDriverId = AMBlestruct.AMReceiveFare.M_DRIVER_ID;
//                    mCallback.serviceMeterState
                }

                //심야할증
                if (AMBlestruct.AMReceiveFare.M_NIGHT_FARE.equals("1")) {  //있음
                    //20% 더하기
                    nightFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_NIGHT_FARE);
                }else if (AMBlestruct.AMReceiveFare.M_NIGHT_FARE.equals("0")) { //없음
                    nightFare = 0;
                }

                //복합할증 요금
                if (AMBlestruct.AMReceiveFare.M_COMPLEX_FARE.equals("1")) {  //있음
                    complexFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
                }else if (AMBlestruct.AMReceiveFare.M_COMPLEX_FARE.equals("0")) {  //없음
                    complexFare = 0;
                }

                //시외할증
                if (AMBlestruct.AMReceiveFare.M_SUBURB_FARE != null) {   //있음
                    suburbFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_SUBURB_FARE);
                }else {  //없음
                    suburbFare = 0;
                }

                //할증율%
                if (AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE.equals("1")) {
                    Log.d("rateCheck_", AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE+"");
                    suburbFareRate = Integer.parseInt(AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);
                    Log.d("rateCheck", suburbFareRate+"");
                }else if (AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE.equals("0")) {
                    suburbFareRate = 0;
                }



                //빈차등 요금 그대로 받기
                if (AMBlestruct.AMReceiveFare.M_START_FARE != null) {
                    mfare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_START_FARE);
                    if (setting.OVERLAY != false) {
                        showHideForgroundservice(true, mfare+"",AMBlestruct.AMReceiveFare.M_STATE);
                    }else {
                        showHideForgroundservice(false, mfare+"", AMBlestruct.AMReceiveFare.M_STATE);
                    }
                }else {
                    mfare = 0;
                }



                if (AMBlestruct.AMReceiveFare.M_STATE.equals("01")) {  //지불

                    mCallback.serviceMeterState(mDriverId, 01, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("05")) { //빈차

                    mCallback.serviceMeterState(mDriverId,05, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("20")) { //주행

                    mCallback.serviceMeterState(mDriverId,20, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("30")) { //할증

                    mCallback.serviceMeterState(mDriverId,4, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("02")) { //수기결제

                    mCallback.serviceMeterState(mDriverId,02, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("50")) { //휴무

                    mCallback.serviceMeterState(mDriverId,6, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("51")) { //예약

                    mCallback.serviceMeterState(mDriverId,7, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("99")) {  //미정된 코드

//                    Log.d("btnCehck_미정코드",AMBlestruct.AMReceiveFare.M_STATE);

                }


                //빈차등수신(19) 데이터를 잘 받았을 경우 -> 응답코드(69) 보내기
                if (AMBlestruct.AMReceiveFare.M_STATE != null) {
                    if (amBluetoothManager != null) {
                        amBluetoothManager.responseMeterState("69","00");
                    }
                }else {
                    if (amBluetoothManager != null) {
                        amBluetoothManager.responseMeterState("69","기타");
                    }
                }


                //status: 빈차등 메뉴 수신값
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE) {

                Log.d("code43_msg",AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE+"" );  //아스키 49 -> 1:메뉴

                mCallback.serviceMeterMenuState(AMBlestruct.AMReceiveMenu.MENU_MSG, AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE);

            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_INPUT_MENU_STATE) {  //빈차등 수신메세지 입력값 보내기

                Log.d("code46_msg", AMBlestruct.AMReceiveMenu.MENU_MSG);

                Log.d("code46_inputtype", AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE);

                mCallback.serviceMeterMenuState(AMBlestruct.AMReceiveMenu.MENU_MSG, AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE);

            }
        }
    };


    public boolean check_attendance(String requestCode, String driverId) {
        if (amBluetoothManager != null) {
            amBluetoothManager.sendAttendanceState(requestCode, driverId);
        }
        return true;
    }

    //me: 버튼값 업데이트 -> 빈차등으로 보내기
    public boolean update_BtnMeterstate(String sstate) {
        if (amBluetoothManager != null) {
            amBluetoothManager.update_AMmeterstate(sstate);
        }
        return true;
    }

    public boolean add_fareState(String requestCode, String addFare) {
        if (amBluetoothManager != null) {
            Log.d("add_fare", requestCode+":  "+addFare);
            amBluetoothManager.add_fareState(requestCode, addFare);
        }
        return true;
    }


    public boolean menu_meterState(String requestCode, String menuType, String pos) {
        Log.d("requestCode>", requestCode+", "+menuType);
        if (amBluetoothManager != null) {
            amBluetoothManager.menu_AMmeterState(requestCode, menuType, pos);
        }
        return true;
    }

    public boolean menu_input_meterState(String requestCode, String inputCheck, int inputLength, String inputMsg) {
//        Log.d("requestCode---", requestCode+") "+inputCheck+", "+inputLength+", "+inputMsg);
        if (amBluetoothManager != null) {
            amBluetoothManager.menu_input_AMmeterState(requestCode, inputCheck, inputLength, inputMsg);
        }
        return true;
    }



}
