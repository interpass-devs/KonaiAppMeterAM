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
            topkt = new AMPacket(); //????????? ????????? (????????? ??????)
        }

        if (outpkt == null) {
            outpkt = new AMPacket(); //????????? ????????? (????????? ??????)
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
//            Log.e(ble, "unable to initialize bluetooth manager"); //y
            return false;
        }

        //bluetooth adapter
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();  //y
        }

        if (mBluetoothAdapter == null) {
//            Log.e(ble,"unable to initialize bluetooth adapter");
            return false;
        } else {
//            Log.d(ble,"Able to initialize bluetooth adapter!"); //y
        }

        if (mBluetoothAdapter == null || setting.BLUETOOTH_DEVICE_ADDRESS.equals("")) {
//            Log.e(ble, "unable to initialize bluetoothAdapter & address");
            return false;
        }

        //me: original
        //bluetooth device
        if (mBluetoothDevice == null) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
//            Log.e(ble, "bluetooth device null");
        } else {
//            Log.d(ble, "bluetooth device not null! "+deviceAddress);
        }

        if (mBluetoothDevice == null) {
//            Log.e(ble, "device not found. unable to connect." );
//            return false;
        } else {
            Log.d(ble, deviceAddress+""); //3C:A5:51:85:1A:36
        }

        //connect to device
        //???????????? ????????? - ????????? ??????????????? GATT ????????? ???????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mGattCallback);
        }

        mBluetoothAddress = deviceAddress;
        mConnectionState = STATE_CONNECTING;

//        Log.d(log+"blePairing", mBluetoothAddress+"!!");

//        makepacketsend("15");

        return true;
    }


    //gatt server ??????
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        connectionState = STATE_CONNECTED;
                        Log.d(log+"ble", "connected to gatt server...");  //y

                        //???????????? ????????? ????????????
                        setting.OVERLAY = false;
                        windowService.set_meterhandler.sendEmptyMessage(100);
                        windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_BLE_STATE);
                        setting.BLE_STATE = true;

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

                        mBluetoothGatt.requestMtu(512);  //?????????

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

                //????????? ????????? ???????????? ???
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


    //me: ?????????(??????/??????/??????/??????) ???????????? -> ??????????????? ?????????
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

    public void menu_AMmeterState(String sstate, String menutype, String pos, String numType) {

        AMBlestruct.mSState = sstate;
        AMBlestruct.MenuType.TYPE = menutype;
        AMBlestruct.MenuType.MENU_CONTENT = pos;
        AMBlestruct.MenuType.MENU_NUMTYPE = numType;

        Log.d("??????-3", sstate+", "+menutype+", "+pos+", "+numType);

        makepacketsend(AMBlestruct.mSState);
    }


    //"47"
    public void menu_input_AMmeterState(String requestCode, String inputCheck, int inputLength, String inputMsg) {

        AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE = inputCheck;  //????????????
        AMBlestruct.AMReceiveMenu.MENU_INPUT_LENGTH = String.format("%02d", inputLength);
        AMBlestruct.MenuType.MENU_CONTENT = inputMsg;  //???????????? ????????? ??????

        makepacketsend(requestCode);
    }


    //??? -> ????????? ????????? ??????
    //thread ?????? ????????????
    synchronized public boolean makepacketsend(@NonNull String requestCode) {

        byte[] mData = null;
        topkt.SetPoint(0);

        topkt.Setbyte(packetdata, (byte) 0x02);  //STX

//        Log.d("??????_????????????", requestCode+",  "+AMBlestruct.B_ADDFARE);

        switch (requestCode) {

            case AMBlestruct.APP_REQUEST_CODE:  //"15"
                topkt.SetString(packetdata, AMBlestruct.APP_REQUEST_CODE);  //????????????
                topkt.SetString(packetdata, getCurDateString()); //????????????
                topkt.SetString(packetdata, AMBlestruct.mSState);  //????????????
//                Log.d("15=>  ", AMBlestruct.APP_REQUEST_CODE+",  "+getCurDateString()+",  "+AMBlestruct.mSState);
                break;

            case "69":
                topkt.SetString(packetdata, "69");
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.appMeterResult);
                break;

            case AMBlestruct.APP_MENU_REQUEST_CODE:  //"41"
                topkt.SetString(packetdata, AMBlestruct.APP_MENU_REQUEST_CODE);
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE); //0-??????/1-??????/2-????????????/3-????????????+???????????????
                break;

            case AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE:  //"43"

                if (AMBlestruct.MenuType.MENU_NUMTYPE.equals("1") || AMBlestruct.MenuType.MENU_NUMTYPE.equals("2")) {
//                    Log.d("check_menu_numtype: "+AMBlestruct.MenuType.MENU_NUMTYPE, "---------------");
                    topkt.SetString(packetdata, AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE);
                    topkt.SetString(packetdata, getCurDateString());
                    topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE);
                    topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_NUMTYPE);  //????????????
                }else {
//                    Log.d("check_menu_numtype", "@@@@@@@@@@@@@");
                    topkt.SetString(packetdata, AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE);
                    topkt.SetString(packetdata, getCurDateString());
                    topkt.SetString(packetdata, AMBlestruct.MenuType.TYPE);
                    topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_CONTENT);  //????????????
                }

                break;

            case "47":
                topkt.SetString(packetdata,"47");
                topkt.SetString(packetdata, getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE);     //????????????
                topkt.SetString(packetdata, AMBlestruct.AMReceiveMenu.MENU_INPUT_LENGTH); //?????? ???????????????
                topkt.SetString(packetdata, AMBlestruct.MenuType.MENU_CONTENT);  //????????????
                break;

            case "20":  //???????????? ?????????
                topkt.SetString(packetdata,"20");
                topkt.SetString(packetdata,getCurDateString());
                topkt.SetString(packetdata,AMBlestruct.B_ADDFARE);
//                Log.d("??????_????????????", requestCode+",  "+AMBlestruct.B_ADDFARE);
                break;

            case "48":  //??????- ????????? ?????????
                topkt.SetString(packetdata,"48");
                topkt.SetString(packetdata,getCurDateString());
                topkt.SetString(packetdata, AMBlestruct.mSState);
                break;

        }

        topkt.SetString(packetdata, topkt.GetAMBleCRC(packetdata));
        topkt.Setbyte(packetdata, (byte) 0x03);
//        Log.d("send_char","ttt");
        mData = new byte[topkt.point];
//        Log.d("mData", mData.toString());
//        Log.d("mData_packetdata", packetdata.toString());
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

//        Log.d(log+"send_char", m_gattCharTrans.toString());

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

//            Log.d(log+"receive_outdata", characteristic.getValue()+"");

//            Log.d(log+"menu_outdata", characteristic.getValue()+"");

            byte[] outdata = characteristic.getValue();

//            Log.d("outdata_before_que", outdata+"");
////
//            OutDataQue que = new OutDataQue();
//
//            que.outdata = outdata;
//
//            Log.d("outdata_que", que.outdata+"");


            //me:
            //  outdata??? Q??? ?????? ???????????? Q??? ???????????? ????????? ???????????? Thread ????????? ????????????
            // 02/ 03 ??? ???????????? ???????????? ????????? ????????? ?????? ???  ->  ??????

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
//                                Log.d(log+"outputData", outDataIndex+"");  //88

                                //me: ?????? ??????..
                                // parsingend_AMBle(outdata, outdata.length);
                                parsingend_AMBle(outputData, outDataIndex);  //????????? ??????
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



    //????????????
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
            strPacket = new String(bytePacket, "KSC5601"); // ????????? ????????? ??????????????? ??????
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Log.d("strPacket", strPacket);
        // end

        String rcvCode = getString(bytePacket, 1, 2);
        Log.d("??????_getCode", rcvCode);  //menu- 42

        // ?????? ?????? ?????? ??????.
        if (outpkt.GetAMBleCRC(outdata, packetlen).equals(String.format("%c%c", outdata[packetlen -3], outdata[packetlen - 2])) == false)
        {
            Log.d("parsing_", "========receive(" + rcvCode + ")" + outpkt.GetAMBleCRC(outdata, packetlen)
                    + "  " + String.format("-------%c%c", outdata[packetlen -3], outdata[packetlen - 2]));
        }

        // ????????????
        String rcvDate = getString(bytePacket, 3, 12);

        switch (rcvCode) {
            case "98":
                windowService.set_meterhandler.sendEmptyMessage(101);
                break;

            case AMBlestruct.METER_REQUEST_CODE: // "19" ??????????????????, ??????????????? ??????
            AMBlestruct.AMReceiveFare.M_RECEIVE_TIME = rcvDate;                                     // ????????????
            AMBlestruct.AMReceiveFare.M_CARNUM = getString(bytePacket, 17, 12);         // ????????????
            AMBlestruct.AMReceiveFare.M_STATE = getString(bytePacket, 29, 2);           // ?????????   //ME: ????????? ?????? ???????????? MainActivity ?????? ???????????? ????????????..
            AMBlestruct.AMReceiveFare.M_START_FARE = getString(bytePacket, 31, 6);      // ????????????
            AMBlestruct.AMReceiveFare.M_CALL_FARE = getString(bytePacket, 37, 4);       // ????????????
            AMBlestruct.AMReceiveFare.M_ETC_FARE = getString(bytePacket, 41, 6);        // ???????????? /????????????
            AMBlestruct.AMReceiveFare.M_NIGHT_FARE = getString(bytePacket, 47, 1);      // ??????????????????
            AMBlestruct.AMReceiveFare.M_COMPLEX_FARE = getString(bytePacket, 48, 1);    // ??????????????????
            AMBlestruct.AMReceiveFare.M_SUBURB_FARE = getString(bytePacket, 49, 1);     // ??????????????????
            AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE = getString(bytePacket, 50, 3); // ?????????
                AMBlestruct.AMReceiveFare.M_DRIVER_ID = getString(bytePacket, 129, 4);   //??????????????????

            Log.d("19=>???????????????",AMBlestruct.AMReceiveFare.M_STATE);
            Log.d("19=>??????????????????",AMBlestruct.AMReceiveFare.M_START_FARE );
//            Log.d("19=>??????/??????", AMBlestruct.AMReceiveFare.M_ETC_FARE);
//            Log.d("19=>??????????????????", AMBlestruct.AMReceiveFare.M_DRIVER_ID);
//            Log.d("19=>??????", AMBlestruct.AMReceiveFare.M_NIGHT_FARE);
//            Log.d("19=>??????", AMBlestruct.AMReceiveFare.M_SUBURB_FARE);
//            Log.d("19=>??????", AMBlestruct.AMReceiveFare.M_COMPLEX_FARE);
//            Log.d("19=>?????????", AMBlestruct.AMReceiveFare.M_EXTRA_FARE_RATE);

            //????????? ?????? --> windowService --> mainActivity(mCallback)
            windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_AM_STATE);
            break;

        case AMBlestruct.METER_MENU_REQUEST_CODE:  // "42" - ????????? ?????? ??????
            AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME = rcvDate;                                      // ????????????
            AMBlestruct.AMReceiveMenu.MENU_MSG_TYPE = getInt(bytePacket, 17, 1);            // ????????? ??????: 0-??????/1-??????/2-????????????/3-????????????+??????
            AMBlestruct.AMReceiveMenu.MENU_MSG = getString(bytePacket, 18, packetlen-21);   // ????????? ** ?????? ?????? ??????.

            Log.d("42=>????????????",AMBlestruct.AMReceiveFare.M_STATE);
            Log.d("42=>???????????????", AMBlestruct.AMReceiveMenu.MENU_MSG);

            windowService.set_meterhandler.sendEmptyMessage(AMBlestruct.AMReceiveMsg.MSG_CUR_MENU_STATE);
            break;

        case AMBlestruct.METER_MENU_INPUT_REQUEST_CODE: // "46" - ???????????? ??????.
            AMBlestruct.AMReceiveMenu.MENU_RECEIVE_TIME =  rcvDate;                                      // ????????????
            AMBlestruct.AMReceiveMenu.MENU_INPUT_TYPE = getString(bytePacket, 17, 1);        // ????????? ??????
            AMBlestruct.AMReceiveMenu.MENU_MSG = getString(bytePacket, 18, packetlen-21);    // ????????? ** ?????? ?????? ??????.

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

        // ?????? ????????? ????????? ?????? ??????
        System.arraycopy(byteSrc, Start, byteTemp, 0, Length);

        // ??????????????? ??????(???????????? ??????)
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

        // ?????? ????????? ????????? ?????? ??????
        System.arraycopy(byteSrc, Start, byteTemp, 0, Length);

        strTemp = new String(byteTemp);
//        // ??????????????? ??????(???????????? ??????)
//        try {
//            strTemp = new String(byteTemp, "KSC5601");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        return Integer.parseInt(strTemp);
    }
}



