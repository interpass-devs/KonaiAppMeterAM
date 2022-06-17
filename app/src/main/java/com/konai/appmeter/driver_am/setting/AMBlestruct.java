package com.konai.appmeter.driver_am.setting;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AMBlestruct {

    public static boolean mBTConnected = false;

    //me: 앱 -> 빈차등
    //엡이 실행되었을 때 요청하여 빈차등과 상태를 동기화한다.
    //빈승차 상태전송 명령
    public static final String curRequestCode = "15";  //요청코드 == 빈차등 미터기의 현재상태를 요청한다.
    public static final String curReponseCode = "69";  //응답코드
    //날짜시간
    public static String getCurDateString() {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyyMMddHHmmss");

        Calendar time = Calendar.getInstance();

        return format1.format(time.getTime());
    }

    //버튼값 B = button
    public static final String B_PAY = "01";        //지불
    public static final String B_EMPTY = "05";      //빈차
    public static final String B_DRIVE = "20";      //주행
    public static final String B_SUBURBAN = "31";   //시외
    public static final String B_COMPLEX = "32";    //복합
    public static final String B_RESERVE = "33";    //호출
    public static final String B_RECEIPT = "34";    //영수
    public static final String B_CLOSE = "50";      //후무
    public static final String B_CANCEL_PAY = "51"; //결제취소




    //me: 빈차등 -> 앱
    //빈차등에서 상태가 바뀐 경우 전달한다.
    //주행 중 요금이 바뀐 경우 전달한다.


}
