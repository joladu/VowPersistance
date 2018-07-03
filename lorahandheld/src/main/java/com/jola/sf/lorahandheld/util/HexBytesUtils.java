package com.jola.sf.lorahandheld.util;

import android.util.Log;

public class HexBytesUtils {



    private static final String qppHexStr = "0123456789ABCDEF";

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * bytes Arr to hex Str
     * @param bytes
     * @return
     */
    public static String byteArrToHexStr(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for(byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }
        return new String(buf);
    }

    /**
     * hex Str to  bytes Arr
     * @param hexStr
     * @return
     */
    public static byte[] hexStrToByteArr(String hexStr) {
        if(hexStr == null || hexStr.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[hexStr.length() / 2];
        for(int i = 0; i < hexStr.length() / 2; i++) {
            String subStr = hexStr.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

//    /**
//     * Hex char convert to byte array
//     */
//    public static byte[] hexStr2Bytes(String hexString) {
//
//        if (hexString == null || hexString.isEmpty()) {
//            return null;
//        }
//
//        hexString = hexString.toUpperCase();
//
//        int length = hexString.length() >> 1;
//        char[] hexChars = hexString.toCharArray();
//
//        int i = 0;
//
//        do {
//            int checkChar = qppHexStr.indexOf(hexChars[i]);
//
//            if (checkChar == -1)
//                return null;
//            i++;
//        } while (i < hexString.length());
//
//        byte[] dataArr = new byte[length];
//
//
//        for (i = 0; i < length; i++) {
//            int strPos = i * 2;
//
//            dataArr[i] = (byte) (charToByte(hexChars[strPos]) << 4 | charToByte(hexChars[strPos + 1]));
//        }
//
//        return dataArr;
//    }

    private static byte charToByte(char c) {
        return (byte) qppHexStr.indexOf(c);
    }

    /**
     * @return 计算校验和
     */
    public static byte getCheckedCode(byte[] checkedByteArr, int startPostion, int endPosition) {
        byte checkSum = 0;
        for (int i = startPostion; i < endPosition + 1; i++) {
            checkSum += checkedByteArr[i];
        }
        return checkSum;
    }

}

