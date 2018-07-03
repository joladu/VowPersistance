package com.jola.sf.lorahandheld.entity;

import com.jola.sf.lorahandheld.util.CrcUtil;
import com.jola.sf.lorahandheld.util.HexBytesUtils;

/**
 * Created by lenovo on 2018/5/8.
 */

public class MainNodeEntity {
    private String macHead;
    private String len;
    private String controlCode;
    private String frameNo;
    private String networkNo;
    private String targetAddr;
    private String sourceAddr;
    private String macLoad;
    private String crcCheck = "0000";
//    private String rssi;


    public String rebuildMac2HexStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(macHead)
                .append(len)
                .append(controlCode)
                .append(frameNo)
                .append(networkNo)
                .append(targetAddr)
                .append(sourceAddr)
                .append(macLoad)
                .append(crcCheck)
//                .append(rssi)
        ;
        String originalHexStr = sb.toString();
//        计算crc校验
        byte[] bytes = HexBytesUtils.hexStrToByteArr(originalHexStr);
        crcCheck = CrcUtil.calCrc16(bytes,0,Integer.parseInt(len,16));

        sb = new StringBuilder();
        sb.append(macHead)
                .append(len)
                .append(controlCode)
                .append(frameNo)
                .append(networkNo)
                .append(targetAddr)
                .append(sourceAddr)
                .append(macLoad)
                .append(crcCheck)
//                .append(rssi)
        ;
        return sb.toString();
    }

    /**
     * 补0操作
     */
    public String prefix0ToLen(String originalStr, int needLen) {
        int length = originalStr.length();
        if (length > needLen) {
            return originalStr;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < needLen - length; i++) {
                sb.append("0");
            }
            sb.append(originalStr);
            return sb.toString();
        }
    }

    public String getMacHead() {
        return macHead;
    }

    public void setMacHead(String macHead) {
        this.macHead = macHead;
    }

    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public String getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(String frameNo) {
        this.frameNo = frameNo;
    }

    public String getNetworkNo() {
        return networkNo;
    }

    public void setNetworkNo(String networkNo) {
        this.networkNo = networkNo;
    }

    public String getTargetAddr() {
        return targetAddr;
    }

    public void setTargetAddr(String targetAddr) {
        this.targetAddr = targetAddr;
    }

    public String getSourceAddr() {
        return sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    public String getMacLoad() {
        return macLoad;
    }

    public void setMacLoad(String macLoad) {
        this.macLoad = macLoad;
    }

    public String getCrcCheck() {
        return crcCheck;
    }

    public void setCrcCheck(String crcCheck) {
        this.crcCheck = crcCheck;
    }

}
