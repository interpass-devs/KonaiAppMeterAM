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

public class AMBluetoothManager {

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


    public boolean connectAM() {
        if (setting.gUseBLE) {
            Log.d(ble, "connectAM at AMBluetoothManager");
            return  connectBLE();
        }
        return true;
    }



    public boolean connectBLE() {

        Log.d(ble, "connectAM at AMBluetoothManager - connectBLE " + setting.BLUETOOTH_DEVICE_NAME+": "+ setting.BLUETOOTH_DEVICE_ADDRESS );

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
        }

        if (mBluetoothAdapter == null || setting.BLUETOOTH_DEVICE_ADDRESS.equals("")) {
            Log.e(ble, "unable to initialize bluetoothAdapter & address");
            return false;
        }

        //me: original
        //bluetooth device
        if (mBluetoothDevice == null) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(setting.BLUETOOTH_DEVICE_ADDRESS);
            Log.e(ble, "bluetooth device null");
        }else {
            Log.d(ble, "bluetooth device not null! "+setting.BLUETOOTH_DEVICE_ADDRESS);
        }

        if (mBluetoothDevice == null) {
            Log.e(ble, "device not found. unable to connect." );
//            return false;
        }else {
            Log.d(ble, setting.BLUETOOTH_DEVICE_ADDRESS+""); //3C:A5:51:85:1A:36
        }

        //connect to device
        //블루투스 페어링 - 기기와 연결하려면 GATT 서버에 연결해야함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }

        mBluetoothAddress = setting.BLUETOOTH_DEVICE_ADDRESS;
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
                        Log.d(log+"blePairing", "connected to gatt server...");  //y


                        setting.OVERLAY = false;
                        windowService.set_meterhandler.sendEmptyMessage(100);
                        //빈차등 연결 성공
                        //블루투스 아이콘 색 변경
                        setting.BLE_STATE = true;
                        //status - 아이콘 변경
                        windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE);
                        makepacketsend(AMBlestruct.APP_REQUEST_CODE);  //"15"
                        Log.d(log+"blePairing", "makepacketsend");  //y

                        //error: 20220627
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
                        //error: 20220627
                        Log.i(log+"bleParinggg", "Server discovery-> " + mBluetoothGatt.discoverServices());  //true
                        broadcastUpdate(intentAction);
                    }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_DISCONNECTED;
                        broadcastUpdate(intentAction);
                        Log.e(log+"c", "disconnected to gatt server");
                        Log.i(log, "Attempting to start service discovery-> " + mBluetoothGatt.discoverServices());

                        //빈차등 연결 실패
                        //블루투스 아이콘 색 변경
                        setting.BLE_STATE = false;
                        //status - 아이콘 변경
                        windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE);

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
//                        Log.w(log, "onServicesDiscovered received: "+status);
                    }
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        Log.d(log, "onMtuChanged: "+mtu);  //244

                        //me 되살리기
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


    //me: 버튼값(빈차/주행/지불/호출) 업데이트 -> 빈차등으로 보내기
    public void update_AMmeterstate(String sstate)
    {
        AMBlestruct.mSState = sstate;
        Log.d(log, "버튼값: "+sstate);
        AMBlestruct.setSStateupdate(true);

        makepacketsend(AMBlestruct.APP_REQUEST_CODE); //"15"
    }

    public void menu_AMmeterState(String sstate, String menutype, String pos) {
        AMBlestruct.mSState = sstate;
        AMBlestruct.MenuType.TYPE = menutype;

        AMBlestruct.MenuType.MENU_CONTENT = pos;

        Log.d(log, "menu_request_code: "+sstate+" , "+menutype+", pos: "+ pos+", AMBlestruct.MenuType.MENU_CONTENT: "+AMBlestruct.MenuType.MENU_CONTENT);

        makepacketsend(AMBlestruct.mSState);
    }


    //앱 -> 빈차등 상태값 요청
    //thread 없이 바로연결
    synchronized public boolean makepacketsend(@NonNull String requestCode) {

        byte[] mData = null;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02);  //STX

        Log.d(log, "packetdata=> "+requestCode+", "+AMBlestruct.mSState+", "+AMBlestruct.MenuType.MENU_CONTENT);

        switch (requestCode) {

            case AMBlestruct.APP_REQUEST_CODE:  //"15"
                topkt.SetString(packetdata, AMBlestruct.APP_REQUEST_CODE);  //요청코드
                topkt.SetString(packetdata, getCurDateString()); //날짜시간
                topkt.SetString(packetdata, AMBlestruct.mSState);  //상태요청
                Log.d("makepacketsend=>  ", AMBlestruct.APP_REQUEST_CODE+",  "+getCurDateString()+",  "+AMBlestruct.mSState);
                break;

            case AMBlestruct.APP_MENU_REQUEST_CODE:  //"41"
                topkt.SetString(packetdata, AMBlestruct.APP_MENU_REQUEST_CODE);  //"41"
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE); //0-닫기/1-메뉴/2-정보출력/3-정보출력+숫자키버튼
                break;

            case AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE:  //"43"
//                Log.d("check43-> ",AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE+", "+getCurDateString()+", "+AMBlestruct.MenuType.MENU_CONTENT+", "+AMBlestruct.MenuType.TYPE);
                topkt.SetString(packetdata, AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE);
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE);
                topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_CONTENT);  //선택번호
                Log.d("check43-> ",AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE+", "+getCurDateString()+", "+AMBlestruct.MenuType.TYPE+", "+AMBlestruct.MenuType.MENU_CONTENT);
                break;

        }

        topkt.SetString(packetdata, topkt.GetAMBleCRC(packetdata));
        topkt.Setbyte(packetdata, (byte) 0x03);
        Log.d("makepacketsend=> ","packetdata: " + packetdata);
        mData = new byte[topkt.point];
        Log.d("makepacketsend=> ","mData2222: " + mData);
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


    synchronized public void parsingend_AMBle(byte[] outdata, int packetlen) {

        String code = outpkt.GetCheckCode(outdata);

        Log.d("menu_getCode", code);  //menu- 42

        if(outpkt.GetAMBleCRC(outdata, packetlen).equals(String.format("%c%c", outdata[packetlen -3], outdata[packetlen - 2])) == false)
        {
            Log.d("parsing_", "========receive(" + code + ")" + outpkt.GetAMBleCRC(outdata, packetlen)
                    + "  " + String.format("-------%c%c", outdata[packetlen -3], outdata[packetlen - 2]));
        }

        outpkt.SetPoint(3);  //날짜시간 부터 시작


        switch (code) {  //택시요금수신, 미터기모드

            case AMBlestruct.METER_REQUEST_CODE: //"19"
//                outpkt.SetPoint(17);
//                Log.d("code19",code+""); //19
//                AMBlestruct.curReponseCode = outpkt.GetString(outdata, 2);
                AMBlestruct.AMReceiveFare.M_RECEIVE_TIME = outpkt.GetString(outdata, 14);  //날짜시간
                AMBlestruct.AMReceiveFare.M_CARNUM = outpkt.GetString(outdata, 12);     //차량번호
                AMBlestruct.AMReceiveFare.M_STATE = outpkt.GetString(outdata, 1);        //버튼값   //ME: 여기서 받은 버튼값을 MainActivity 에서 확인하여 화면전환..

                AMBlestruct.AMReceiveFare.M_START_FARE = outpkt.GetString(outdata, 6);  //승차요금
                AMBlestruct.AMReceiveFare.M_CALL_FARE = outpkt.GetString(outdata, 4);   //호출요금
                AMBlestruct.AMReceiveFare.M_ETC_FARE = outpkt.GetString(outdata, 6);    //기타요금
                AMBlestruct.AMReceiveFare.M_EXTRA_FARE_TYPE = outpkt.GetString(outdata, 4); //할증여부
                AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE = outpkt.GetString(outdata, 3); //할증율

                //나중에 사용
                AMBlestruct.AMReceiveFare.M_START_TIME = outpkt.GetString(outdata, 14);  //승차시간
                AMBlestruct.AMReceiveFare.M_START_X = outpkt.GetString(outdata, 14);    //승차좌표-X
                AMBlestruct.AMReceiveFare.M_START_Y = outpkt.GetString(outdata, 14);    //승차좌표-Y
                AMBlestruct.AMReceiveFare.M_END_X = outpkt.GetString(outdata, 14);      //하차좌표-X
                AMBlestruct.AMReceiveFare.M_END_Y = outpkt.GetString(outdata, 14);      //하차좌표-Y
                AMBlestruct.AMReceiveFare.M_START_DISTANCE = outpkt.GetString(outdata, 14);  //승차거리
                AMBlestruct.AMReceiveFare.M_EMPTY_DISTANCE = outpkt.GetString(outdata, 14);  //빈차거리

                //버튼값 절달 --> windowService --> mainActivity(mCallback)
                windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE);

                break;

            case "92":  //응답) 이렇게하면 안됨... 앱에서 -> 빈차등으로 보내는거임
                break;

            case AMBlestruct.METER_MENU_REQUEST_CODE:  //"42"- 미터기 메뉴 응답

                outpkt.SetPoint(1);

                Log.d(log+"menu_getCode", outpkt.GetString(outdata, 2));  //20

                AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME = outpkt.GetString(outdata, 14); //날짜시간
                AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE = outpkt.Getbyte(outdata);  //메세지 종류
                AMBlestruct.AMReceiveMenu.MENU_MSG = outpkt.Gettextbytoken(outdata, (byte) 0x03, packetlen, -2);  //outDataIndex-21 =>  88-21 = n

                Log.d("receive_43->" , AMBlestruct.AMReceiveMenu.MENU_MSG+"");
                Log.d("receive_43_msgType->" , AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE+"");  //49-> 0

                windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE);
                break;

            case AMBlestruct.METER_MENU_INPUT_REQUEST_CODE: //"46" - 날짜입력 등 요청값 전달...
                AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME = outpkt.GetString(outdata, 14);
                AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE = outpkt.GetString(outdata, 1);
                AMBlestruct.AMReceiveMenu.MENU_MSG = outpkt.Gettextbytoken(outdata, (byte) 0x03, packetlen, -2);  // outDataIndex - n = ?

                Log.d("code46", AMBlestruct.METER_MENU_INPUT_REQUEST_CODE);
                Log.d("code46", AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME);
                Log.d("code46", AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE);
                Log.d("code46", AMBlestruct.AMReceiveMenu.MENU_MSG);
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


}



