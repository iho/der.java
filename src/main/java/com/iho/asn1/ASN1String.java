package com.iho.asn1;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class ASN1String implements DERParseable<ASN1String>, DERSerializable {
    public final String value;
    protected final ASN1Identifier identifier;

    protected ASN1String(String value, ASN1Identifier identifier) {
        this.value = value;
        this.identifier = identifier;
        validate();
    }

    protected void validate() {
        // Default no-op
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        // Default serialization (UTF-8) - subclasses should override if different
        writer.writePrimitive(identifier, value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1String that = (ASN1String) o;
        return Objects.equals(value, that.value) && Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, identifier);
    }

    public static class UTF8String extends ASN1String {
        public UTF8String(String value) {
            super(value, ASN1Identifier.UTF8_STRING);
        }

        @Override
        public UTF8String fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.UTF8_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected UTF8String");
            }
            return new UTF8String(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.UTF_8));
        }
    }

    public static class PrintableString extends ASN1String {
        public PrintableString(String value) {
            super(value, ASN1Identifier.PRINTABLE_STRING);
        }

        @Override
        protected void validate() {
            for (char c : value.toCharArray()) {
                if (!isPrintable(c)) {
                    throw new IllegalArgumentException("Invalid character in PrintableString: " + c);
                }
            }
        }

        private boolean isPrintable(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
                    " '()+,-./:=?".indexOf(c) != -1;
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
             writer.writePrimitive(identifier, value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public PrintableString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.PRINTABLE_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected PrintableString");
            }
            return new PrintableString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII));
        }
    }

    public static class IA5String extends ASN1String {
        public IA5String(String value) {
            super(value, ASN1Identifier.IA5_STRING);
        }

        @Override
        protected void validate() {
            for (char c : value.toCharArray()) {
                if (c > 127) {
                    throw new IllegalArgumentException("Invalid character in IA5String: " + c);
                }
            }
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
             writer.writePrimitive(identifier, value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public IA5String fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.IA5_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected IA5String");
            }
            return new IA5String(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII));
        }
    }

    public static class NumericString extends ASN1String {
        public NumericString(String value) {
            super(value, ASN1Identifier.NUMERIC_STRING);
        }

        @Override
        protected void validate() {
            for (char c : value.toCharArray()) {
                if (!(c >= '0' && c <= '9') && c != ' ') {
                    throw new IllegalArgumentException("Invalid character in NumericString: " + c);
                }
            }
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
             writer.writePrimitive(identifier, value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public NumericString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.NUMERIC_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected NumericString");
            }
            return new NumericString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII));
        }
    }

    public static class VisibleString extends ASN1String {
        public VisibleString(String value) {
            super(value, ASN1Identifier.VISIBLE_STRING);
        }

        @Override
        protected void validate() {
            for (char c : value.toCharArray()) {
                if (c < 32 || c > 126) {
                    throw new IllegalArgumentException("Invalid character in VisibleString: " + c);
                }
            }
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
             writer.writePrimitive(identifier, value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public VisibleString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.VISIBLE_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected VisibleString");
            }
            return new VisibleString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII));
        }
    }

    public static class TeletexString extends ASN1String {
        public TeletexString(String value) {
            super(value, ASN1Identifier.TELETEX_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            writer.writePrimitive(identifier, value.getBytes(StandardCharsets.ISO_8859_1));
        }

        @Override
        public TeletexString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.TELETEX_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected TeletexString");
            }
            return new TeletexString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.ISO_8859_1));
        }
    }

    public static class VideotexString extends ASN1String {
        public VideotexString(String value) {
            super(value, ASN1Identifier.VIDEOTEX_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            writer.writePrimitive(identifier, value.getBytes(StandardCharsets.ISO_8859_1));
        }

        @Override
        public VideotexString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.VIDEOTEX_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected VideotexString");
            }
            return new VideotexString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.ISO_8859_1));
        }
    }

    public static class GraphicString extends ASN1String {
        public GraphicString(String value) {
            super(value, ASN1Identifier.GRAPHIC_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            writer.writePrimitive(identifier, value.getBytes(StandardCharsets.ISO_8859_1));
        }

        @Override
        public GraphicString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.GRAPHIC_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected GraphicString");
            }
            return new GraphicString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.ISO_8859_1));
        }
    }

    public static class GeneralString extends ASN1String {
        public GeneralString(String value) {
            super(value, ASN1Identifier.GENERAL_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            writer.writePrimitive(identifier, value.getBytes(StandardCharsets.ISO_8859_1));
        }

        @Override
        public GeneralString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.GENERAL_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected GeneralString");
            }
            return new GeneralString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.ISO_8859_1));
        }
    }

    public static class UniversalString extends ASN1String {
        public UniversalString(String value) {
            super(value, ASN1Identifier.UNIVERSAL_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            try {
                writer.writePrimitive(identifier, value.getBytes("UTF-32BE"));
            } catch (java.io.UnsupportedEncodingException e) {
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "UTF-32BE not supported");
            }
        }

        @Override
        public UniversalString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.UNIVERSAL_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected UniversalString");
            }
            try {
                return new UniversalString(new String(((ASN1Node.Primitive) node.content).data, "UTF-32BE"));
            } catch (java.io.UnsupportedEncodingException e) {
                 throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "UTF-32BE not supported");
            }
        }
    }

    public static class BMPString extends ASN1String {
        public BMPString(String value) {
            super(value, ASN1Identifier.BMP_STRING);
        }

        @Override
        public void serialize(DERWriter writer) throws ASN1Exception {
            writer.writePrimitive(identifier, value.getBytes(StandardCharsets.UTF_16BE));
        }

        @Override
        public BMPString fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.BMP_STRING)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected BMPString");
            }
            return new BMPString(new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.UTF_16BE));
        }
    }
}
