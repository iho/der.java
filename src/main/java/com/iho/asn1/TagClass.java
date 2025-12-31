package com.iho.asn1;

public enum TagClass {
    Universal(0x00),
    Application(0x01),
    ContextSpecific(0x02),
    Private(0x03);

    private final int value;

    TagClass(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TagClass fromTopByte(byte topByte) {
        int index = (topByte & 0xFF) >> 6;
        return TagClass.values()[index];
    }

    public byte topByteFlags() {
        return (byte) (value << 6);
    }
}
