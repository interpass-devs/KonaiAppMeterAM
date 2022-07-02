package com.konai.appmeter.driver_am.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.konai.appmeter.driver_am.BuildConfig;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.util.ButtonFitText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    String resultData = "";
    ButtonFitText mainBtn;
    Context mContext;
    String mdn, carNum, mirrorappUpdate, strAppVersion;
    TextView updateText, updateMsg;
    TextView appVersion;

    /** Called when the activity is first created. */
    String File_Name = "app-debug.apk";   //확장자를 포함한 파일명
    String File_extend = "apk";

    String fileURL = "http://175.125.20.72:8080/update/";   // URL: 웹서버 쪽 파일이 있는 경로
    String Save_Path;
    String Save_folder = "/mydown";

    Button updateBtn;
    ProgressBar loadingBar;
    DownloadThread dThread;

    String log = "log_";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        storagePermission();

        mContext = this;
        appVersion = findViewById(R.id.appVersion);
        updateText = findViewById(R.id.updateText);
        mainBtn = findViewById(R.id.moveToMainActivity);

        //업데이트건이 있는지 확인 -json data 불러옴
        Thread NetworkThread = new Thread(new NetworkThread());
        NetworkThread.start();

        try {
            NetworkThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String currentAppVer = getVersionInfo(getApplicationContext());
        Log.d("currentAppVer",currentAppVer);

        //업데이트건이 있다면
        if (strAppVersion != null) {
            if (!currentAppVer.equals(strAppVersion)) {
                updateText.setText("앱업데이트 진행을위해\n확인버튼을 눌러주세요.");
                appVersion.setText("현재버전: v"+currentAppVer+"/ 새버전:  v"+strAppVersion);
            }else {
                //업데이트건이 없다면
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
        }else {
            //업데이트건이 없다면
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }

        //업데이트
        updateBtn = (Button) findViewById(R.id.downbtn);
        updateBtn.setOnClickListener(this);

        loadingBar = (ProgressBar) findViewById(R.id.Loading);


        // 다운로드 경로를 외장메모리 사용자 지정 폴더로 함.
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            Save_Path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + Save_folder;
            Log.d("save_path", Save_Path);   //storage/emulated/0/mydown
        }


    }//onCreate

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view.getId() == R.id.downbtn) {
            File dir = new File(Save_Path);
            // 폴더가 존재하지 않을 경우 폴더를 만듦
            if (!dir.exists()) {
                dir.mkdir();
            }

            // 다운로드 폴더에 동일한 파일명이 존재하는지 확인해서
            // 없으면 다운받고 있으면 해당 파일 실행시킴.
//            if (new File(Save_Path + "/" + File_Name).exists() == false) {
//                loadingBar.setVisibility(View.VISIBLE);
//                dThread = new DownloadThread(fileURL + "/" + File_Name,
//                        Save_Path + "/" + File_Name);
//                dThread.start();
//            } else {
//                showDownloadFile();
//            }

            updateBtn.setClickable(false);
            loadingBar.setVisibility(View.VISIBLE);
            dThread = new DownloadThread(fileURL + "/" + File_Name,
                    Save_Path + "/" + File_Name);
            dThread.start();

        }
    }

    public void storagePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) { }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    // 다운로드 쓰레드로 돌림..
    class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;

        DownloadThread(String serverPath, String localPath) {
            ServerUrl = serverPath;
            LocalPath = localPath;   // /storage/emulated/0/mydown/app-debug.apk
        }

        @Override
        public void run() {
            URL url;
            int Read;

            try {
                url = new URL(ServerUrl);          //"com.android.okhttp.internal.huc.HttpURLConnectionImpl:http://175.125.20.72:8080/update/app-debug.apk/app-debug.apk"
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();  //"com.android.okhttp.internal.huc.HttpURLConnectionImpl:http://175.125.20.72:8080/update/app-debug.apk/app-debug.apk"  //status: 여기서 에러남

                File file = new File(LocalPath);  // /storage/emulated/0/mydown/app-debug.apk

                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream fos = new FileOutputStream(file);
                for (;;) {
                    Read = is.read(tmpByte); //[0,0,0,0,0,0,0,0 ....... ]
                    if (Read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, Read);
                }
                is.close();
                fos.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                Log.e("ERROR1", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR2", e.getMessage());  //y
                e.printStackTrace();
            }
            mAfterDown.sendEmptyMessage(0);
        }
    }



    Handler mAfterDown = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            loadingBar.setVisibility(View.GONE);
            // 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
            updateBtn.setClickable(true);
            showDownloadFile();
        }

    };

    private void showDownloadFile() {

        File apkFile = new File(Save_Path + "/" + File_Name);   //storage/emulated/0/mydown/app-debug.apk
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (apkFile != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //누가버전
                try {
                    Uri fileUri = FileProvider.getUriForFile(this.getApplicationContext(), this.getApplicationContext().getPackageName() + ".fileprovider", apkFile);
//                    String fileUri = "content://com.konai.appmeter.driver_am.fileprovider/storage/emulated/0/mydown/app-debug.apk";
                    intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.startActivity(intent);
                    finish();

                }catch (Exception e) {

                }

            }else {
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                startActivity(intent);
            }

        }

    }

    public String getVersionInfo(Context context){
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch(PackageManager.NameNotFoundException e) { }
        return version;
    }


    class NetworkThread implements Runnable {

        @Override
        public void run() {

            HttpURLConnection conn = null;
            StringBuilder jasonData = new StringBuilder();

            try {

                URL url = new URL("http://175.125.20.72:33030/querymirrorapp?appname=mirror_app");
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
                        mirrorappUpdate = jsonObject.getString("mirrorapp_name");
                        strAppVersion = jsonObject.getString("mirrorapp_version");

                        Log.d("resultDa", strAppVersion+"");

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



}//Activity