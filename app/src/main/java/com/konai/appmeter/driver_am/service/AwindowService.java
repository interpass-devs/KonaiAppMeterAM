package com.konai.appmeter.driver_am.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.view.MainActivity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AwindowService extends Service {

    String TAG = "WindowService";
    NotificationManager notificationManager;
    private LocationManager locationManager;
    private static AMBluetoothManager amBluetoothManager = null;
    public BluetoothDevice bluetoothDevice = null;
    static final UUID BT_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public Set<BluetoothDevice> bluetoothDeviceSet;
    public ArrayList<String> deviceList = new ArrayList<>();
    public WindowManager windowManager;
    private BluetoothAdapter mBluetoothAdapter = null;
    public View mView;
    private Handler mHandler;
    private String mDrvnum = "";
    public String log="log_";

    private static Thread mainThread = null;
    private static Thread checkstateThread = null;

    private final IBinder m_ServiceBinder = new ServiceBinder();
    private int mfare = 0;

//    BlockingQueue<CalQueue> mCalblockQ = new ArrayBlockingQueue<CalQueue>(10);


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

    // Activity 에서 정의해 해당 서비스와 통신 할 함수를 추상함수로 정의
    public interface mainCallBack {
        void serviceBleStatus(boolean bleStatus);  //블루투스 수신상태
        void serviceMeterState(int btnType, int mFare);  //미터기 버튼 수신상태
        void serviceMeterMenuState(String menuMsg);  //미터기 메뉴 수신상태
//        void serviceMeterMenuState(ArrayList<String> arrayList, int listsize);
    }

    // Activity 와 통신할 callback 객체
    public mainCallBack mCallback = null;

    // callback 객체 등록함수
    public void registerCallback(mainCallBack callBack) {
        Log.d("mainCallBack-> ", "registerCallback");
        mCallback = callBack;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        amBluetoothManager = new AMBluetoothManager(this, AwindowService.this);
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//            Log.d("start_scan", "oooo");

            startForegroundService();

            _overlaycarstate();
        }


        mainThread = new Thread(new MainThread());
        mainThread.start();


        if (setting.gUseBLE == true) {
            setBleScan();  //me: original
//            startPairingBluetooth();

        } else {

//            setBleScan();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        connectAM();

//        startForegroundService();

//        _overlaycarstate();
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


        params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        // 위치 지정

        mView = inflate.inflate(R.layout.layout_show_mainactivity, null);
        // view_in_service.xml layout 불러오기
        // mView.setOnTouchListener(onTouchListener);
        // Android O 이상의 버전에서는 터치리스너가 동작하지 않는다. ( TYPE_APPLICATION_OVERLAY 터치 미지원)


        final ImageView btn_img = (ImageView) mView.findViewById(R.id.btn_img);
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "onClick ");
                // do something!
                //다시 메인 액티비티 열기
                show_mainActivity();
            }
        });

        // btn_img 에 android:filterTouchesWhenObscured="true" 속성 추가하면 터치리스너가 동작한다.
        btn_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("test", "touch DOWN ");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("test", "touch UP");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("test", "touch move ");
                        break;
                }
                return false;
            }
        });

//        showhideCallBtn(false);

//        Log.d("check_params", mView.getLayoutParams() + ""); //null..

        windowManager.addView(mView, params); // 윈도우에 layout 을 추가 한다.
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


    //status --- 블루투스 -------------

    public void setBleScan() {

        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {

//            Log.d("start_scan_device", "isEnabled");  //y
//            Log.d("start_scan_device", setting.BLUETOOTH_DEVICE_NAME); //null


            //me: new
            startPairingBluetooth();

        }
    }


    private void startPairingBluetooth() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : bluetoothDeviceSet) {
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            deviceList.add(deviceName + ": " + deviceAddress);
            Log.d("deviceList", deviceList + "");   //status - 여러가지 등록된 블루투스 기기들이 있을 경우 하나만 페어링 되게 설정이 안되어있음..

            if (deviceName.contains("AM101")) {
                Log.d("deviceList_tobePaired", deviceName);
                setting.BLUETOOTH_DEVICE_NAME = deviceName;
                setting.BLUETOOTH_DEVICE_ADDRESS = deviceAddress;
                Log.d("deviceList_paired", setting.BLUETOOTH_DEVICE_NAME + ": " + setting.BLUETOOTH_DEVICE_ADDRESS);  //AM1010003: 3C:A5:51:85:1A:36

                //status - 현재는 thread 없이 바로 갓서버에 연결

                //gatt 서버에 연결
                connectAM();
            }
        }
    }

    public boolean connectAM() {

        if (amBluetoothManager != null) {

            amBluetoothManager.connectAM();
        }else {
//            Log.d("connectAM","connectAM null");
        }
        return true;
    }


    public Handler set_meterhandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
//            Log.d("meterHandler_service", msg.what+"");

            //status: 블루투스 상태값
            if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE) {

                if (setting.BLE_STATE == true) {
                    mCallback.serviceBleStatus(true); //null obj??
                }else {
                    mCallback.serviceBleStatus(false);
                }

                //status: 빈차등 현재상태 수신값
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE) {

//                Log.d("mfare_check", AMBlestruct.AMReceiveFare.M_START_FARE+" + "+AMBlestruct.AMReceiveFare.M_CALL_FARE+" + "+AMBlestruct.AMReceiveFare.M_ETC_FARE);

                if (AMBlestruct.AMReceiveFare.M_START_FARE != null) {
                    mfare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_START_FARE);
                }else {
                    mfare = 0;
                }

                if (AMBlestruct.AMReceiveFare.M_STATE.equals("1")) {
                    int startFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_START_FARE);
                    int callFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_CALL_FARE);
                    int etcFare = Integer.parseInt(AMBlestruct.AMReceiveFare.M_ETC_FARE);

                    mfare = startFare + callFare + etcFare;

                    mCallback.serviceMeterState(AMBlestruct.MeterState.PAY, mfare);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("2")) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.EMPTY, mfare);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("3")) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.DRIVE, mfare);

                }else if (AMBlestruct.AMReceiveFare.M_STATE.equals("4")) {

                    mCallback.serviceMeterState(AMBlestruct.MeterState.CALL, mfare);
                }
                //status: 빈차등 메뉴 수신값
            }else if (msg.what == AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE) {

                ArrayList<String> arrList = new ArrayList<>(Arrays.asList(AMBlestruct.AMReceiveMenu.MENU_MSG.split("\n")));

//                Log.d("arrList", arrList.toString());
//                Log.d("arrList", arrList.size()+"");

                mCallback.serviceMeterMenuState(AMBlestruct.AMReceiveMenu.MENU_MSG);

            }
        }
    };

    //me: 버튼값 업데이트 -> 빈차등으로 보내기
    public boolean update_BLEmeterstate(String sstate) {
        if (amBluetoothManager != null) {
            amBluetoothManager.update_AMmeterstate(sstate);
        }
        return true;
    }

    public boolean menu_meterState(String requestCode, String menuType) {
        Log.d("requestCode>", requestCode+", "+menuType);
        if (amBluetoothManager != null) {
            amBluetoothManager.menu_AMmeterState(requestCode, menuType);
        }
        return true;
    }

    class MainThread implements Runnable {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {

                }catch (Exception e) {
                    Log.e("mainThreaad_error", e.toString());
                }
            }

        }
    }




}
