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
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    String resultData = "";
    ButtonFitText mainBtn;
    Context mContext;
    String mdn, carNum, mirrorappUpdate, strAppVersion;
    TextView updateText, updateMsg;
    TextView appVersion;

    String File_Name = "app-debug.apk";   //확장자를 포함한 파일명
    String File_extend = "apk";  //확장자명

    String fileURL = "http://175.125.20.72:8080/update/app-debug.apk"; // URL: 웹서버 쪽 파일이 있는 경로
    String Save_Path;
    String Save_folder = "/mydown";

    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        mContext = this;

        appVersion = findViewById(R.id.appVersion);
        updateText = findViewById(R.id.updateText);
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
        //서버에서 jsonData를 불러온다.
        Thread NetworkThread = new Thread(new NetworkThread());
        NetworkThread.start();

        try {
            NetworkThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mirrorappUpdate != null) {
            if (mirrorappUpdate.equals("Y")) {
                updateText.setText("앱업데이트 진행을위해\n확인버튼을 눌러주세요.");
                appVersion.setText("앱버전:  v"+strAppVersion);
            }
        }



        //업데이트 건이 있을 경우, 파일 다운로드
        ButtonFitText btn = (ButtonFitText) findViewById(R.id.downbtn);
        btn.setOnClickListener(this);

        loadingBar = (ProgressBar) findViewById(R.id.Loading);

    }//onCreate



    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.downbtn) {
//
            loadingBar.setVisibility(View.VISIBLE);
            new Thread(new UpdateThread()).start();
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

                loadingBar.setVisibility(View.GONE);

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



}//Activity