package com.konai.appmeter.driver_am.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.security.identity.ResultData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.konai.appmeter.driver_am.BuildConfig;
import com.konai.appmeter.driver_am.R;
import com.konai.appmeter.driver_am.util.ButtonFitText;
import com.konai.appmeter.driver_am.util.FontFitTextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    String resultData = "";
    ButtonFitText mainBtn;
    Context mContext;
    String mdn, carNum, mirrorappUpdate, strAppVersion;
    TextView updateText, updateMsg;
    FontFitTextView appVersion;

    String File_Name = "app-debug.apk";   //확장자를 포함한 파일명
    String File_extend = "apk";  //확장자명

    String fileURL = "http://175.125.20.72:8080/update/app-debug.apk"; // URL: 웹서버 쪽 파일이 있는 경로
    String Save_Path;
    String Save_folder = "/mydown";

    ProgressBar loadingBar;
    DownloadThread dThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        mContext = this;

        appVersion = findViewById(R.id.appVersion);
        updateText = findViewById(R.id.updateText);
        updateMsg = findViewById(R.id.updateMsg);
        mainBtn = findViewById(R.id.moveToMainActivity);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, MainActivity.class);
                startActivity(i);
            }
        });




        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //먼저 업데이트가 있는지 확인
        Thread NetworkThread = new Thread(new NetworkThread());
        NetworkThread.start();

        try {
            NetworkThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mirrorappUpdate != null) {
            if (mirrorappUpdate.equals("Y")) {
                updateText.setText("AppMeter앱을\n업데이트하시겠습니까?");
                appVersion.setText("다운로드 크기: 11MB/  앱버전: "+strAppVersion);
                updateMsg.setText("앱을 최신 버전으로 업데이트 할 것을 권장합니다.\n업데이트 버튼을 눌러주세요.");
            }else {
                updateText.setText("업데이트 건이 없습니다.");
            }
        }



        //업데이트 건이 있을 경우, 파일 다운로드
        ButtonFitText btn = (ButtonFitText) findViewById(R.id.downbtn);
        btn.setOnClickListener(this);

        loadingBar = (ProgressBar) findViewById(R.id.Loading);

        // 다운로드 경로를 외장메모리 사용자 지정 폴더로 함.
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            Save_Path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + Save_folder;
        }
    }//onCreate



    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.downbtn) {
            Toast.makeText(mContext, "click up", Toast.LENGTH_SHORT).show();
            File dir = new File(Save_Path);
            // 폴더가 존재하지 않을 경우 폴더를 만듦
            if (!dir.exists()) {
                dir.mkdir();
            }

            // 다운로드 폴더에 동일한 파일명이 존재하는지 확인해서
            // 없으면 다운받고 있으면 해당 파일 실행시킴.
            if (new File(Save_Path + "/" + File_Name).exists() == false) {
                loadingBar.setVisibility(View.VISIBLE);
                dThread = new DownloadThread(fileURL + "/" + File_Name,
                        Save_Path + "/" + File_Name);
                dThread.start();
            } else {
                showDownloadFile();
            }
        }

    }//onClick



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
                        strAppVersion = jsonObject.getString("mirrorapp_version");

                        Log.d("resultData-update", mdn+":  "+carNum+":  "+mirrorappUpdate+", appVersion: "+strAppVersion);

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




    class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;

        DownloadThread(String serverPath, String localPath) {
            ServerUrl = serverPath;
            LocalPath = localPath;
        }

        @Override
        public void run() {
            URL imgurl;
            int Read;
            try {
                imgurl = new URL(ServerUrl);
                HttpURLConnection conn = (HttpURLConnection) imgurl
                        .openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(LocalPath);
                FileOutputStream fos = new FileOutputStream(file);
                for (;;) {
                    Read = is.read(tmpByte);
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
                Log.e("ERROR2", e.getMessage());
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
            showDownloadFile();
        }

    };

    private void showDownloadFile() {

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(Save_Path + "/" + File_Name);

        // 파일 확장자 별로 mime type 지정해 준다.
        if (File_extend.equals("apk")) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(intent);
    }

}//Activity