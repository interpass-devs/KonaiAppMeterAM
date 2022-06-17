package com.konai.appmeter.driver_am.setting;

import android.content.Intent;

import com.konai.appmeter.driver_am.service.AwindowService;

import java.util.UUID;

public class setting {

    public static double APP_VERSION = 0.0;
    public static int gOrient;
    public static boolean gUseBLE = true;
    public static boolean BLE_STATE = false;
//    public static LocService m_Service = null;
    public static AwindowService windowService = null;
    public static boolean bOverlaymode = true; //사용유무.
    public static Intent g_MainIntent = null;
    public static boolean BLESCANNING_MODE = false;
    public static boolean BLUETOOTH_FINDEND = false;
    public static String BLUETOOTH_DEVICE_ADDRESS = "";  //3C:A5:51:85:1A:36
    public static String BLUETOOTH_DEVICE_NAME = "";
    public static String BLUETOOTH_CARNO ="";
    public static String AM101 = "";
    public static final long SCAN_PERIOD = 15000;
    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_TRANJACTION =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_DESCRIPTION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CONFIGURE = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
}
