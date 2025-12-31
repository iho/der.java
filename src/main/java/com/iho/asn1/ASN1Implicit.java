package com.iho.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ASN1Implicit implements DERSerializable {
    public final ASN1Identifier identifier;
    public final DERSerializable value;

    public ASN1Implicit(ASN1Identifier identifier, DERSerializable value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        DERWriter innerWriter = new DERWriter();
        value.serialize(innerWriter);
        byte[] innerBytes = innerWriter.toByteArray();
        
        ASN1Node innerNode = DERParser.parse(innerBytes);
        
        boolean constructed = innerNode.isConstructed();
        byte[] contentBytes;
        
        if (constructed) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ASN1Node.Constructed constructedContent = (ASN1Node.Constructed) innerNode.content;
            for (ASN1Node child : constructedContent.getCollection()) {
                try {
                    baos.write(child.encodedBytes);
                } catch (IOException e) {
                    throw new ASN1Exception(ErrorCode.UnknownError, "Error writing implicit content");
                }
            }
            contentBytes = baos.toByteArray();
        } else {
            contentBytes = ((ASN1Node.Primitive) innerNode.content).data;
        }
        
        writer.writeNode(identifier, constructed, contentBytes);
    }
}
