package com.konai.appmeter.driver_am.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver_am.BuildConfig;
import com.konai.appmeter.driver_am.adapter.MenuAdapter;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.handler.BackPressedKeyHandler;
import com.konai.appmeter.driver_am.service.AwindowService;
import com.konai.appmeter.driver_am.setting.AMBlestruct;
import com.konai.appmeter.driver_am.setting.PermissionSupoort;
import com.konai.appmeter.driver_am.setting.setting;
import com.konai.appmeter.driver_am.socket.AMBluetoothManager;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;
import com.konai.appmeter.driver_am.util.MyTouchListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String updateVersion, mdn, carNum, mirrorappUpdate;
    private String appVersion;
    private String resultData = "";
    String File_Name = "app-debug.apk";   //확장자를 포함한 파일명
    String fileURL = "http://175.125.20.72:8080/update/app-debug.apk"; // URL: 웹서버 쪽 파일이 있는 경로

    private BackPressedKeyHandler backKeyHandler = new BackPressedKeyHandler(this);

    private PermissionSupoort mPermission;
    private AMBluetoothManager amBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice = null;
    private String mBluetoothAddress = "";
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int ACCESS_FINE_LOCATION_DENIED = 10;
    private static final int BLUETOOTH_CONNECT_DENIED = 50;
    private static final int REQUEST_ENABLE_BT = 30;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    public static AwindowService windowService = null;
    private String log_ = "log_mainActivity";
    private Context mContext;
    private MyTouchListener mTouchListener;
    private View viewframe1, viewframe2, viewframe3, viewframe4, viewframe5;
    private FrameLayout frame1, frame2, frame3, frame4, frame5;
    private LinearLayout main_layout, menu_layout, numberPadLayout, menu_main_layout, main_all_layout, radio_button_layout, main_btn_layout, number_pad_layout, add_fare_frame_layout, number_pad_frame_layout, menu_list_layout;
    private MenuAdapter menuAdapter;
    private RecyclerView menuRecyclerView;
    private TextView menu_text;
    private ArrayList<String> itemList = new ArrayList<>();
    private ButtonFitText btn_menu, btn_complex, btn_login, btn_arrive, btn_add_pay, close_menu_btn, back_menu_btn;
    private ButtonFitText btn_empty, btn_drive, btn_call, btn_pay;
    private ImageView iv_ble;
    private FontFitTextView tv_night_status, tv_add_pay, tv_rescall_pay, tv_total_pay, btn_main_status, menu_title;
    private Boolean menuClicked, isDrivedClicked = false;
    private ButtonFitText btn_close, btn_ok;
    private Intent enableIntent;


    private AwindowService.mainCallBack mCallback = new AwindowService.mainCallBack() {
        @Override
        public void serviceBleStatus(boolean bleStatus) {

            //블루투스 상태값 변경
            display_bleStatus(bleStatus);
        }

        @Override
        public void serviceMeterState(int btnType, int mFare, int startFare, int callFare, int etcFare) {

            AMBlestruct.AMReceiveFare.M_CALL_FARE = callFare + "";

            if (!AMBlestruct.AMReceiveFare.M_CALL_FARE.equals("0")) {
                tv_rescall_pay.setVisibility(View.VISIBLE);
                tv_rescall_pay.setText("호출 " + AMBlestruct.AMReceiveFare.M_CALL_FARE);
            } else {
                tv_rescall_pay.setVisibility(View.GONE);
            }


            long value = Long.parseLong(mFare + "");
            DecimalFormat format = new DecimalFormat("###,###");

            //상태값 요금 변경
            display_Runstate(format.format(value));

            if (btnType == AMBlestruct.MeterState.PAY) {
                btn_pay.performClick();
                btn_main_status.setText("지불");
                btn_main_status.setTextColor(getResources().getColor(R.color.orange));

            } else if (btnType == AMBlestruct.MeterState.EMPTY) {
                menu_main_layout.setVisibility(View.GONE);
                main_all_layout.setVisibility(View.VISIBLE);
                main_layout.setVisibility(View.VISIBLE);
                btn_empty.performClick();
                btn_main_status.setText("빈차");
                btn_main_status.setTextColor(getResources().getColor(R.color.white));

            } else if (btnType == AMBlestruct.MeterState.DRIVE) {
                btn_drive.performClick();
                btn_main_status.setText("주행");
                btn_main_status.setTextColor(getResources().getColor(R.color.yellow));

            } else if (btnType == AMBlestruct.MeterState.CALL) {  //호출대신 주행이 불려짐.. i don't know why.
                btn_call.performClick();
                btn_main_status.setText("호출");
                tv_rescall_pay.setVisibility(View.VISIBLE);
                tv_rescall_pay.setText(AMBlestruct.AMReceiveFare.M_CALL_FARE);
            }
        }

        //메뉴 빈차등 수신요청.. -> windowService에 넘겨줌
        @Override
        public void serviceMeterMenuState(String menuMsg, int menuType) {

            //빈차등에서 메뉴 -> 빈차 클릭시 = 초기화시킴
            if (menuType == 48) {
                Log.d("menuType@@", menuType+"");
                main_all_layout.setVisibility(View.VISIBLE);
                main_layout.setVisibility(View.VISIBLE);
                menu_main_layout.setVisibility(View.GONE);
                numberPadLayout.setVisibility(View.GONE);
                add_fare_frame_layout.setVisibility(View.VISIBLE);
            }

            //받아온 빈차등 메뉴 리스트
            ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menuMsg.split("\n")));
            Log.d("menulist", menuList.toString());
            Log.d("menulist", menuList.size() + "");

            menuAdapter = new MenuAdapter(MainActivity.this, menuList);
            menuRecyclerView.setAdapter(menuAdapter);

            if (menuList.size() == 1) {
                //메뉴 리스트가 1일땐 -> 제목으로 (ex; [메인메뉴])
                //클릭 안되게 설정
                //숫자입력패드 보이기
                numberPadLayout.setVisibility(View.VISIBLE);
            }else {
                numberPadLayout.setVisibility(View.GONE);
                //메뉴어뎁터 클릭리스너
                menuAdapter.setmListener(new MenuAdapter.onItemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos) {
                        //me: 앱 -> 빈차등
                        // 메뉴목록 선택시 빈차등수신요청..
                        AMBlestruct.MenuType.MENU_CONTENT = pos + "";
                        windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE, "0", AMBlestruct.MenuType.MENU_CONTENT);  //"43", 선택목록번호
                    }
                });
                //메뉴어뎁터 터치리스너
                menuAdapter.setmTouchListener(new MenuAdapter.onItemTouchListener() {
                    @Override
                    public void onItemTouch(View v, int pos, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            v.setBackgroundResource(R.drawable.yellow_selected_btn);
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            v.setBackgroundColor(getResources().getColor(R.color.main_background));
                        }
                    }
                });
            }


        }
    };


    public void display_bleStatus(boolean ble) {
        if (ble == true) {
            Log.d("bleconnnn", "bleconnnn");
            iv_ble.setBackgroundResource(R.drawable.bluetooth_green);
            Toast.makeText(MainActivity.this, "빈차등 연결 성공", Toast.LENGTH_SHORT).show();
        } else {
            iv_ble.setBackgroundResource(R.drawable.bluetooth_blue);
        }
    }

    //총 요금
    public void display_Runstate(String mFare) {
//        tv_total_pay.setTextSize(4.0f * 2);
        tv_total_pay.setText(mFare);
    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(log_, "service connected");
            AwindowService.ServiceBinder binder = (AwindowService.ServiceBinder) iBinder;
            windowService = binder.getService();
            windowService.registerCallback(mCallback);

            if (windowService == null) {
                Log.d(log_, "service null");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(log_, "service disconnected");
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //앱버전 체크
        appVersion = getVersionInfo(getApplicationContext());

        //블루투스 관리자객체 소환
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
        //업데이트버전 체크
        Thread NetworkThread = new Thread(new NetworkThread());
        NetworkThread.start();

        try {
            NetworkThread.join();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (mirrorappUpdate != null) {
                if (mirrorappUpdate.equals("Y")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("앱업데이트 진행을 위해\n확인버튼을 눌러주세요.");
                    builder.setMessage("현재버전: "+appVersion+" -> 새버전: "+updateVersion);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //파일 다운로드
                            new Thread(new UpdateThread()).start();

                            goMain();

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }else { //"N"
                    finish();  //앱종료
                }
            }else {
                goMain();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
**/

    goMain();

    }//onCreate



    private void goMain() {
        //위치권한
        locationPermission();

        /**
         //블루투스 활성화 (블루투스가 켜져있는지 확인)
         if (mBluetoothAdapter == null | !mBluetoothAdapter.isEnabled()) {
         Toast.makeText(MainActivity.this, "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_SHORT).show();
         //블루투스 활성화
         Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
         startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         } else {
         //me: 기기검색 실행코드
         //status -- AWindowService 클래스에서 연결해주고 있음..
         }
         **/

        //앱위에그리기 권한
        oerlayPermission();

        //뷰 아이디 찾기
        viewFrameVariablesConfiguration();
    }


    class UpdateThread implements Runnable {

        @Override
        public void run() {
            update();
        }
    }


    public void update() {

        try {

            String url = fileURL;
            URLConnection conn = new URL(url).openConnection();
            InputStream in = conn.getInputStream();

            int len = 0, total = 0;
            byte[] buf = new byte[2048];

            File path = getFilesDir();
            File apk = new File(path, File_Name);

            if (apk.exists()) {
                apk.delete();
            }
            apk.createNewFile();

            //다운로드
            FileOutputStream fos = new FileOutputStream(apk);

            while ((len = in.read(buf, 0, 2048)) != -1) {
                total += len;
                fos.write(buf,0,len);
            }
            in.close();

            fos.flush();
            fos.close();

        }catch (Exception e) {
            Log.d("update_error", "update_error: "+e.toString());

            e.printStackTrace();

            return;
        }

        File paths = getFilesDir();
        File apkFile = new File(paths, File_Name);

        if (apkFile != null) {

            if (Build.VERSION.SDK_INT >= 24) {
                //install app
                installApk(apkFile);

            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                startActivity(intent);

//                loadingBar.setVisibility(View.GONE);

                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }
        }
    }

    public void installApk(File file) {
        Uri fileUri = FileProvider.getUriForFile(this.getApplicationContext(), this.getApplicationContext().getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri,"application/vnd.android.package-archive");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
//        finish();

//        loadingBar.setVisibility(View.GONE);

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public String getVersionInfo(Context context) {
        String version = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        }catch (PackageManager.NameNotFoundException e) {}
        return version;
    }


    class NetworkThread implements Runnable {

        @Override
        public void run() {

            HttpURLConnection conn = null;
            StringBuilder jasonData = new StringBuilder();

            try {

                URL url = new URL("http://175.125.20.72:33030/querymirrorapp?mdn=01236461422");
                conn = (HttpURLConnection) url.openConnection();

                Log.d("resultData-url", url.toString());

                if (conn != null) {
                    conn.setConnectTimeout(2000);
                    conn.setUseCaches(false);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader((conn.getInputStream()), "UTF-8"));

                        for (;;) {
                            String line = br.readLine();

                            if (line == null) {
                                break;
                            }
                            jasonData.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();

                    resultData = jasonData.toString();
                    Log.d("resultData-1", resultData);

                    //특정 파라미터 얻기
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(jasonData));
                        mdn = jsonObject.getString("mdn");
                        carNum = jsonObject.getString("car_num");
                        mirrorappUpdate = jsonObject.getString("mirrorapp_update");
                        updateVersion = jsonObject.getString("mirrorapp_version");

                        Log.d("resultData-update", mdn+":  "+carNum+":  "+mirrorappUpdate+", appVersion: "+updateVersion);

                    }catch (Exception e) {
                        Log.e("jsonObject-error", e.toString());
                    }
                }else {
                    resultData = "fail";
                    Log.d("resultData-2", resultData);
                }

            }catch (Exception e) {

                Log.e("error-networkthread", e.toString());

                if (conn != null) {
                    conn.disconnect();
                }

                resultData = "fail-3";
                Log.d("resultData", resultData);
            }
        }
    }


    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();

        setting.OVERLAY = true;
        windowService.set_meterhandler.sendEmptyMessage(100);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter.isEnabled()) {
            //블루투스 연결 권한
            bleConnPermission();
        }else {
            enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }


        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        if (windowService != null) {
            Log.d(log_, "windowService not null!!");
//            windowService.connectAM();
            setting.OVERLAY = false;
            windowService.set_meterhandler.sendEmptyMessage(100);
        }else {
            Log.d(log_, "windowService NULL!!"); //null
        }

    }

    //위치권한데 대한 동적퍼미션 작업 (Location permission)
    public void locationPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //23
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{permission}, ACCESS_FINE_LOCATION_DENIED);
            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void bleConnPermission() {
        Log.d("bleconn", "bleconn");
        String ble_conn_permission = Manifest.permission.BLUETOOTH_CONNECT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //31
            Log.d("bleconn--",Build.VERSION.SDK_INT+"");
            Log.d("bleconn", "bleconn_lollipop");
            if (checkSelfPermission(ble_conn_permission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{ble_conn_permission}, BLUETOOTH_CONNECT_DENIED);
            }
        }else {
            //if the api version is lower than 31 --> do nothing
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
                startWindowService();
            }
        } else
            startWindowService();
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            switch (requestCode) {

                //위치권한 설정을 거부했을때
                case ACCESS_FINE_LOCATION_DENIED:
                    Toast.makeText(mContext, "위치권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                    break;

                case BLUETOOTH_CONNECT_DENIED:
                    Toast.makeText(mContext, "블루투스 연결을 거부하셨습니다.", Toast.LENGTH_SHORT).show();
                    break;

                //블루투스 활성화
                case REQUEST_ENABLE_BT:
                    if (resultCode == RESULT_OK) {
                        Toast.makeText(mContext, "블루투스 ON", Toast.LENGTH_SHORT).show();

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
                    }
                    break;
            }
        }


    }


    //me: 메인화면에서는 보이지말고 앱 밖에서만 보이도록..
    void startWindowService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(windowService == null) {
                bindService(new Intent(getApplicationContext(),
                        AwindowService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }

            startForegroundService(new Intent(MainActivity.this, AwindowService.class));
        } else {
            startService(new Intent(MainActivity.this, AwindowService.class));
        }
    }





    private void viewFrameVariablesConfiguration() {

        viewframe1 = null;
        viewframe2 = null;
        viewframe3 = null;
        viewframe4 = null;
        viewframe5 = null;
        frame1 = (FrameLayout) findViewById(R.id.frame1);
        frame2 = (FrameLayout) findViewById(R.id.frame2);
        frame3 = (FrameLayout) findViewById(R.id.frame3);
        frame4 = (FrameLayout) findViewById(R.id.frame4);
        frame5 = (FrameLayout) findViewById(R.id.frame5);

        LayoutInflater inflater = null;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewframe1 = inflater.inflate(R.layout.mainframe1, frame1,true);  //메인화면
        viewframe2 = inflater.inflate(R.layout.mainbuttonframe2, frame2, true); //메인하단버튼
        viewframe3 = inflater.inflate(R.layout.mainbuttonframe3, frame3, true); //메인상단버튼

        viewframe4 = inflater.inflate(R.layout.mainframe1, frame4);   //추가금액 화면
        viewframe5 = inflater.inflate(R.layout.numberpadframe5, frame5, true);  //넘버패드버튼

        /*메인화면버튼 frame1*/
        iv_ble = (ImageView) viewframe1.findViewById(R.id.nbtn_connectble);
        btn_main_status = (FontFitTextView) viewframe1.findViewById(R.id.nbtn_main_status);
        tv_night_status = (FontFitTextView) viewframe1.findViewById(R.id.ntv_status);
        tv_add_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_addpayment);
        tv_rescall_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_rescallpay);
        tv_total_pay = (FontFitTextView) viewframe1.findViewById(R.id.ntv_totalpay);
        menu_title = (FontFitTextView) viewframe1.findViewById(R.id.menu_title);
        menu_list_layout = (LinearLayout) viewframe1.findViewById(R.id.layoutlist);
//        menuRecyclerView = (RecyclerView) viewframe1.findViewById(R.id.menuRecyclerView);
        menuRecyclerView = (RecyclerView) findViewById(R.id.menuRecyclerView);
        menu_text = (TextView) findViewById(R.id.menu_text);
        main_layout = viewframe1.findViewById(R.id.mainframe1_layout);
        menu_layout = viewframe1.findViewById(R.id.menu_frame_layout);
        menu_main_layout = (LinearLayout) findViewById(R.id.menu_main_layout);
//        radio_button_layout = (LinearLayout) findViewById(R.id.radio_button_layout);
        main_all_layout = (LinearLayout) findViewById(R.id.main_all_layout);
        numberPadLayout = (LinearLayout) findViewById(R.id.numberPadLayout);


        /*메인버튼 frame2*/
        btn_empty = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_emptycar);
        btn_drive = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_drivestart);
        btn_call = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_reserve);
        btn_pay = (ButtonFitText) viewframe2.findViewById(R.id.nbtn_driveend);
        main_btn_layout = (LinearLayout) viewframe2.findViewById(R.id.main_btn_layout);

        /*메인버튼 frame3*/
        btn_menu = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_menu);
        btn_complex = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_complex);
        btn_login = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_login);
        btn_arrive = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_arrived);
        btn_add_pay = (ButtonFitText) viewframe3.findViewById(R.id.nbtn_addpayment);

        /*추가요금 frame4 & frame5*/
        number_pad_frame_layout = (LinearLayout) findViewById(R.id.number_pad_frame_layout);
        add_fare_frame_layout = viewframe4.findViewById(R.id.add_fare_frame_layout);
        btn_close = (ButtonFitText) viewframe5.findViewById(R.id.btn_close);
        btn_ok = (ButtonFitText) viewframe5.findViewById(R.id.btn_ok);
//
        btn_close.setOnClickListener(numberPadClickListener);
        btn_ok.setOnClickListener(numberPadClickListener);

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
        btn_call.setOnTouchListener(mTouchListener);
        btn_pay.setOnTouchListener(mTouchListener);
        btn_empty.setOnClickListener(mainBtnClickListener);
        btn_drive.setOnClickListener(mainBtnClickListener);
        btn_call.setOnClickListener(mainBtnClickListener);
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

        /* 빈차등 메뉴 */
        close_menu_btn = (ButtonFitText) findViewById(R.id.close_menu_btn);
        back_menu_btn = (ButtonFitText) findViewById(R.id.back_menu_btn);
        close_menu_btn.setOnClickListener(menuBtnClickListener);
        back_menu_btn.setOnClickListener(menuBtnClickListener);

    }//viewFrameVariablesConfiguration



    //Activity 파괴시 데이터 저장
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

    }


    //넘버패드 클릭리스너
    private View.OnClickListener numberPadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    break;
                case R.id.btn_close:
                    number_pad_layout.setVisibility(View.GONE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    number_pad_frame_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    break;
            }
        }
    };


    //메뉴버튼 클릭리스너
    private View.OnClickListener menuBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.close_menu_btn:  //메뉴-이전버튼
                    //me: 앱 -> 빈차등
                    AMBlestruct.MenuType.MENU_CONTENT = "2";
                    windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE,  AMBlestruct.MenuType.MENU_CONTENT,""); //닫기
                    main_all_layout.setVisibility(View.VISIBLE);
                    menu_main_layout.setVisibility(View.GONE);  //status:  닫기 수신번호가 잘 왔을 때 설정..  !!!!!!!!!!!!
                    main_layout.setVisibility(View.VISIBLE);  //빈차화면
                    break;
                case R.id.back_menu_btn:
                    //me: 앱 -> 빈차등
                    AMBlestruct.MenuType.MENU_CONTENT = "1";
                    windowService.menu_meterState(AMBlestruct.APP_MENU_CONTENTS_REQUEST_CODE,  AMBlestruct.MenuType.MENU_CONTENT,""); //이전(정정)
                    break;

            }

        }
    };


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
                    isDrivedClicked = false;
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    btn_menu.setClickable(true);
                    btn_menu.setBackgroundResource(R.drawable.grey_gradi_btn_rec);
                    //me: 버튼 -> 빈차등
                    windowService.update_BLEmeterstate(AMBlestruct.B_EMPTY);
                    break;
                case R.id.nbtn_drivestart:  //주행버튼
                    isDrivedClicked = true;
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
//                    menu_layout.setVisibility(View.GONE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    //주행버튼 클릭 -> 메뉴버튼 클릭 못하게
//                    btn_menu.setClickable(false);
//                    Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    //me: 버튼 -> 빈차등
                    windowService.update_BLEmeterstate(AMBlestruct.B_DRIVE);
                    break;

                case R.id.nbtn_reserve:    //호출버튼
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);
                    menu_main_layout.setVisibility(View.GONE);
                    add_fare_frame_layout.setVisibility(View.GONE);
                    //me: 버튼 -> 빈차등
                    windowService.update_BLEmeterstate(AMBlestruct.B_CALL);
//                    tv_rescall_pay.setVisibility(View.VISIBLE); //빈차등으로 부터 호출값 받으면 보이기
                    break;

                case R.id.nbtn_driveend:  //지불버튼
                    main_all_layout.setVisibility(View.VISIBLE);
                    main_layout.setVisibility(View.VISIBLE);
                    menu_main_layout.setVisibility(View.GONE);
                    //me: 버튼 -> 빈차등
                    windowService.update_BLEmeterstate(AMBlestruct.B_PAY);
                    break;

                /*frame3 메인상단버튼*/
                case R.id.nbtn_menu:  //메뉴 btn
                    //빈차등메뉴
//                    setupBluetooth(2);
                    if (isDrivedClicked == true) {
                        Toast.makeText(mContext, R.string.drive_toast, Toast.LENGTH_SHORT).show();
                    }else {
                        main_all_layout.setVisibility(View.GONE);
                        main_layout.setVisibility(View.GONE);
                        menu_main_layout.setVisibility(View.VISIBLE);
                        add_fare_frame_layout.setVisibility(View.GONE);
                        setEmptyStatus(btn_empty, btn_drive, btn_call, btn_pay);
                        //me: 버튼 -> 빈차등
                        windowService.menu_meterState(AMBlestruct.APP_MENU_REQUEST_CODE, AMBlestruct.MenuType.OPEN,"");
//                    Intent i = new Intent(mContext, AMMenuActivity.class);
//                    startActivity(i);
                    }

                    break;
                case R.id.nbtn_complex:  //복합 btn
                    break;
                case R.id.nbtn_login:  //로그인 btn
                    break;
                case R.id.nbtn_arrived:  //도착 btn
                    break;
                case R.id.nbtn_addpayment:  //추가금액 btn
                    //show frame 4
                    main_layout.setVisibility(View.GONE);
                    menu_layout.setVisibility(View.GONE);
                    number_pad_frame_layout.setVisibility(View.VISIBLE);
                    add_fare_frame_layout.setVisibility(View.VISIBLE);
                    break;

            }
        }
    };


    private void setEmptyStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
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

    private void setDriveStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
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

    private void setReserveStatus( ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
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

    private void setPayStatus(ButtonFitText btnEmpty, ButtonFitText btnDrive, ButtonFitText btnReserve, ButtonFitText btnPay) {
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