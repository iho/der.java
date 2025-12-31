package com.iho.asn1;

import java.util.ArrayList;
import java.util.List;

public class ASN1Sequence implements DERSerializable {
    public final List<DERSerializable> components;

    public ASN1Sequence(List<DERSerializable> components) {
        this.components = components;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writeSequence(new DERSerializable() {
            @Override
            public void serialize(DERWriter nestedWriter) throws ASN1Exception {
                for (DERSerializable component : components) {
                    component.serialize(nestedWriter);
                }
            }
        });
    }

    public static <T> List<T> decode(ASN1Node node, DERParseable<T> decoder) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.SEQUENCE)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected SEQUENCE, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Constructed)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "SEQUENCE must be constructed");
        }
        List<T> result = new ArrayList<>();
        for (ASN1Node child : (ASN1Node.Constructed) node.content) {
            result.add(decoder.fromDERNode(child));
        }
        return result;
    }
}
