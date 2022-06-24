package com.konai.appmeter.driver_am.handler;

import android.app.Activity;
import android.widget.Toast;

public class BackPressedKeyHandler {

    private long backKeyPressedTime = 0;
    private Activity mActivity;
    private Toast toast;

    public BackPressedKeyHandler(Activity activity) {
        this.mActivity = activity;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {  //2초
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            mActivity.finish();
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
