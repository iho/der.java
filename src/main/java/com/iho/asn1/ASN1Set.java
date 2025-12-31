package com.iho.asn1;

import java.util.ArrayList;
import java.util.List;

public class ASN1Set implements DERSerializable {
    public final List<DERSerializable> components;

    public ASN1Set(List<DERSerializable> components) {
        this.components = components;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writeConstructed(ASN1Identifier.SET, new DERSerializable() {
            @Override
            public void serialize(DERWriter nestedWriter) throws ASN1Exception {
                List<byte[]> serializedComponents = new ArrayList<>();
                for (DERSerializable component : components) {
                    DERWriter sub = new DERWriter();
                    component.serialize(sub);
                    serializedComponents.add(sub.toByteArray());
                }
                
                // DER requires sorting components by their encoding
                serializedComponents.sort(ASN1Set::compareBytes);
                
                for (byte[] comp : serializedComponents) {
                    nestedWriter.writeRaw(comp);
                }
            }
        });
    }

    private static int compareBytes(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int ai = a[i] & 0xFF;
            int bi = b[i] & 0xFF;
            if (ai != bi) return ai - bi;
        }
        return a.length - b.length;
    }

    public static <T> List<T> decode(ASN1Node node, DERParseable<T> decoder) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.SET)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected SET, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Constructed)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "SET must be constructed");
        }
        List<T> result = new ArrayList<>();
        for (ASN1Node child : (ASN1Node.Constructed) node.content) {
            result.add(decoder.fromDERNode(child));
        }
        return result;
    }
}
