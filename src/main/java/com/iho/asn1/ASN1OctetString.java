package com.iho.asn1;

import java.util.Arrays;

public class ASN1OctetString implements DERParseable<ASN1OctetString>, DERSerializable {
    public final byte[] value;

    public ASN1OctetString(byte[] value) {
        this.value = value;
    }

    @Override
    public ASN1OctetString fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.OCTET_STRING)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected OCTET STRING, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "OCTET STRING must be primitive in DER");
        }
        return new ASN1OctetString(((ASN1Node.Primitive) node.content).data);
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(ASN1Identifier.OCTET_STRING, value);
    }

    @Override
    public String toString() {
        return "ASN1OctetString(" + Arrays.toString(value) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1OctetString that = (ASN1OctetString) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
