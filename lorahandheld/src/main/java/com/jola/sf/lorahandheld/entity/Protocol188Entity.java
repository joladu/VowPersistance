package com.jola.sf.lorahandheld.entity;

import com.jola.sf.lorahandheld.util.HexBytesUtils;

/**
 * 封装188协议内容
 */

public class Protocol188Entity {

    /**
     * 帧起始标志
     * 字节数：1
     */
    private static final String StartTag = "68";
    /**
     * 帧结束标志
     * 字节数：1
     */
    private static final String EndTag = "16";

    /**
     *仪表类型
     * 字节数：1
     */
    private String equipType;

    /**
     * 地址域：7个字节 12个长度字符串
     * 字节数：7
     */
    private String address;

    /**
     * 控制码 一个字节 解析成二进制解析
     * 字节数：1
     */
    private String controlCode;


    /**
     * 数据长度 ： 数据域的字节数
     * 字节数：1
     */
    private String dataLen;

    /**
     * 数据标识
     */
    private String dataFlag;

    /**
     * 帧序列号
     */
    private String frameNo;


    /**
     * 数据域
     * 字节数：数据长度的值
     */
    private String dataStr;

    /**
     * 从起始符到校验码之前，各个字节算术累加，不超过FFH的溢出值
     */
    private String checkCode = "00";


    public Protocol188Entity(){}

    /**
     * @return 将16进制的报文转化成字节数组发送给抄控器
     */
    public byte[] rebuildHex2ByteArr(){
        if (null == equipType || null == address || null == controlCode || null == dataLen  || null == dataFlag || null == frameNo || null == dataStr){
            return null;
        }
        StringBuilder sb = new StringBuilder();
              sb.append(StartTag)
                .append(equipType)
                .append(address)
                .append(controlCode)
                .append(dataLen)
                .append(dataFlag)
                .append(frameNo)
                .append(dataStr)
                .append(checkCode)
                .append(EndTag);
        byte[] bytes = HexBytesUtils.hexStrToByteArr(sb.toString());
//        计算校验和
        bytes[bytes.length - 2]  = HexBytesUtils.getCheckedCode(bytes,0,bytes.length - 2);
        return bytes;
    }

    /**
     *
     * 字节：起始符 仪表类型 地址 控制码 数据长度 数据 校验码 结束符
     *          1       1     7     1       1     len     1     1
     *
     * @param originalHexStr   构建好的 16 进制字符串 进行解析
     */
    public Protocol188Entity(String originalHexStr) {
        if (null != originalHexStr && originalHexStr.length() > 0){

        }
    }

    public String getEquipType() {
        return equipType;
    }

    public void setEquipType(String equipType) {
        this.equipType = equipType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = reverseStringByByte(address);
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public String getDataLen() {
        return dataLen;
    }

    public void setDataLen(String dataLen) {
        this.dataLen = dataLen;
    }

    public String getDataStr() {
        return dataStr;
    }

    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(String frameNo) {
        this.frameNo = frameNo;
    }

    public String getDataFlag() {
        return dataFlag;
    }

    public void setDataFlag(String dataFlag) {
        this.dataFlag = dataFlag;
    }


    /**
     * 按字节返序
     *
     * @param originalStr 12345678
     * @return 78 56 34 12
     */
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

    //    public static class Builder{
//        /**
//         *仪表类型
//         */
//        private byte equipType;
//
//        /**
//         * 地址域：7个字节 12个长度字符串
//         */
//        private String address;
//
//        /**
//         * 控制码 一个字节 解析成二进制解析
//         */
//        private int controlCode;
//
//        /**
//         * 数据长度 ： 数据域的字节数
//         */
//        private int dataLen;
//        /**
//         * 数据域
//         */
//        private byte[] dataByteArr;
//
//        /**
//         * 从起始符到校验码之前，各个字节算术累加，不超过FFH的溢出值
//         */
//        private int checkCode;
//
//
//        public Builder equipType(byte equipType){
//            this.equipType = equipType;
//            return this;
//        }
//
//        public Builder address(String  address){
//            this.address = address;
//            return this;
//        }
//
//        public Builder controlCode(int controlCode){
//            this.controlCode = controlCode;
//            return this;
//        }
//
//        public Builder dataLen(int dataLen){
//            this.dataLen = dataLen;
//            return this;
//        }
//
//        public Builder dataArr(byte[] dataByteArr){
//            this.dataByteArr = dataByteArr;
//            return this;
//        }
//    }

}
