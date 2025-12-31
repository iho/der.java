package com.iho.asn1;

import java.util.Arrays;

public class ASN1BitString implements DERParseable<ASN1BitString>, DERSerializable {
    public final byte[] value;
    public final int paddingBits;

    public ASN1BitString(byte[] value, int paddingBits) throws ASN1Exception {
        if (paddingBits < 0 || paddingBits > 7) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Invalid padding bits: " + paddingBits);
        }
        if (value.length == 0 && paddingBits != 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Empty BIT STRING must have 0 padding bits");
        }
        this.value = value;
        this.paddingBits = paddingBits;
    }

    @Override
    public ASN1BitString fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.BIT_STRING)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected BIT STRING, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "BIT STRING must be primitive in DER");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length == 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "BIT STRING missing padding byte");
        }
        int paddingBits = data[0] & 0xFF;
        if (paddingBits > 7) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Invalid padding bits: " + paddingBits);
        }
        byte[] bits = Arrays.copyOfRange(data, 1, data.length);
        if (bits.length == 0 && paddingBits != 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Empty BIT STRING with padding");
        }
        
        // DER check: unused bits must be zero
        if (bits.length > 0 && paddingBits > 0) {
            int last = bits[bits.length - 1] & 0xFF;
            int mask = (1 << paddingBits) - 1;
            if ((last & mask) != 0) {
                throw new ASN1Exception(ErrorCode.InvalidASN1Object, "BIT STRING unused bits must be zero");
            }
        }
        return new ASN1BitString(bits, paddingBits);
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        byte[] data = new byte[value.length + 1];
        data[0] = (byte) paddingBits;
        System.arraycopy(value, 0, data, 1, value.length);
        writer.writePrimitive(ASN1Identifier.BIT_STRING, data);
    }

    @Override
    public String toString() {
        return String.format("ASN1BitString(bits: %s, padding: %d)", Arrays.toString(value), paddingBits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1BitString that = (ASN1BitString) o;
        return paddingBits == that.paddingBits && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(value);
        result = 31 * result + paddingBits;
        return result;
    }
}
