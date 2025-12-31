package com.iho.asn1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;

public abstract class ASN1Time implements DERParseable<ASN1Time>, DERSerializable {
    public final ZonedDateTime value;
    protected final ASN1Identifier identifier;

    protected ASN1Time(ZonedDateTime value, ASN1Identifier identifier) {
        this.value = value;
        this.identifier = identifier;
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        writer.writePrimitive(identifier, getEncodedString().getBytes(StandardCharsets.US_ASCII));
    }

    protected abstract String getEncodedString();

    public static class GeneralizedTime extends ASN1Time {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'");

        public GeneralizedTime(ZonedDateTime value) {
            super(value, ASN1Identifier.GENERALIZED_TIME);
        }

        @Override
        protected String getEncodedString() {
            return value.format(FORMATTER);
        }

        @Override
        public GeneralizedTime fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.GENERALIZED_TIME)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected GeneralizedTime");
            }
            String s = new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII);
            if (!s.endsWith("Z")) {
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "GeneralizedTime must end with Z in DER");
            }
            try {
                // Simplified: assuming YYYYMMDDHHMMSSZ format as per rust-asn1 logic
                ZonedDateTime dt = ZonedDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'").withZone(java.time.ZoneOffset.UTC));
                return new GeneralizedTime(dt);
            } catch (Exception e) {
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "Invalid GeneralizedTime format: " + s);
            }
        }
    }

    public static class UTCTime extends ASN1Time {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'");

        public UTCTime(ZonedDateTime value) {
            super(value, ASN1Identifier.UTC_TIME);
        }

        @Override
        protected String getEncodedString() {
            return value.format(FORMATTER);
        }

        @Override
        public UTCTime fromDERNode(ASN1Node node) throws ASN1Exception {
            if (!node.identifier.equals(ASN1Identifier.UTC_TIME)) {
                throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected UTCTime");
            }
            String s = new String(((ASN1Node.Primitive) node.content).data, StandardCharsets.US_ASCII);
            if (s.length() != 13 || !s.endsWith("Z")) {
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "UTCTime must be YYMMDDHHMMSSZ");
            }
            try {
                // ASN.1 UTCTime year logic: 00-49 -> 2000-2049, 50-99 -> 1950-1999
                int year = Integer.parseInt(s.substring(0, 2));
                int century = (year >= 50) ? 1900 : 2000;
                String fullYearS = String.format("%02d", century / 100) + s;
                ZonedDateTime dt = ZonedDateTime.parse(fullYearS, DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'").withZone(java.time.ZoneOffset.UTC));
                return new UTCTime(dt);
            } catch (Exception e) {
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "Invalid UTCTime format: " + s);
            }
        }
    }
}
