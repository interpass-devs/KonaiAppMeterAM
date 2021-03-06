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
    private float start_x, start_y, START_X, START_Y; //???????????? ?????? ????????? ?????? ???
    private int prev_x, prev_y, PREV_X, PREV_Y;
    public int view_w, view_h;
    private boolean move_view = false;
    private static Thread mainThread = null;
    private static Thread checkstateThread = null;
    private final IBinder m_ServiceBinder = new ServiceBinder();
    private int mfare = 0;
    private int startFare = 0;
    private int callFare = 0;
    private int etcFare = 0;  //?????? ?????? ????????????
    private int nightFare = 0;
    private int complexFare = 0;
    private int suburbFare = 0;
    private int suburbFareRate = 0;
    private String mDriverId ="";


    // Activity ?????? ????????? ?????? ???????????? ?????? ??? ????????? ??????????????? ??????
    public interface mainCallBack {

        void changedDriverId(String driverID);

        //???????????? ????????????
        void serviceBleStatus(boolean bleStatus);

        //????????? ?????? ????????????
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

        //????????? ?????? ????????????
        void serviceMeterMenuState(String menuMsg, int menuType);
    }


    // Activity ??? ????????? callback ??????
    public mainCallBack mCallback = null;

    // callback ?????? ????????????
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
            stopForeground(true); // Foreground service ??????
        }

        if (windowManager != null) {
            if (mView != null) {
                windowManager.removeView(mView); // View ?????????
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


        //status: ???????????? ??????/ ???????????? ??????
        setBleScan();

//        bluetoothConnectThread = new BluetoothConnectThread();
//        bluetoothConnectThread.start();

        return super.onStartCommand(intent, flags, startId);
    }


    //status ---------- ????????? ????????? ----------

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
                    btn_type_status.setText("???\n???");
                    btn_type_status.setBackgroundResource(R.drawable.empty_bg);
                    break;
                case "20":
                    btn_type_status.setText("???\n???");
                    btn_type_status.setBackgroundResource(R.drawable.drive_bg);
                    break;
                case "01":
                    btn_type_status.setText("???\n???");
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
        // inflater ??? ???????????? layout ??? ????????????
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // ?????????????????? ??????

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                // Android O ????????? ?????? TYPE_APPLICATION_OVERLAY ??? ??????
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.TOP | Gravity.LEFT;
        // ?????? ??????

        mView = inflate.inflate(R.layout.layout_show_mainactivity, null);
        // view_in_service.xml layout ????????????
        // mView.setOnTouchListener(onTouchListener);
        // Android O ????????? ??????????????? ?????????????????? ???????????? ?????????. ( TYPE_APPLICATION_OVERLAY ?????? ?????????)

        launcher_icon = (ImageView) mView.findViewById(R.id.launcher_icon);
        show_fare_layout = (LinearLayout) mView.findViewById(R.id.show_fare_layout);
        btn_type_status = (TextView) mView.findViewById(R.id.btn_type_text);
        backgroundIcon = (TextView) mView.findViewById(R.id.btn_img);
        show_fare_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something!
                //?????? ?????? ???????????? ??????
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
//                        windowManager.getDefaultDisplay().getMetrics(matrix);  //?????? ????????? ????????????
//
//                        MAX_X = matrix.widthPixels - mView.getWidth(); //x ????????? ??????
//                        MAX_Y = matrix.heightPixels - mView.getHeight(); //y ????????? ??????
//                    }
//                        moveV = false;
//
//                        START_X = event.getRawX();  //?????? ?????? ???
//                        START_Y = event.getRawY();  //?????? ?????? ???
//                        PREV_X = params.x;        //?????? ?????? ???
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
//                            //???????????? ????????? ?????? ?????? ?????????
//                            params.x = PREV_X + x;
//                            params.y = PREV_Y + y;
//
//                            //????????? ???????????? ?????? ??????
//                            if (params.x > MAX_X) params.x = MAX_X;
//                            if (params.y > MAX_Y) params.y = MAX_Y;
//                            if (params.x < 0) params.x = 0;
//                            if (params.y < 0) params.y = 0;
//
//                            windowManager.updateViewLayout(mView, params);    //??? ????????????
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
                        windowManager.getDefaultDisplay().getMetrics(matrix);  //?????? ????????? ????????????

                        max_x = matrix.widthPixels - mView.getWidth(); //x ????????? ??????
                        max_y = matrix.heightPixels - mView.getHeight(); //y ????????? ??????
                    }
                        moveV = false;

                        start_x = event.getRawX();  //?????? ?????? ???
                        start_y = event.getRawY();  //?????? ?????? ???
                        prev_x = params.x;        //?????? ?????? ???
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

                            //???????????? ????????? ?????? ?????? ?????????
                            params.x = prev_x + x;
                            params.y = prev_y + y;

                            //????????? ???????????? ?????? ??????
                            if (params.x > max_x) params.x = max_x;
                            if (params.y > max_y) params.y = max_y;
                            if (params.x < 0) params.x = 0;
                            if (params.y < 0) params.y = 0;

                            windowManager.updateViewLayout(mView, params);    //??? ????????????
                        }
                        break;
                }
                return false;
            }
        });


        windowManager.addView(mView, params); // ???????????? layout ??? ?????? ??????.

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
//            super.run();  //???????????? ??????

            while (true) {

                Log.d(log, "bleThread start..");

                //??? ???????????? ????????? ?????? ??????
                //???????????? ??? ??????
            if (isRun)
                setBleScan();

                //5??? ?????? ????????????
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }//while

        }//run


        //???????????? ???????????? ?????????
        void startThread() {
            isRun = true;
            Log.d(log, "bleThread restarted.. ");
//            mCallback.serviceBleStatus(true);   //status: ????????? ?????? ??????
        }

        //???????????? ???????????? ?????????
        void stopThread() {
            isRun = false;
            Log.d(log, "bleThread stopped.. ");
//            mCallback.serviceBleStatus(true);   //status: ????????? ?????? ??????
        }
    }//bluetoothConnectThread


    //status --- ???????????? -------------

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
//                Log.d("deviceList", deviceList + "");   //status - ???????????? ????????? ????????? ?????????

                    if (deviceName.contains("AM101"))
                    {
                        Log.d("deviceList_tobePaired", deviceName+":  " +deviceAddress);

                        // gatt ????????? ??????
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




    //???????????? ???????????? ???????????????????????????
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
                        //31??????
                        //?????? ??????..
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
            //????????? ???????????? & ????????? ??? ??????
//            bluetoothConnectThread = new BluetoothConnectThread();
//            bluetoothConnectThread.start();
            bluetoothConnectThread.startThread();
        }else {
            Log.d("isConnected!", isConnected+"");
            //????????? ?????? & ????????? ??? ??????
            bluetoothConnectThread.stopThread();
        }
    }


    public Handler set_meterhandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
//            Log.d("meterHandler_service", msg.what+"");


            //status: ?????? ??? ?????? ????????? ????????????
            if (msg.what == 100) {

                if (setting.OVERLAY == false) {
                    showHideForgroundservice(false, mfare+"", AMBlestruct.AMReceiveFare.M_STATE);
                }else {
                    showHideForgroundservice(true, mfare+"", AMBlestruct.AMReceiveFare.M_STATE);
                }
            }else if (msg.what == 101) {
                mCallback.changedDriverId(AMBlestruct.AMReceiveFare.M_DRIVER_ID);
            }

            //status: ???????????? ?????????
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


                //status: ????????? ???????????? ?????????
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE) {

                startFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_START_FARE);   //????????????   //status: ????????? ??? ??????
                callFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_CALL_FARE);     //????????????
                etcFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_ETC_FARE);       //????????????/????????????
                complexFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
                suburbFareRate = Integer.parseInt(AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);
                Log.d("rateCheck_1", suburbFareRate+"");

//                backgroundIcon.setText(startFare);

                if (AMBlestruct.AMReceiveFare.M_DRIVER_ID != null) {
                    mDriverId = AMBlestruct.AMReceiveFare.M_DRIVER_ID;
//                    mCallback.serviceMeterState
                }

                //????????????
                if (AMBlestruct.AMReceiveFare.M_NIGHT_FARE.equals("1")) {  //??????
                    //20% ?????????
                    nightFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_NIGHT_FARE);
                }else if (AMBlestruct.AMReceiveFare.M_NIGHT_FARE.equals("0")) { //??????
                    nightFare = 0;
                }

                //???????????? ??????
                if (AMBlestruct.AMReceiveFare.M_COMPLEX_FARE.equals("1")) {  //??????
                    complexFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
                }else if (AMBlestruct.AMReceiveFare.M_COMPLEX_FARE.equals("0")) {  //??????
                    complexFare = 0;
                }

                //????????????
                if (AMBlestruct.AMReceiveFare.M_SUBURB_FARE != null) {   //??????
                    suburbFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_SUBURB_FARE);
                }else {  //??????
                    suburbFare = 0;
                }

                //?????????%
                if (AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE.equals("1")) {
                    Log.d("rateCheck_", AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE+"");
                    suburbFareRate = Integer.parseInt(AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);
                    Log.d("rateCheck", suburbFareRate+"");
                }else if (AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE.equals("0")) {
                    suburbFareRate = 0;
                }



                //????????? ?????? ????????? ??????
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



                if (AMBlestruct.AMReceiveFare.M_STATE.equals("01")) {  //??????

                    mCallback.serviceMeterState(mDriverId, 01, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("05")) { //??????

                    mCallback.serviceMeterState(mDriverId,05, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("20")) { //??????

                    mCallback.serviceMeterState(mDriverId,20, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("30")) { //??????

                    mCallback.serviceMeterState(mDriverId,4, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("02")) { //????????????

                    mCallback.serviceMeterState(mDriverId,02, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("50")) { //??????

                    mCallback.serviceMeterState(mDriverId,6, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("51")) { //??????

                    mCallback.serviceMeterState(mDriverId,7, mfare, startFare, callFare, etcFare, nightFare, complexFare, suburbFare, suburbFareRate);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("99")) {  //????????? ??????

//                    Log.d("btnCehck_????????????",AMBlestruct.AMReceiveFare.M_STATE);

                }


                //???????????????(19) ???????????? ??? ????????? ?????? -> ????????????(69) ?????????
                if (AMBlestruct.AMReceiveFare.M_STATE != null) {
                    if (amBluetoothManager != null) {
                        amBluetoothManager.responseMeterState("69","00");
                    }
                }else {
                    if (amBluetoothManager != null) {
                        amBluetoothManager.responseMeterState("69","??????");
                    }
                }


                //status: ????????? ?????? ?????????
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE) {

                Log.d("code43_msg",AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE+"" );  //????????? 49 -> 1:??????

                mCallback.serviceMeterMenuState(AMBlestruct.AMReceiveMenu.MENU_MSG, AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE);

            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_INPUT_MENU_STATE) {  //????????? ??????????????? ????????? ?????????

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

    //me: ????????? ???????????? -> ??????????????? ?????????
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
