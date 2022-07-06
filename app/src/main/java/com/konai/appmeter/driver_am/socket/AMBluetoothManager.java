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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.ByteArrayBuffer;
import com.konai.appmeter.driver_am.service.AwindowService;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.view.MainActivity;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.UnsupportedEncodingException;

public class AMBluetoothManager {

//    BleConnThread bleConnThread = new BleConnThread();
    int outDataIndex;
    byte[] outputData;
    private Handler mHandler;
    private String mDrvnum = "";
    String ble ="connectBLE";
    com.konai.appmeter.driver_am.setting.setting setting = new setting();
    private Context mContext;

    public static AwindowService windowService = null;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    Set<BluetoothDevice> bluetoothDeviceSet;
    String log = "log_";
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
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    private AMPacket topkt = null;
    private AMPacket outpkt = null;
    private static byte[] packetdata;
    ByteArrayBuffer outbuffer = null;



    public AMBluetoothManager(Context context, AwindowService service) {
        mContext = context;
        windowService = service;

        if (topkt == null) {
            topkt = new AMPacket(); //데이터 보낼때 (아스키 변환)
        }

        if (outpkt == null) {
            outpkt = new AMPacket(); //데이터 받을때 (아스키 변환)
        }

        if (packetdata == null) {
            packetdata = new byte[2048];
        }

        if (outputData == null) {
            outputData = new byte[2048];
        }
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

    public boolean connectBLE(String deivceName, String deviceAddress)
    {
        setting.BLUETOOTH_DEVICE_ADDRESS = deviceAddress;
        setting.BLUETOOTH_DEVICE_NAME = deivceName;

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
        } else {
            Log.d(ble,"Able to initialize bluetooth adapter!"); //y
        }

        if (mBluetoothAdapter == null || setting.BLUETOOTH_DEVICE_ADDRESS.equals("")) {
            Log.e(ble, "unable to initialize bluetoothAdapter & address");
            return false;
        }

        //me: original
        //bluetooth device
        if (mBluetoothDevice == null) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.e(ble, "bluetooth device null");
        } else {
            Log.d(ble, "bluetooth device not null! "+deviceAddress);
        }

        if (mBluetoothDevice == null) {
            Log.e(ble, "device not found. unable to connect." );
//            return false;
        } else {
            Log.d(ble, deviceAddress+""); //3C:A5:51:85:1A:36
        }

        //connect to device
        //블루투스 페어링 - 기기와 연결하려면 GATT 서버에 연결해야함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mGattCallback);
        }

        mBluetoothAddress = deviceAddress;
        mConnectionState = STATE_CONNECTING;

        Log.d(log+"blePairing", mBluetoothAddress+"!!");

//        makepacketsend("15");

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
                        Log.d(log+"ble", "connected to gatt server...");  //y

                        //앱그리기 숨기기 유무기능
                        setting.OVERLAY = false;
                        windowService.set_meterhandler.sendEmptyMessage(100);
                        windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE);
                        setting.BLE_STATE = true;

//                        try {
//                            wait(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        AMBlestruct.mSState = "00";
//                        makepacketsend(AMBlestruct.APP_REQUEST_CODE);  //"15"   //현재상태 전송

                        Log.i(log+"ble", "Server discovery-> " + mBluetoothGatt.discoverServices());  //true
                        broadcastUpdate(intentAction);


                    }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_DISCONNECTED;
                        broadcastUpdate(intentAction);
                        Log.e(log+"ble", "disconnected to gatt server");

                        windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE);
                        setting.BLE_STATE = false;

                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        findGattServices(mBluetoothGatt.getServices());

                        mBluetoothGatt.requestMtu(512);  //사이즈

                    }else {
                    }
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);

                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        initGattCharaceristic();
                    }
                }

                //데이터 요청시 들어오는 곳
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            AMBleReciveData(ACTION_DATA_AVAILABLE, characteristic);
//                            Log.d(log, characteristic+"--");
                        }
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    AMBleReciveData(ACTION_DATA_AVAILABLE, characteristic);
//                    Log.d("receive_change", characteristic+"");

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
                                Log.e(log, "descriptor:  "+descriptor.getUuid().toString());  //00002902-0000-1000-8000-00805f9b34fb
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


    public void sendAttendanceState(String requestCode, String driverID) {
        AMBlestruct.mSState = driverID;
        Log.d("check_attendance",AMBlestruct.mSState);
        makepacketsend("48");
    }


    //me: 버튼값(빈차/주행/지불/호출) 업데이트 -> 빈차등으로 보내기
    public void update_AMmeterstate(String sstate)
    {
        AMBlestruct.mSState = sstate;

        makepacketsend(AMBlestruct.APP_REQUEST_CODE); //"15"

    }

    public void add_fareState(String requestCode, String addFare) {
        AMBlestruct.B_ADDFARE = addFare;
        Log.d("add_fare_check", AMBlestruct.B_ADDFARE);
        makepacketsend(requestCode);
    }

    //69
    public void responseMeterState(String responseCode, String result) {

        AMBlestruct.appMeterResult = result;

        makepacketsend(responseCode);
    }

    public void menu_AMmeterState(String sstate, String menutype, String pos) {

        AMBlestruct.mSState = sstate;
        AMBlestruct.MenuType.TYPE = menutype;
        AMBlestruct.MenuType.MENU_CONTENT = pos;

        makepacketsend(AMBlestruct.mSState);
    }


    //"47"
    public void menu_input_AMmeterState(String requestCode, String inputCheck, int inputLength, String inputMsg) {

        AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE = inputCheck;  //입력여부
        AMBlestruct.AMReceiveMenu.MENU_INPUT_LENGTH = String.format("%02d", inputLength);
        AMBlestruct.MenuType.MENU_CONTENT = inputMsg;  //알림창에 입력한 내용

        makepacketsend(requestCode);
    }


    //앱 -> 빈차등 상태값 요청
    //thread 없이 바로연결
    synchronized public boolean makepacketsend(@NonNull String requestCode) {

        byte[] mData = null;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02);  //STX

        Log.d("현재_requestCode", requestCode+",  "+AMBlestruct.mSState);

        switch (requestCode) {

            case AMBlestruct.APP_REQUEST_CODE:  //"15"
                topkt.SetString(packetdata, AMBlestruct.APP_REQUEST_CODE);  //요청코드
                topkt.SetString(packetdata, getCurDateString()); //날짜시간
                topkt.SetString(packetdata, AMBlestruct.mSState);  //상태요청
                Log.d("15=>  ", AMBlestruct.APP_REQUEST_CODE+",  "+getCurDateString()+",  "+AMBlestruct.mSState);
                break;

            case "69":
                topkt.SetString(packetdata, "69");
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.appMeterResult);
                break;

            case AMBlestruct.APP_MENU_REQUEST_CODE:  //"41"
                topkt.SetString(packetdata, AMBlestruct.APP_MENU_REQUEST_CODE);  //"41"
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE); //0-닫기/1-메뉴/2-정보출력/3-정보출력+숫자키버튼
                break;

            case AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE:  //"43"
                topkt.SetString(packetdata, AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE);
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE);
                topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_CONTENT);  //선택번호
                break;

            case "47":
                topkt.SetString(packetdata,"47");
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE);     //입력유무
                topkt.SetString(packetdata, AMBlestruct.AMReceiveMenu.MENU_INPUT_LENGTH); //입력 데이터길이
                topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_CONTENT);  //입력내용
                break;

            case "20":
                topkt.SetString(packetdata,"20");
                topkt.SetString(packetdata,getCurDateString());
                topkt.SetString(packetdata,AMBlestruct.B_ADDFARE);
                break;

            case "48":
                topkt.SetString(packetdata,"48");
                topkt.SetString(packetdata,getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.mSState);
                break;

        }

        topkt.SetString(packetdata, topkt.GetAMBleCRC(packetdata));
        topkt.Setbyte(packetdata, (byte) 0x03);
        Log.d("send_char","ttt");
        mData = new byte[topkt.point];
        Log.d("mData", mData.toString());
        Log.d("mData_packetdata", packetdata.toString());
        System.arraycopy(packetdata, 0, mData, 0, topkt.point);
        write(mData);

        return true;
    }


    public boolean write(byte[] data) {

        data[0] = 0x02;

        data[data.length - 1] = 0x03;


        if(setting.gUseBLE) {
            if (mBluetoothGatt == null || m_gattCharTrans == null) {
//                Log.d(TAG, "# BluetoothGatt not initialized");
                return false;
            }

            m_gattCharTrans.setValue(data);
            m_gattCharTrans.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(m_gattCharTrans);
        }

        Log.d(log+"send_char", m_gattCharTrans.toString());

        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));
        Log.d(log+"send_builder", "++++++++send(" + data.length + ")" + stringBuilder.toString());

        return true;
    }

    //Outdata Que
    public class OutDataQue {
        byte[] outdata;
    }


    private void AMBleReciveData(final String action, final BluetoothGattCharacteristic characteristic) {

        final Intent i = new Intent(action);

        try {

//            StringBuilder builder = new StringBuilder();
//            builder.append(characteristic);

            Log.d(log+"receive_outdata", characteristic.getValue()+"");

            Log.d(log+"menu_outdata", characteristic.getValue()+"");

            byte[] outdata = characteristic.getValue();

//            Log.d("outdata_before_que", outdata+"");
////
//            OutDataQue que = new OutDataQue();
//
//            que.outdata = outdata;
//
//            Log.d("outdata_que", que.outdata+"");


            //me:
            //  outdata를 Q에 먼저 쌓아넣고 Q에 데이터가 있는지 없는지를 Thread 안에서 확인하고
            // 02/ 03 로 아래처럼 구분하여 데이터 한줄을 만든 뒤  ->  파싱

            if (outdata != null && outdata.length > 0) {


//                if (true) {
                    final StringBuilder stringBuilder = new StringBuilder(outdata.length);
                    for (byte byteChar : outdata) {

                        switch (byteChar) {
                            case 0x02:
//                                outputArray = outdata;
//                                System.arraycopy(outdata, 0, ,);
                                outDataIndex = 0;
                                outputData[outDataIndex++] = byteChar;
                                break;

                            case 0x03:
                                outputData[outDataIndex++] = byteChar;
                                Log.d(log+"outputData", outDataIndex+"");  //88

                                //me: 바로 파싱..
                                // parsingend_AMBle(outdata, outdata.length);
                                parsingend_AMBle(outputData, outDataIndex);  //형변환 파싱
                                break;

                                default:
                                    outputData[outDataIndex] = byteChar;
                                    outDataIndex++;
                                    break;
                        }
                    }

                }

//            }

            outdata = null;

        }catch (Exception e) {
            e.printStackTrace();
            Log.d(log+"receive_error", e.toString());
        }
    }



    //필요없음
    public void checkgetData_AMBle(byte[] bytetmp, int bytesRead) {

        byte[] outPack;
        byte[] outdata;

        int stCnt = 0;
        int enCnt = 0;

        int reStCnt = 0;

        boolean stIdx = false;
        boolean edIdx = false;

        for(int i=0; i<bytesRead; i++)
        {
            if(bytetmp[i] == (byte)0x02)
            {
                if(!stIdx) {
                    stIdx = true;
                    stCnt = i;
                }
            }
            else if(bytetmp[i] == (byte)0x03)
            {
                if(i>0)
                {
                    edIdx = true;
                    enCnt = i;
                    reStCnt = i+1;
                }
            }

            if(stIdx && edIdx)
            {
                if(enCnt - stCnt + 1 > 0) //20220307 tra..sh
                {
                    outdata = new byte[enCnt - stCnt + 1];
                    System.arraycopy(bytetmp, stCnt, outdata, 0, enCnt - stCnt + 1);
                    parsingend_AMBle(outdata, outdata.length);
//                Log.d(TAG, "dtgform.distance parsingend_AMBle end");
                    stIdx = false;
                    edIdx = false;
                    outdata = null;
                }
            }
        }

        if(bytesRead - reStCnt > 0)
        {
            outPack = new byte[bytesRead - reStCnt];
            System.arraycopy(bytetmp, reStCnt, outPack, 0, bytesRead - reStCnt);
            outbuffer.clear();
            outbuffer.append(outPack, 0, outPack.length);
            outPack = null;

        }
        else
            outbuffer.clear();

    }

//xptmxm
    synchronized public void parsingend_AMBle(byte[] outdata, int packetlen)
    {
        byte[] bytePacket = new byte[packetlen];
        System.arraycopy(outdata, 0, bytePacket, 0, packetlen);

        // for Debug
        String strPacket=null;
        try {
            strPacket = new String(bytePacket, "KSC5601"); // 수신된 데이터 스트링으로 변환
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("strPacket", strPacket);   //1920220705162428서울12가00032000000000000000000000020220705162424000000000000000000197001010000000000000000000000000000000000008B
        // end

        String rcvCode = getString(bytePacket, 1, 2);
        Log.d("현재_getCode", rcvCode);  //menu- 42

        // 아래 코드 확인 필요.
        if (outpkt.GetAMBleCRC(outdata, packetlen).equals(String.format("%c%c", outdata[packetlen -3], outdata[packetlen - 2])) == false)
        {
            Log.d("parsing_", "========receive(" + rcvCode + ")" + outpkt.GetAMBleCRC(outdata, packetlen)
                    + "  " + String.format("-------%c%c", outdata[packetlen -3], outdata[packetlen - 2]));
        }

        // 수신날짜
        String rcvDate = getString(bytePacket, 3, 12);

        switch (rcvCode) {
            case "98":
//                Log.d("98_requestcode", getString(bytePacket, 14))
                break;

            case AMBlestruct.METER_REQUEST_CODE: // "19" 택시요금수신, 미터기모드 응답
            AMBlestruct.AMReceiveFare.M_RECEIVE_TIME = rcvDate;                                     // 날짜시간
            AMBlestruct.AMReceiveFare.M_CARNUM = getString(bytePacket, 17, 12);         // 차량번호
            AMBlestruct.AMReceiveFare.M_STATE = getString(bytePacket, 29, 2);           // 버튼값   //ME: 여기서 받은 버튼값을 MainActivity 에서 확인하여 화면전환..
            AMBlestruct.AMReceiveFare.M_START_FARE = getString(bytePacket, 31, 6);      // 승차요금
            AMBlestruct.AMReceiveFare.M_CALL_FARE = getString(bytePacket, 37, 4);       // 호출요금
            AMBlestruct.AMReceiveFare.M_ETC_FARE = getString(bytePacket, 41, 6);        // 기타요금 /추가요금
            AMBlestruct.AMReceiveFare.M_NIGHT_FARE = getString(bytePacket, 47, 1);      // 심야할증여부
            AMBlestruct.AMReceiveFare.M_COMPLEX_FARE = getString(bytePacket, 48, 1);    // 복합할증여부
            AMBlestruct.AMReceiveFare.M_SUBURB_FARE = getString(bytePacket, 49, 1);     // 시계할증여부
            AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE = getString(bytePacket, 50, 3); // 할증율

            Log.d("19=>받은버튼값",AMBlestruct.AMReceiveFare.M_STATE);
            Log.d("19=>현재받은요금",AMBlestruct.AMReceiveFare.M_START_FARE );
            Log.d("19=>기타/추가", AMBlestruct.AMReceiveFare.M_ETC_FARE);
//            Log.d("19=>심야할증", AMBlestruct.AMReceiveFare.M_NIGHT_FARE);
//            Log.d("19=>시외할증", AMBlestruct.AMReceiveFare.M_SUBURB_FARE);
//            Log.d("19=>복합할증", AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
//            Log.d("19=>할증율", AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);


//                AMBlestruct.AMReceiveFare.M_START_FARE = outpkt.GetString(outdata, 6);  //승차요금
//                AMBlestruct.AMReceiveFare.M_CALL_FARE = outpkt.GetString(outdata, 4);   //호출요금
//                AMBlestruct.AMReceiveFare.M_ETC_FARE = outpkt.GetString(outdata, 6);    //기타요금
//                AMBlestruct.AMReceiveFare.M_NIGHT_FARE = outpkt.GetString(outdata, 1); //심야할증여부
//                AMBlestruct.AMReceiveFare.M_COMPLEX_FARE = outpkt.GetString(outdata, 1); //복합할증여부
//                AMBlestruct.AMReceiveFare.M_SUBURB_FARE = outpkt.GetString(outdata, 1); //시계할증여부
//                AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE = outpkt.GetString(outdata, 3); //할증율
//
//                //나중에 사용---------------------------------------------------------------------------------------------------------------------------------------------------------------------
//                AMBlestruct.AMReceiveFare.M_START_TIME = outpkt.GetString(outdata, 14);  //승차시간
//                AMBlestruct.AMReceiveFare.M_START_X = outpkt.GetString(outdata, 14);    //승차좌표-X
//                AMBlestruct.AMReceiveFare.M_START_Y = outpkt.GetString(outdata, 14);    //승차좌표-Y
//                AMBlestruct.AMReceiveFare.M_END_X = outpkt.GetString(outdata, 14);      //하차좌표-X
//                AMBlestruct.AMReceiveFare.M_END_Y = outpkt.GetString(outdata, 14);      //하차좌표-Y
//                AMBlestruct.AMReceiveFare.M_START_DISTANCE = outpkt.GetString(outdata, 14);  //승차거리
//                AMBlestruct.AMReceiveFare.M_EMPTY_DISTANCE = outpkt.GetString(outdata, 14);  //빈차거리
//
//                Log.d("meter_getCode_", "--------------------------------------");
//                Log.d("meter_getCode_버튼값", AMBlestruct.AMReceiveFare.M_STATE);
//                Log.d("meter_getCode_승차", AMBlestruct.AMReceiveFare.M_START_FARE);
//                Log.d("meter_getCode_호출", AMBlestruct.AMReceiveFare.M_CALL_FARE);
//                Log.d("meter_getCode_심야할증", AMBlestruct.AMReceiveFare.M_NIGHT_FARE);
//                Log.d("meter_getCode_복합할증", AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
//                Log.d("meter_getCode_시외할증", AMBlestruct.AMReceiveFare.M_SUBURB_FARE);
//                Log.d("meter_getCode_할증율", AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);

            //버튼값 절달 --> windowService --> mainActivity(mCallback)
            windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE);
            break;

        case AMBlestruct.METER_MENU_REQUEST_CODE:  // "42" - 미터기 메뉴 응답
            AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME = rcvDate;                                      // 날짜시간
            AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE = getInt(bytePacket, 17, 1);            // 메세지 종류
            AMBlestruct.AMReceiveMenu.MENU_MSG = getString(bytePacket, 18, packetlen-21);   // 메시지 ** 전문 수정 필요.

//            Log.d("menu_getCode_","---------------------------------------");
//            Log.d("menu_getCode_요청코드" , AMBlestruct.METER_MENU_REQUEST_CODE+"");
            Log.d("menu_getCode_메뉴종류" , AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE+"");  //49-> 0
//            Log.d("menu_getCode_메뉴메세지" , AMBlestruct.AMReceiveMenu.MENU_MSG+"");

            windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE);
            break;

        case AMBlestruct.METER_MENU_INPUT_REQUEST_CODE: // "46" - 숫자입력 응답.
            AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME =  rcvDate;                                      // 날짜시간
            AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE = getString(bytePacket, 17, 1);        // 메세지 종류
            AMBlestruct.AMReceiveMenu.MENU_MSG = getString(bytePacket, 18, packetlen-21);    // 메시지 ** 전문 수정 필요.

//            Log.d("menu_getCode_","---------------------------------------");
//            Log.d("menu_getCode_요청코드", AMBlestruct.METER_MENU_INPUT_REQUEST_CODE);
//            Log.d("menu_getCode_날짜시간", AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME);
//            Log.d("menu_getCode_입력타입", AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE);  //0-일반숫자/ 1-비밀번호입력(숫자를 *로 표시)
//            Log.d("menu_getCode_입력메세지", AMBlestruct.AMReceiveMenu.MENU_MSG);

            windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_INPUT_MENU_STATE);
            break;
        }
    }

    private String getCurDateString()
    {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    private String getString(byte[] byteSrc, int Start, int Length)
    {
        String strTemp=null;
        byte[] byteTemp = new byte[Length];

        // 임시 바이트 배열에 잘라 복사
        System.arraycopy(byteSrc, Start, byteTemp, 0, Length);

        // 스트링으로 변환(한글코드 포함)
        try {
            strTemp = new String(byteTemp, "KSC5601");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return strTemp;
    }

    private int getInt(byte[] byteSrc, int Start, int Length)
    {
        String strTemp=null;
        byte[] byteTemp = new byte[Length];

        // 임시 바이트 배열에 잘라 복사
        System.arraycopy(byteSrc, Start, byteTemp, 0, Length);

        strTemp = new String(byteTemp);
//        // 스트링으로 변환(한글코드 포함)
//        try {
//            strTemp = new String(byteTemp, "KSC5601");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        return Integer.parseInt(strTemp);
    }
}



