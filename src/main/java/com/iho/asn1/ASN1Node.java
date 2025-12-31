package com.iho.asn1;

import java.util.Iterator;

public class ASN1Node {
    public final ASN1Identifier identifier;
    public final Content content;
    public final byte[] encodedBytes;

    public ASN1Node(ASN1Identifier identifier, Content content, byte[] encodedBytes) {
        this.identifier = identifier;
        this.content = content;
        this.encodedBytes = encodedBytes;
    }

    public boolean isConstructed() {
        return content instanceof Constructed;
    }

    public interface Content {}

    public static final class Primitive implements Content {
        public final byte[] data;

        public Primitive(byte[] data) {
            this.data = data;
        }
    }

    public static final class Constructed implements Content, Iterable<ASN1Node> {
        private final ASN1NodeCollection collection;

        public Constructed(ASN1NodeCollection collection) {
            this.collection = collection;
        }

        @Override
        public Iterator<ASN1Node> iterator() {
            return collection.iterator();
        }

        public ASN1NodeCollection getCollection() {
            return collection;
        }
    }
}
