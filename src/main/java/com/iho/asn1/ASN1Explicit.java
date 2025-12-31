package com.iho.asn1;

public class ASN1Explicit implements DERSerializable {
    public final ASN1Identifier identifier;
    public final DERSerializable value;

    public ASN1Explicit(ASN1Identifier identifier, DERSerializable value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        // Explicit tagging wraps the value in a constructed node with the new tag
        writer.writeConstructed(identifier, value);
    }

    public static <T> T decode(ASN1Node node, ASN1Identifier expectedIdentifier, DERParseable<T> decoder) throws ASN1Exception {
        if (!node.identifier.equals(expectedIdentifier)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected " + expectedIdentifier + ", got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Constructed)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Explicit tag must be constructed");
        }
        ASN1NodeCollection children = ((ASN1Node.Constructed) node.content).getCollection();
        
        // Explicit tag should contain exactly one child: the original value
        java.util.Iterator<ASN1Node> it = children.iterator();
        if (!it.hasNext()) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Explicit tag must contain a value");
        }
        ASN1Node child = it.next();
        if (it.hasNext()) {
             throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Explicit tag must contain only one value");
        }
        
        return decoder.fromDERNode(child);
    }
}
