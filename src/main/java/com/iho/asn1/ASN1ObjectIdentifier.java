package com.iho.asn1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASN1ObjectIdentifier implements DERParseable<ASN1ObjectIdentifier>, DERSerializable {
    private final byte[] encoded;

    private ASN1ObjectIdentifier(byte[] encoded) {
        this.encoded = encoded;
    }

    public ASN1ObjectIdentifier(long[] components) throws ASN1Exception {
        if (components.length < 2) {
            throw new ASN1Exception(ErrorCode.TooFewOIDComponents, "Must have at least 2 components");
        }
        if (components[0] > 2) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "First OID component must be 0, 1, or 2");
        }
        if (components[0] < 2 && components[1] > 39) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Second OID component must be <= 39 if first is 0 or 1");
        }

        List<Byte> buffer = new ArrayList<>();
        long firstValue = components[0] * 40 + components[1];
        writeSubidentifier(firstValue, buffer);
        for (int i = 2; i < components.length; i++) {
            writeSubidentifier(components[i], buffer);
        }
        
        this.encoded = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); i++) {
            this.encoded[i] = buffer.get(i);
        }
    }

    private void writeSubidentifier(long value, List<Byte> buffer) {
        if (value == 0) {
            buffer.add((byte) 0);
            return;
        }
        int numBits = 64 - Long.numberOfLeadingZeros(value);
        int numBytes = (numBits + 6) / 7;
        for (int i = numBytes - 1; i >= 0; i--) {
            byte b = (byte) ((value >> (i * 7)) & 0x7F);
            if (i > 0) b |= 0x80;
            buffer.add(b);
        }
    }

    public long[] getComponents() throws ASN1Exception {
        List<Long> components = new ArrayList<>();
        int index = 0;
        
        long firstValue = readSubidentifier(index);
        index = skipSubidentifier(index);
        
        long first = firstValue / 40;
        long second = firstValue % 40;
        if (first > 2) {
             second = firstValue - 80;
             first = 2;
        }
        components.add(first);
        components.add(second);

        while (index < encoded.length) {
            components.add(readSubidentifier(index));
            index = skipSubidentifier(index);
        }

        long[] result = new long[components.size()];
        for (int i = 0; i < components.size(); i++) {
            result[i] = components.get(i);
        }
        return result;
    }

    private long readSubidentifier(int index) throws ASN1Exception {
        long value = 0;
        while (true) {
            if (index >= encoded.length) {
                throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Truncated OID subidentifier");
            }
            byte b = encoded[index++];
            value = (value << 7) | (b & 0x7F);
            if ((b & 0x80) == 0) break;
        }
        return value;
    }

    private int skipSubidentifier(int index) {
        while ((encoded[index++] & 0x80) != 0);
        return index;
    }

    @Override
    public ASN1ObjectIdentifier fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.OBJECT_IDENTIFIER)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected OID, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "OID must be primitive");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length == 0) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Zero components in OID");
        }
        
        // Validate subidentifiers
        int i = 0;
        while (i < data.length) {
            if (data[i] == (byte) 0x80) {
                 throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Leading zero in subidentifier");
            }
            while (i < data.length && (data[i] & 0x80) != 0) i++;
            if (i >= data.length) {
                 throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Truncated subidentifier");
            }
            i++;
        }

        return new ASN1ObjectIdentifier(data);
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(ASN1Identifier.OBJECT_IDENTIFIER, encoded);
    }

    @Override
    public String toString() {
        try {
            long[] comps = getComponents();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < comps.length; i++) {
                if (i > 0) sb.append(".");
                sb.append(comps[i]);
            }
            return "ASN1ObjectIdentifier(" + sb + ")";
        } catch (ASN1Exception e) {
            return "ASN1ObjectIdentifier(invalid)";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1ObjectIdentifier that = (ASN1ObjectIdentifier) o;
        return Arrays.equals(encoded, that.encoded);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encoded);
    }
    
    private static class Arrays {
        static boolean equals(byte[] a, byte[] a2) {
            return java.util.Arrays.equals(a, a2);
        }
        static int hashCode(byte[] a) {
            return java.util.Arrays.hashCode(a);
        }
    }
}
