package com.iho.asn1;

import java.io.ByteArrayOutputStream;

public class DERWriter {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }

    public void writeSerializable(DERSerializable value) throws ASN1Exception {
        value.serialize(this);
    }

    public void writeRaw(byte[] bytes) {
        buffer.writeBytes(bytes);
    }

    public void writePrimitive(ASN1Identifier identifier, byte[] content) throws ASN1Exception {
        writeNode(identifier, false, content);
    }

    public void writeConstructed(ASN1Identifier identifier, DERSerializable content) throws ASN1Exception {
        DERWriter nested = new DERWriter();
        content.serialize(nested);
        writeNode(identifier, true, nested.toByteArray());
    }

    public void writeSequence(DERSerializable content) throws ASN1Exception {
        writeConstructed(ASN1Identifier.SEQUENCE, content);
    }

    public void writeSet(DERSerializable content) throws ASN1Exception {
        writeConstructed(ASN1Identifier.SET, content);
    }

    void writeNode(ASN1Identifier identifier, boolean constructed, byte[] content) throws ASN1Exception {
        writeIdentifier(identifier, constructed);
        writeLength(content.length);
        buffer.writeBytes(content);
    }

    private void writeIdentifier(ASN1Identifier identifier, boolean constructed) throws ASN1Exception {
        if (identifier.tagNumber < 0x1F) {
            byte b = (byte) identifier.tagNumber;
            if (constructed) b |= 0x20;
            b |= identifier.tagClass.topByteFlags();
            buffer.write(b);
        } else {
            byte top = (byte) (0x1F | identifier.tagClass.topByteFlags());
            if (constructed) top |= 0x20;
            buffer.write(top);
            writeBase128Int(identifier.tagNumber);
        }
    }

    private void writeBase128Int(long value) {
        if (value == 0) {
            buffer.write(0);
            return;
        }
        int numBits = 64 - Long.numberOfLeadingZeros(value);
        int numBytes = (numBits + 6) / 7;
        for (int i = numBytes - 1; i >= 0; i--) {
            byte b = (byte) ((value >> (i * 7)) & 0x7F);
            if (i > 0) b |= 0x80;
            buffer.write(b);
        }
    }

    private void writeLength(int length) {
        if (length <= 0x7F) {
            buffer.write((byte) length);
        } else {
            int numBits = 32 - Integer.numberOfLeadingZeros(length);
            int numBytes = (numBits + 7) / 8;
            buffer.write((byte) (0x80 | numBytes));
            for (int i = numBytes - 1; i >= 0; i--) {
                buffer.write((byte) ((length >> (i * 8)) & 0xFF));
            }
        }
    }
}
