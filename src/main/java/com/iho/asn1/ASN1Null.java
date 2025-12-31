package com.iho.asn1;

public class ASN1Null implements DERParseable<ASN1Null>, DERSerializable {
    public static final ASN1Null INSTANCE = new ASN1Null();

    private ASN1Null() {}

    @Override
    public ASN1Null fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.NULL)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected NULL, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "NULL must be primitive");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length != 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "NULL must have length 0");
        }
        return INSTANCE;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(ASN1Identifier.NULL, new byte[0]);
    }

    @Override
    public String toString() {
        return "ASN1Null";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ASN1Null;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
