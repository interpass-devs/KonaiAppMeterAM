package com.konai.appmeter.driver_am.setting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupoort {

    private Context context;
    private Activity activity;

    //요청권한 배열
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };

    private List permissionList;

    //요청결과값
    private final int MULTIPLE_PERMISSIONS = 1023;

    //생성자
    public PermissionSupoort(Activity _activity, Context _context) {
        this.activity = _activity;
        this.context = _context;
    }


    //배열로 선언한 권한 중 허용되지 않은 권한 있는지 체크
    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<>();

        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(context, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            return false;
        }
        return true;
    }


    //배열로 선안한 권한에 대해 사옹자가 허용 요청
    public void requestPermission() {
        ActivityCompat.requestPermissions(
                activity,
                (String[]) permissionList.toArray(new String[permissionList.size()]),
                MULTIPLE_PERMISSIONS
        );
    }


    //요청한 권한에 대한 결과값 판단 및 처리
    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //requestCode가 아까 위에 final로 선언하였던 숫자와 맞는지, 결과값의 길이가 0보다는 큰지 먼저 체크
        if (requestCode == MULTIPLE_PERMISSIONS && (grantResults.length > 0)) {

            for (int i=0; i<grantResults.length; i++) {  //grantResult == 0 (허용) / grantResult == -1 (거부)

                //하나라도 -1이면 false 를 리턴
                if (grantResults[i] == -1) {
                    return false;
                }
            }
        }
        return true;
    }


}
