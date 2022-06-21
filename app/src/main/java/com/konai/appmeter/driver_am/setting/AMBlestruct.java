package com.konai.appmeter.driver_am.setting;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AMBlestruct {

    public static boolean mBTConnected = false;

    //me: 앱 -> 빈차등
    //엡이 실행되었을 때 요청하여 빈차등과 상태를 동기화한다.
    //빈승차 상태전송 명령
    public static final String curRequestCode = "15";  //요청코드 == 빈차등 미터기의 현재상태를 요청한다.
    public static final String curReponseCode = "19";  //응답코드
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

    public static String mSState = "";


    //me: 빈차등 -> 앱
    //빈차등에서 상태가 바뀐 경우 전달한다.
    //주행 중 요금이 바뀐 경우 전달한다.
    public static class AMReceiveFare {
        public static String M_RECEIVE_TIME; //날짜시간
        public static String M_CARNUM;     //차량번호
        public static String M_STATE; //버튼값 (지불/빈차/주행/할증)

        public static String M_START_FARE; //승차요금
        public static String M_CALL_FARE;  //호출요금
        public static String M_ETC_FARE;   //기타요금
        public static String M_EXTRA_FARE_TYPE;  //할증여부
        public static String M_EXTRA_FARE_RATE;  //할증율

        public static String M_START_TIME;  //승차시간
        public static String M_START_X;    //승차좌표-X
        public static String M_START_Y;    //승차좌표-Y
        public static String M_END_X;      //하차좌표-X
        public static String M_END_Y;      //하차좌표-Y

        public static String M_START_DISTANCE; //승차거리
        public static String M_EMPTY_DISTANCE; //빈차거리
    }


    public static class  MeterState {

        public static final int PAY = 1;
        public static final int EMPTY = 2;
        public static final int DRIVE = 3;
        public static final int EXTRA = 4;
    }

}
