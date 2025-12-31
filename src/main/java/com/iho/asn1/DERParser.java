package com.iho.asn1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DERParser {
    private static final int MAXIMUM_NODE_DEPTH = 50;
    private static final int MAXIMUM_TOTAL_NODES = 100_000;

    public static class ParserNode {
        public final ASN1Identifier identifier;
        public final int depth;
        public final boolean isConstructed;
        public final byte[] encodedBytes;
        public final byte[] dataBytes;

        public ParserNode(ASN1Identifier identifier, int depth, boolean isConstructed, byte[] encodedBytes, byte[] dataBytes) {
            this.identifier = identifier;
            this.depth = depth;
            this.isConstructed = isConstructed;
            this.encodedBytes = encodedBytes;
            this.dataBytes = dataBytes;
        }

        public boolean isEndMarker() {
            return identifier.tagClass == TagClass.Universal &&
                    identifier.tagNumber == 0 &&
                    !isConstructed &&
                    encodedBytes.length == 2 &&
                    encodedBytes[0] == 0 && encodedBytes[1] == 0;
        }
    }

    public static ASN1Node parse(byte[] data) throws ASN1Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<ParserNode> nodes = new ArrayList<>();
        int[] nodeCount = {0};
        
        parseNode(buffer, 1, nodes, nodeCount);

        if (buffer.hasRemaining()) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Trailing unparsed data is present");
        }

        if (nodes.isEmpty()) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "No ASN.1 nodes parsed");
        }

        ParserNode first = nodes.get(0);
        int rootDepth = first.depth;

        // Verify single root
        for (int i = 1; i < nodes.size(); i++) {
            if (nodes.get(i).depth <= rootDepth) {
                throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Multiple root nodes detected");
            }
        }

        ASN1Node.Content content;
        if (first.isConstructed) {
            content = new ASN1Node.Constructed(new ASN1NodeCollection(nodes, 1, nodes.size(), rootDepth));
        } else {
            content = new ASN1Node.Primitive(first.dataBytes);
        }

        return new ASN1Node(first.identifier, content, first.encodedBytes);
    }

    private static void parseNode(ByteBuffer buffer, int depth, List<ParserNode> nodes, int[] nodeCount) throws ASN1Exception {
        nodeCount[0]++;
        if (nodeCount[0] > MAXIMUM_TOTAL_NODES) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Excessive number of ASN.1 nodes");
        }
        if (depth > MAXIMUM_NODE_DEPTH) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Excessive stack depth was reached");
        }

        if (!buffer.hasRemaining()) {
            throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Buffer is empty");
        }

        int mark = buffer.position();
        byte rawIdentifier = buffer.get();
        boolean constructed = (rawIdentifier & 0x20) != 0;
        ASN1Identifier identifier;

        if ((rawIdentifier & 0x1F) == 0x1F) {
            TagClass tagClass = TagClass.fromTopByte(rawIdentifier);
            long tagNumber = readBase128Int(buffer);
            if (tagNumber < 0x1F) {
                throw new ASN1Exception(ErrorCode.InvalidASN1Object, "ASN.1 tag incorrectly encoded in long form: " + tagNumber);
            }
            identifier = new ASN1Identifier(tagNumber, tagClass);
        } else {
            identifier = ASN1Identifier.fromShortIdentifier(rawIdentifier);
        }

        long length = readLength(buffer);
        if (length == -1) { // Indefinite length
            // DER does not allow indefinite length
            throw new ASN1Exception(ErrorCode.UnsupportedFieldLength, "Indefinite form of field length not supported in DER.");
        }

        if (length > buffer.remaining()) {
            throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Field length exceeds remaining data");
        }

        int headerLen = buffer.position() - mark;
        int totalLen = (int) (headerLen + length);
        byte[] encodedBytes = new byte[totalLen];
        
        // Go back and read the full node
        int endPos = buffer.position() + (int) length;
        buffer.position(mark);
        buffer.get(encodedBytes);
        buffer.position(endPos);

        if (constructed) {
            nodes.add(new ParserNode(identifier, depth, true, encodedBytes, null));
            ByteBuffer subBuffer = ByteBuffer.wrap(encodedBytes, headerLen, (int) length);
            while (subBuffer.hasRemaining()) {
                parseNode(subBuffer, depth + 1, nodes, nodeCount);
            }
        } else {
            byte[] dataBytes = Arrays.copyOfRange(encodedBytes, headerLen, totalLen);
            nodes.add(new ParserNode(identifier, depth, false, encodedBytes, dataBytes));
        }
    }

    private static long readBase128Int(ByteBuffer buffer) throws ASN1Exception {
        long value = 0;
        while (true) {
            if (!buffer.hasRemaining()) {
                throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Truncated base-128 integer");
            }
            byte b = buffer.get();
            long chunk = b & 0x7F;
            if ((value & 0xFE00000000000000L) != 0) { // Overflow check for 7 bits shift
                throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Base-128 integer exceeds long range");
            }
            value = (value << 7) | chunk;
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return value;
    }

    private static long readLength(ByteBuffer buffer) throws ASN1Exception {
        if (!buffer.hasRemaining()) {
            throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Length missing");
        }
        int firstByte = buffer.get() & 0xFF;
        if (firstByte == 0x80) {
            return -1; // Indefinite
        }
        if ((firstByte & 0x80) == 0) {
            return firstByte;
        }

        int numBytes = firstByte & 0x7F;
        if (numBytes == 0) {
             throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Invalid length encoding");
        }
        if (numBytes > 8) {
             throw new ASN1Exception(ErrorCode.InvalidASN1Object, "Length exceeds supported range");
        }
        
        long length = 0;
        for (int i = 0; i < numBytes; i++) {
            if (!buffer.hasRemaining()) {
                throw new ASN1Exception(ErrorCode.TruncatedASN1Field, "Truncated length");
            }
            length = (length << 8) | (buffer.get() & 0xFF);
        }

        // Minimal encoding check for DER
        if (length < 128) {
            throw new ASN1Exception(ErrorCode.UnsupportedFieldLength, "Field length encoded in long form, but DER requires short form");
        }
        int requiredBytes = minimalOctetLen(length);
        if (numBytes > requiredBytes) {
             throw new ASN1Exception(ErrorCode.UnsupportedFieldLength, "Field length encoded in excessive number of bytes");
        }

        return length;
    }

    private static int minimalOctetLen(long value) {
        if (value == 0) return 1;
        int bits = 64 - Long.numberOfLeadingZeros(value);
        return (bits + 7) / 8;
    }
}
