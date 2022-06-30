package com.konai.appmeter.driver_am.handler;

import android.app.Activity;
import android.app.Service;
import android.os.Build;
import android.widget.Toast;

public class BackPressedKeyHandler {

    private long backKeyPressedTime = 0;
    private Activity mActivity;
    private Service mService;
    private Toast toast;

    public BackPressedKeyHandler(Activity activity, Service service) {
        this.mActivity = activity;
        this.mService = service;
    }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {  //2초
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {

            mActivity.moveTaskToBack(true);  //테스크를 백그라운드로 이동

            if (Build.VERSION.SDK_INT >= 21) {
                mActivity.finishAndRemoveTask();    //액티비티 종료 + 태스크 리스크에서 지우기
            }else {
                mActivity.finish();  //액티비티 종료
            }

            mService.stopSelf();

            toast.cancel();
        }
    }

    private void showGuide() {
        toast = Toast.makeText(mActivity, "뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}
