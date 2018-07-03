package com.jola.sf.lorahandheld;

import android.util.Log;

import com.jola.sf.lorahandheld.util.CrcUtil;
import com.jola.sf.lorahandheld.util.HexBytesUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private String reverseStringByByte(String originalStr) {
        if (null == originalStr || originalStr.length() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int len = originalStr.length() / 2;
        for (int i = len; i > 0; i--) {
//            len = 4
//            6-8 4-6 2-4 0-2
            sb.append(originalStr.substring(i * 2 - 2, i * 2));
        }
        return sb.toString();
    }


    @Test
    public void addition_isCorrect() throws Exception {


        int len = "st25sdk-1.2.0.jar".length();
        System.out.println(len);

//        String inputNum = "1001.0";
//        System.out.println("inputNum:"+inputNum);
//        String  floatStr = Float.parseFloat(inputNum)+"";
//        int indexDian = floatStr.lastIndexOf(".");
//        String zhengshu = floatStr.substring(0,indexDian);
//        String xiaoshu = floatStr.substring(indexDian + 1,floatStr.length());
//
//        System.out.println("zhengshu:"+zhengshu);
//        System.out.println("xiaoshu:"+xiaoshu);



//        String testStr = "9921170802063000045584";
//        System.out.println("testStr:"+testStr.length());

//        String hexStr = "681070000107172000B31820A004000000002C000000002C2018051814482003016B014F164F4B";
//        StringBuilder resolveSb = new StringBuilder();
//        String dataContentStr = hexStr.substring(28,70);
////                        String timeStr = reverseStringByByte(dataContentStr.substring(16,30));
//        resolveSb.append("成功;");
//        resolveSb.append("; 当前累计流量:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(0,6)))+"."+Integer.parseInt(reverseStringByByte(dataContentStr.substring(6,8))));
//        resolveSb.append("; 结算日累积量:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(10,16)))+"."+Integer.parseInt(reverseStringByByte(dataContentStr.substring(16,18))));
//        resolveSb.append("; 实时时间:"+dataContentStr.substring(20,24)+"-"+dataContentStr.substring(24,26)+"-"+dataContentStr.substring(26,28)+" "+dataContentStr.substring(28,30)+":"+dataContentStr.substring(30,32)+":"+dataContentStr.substring(32,34));
//        resolveSb.append("; 表状态:"+dataContentStr.substring(34,36)+" "+dataContentStr.substring(36,38));
//        resolveSb.append("; 电池电压:"+Integer.parseInt(reverseStringByByte(dataContentStr.substring(38,42)),16)+" (10mV)");
//        System.out.println("resolveSb:"+resolveSb.toString());

//        Long time = Long.parseLong("8AC4016C", 16);
//        System.out.println("time:"+time);

//        String hexStr = "681070000107172000810820D102000000002CCF";
//        String totalFow = reverseStringByByte(hexStr.substring(28, 36));
////        String zhengshu = totalFow.substring(0, 6);
////        String xiaoshu = totalFow.substring(6, 8);
//        String result = "; 上"+"月结算日累积量:"+Integer.parseInt(totalFow.substring(0, 6))+"."+Integer.parseInt(totalFow.substring(6, 8));
//        System.out.println("result:"+result);

//       String hexStr = "681A03035804FFFFFFFF0100008051180505150156322E30306F063070";
//        final String hardwareV =
//                (char) (Integer.parseInt(hexStr.substring(38, 40), 16))+""
//                        + (char) (Integer.parseInt(hexStr.substring(40, 42), 16)) +""
//                        +(char) (Integer.parseInt(hexStr.substring(42, 44), 16))+""
//                        +(char) (Integer.parseInt(hexStr.substring(44, 46), 16)) + ""
//                        +(char)(Integer.parseInt(hexStr.substring(46, 48), 16))+"";
//        Log.e("Jola", " hardwareV="+  hardwareV);

////        硬件版本 56 32 2E 30 30   V 2 . 0 0
//        final String hardwareV = (char) (Integer.parseInt("56", 16))+""
//                + (char) (Integer.parseInt("32", 16)) + ""
//                +(char) (Integer.parseInt("2E", 16))+ ""
//                +(char) (Integer.parseInt("30", 16)) + ""
//                +(char)(Integer.parseInt("30", 16))+"";
//
//        System.out.println("hardwareV:"+hardwareV);

//        硬件版本 56 32 2E 30 30   V 2 . 0 0
//        char char_v = 'V';
//        char char_2 = '2';
//        char char_dian = '.';
//        char char_0 = '0';
//        System.out.println("char_v:"+char_v);
//        System.out.println("char_v:"+(int)char_v);
//        System.out.println("char_2:"+char_2);
//        System.out.println("char_2:"+(int)char_2);
//        System.out.println("char_dian:"+char_dian);
//        System.out.println("char_dian:"+(int)char_dian);
//        System.out.println("char_0:"+char_0);
//        System.out.println("char_0:"+(int)char_0);
//
//        System.out.println("result:"+char_v+""+char_2+""+char_dian+""+char_0+""+char_0);

//        String originalStr = "12345678";
//        if (null == originalStr || originalStr.length() == 0){
//            return ;
//        }
//        StringBuilder sb = new StringBuilder();
//        int len = originalStr.length() / 2;
//        for (int i = len;i > 0;i--){
////            len = 4
////            6-8 4-6 2-4 0-2
//            sb.append(originalStr.substring(i *2 - 2,i*2));
//        }
//        System.out.println("sb.toString():"+sb.toString());

//        String test = "680F0301FFFFFFFFFFFFFFFFFFFF22";
//        String test = "68130301FFFF01000080FFFFFFFF2003000090C402";
////        String test = "68130301FFFF01000080FFFFFFFF2003000090";
//        byte[] bytes = HexBytesUtils.hexStr2Bytes(test);
//        String hexStr = CrcUtil.calCrc16(bytes,0,19);
//        System.out.println("crc1:"+hexStr);

//        int crc = CrcUtil.calCrc16(new byte[] { 0x02, 0x05, 0x00, 0x03, (byte) 0xff, 0x00 },0,6);
//        System.out.println(String.format("0x%04x", crc));


//        assertEquals(4, 2 + 2);
//        String test1 = "680F0301FFFFFFFFFFFFFFFFFFFF22C569";
//        String test1 = "680F0301FFFFFFFFFFFFFFFFFFFF22C569";
//        System.out.println("test1:"+test1);
//        byte[] bytes1 = HexBytesUtils.hexStr2Bytes(test1);
//        String hexStr = HexBytesUtils.byteArrToHexStr(bytes1);
//        System.out.println("hexStr:"+hexStr);
//        System.out.println("len - 2:"+bytes1[bytes1.length - 2]);
//        System.out.println("len - 1:"+bytes1[bytes1.length - 1]);




//        int resultCode = 0;
//        for (int i = 0;i < bytes.length;i++){
//            resultCode += bytes[i];
//        }
//        System.out.println("resultCode:"+resultCode);
//        String hexString = Integer.toHexString(resultCode);
//        System.out.println("hexString :"+hexString);

//        byte[] bytes2 = CrcUtil.setParamCRC(bytes);
//        String hexStr1 = HexBytesUtils.byteArrToHexStr(bytes2);
//        System.out.println("hexStr1:"+hexStr1);
//        System.out.println("len - 2:"+bytes2[bytes2.length - 2]);
//        System.out.println("len - 1:"+bytes2[bytes2.length - 1]);


    }
}