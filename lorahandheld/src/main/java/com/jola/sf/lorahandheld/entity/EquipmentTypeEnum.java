package com.jola.sf.lorahandheld.entity;

/**
 * 帧报文中对应的 “仪表类型”
 */

public enum EquipmentTypeEnum {

    WaterMeter((byte) 0x10), GasoMeter((byte) 0x30);

    private byte typeValue;

    EquipmentTypeEnum(byte typeValue) {
        this.typeValue = typeValue;
    }

    public byte getTypeValue(){
        return typeValue;
    }

//    public static int getTypeValue(String typeName) {
//        for (EquipmentTypeEnum tempEnum : EquipmentTypeEnum.values()) {
//            if (typeName.equals(tempEnum.getTypeName())) {
//                return tempEnum.getTypeValueHex();
//            }
//        }
//        return 0;
//    }



}
