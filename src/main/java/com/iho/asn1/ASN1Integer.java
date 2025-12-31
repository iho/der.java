package com.iho.asn1;

import java.math.BigInteger;

public class ASN1Integer implements DERParseable<ASN1Integer>, DERSerializable {
    public final BigInteger value;

    public ASN1Integer(BigInteger value) {
        this.value = value;
    }

    public ASN1Integer(long value) {
        this.value = BigInteger.valueOf(value);
    }

    @Override
    public ASN1Integer fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.INTEGER)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected INTEGER, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "INTEGER must be primitive");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length == 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "INTEGER with 0 bytes");
        }

        // DER minimal encoding check
        if (data.length > 1) {
            byte first = data[0];
            byte second = data[1];
            if (first == 0x00 && (second & 0x80) == 0) {
                throw new ASN1Exception(ErrorCode.InvalidASN1IntegerEncoding, "Redundant leading zero");
            }
            if (first == (byte) 0xFF && (second & 0x80) != 0) {
                throw new ASN1Exception(ErrorCode.InvalidASN1IntegerEncoding, "Redundant leading FF");
            }
        }

        return new ASN1Integer(new BigInteger(data));
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(ASN1Identifier.INTEGER, value.toByteArray());
    }

    @Override
    public String toString() {
        return "ASN1Integer(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1Integer that = (ASN1Integer) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
