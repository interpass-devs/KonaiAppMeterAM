package com.konai.appmeter.driver_am.setting;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AMBlestruct {

    public static boolean mBTConnected = false;

    //me: 앱 -> 빈차등
    //엡이 실행되었을 때 요청하여 빈차등과 상태를 동기화한다.
    //빈승차 상태전송 명령
    public static final String APP_REQUEST_CODE = "15";  //요청코드 == 빈차등 미터기의 현재상태를 요청한다.
    public static final String METER_REQUEST_CODE = "19";  //응답코드
    public static final String APP_MENU_REQUEST_CODE = "41";
    public static final String APP_MENU_CONTENTS_REQUEST_CODE = "43";
    public static final String METER_MENU_REQUEST_CODE = "42";
    public static final String METER_MENU_INPUT_REQUEST_CODE = "46";  //빈차등 -> 앱으로 입력요청 (etc: 날짜입력..)
    public static final String APP_MENU_INPUT_REQUEST_CODE = "47";  //앱 -> 빈차등으로 입력결과 전송

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
    public static final String B_CALL = "33";    //호출
    public static final String B_RECEIPT = "34";    //영수
    public static final String B_CLOSE = "50";      //후무
    public static final String B_CANCEL_PAY = "51"; //결제취소
    public static final String B_MENU = "" ;

    public static String mSState = "00";  //미터기 현재상태
    public static String menuBtnStatus = "1";  //0-닫기/ 1-열기
    public static boolean mbSStateupdated = false;

    public static class AMReceiveMsg {
        public static int MSG_CUR_BLE_STATE = 1;
        public static int MSG_CUR_AM_STATE = 2;
        public static int MSG_CUR_MENU_STATE = 3;
        public static int MSG_CUR_INPUT_MENU_STATE = 4;
    }

    public static class MenuType {
        public static String TYPE ="1";  //메세지종류(닫기/메뉴/정보출력/숫자키버튼) 또는 메세지방번호(리사이클러뷰)
        public static String CLOSE = "0";  //메뉴닫기
        public static String OPEN = "1";   //메뉴 메세지
        public static String PRINT_INFO = "2";  //정보출력
        public static String PRINT_BY_NUMBER = "3"; //정보출력 + 숫자키버튼 0-9
        public static String MENU_CONTENT = "0";  //기능선택 (0-번호선택/ 1-이전(정정) /2-닫기)
    }

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
        //빈차등으로부터 수신받을 때
        //전송 버튼값
        public static final int PAY = 1;  //지불
        public static final int EMPTY = 2; //빈차
        public static final int DRIVE = 3; //주행
        public static final int CALL = 4;  //호출
    }


    //메뉴 수신값
    public static class AMReceiveMenu {

        public static String MENU_RECEIVE_TIME; //날짜시간
        public static byte MENU_MSG_TYPE;    //메세지 종류
        public static String MENU_MSG;         //메뉴 메세지
        public static String MENU_INPUT_TYPE;  //메뉴- 키패드 입력형식

    }

    synchronized public static void setSStateupdate(boolean bupdate)
    {

        mbSStateupdated = bupdate;

    }
}
