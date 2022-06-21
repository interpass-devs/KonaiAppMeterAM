package com.konai.appmeter.driver_am.socket;

import java.io.UnsupportedEncodingException;

//me: 프로토콜 아스키 코드로..
public class AMPacket {

    public int point = 0;

    public void Setbyte(byte[] v, byte byt) {
        v[point++] = byt;

    }

    public void Setbyte(byte[] v, byte byt, int start) {
        v[start++] = byt;

    }

    public void SetString(byte[] v, String sdata) {
        int cut = 0;
        int tmp = point;
        for (; point < sdata.length() + tmp;) {
            v[point++] = (byte) sdata.charAt(cut++);
        }
    }

    public void SetString(byte[] v, String sdata, int nlen) {
        String sTemp;
        int ntmp = sdata.length();

        SetString(v, sdata);

        for (int i = 0; i < nlen - ntmp; i++)
            v[point++] = ' ';

    }

    //nlen 자릿수 0맞추기
    public void SetIntString(byte[] v, int ndata, int nlen) {

        String stmp = String.format("%d", ndata);
        int ntmp = stmp.length();

        for (int i = 0; i < nlen - ntmp; i++)
            v[point++] = '0';

        SetString(v, stmp);

    }

    public void SetIntString(byte[] v, int number) {
        int cut = 0;
        int tmp = point;
        String sTemp;
        sTemp = String.valueOf(number);

        for (; point < sTemp.length() + tmp;) {
            v[point++] = (byte) sTemp.charAt(cut++);
        }
    }

    public byte Getbyte(byte[]v)
    {

        return v[point++];

    }

    public int Getint(byte[] v, int nlen) {

        String stmp = "";
        for(int i = 0; i < nlen; i++)
        {
            stmp += (char)v[point++];
        }

        return Integer.valueOf(stmp);

    }

    public long Getlong(byte[] v, int nlen) {

        String stmp = "";
        for(int i = 0; i < nlen; i++)
        {
            stmp += (char)v[point++];
        }

        return Long.valueOf(stmp);

    }

    public double Getdouble(byte[] v, int nlen) {

        String stmp = "";
        for(int i = 0; i < nlen; i++)
        {
            stmp += (char)v[point++];
        }

        return Double.valueOf(stmp);

    }

    public String GetString(byte[] v, int nlen) {  //원본/ LENGTH
        String stmp = "";
        for(int i = 0; i < nlen; i++)
        {
            stmp += (char)v[point++];
        }

        return stmp;
    }

    public String GetDate(byte[] v) {
        String stmp = "";
        for(int i = 0; i < 14; i++)
        {
            stmp += (char)v[point++];
        }

        return stmp;

    }

    public String Gettextbytoken(byte[] v, byte by, int packetlen, int offset) {
        String stmp = null;
        int nlen = 0;

        for(int i = point; i < packetlen; i++)
        {
            if(v[i] == by)
                break;
            nlen++;
        }

        nlen += offset;

        byte[] tmpbyte = new byte[nlen];

        for(int i = 0; i < nlen; i++)
        {

            tmpbyte[i] += v[point++];

        }

        try {
            stmp = new String(tmpbyte, "KSC5601");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return stmp;

    }

    public byte GetCRC(byte[] v) {
        byte crc = 0;
        for (int i = 0; i < point; i++) {
            crc ^= v[i];
        }
        crc |= 0x20;
        return crc;
    }

    public byte GetCRC(byte[] v, int len) {
        byte crc = 0;
        for (int i = 0; i < len; i++) {
            crc ^= v[i];
        }
        crc |= 0x20;
        return crc;
    }

    public String GetAMBleCRC(byte[] v) {
        byte crc = 0;
        for (int i = 1; i < point; i++) {
            crc += v[i];
        }

        return String.format("%02X", crc);

    }

    public String GetAMBleCRC(byte[] v, int len) {
        byte crc = 0;
        for (int i = 1; i < len - 3; i++) {
            crc += v[i];
        }

        return String.format("%02X", crc);

    }

    // CODE부분 을 얻어온다.
    public String GetCheckCode(byte[] data) {
        String tmp = (char) data[1] + "";
        tmp += (char) data[2] + "";

        return tmp;
    }

    public void SetPoint(int i) {
        point = i;
    }

    public int GetPoint() {
        return point;
    }

}
