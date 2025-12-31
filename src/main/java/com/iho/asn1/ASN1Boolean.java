package com.iho.asn1;

public class ASN1Boolean implements DERParseable<ASN1Boolean>, DERSerializable {
    public final boolean value;

    public ASN1Boolean(boolean value) {
        this.value = value;
    }

    @Override
    public ASN1Boolean fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.BOOLEAN)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected BOOLEAN, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "BOOLEAN must be primitive");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length != 1) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "BOOLEAN must have length 1");
        }
        if (data[0] == 0x00) {
            return new ASN1Boolean(false);
        } else if (data[0] == (byte) 0xFF) {
            return new ASN1Boolean(true);
        } else {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "DER BOOLEAN must be 0x00 or 0xFF");
        }
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(ASN1Identifier.BOOLEAN, new byte[]{value ? (byte) 0xFF : 0x00});
    }

    @Override
    public String toString() {
        return "ASN1Boolean(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1Boolean that = (ASN1Boolean) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
