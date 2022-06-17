package com.konai.appmeter.driver_am.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.view.MainActivity;

import java.util.List;

public class AwindowService extends Service {

    String TAG = "WindowService";
    NotificationManager notificationManager;
    private LocationManager locationManager;
    private static AMBluetoothManager mBluetoothLE = null;
    public WindowManager windowManager;
    private BluetoothAdapter mBluetoothAdapter = null;
    public View mView;
    private Handler mHandler;
    private String mDrvnum = "";

    private static Thread mainThread = null;
    private static Thread checkstateThread = null;

    public int lbs_initx = -1;
    public int lbs_inity = -1;
    public int lbs_initw = 300;
    public int lbs_inith = 138;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnbind(Intent intent) {
//        close();
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mBluetoothLE = new AMBluetoothManager(this, AwindowService.this);
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService();

            _overlaycarstate();
        }


        if (setting.gUseBLE == true) {
            Log.d("start_scan", "gBLE == true" );
            setBleScan();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        startForegroundService();

//        _overlaycarstate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); // Foreground service 종료
        }

        if(windowManager != null) {
            if(mView != null) {
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
                        CHANNEL_ID+" "+TITLE,
                        NotificationManager.IMPORTANCE_LOW);
                channel.setSound(null, null);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
            startForeground(1, notification);

        }else {


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
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                // Android O 이상인 경우 TYPE_APPLICATION_OVERLAY 로 설정
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;
        // 위치 지정

        mView = inflate.inflate(R.layout.layout_show_mainactivity, null);
        // view_in_service.xml layout 불러오기
        // mView.setOnTouchListener(onTouchListener);
        // Android O 이상의 버전에서는 터치리스너가 동작하지 않는다. ( TYPE_APPLICATION_OVERLAY 터치 미지원)


        final ImageView btn_img =  (ImageView) mView.findViewById(R.id.btn_img);
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test","onClick ");
                // do something!
                //다시 메인 액티비티 열기
                show_mainActivity();
            }
        });

        // btn_img 에 android:filterTouchesWhenObscured="true" 속성 추가하면 터치리스너가 동작한다.
        btn_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.d("test","touch DOWN ");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("test","touch UP");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("test","touch move ");
                        break;
                }
                return false;
            }
        });

//        showhideCallBtn(false);

        Log.d("check_params", mView.getLayoutParams()+""); //null..

        windowManager.addView(mView, params); // 윈도우에 layout 을 추가 한다.
    }

    private void show_mainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }//show_mainActivity

    private void showhideCallBtn(boolean show) {

        if (mView == null) { return; }

        if (show == true) {
            mView.setVisibility(View.VISIBLE);
            windowManager.updateViewLayout(mView, mView.getLayoutParams());
        }else {
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

            Log.d("start_scan_device", "isEnabled");  //y

            Log.d("start_scan_device",setting.BLUETOOTH_DEVICE_NAME); //null


//            if (setting.BLUETOOTH_DEVICE_ADDRESS.equals("") == false && setting.BLUETOOTH_CARNO.equals(drv))
            if (setting.BLUETOOTH_DEVICE_ADDRESS.equals("") == false && setting.BLUETOOTH_CARNO.equals(mDrvnum)) {
                Log.d("start_scan_device", setting.BLUETOOTH_DEVICE_NAME); //null

                setting.BLUETOOTH_FINDEND = true;
                scanLeDevice(true);
            }else {
                scanLeDevice(true);
            }


        }
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            //stop scanning after a pre-defined scan period
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.e("scan_run", "stopScanBLE");

                    stopScanBLE();

                }
            }, setting.SCAN_PERIOD);

            Log.d("scan_device", "start scan ble - auto conn");

            startScanBLE();

        } else {

            stopScanBLE();
        }
    }//scanLeDevice



    private void startScanBLE() {

        setting.BLESCANNING_MODE = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("scan_ble_", "LOLLIPOP");
//            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallBack);
        }
    }




    private void stopScanBLE() {

        Log.d("stopScanBLE", "stopScanBLE");

        setting.BLESCANNING_MODE = false;
        mHandler.removeCallbacksAndMessages(null);

        if (mBluetoothAdapter.isEnabled() == false) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallBack);
        }

    }



    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d("scan_mScanCallback_1", "111");

            BluetoothDevice device = result.getDevice();
            Log.d("scan_mScanCallback", device + "");  //52:43:BC:9D:5C:C5

            try {
//                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions`
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
                final String deviceName = device.getName();

                if (deviceName.contains("AM")) {

                    setting.BLUETOOTH_DEVICE_ADDRESS = device.getAddress();
                    setting.BLUETOOTH_DEVICE_NAME = device.getName();
//                    setting.BLUETOOTH_CARNO = mDrvnum;
                    setting.BLUETOOTH_CARNO = deviceName.substring(5,9);  //0001
                    Log.d("scan_address", setting.BLUETOOTH_DEVICE_ADDRESS); //3C:A5:49:DE:B7:97
                    Log.d("scan_name", setting.BLUETOOTH_DEVICE_NAME);       //AM1010001
                    Log.d("scan_carno", setting.BLUETOOTH_CARNO);

                    stopScanBLE();

                    //status = save_info

                    //gatt 서버에 연결
                    connectAM();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e("scan_result - Results", results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("scan_result - Results", errorCode + ": failed");
        }
    };//mScanCallback




    private BluetoothAdapter.LeScanCallback mLeScanCallBack =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    try {
//                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return;
//                        }
                        final String deviceName = device.getName();
                        if (deviceName != null && deviceName.equals("") == false) {
                            if (deviceName.contains(mDrvnum)) {
                                setting.BLUETOOTH_DEVICE_ADDRESS = device.getAddress();
                                setting.BLUETOOTH_DEVICE_NAME = device.getName();

                                Log.d("scan_deviceAddress", device.getAddress());
                                Log.d("scan_deviceName", device.getName());
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
            };//mLeScanCallBack



    public boolean connectAM() {

        if (mBluetoothLE != null)

            Log.d("connectAM","connectAM");

//            mBluetoothLE.scanLeDevice(true);
            mBluetoothLE.connectBLE();

        return true;
    }





}
