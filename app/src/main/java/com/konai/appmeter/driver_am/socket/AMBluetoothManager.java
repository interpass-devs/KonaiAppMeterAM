package com.konai.appmeter.driver_am.socket;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.konai.appmeter.driver_am.service.AwindowService;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.view.MainActivity;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AMBluetoothManager {

    private Handler mHandler;
    private String mDrvnum = "";
    String ble ="connectBLE";
    String bleGatt = "connectGatt";
    String broadCast = "connectBroadcast";
    com.konai.appmeter.driver_am.setting.setting setting = new setting();
    private Context mContext;
//    public static LocService m_Service = null;
    public static AwindowService windowService = null;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;
    public static BluetoothGattService m_gattService = null;
    private static BluetoothGattCharacteristic m_gattCharTrans = null;
    private static BluetoothGattCharacteristic m_gattCharConfig = null;
    private static BluetoothGattDescriptor m_descriptor = null;
    private String mBluetoothAddress = "";
    private int mConnectionState = STATE_DISCONNECTED;
    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

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

    private AMPacket topkt = null;
    private AMPacket outpkt = null;
    private static byte[] packetdata;
    public byte[] outpacket = null;



    public AMBluetoothManager(Context context, AwindowService service) {
        mContext = context;
        windowService = service;
    }

    public AMBluetoothManager(Context context) {
        mContext = context;
    }

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

            Log.d("scan_device", "start scan ble");

            startScanBLE();

        } else {

            stopScanBLE();
        }
    }//scanLeDevice


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

    private void startScanBLE() {

        setting.BLESCANNING_MODE = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("scan_ble_", "LOLLIPOP");
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallBack);
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            Log.d("scan_mScanCallback", device + "");  //52:43:BC:9D:5C:C5

            try {

                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions`
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
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

                    //gatt 서버에 연결
                    connectBLE();

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

                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
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
        if (setting.gUseBLE) {
            return  connectBLE();
        }
        return true;
    }




    public boolean connectBLE() {
        //bluetooth manager
        if (mBluetoothManager == null) {

            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (mBluetoothManager == null) {
            Log.e(ble, "unable to initialize bluetooth manager"); //y
            return false;
        }
        //bluetooth adapter
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();  //y
        }

        if (mBluetoothAdapter == null) {
            Log.e(ble,"unable to initialize bluetooth adapter");
            return false;
        }else {
            Log.d(ble,"Able to initialize bluetooth adapter!"); //y
            Log.d(ble, mBluetoothAdapter+"");
            Log.d(ble, mBluetoothManager.getAdapter()+"");
        }

        if (mBluetoothAdapter == null || setting.BLUETOOTH_DEVICE_ADDRESS.equals("")) {
            Log.e(ble, "unable to initialize bluetoothAdapter & address");
            return false;
        }

        //bluetooth device
        if (mBluetoothDevice == null) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(setting.BLUETOOTH_DEVICE_ADDRESS);
            Log.e(ble, "bluetooth device null");
        }else {
            Log.d(ble, "bluetooth device not null!");
        }

        if (mBluetoothDevice == null) {
            Log.e(ble, "device not found. unable to connect." );
//            return false;
        }else {
            Log.d(ble, "device not null. so device found" );
            Log.d(ble, mBluetoothDevice+"");                 //3C:A5:51:85:1A:36
            Log.d(ble, setting.BLUETOOTH_DEVICE_ADDRESS+""); //3C:A5:51:85:1A:36
        }

        //connect to device
        //저전력 블루투스 기기와 연결하려면 GATT 서버에 연결해야함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }

        mBluetoothAddress = setting.BLUETOOTH_DEVICE_ADDRESS;
        mConnectionState = STATE_CONNECTING;

        return true;
    }


    //gatt server 연결
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_CONNECTED;
                        Log.d("gatt_", "connected to gatt server");  //y

                        //빈차등 연결 성공
                        //블루투스 아이콘 색 변경
                        setting.BLE_STATE = true;
                        //status - 아이콘 변경
//                        iv_ble.setBackgroundResource(R.drawable.bluetooth_green);
//                        Toast.makeText(mContext, R.string.ble_connected, Toast.LENGTH_SHORT).show();
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Log.i("gatt_i", "Server discovery-> " + mBluetoothGatt.discoverServices());  //true
                        broadcastUpdate(intentAction);
                    }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_DISCONNECTED;
                        broadcastUpdate(intentAction);
                        Log.e("gatt_e", "disconnected to gatt server");
                        Log.i("gatt_ei", "Attempting to start service discovery-> " + mBluetoothGatt.discoverServices());

                        //빈차등 연결 실패
                        //블루투스 아이콘 색 변경
                        setting.BLE_STATE = false;
                        //status - 아이콘 변경
//                        iv_ble.setBackgroundResource(R.drawable.bluetooth_blue);
//                        Toast.makeText(mContext, R.string.ble_disconnected, Toast.LENGTH_SHORT).show();

                        connectBLE();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        findGattServices(mBluetoothGatt.getServices());

                        mBluetoothGatt.requestMtu(512);  //사이즈

                    }else {
                        Log.w("gatt_w", "onServicesDiscovered received: "+status);
                    }
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("gatt_d", "onMtuChanged: "+mtu);  //244

                        //me 되살리기
                        initGattCharaceristic();
                    }
                }

                //데이터 요청시 들어오는 곳
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
            }; //mGattCallback..


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }


    private void findGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        for (BluetoothGattService gattservice : gattServices) {
            uuid = gattservice.getUuid().toString();

            //Loops through available GATT Service.
            if (uuid.equals(setting.UUID_SERVICE.toString())) {

                m_gattService = gattservice;

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattservice.getCharacteristics();

                //Loops through available characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();

                    if (uuid.equals(setting.UUID_TRANJACTION.toString())) {
                        m_gattCharTrans = gattCharacteristic;
                        for (BluetoothGattDescriptor descriptor : gattCharacteristic.getDescriptors()) {
                            if (descriptor.getUuid().toString().equals(setting.UUID_DESCRIPTION.toString())) {

                                m_descriptor = descriptor;
                                Log.e("gatt_descriptor", descriptor.getUuid().toString());  //00002902-0000-1000-8000-00805f9b34fb


                            }

                        }
                    }else if (uuid.equals(setting.UUID_CONFIGURE.toString())) {
                        m_gattCharConfig = gattCharacteristic;
                    }
                    break;
                }

            }
        }
    }


    private void initGattCharaceristic() {

        if (m_gattCharTrans != null) {

            final int charaProp = m_gattCharTrans.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.

//							mBluetoothGatt.setCharacteristicNotification(m_gattCharTrans, false);

//							mBluetoothGatt.readCharacteristic(m_gattCharTrans);
            }

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                mBluetoothGatt.setCharacteristicNotification(m_gattCharTrans, true);

            }

            if(m_descriptor != null) {
                // This is specific to Heart Rate Measurement.
                m_descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(m_descriptor);
            }
        }

        if(m_gattCharConfig != null)
        {
            //todo.
        }
    }


    //앱 -> 빈차등 상태값 요청
    synchronized public boolean makePacketSendRequest(String requestCode) {

        byte[] mData = null;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02);  //STX

        Log.d("send_requestconde", requestCode);

        switch (requestCode) {

            case AMBlestruct.curReponseCode:

                topkt.SetString(packetdata, "15");

                topkt.SetString(packetdata, AMBlestruct.mSState);

                Log.d("send_빈차등차량상태전송", AMBlestruct.mSState);

                break;
        }

        return true;
    }


}



