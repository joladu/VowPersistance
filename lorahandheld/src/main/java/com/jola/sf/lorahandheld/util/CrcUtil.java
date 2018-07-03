package com.jola.sf.lorahandheld.util;

/**
 * Created by lenovo on 2018/5/9.
 */

public class CrcUtil {

    private static int CRC_CODE = 0x8408;
    /**
     * @param index 起始地址
     * @param len 计算长度
     * @return 计算的hexstr
     */
    public static String  calCrc16(byte[] ptr, int index, int len) {
        int crc = 0xffff, tab = 0;
        byte i;

        tab = index;
        while (true) {
            crc ^= (0xff & ptr[tab]);
            tab++;
            for (i = 0; i < 8; i++) {
                if ((crc & 0x0001) > 0) {
                    crc >>= 1;
                    crc ^= CRC_CODE;
                } else {
                    crc >>= 1;
                }
            }
            if (tab == (len + index))
                break;
        }
        crc ^= 0xffff;
//        crc :69C5  变换一下顺序 C569 直接作为帧的crc校验
        String hexStr = Integer.toHexString(crc).toUpperCase();
        if (hexStr.length() < 4){
            for (int k = 0 ; k < 4 - hexStr.length() ; k++){
                hexStr = "0"+hexStr;
            }
        }
        return hexStr.substring(2,4)+hexStr.substring(0,2);
    }


}
