package com.iho.asn1;

import java.util.Objects;
import java.util.Optional;

public class ASN1Identifier {
    public final long tagNumber;
    public final TagClass tagClass;

    public ASN1Identifier(long tagNumber, TagClass tagClass) {
        this.tagNumber = tagNumber;
        this.tagClass = tagClass;
    }

    public static ASN1Identifier fromShortIdentifier(byte shortIdentifier) {
        if ((shortIdentifier & 0x1F) == 0x1F) {
            throw new IllegalArgumentException("Short identifier cannot have tag number 31");
        }
        return new ASN1Identifier(shortIdentifier & 0x1F, TagClass.fromTopByte(shortIdentifier));
    }

    public Optional<Byte> shortForm() {
        if (tagNumber < 0x1F) {
            byte baseNumber = (byte) tagNumber;
            baseNumber |= tagClass.topByteFlags();
            return Optional.of(baseNumber);
        } else {
            return Optional.empty();
        }
    }

    public static final ASN1Identifier BOOLEAN = new ASN1Identifier(0x01, TagClass.Universal);
    public static final ASN1Identifier INTEGER = new ASN1Identifier(0x02, TagClass.Universal);
    public static final ASN1Identifier BIT_STRING = new ASN1Identifier(0x03, TagClass.Universal);
    public static final ASN1Identifier OCTET_STRING = new ASN1Identifier(0x04, TagClass.Universal);
    public static final ASN1Identifier NULL = new ASN1Identifier(0x05, TagClass.Universal);
    public static final ASN1Identifier OBJECT_IDENTIFIER = new ASN1Identifier(0x06, TagClass.Universal);
    public static final ASN1Identifier REAL = new ASN1Identifier(0x09, TagClass.Universal);
    public static final ASN1Identifier ENUMERATED = new ASN1Identifier(0x0A, TagClass.Universal);
    public static final ASN1Identifier UTF8_STRING = new ASN1Identifier(0x0C, TagClass.Universal);
    public static final ASN1Identifier SEQUENCE = new ASN1Identifier(0x10, TagClass.Universal);
    public static final ASN1Identifier SET = new ASN1Identifier(0x11, TagClass.Universal);
    public static final ASN1Identifier NUMERIC_STRING = new ASN1Identifier(0x12, TagClass.Universal);
    public static final ASN1Identifier PRINTABLE_STRING = new ASN1Identifier(0x13, TagClass.Universal);
    public static final ASN1Identifier TELETEX_STRING = new ASN1Identifier(0x14, TagClass.Universal);
    public static final ASN1Identifier VIDEOTEX_STRING = new ASN1Identifier(0x15, TagClass.Universal);
    public static final ASN1Identifier IA5_STRING = new ASN1Identifier(0x16, TagClass.Universal);
    public static final ASN1Identifier UTC_TIME = new ASN1Identifier(0x17, TagClass.Universal);
    public static final ASN1Identifier GENERALIZED_TIME = new ASN1Identifier(0x18, TagClass.Universal);
    public static final ASN1Identifier GRAPHIC_STRING = new ASN1Identifier(0x19, TagClass.Universal);
    public static final ASN1Identifier VISIBLE_STRING = new ASN1Identifier(0x1A, TagClass.Universal);
    public static final ASN1Identifier GENERAL_STRING = new ASN1Identifier(0x1B, TagClass.Universal);
    public static final ASN1Identifier UNIVERSAL_STRING = new ASN1Identifier(0x1C, TagClass.Universal);
    public static final ASN1Identifier BMP_STRING = new ASN1Identifier(0x1E, TagClass.Universal);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1Identifier that = (ASN1Identifier) o;
        return tagNumber == that.tagNumber && tagClass == that.tagClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagNumber, tagClass);
    }

    @Override
    public String toString() {
        return String.format("ASN1Identifier(tagNumber: %d, tagClass: %s%s)",
                tagNumber, tagClass, shortForm().map(s -> String.format(", shortForm: 0x%02X", s)).orElse(", longForm"));
    }
}
