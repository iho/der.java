package com.iho.asn1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class DERTests {

    @Test
    public void test_boolean_roundtrip() throws ASN1Exception {
        ASN1Boolean trueVal = new ASN1Boolean(true);
        ASN1Boolean falseVal = new ASN1Boolean(false);

        DERWriter writerTrue = new DERWriter();
        trueVal.serialize(writerTrue);
        byte[] encodedTrue = writerTrue.toByteArray();
        assertArrayEquals(new byte[]{0x01, 0x01, (byte) 0xFF}, encodedTrue);

        DERWriter writerFalse = new DERWriter();
        falseVal.serialize(writerFalse);
        byte[] encodedFalse = writerFalse.toByteArray();
        assertArrayEquals(new byte[]{0x01, 0x01, 0x00}, encodedFalse);

        ASN1Node nodeTrue = DERParser.parse(encodedTrue);
        assertEquals(trueVal, new ASN1Boolean(true).fromDERNode(nodeTrue));

        ASN1Node nodeFalse = DERParser.parse(encodedFalse);
        assertEquals(falseVal, new ASN1Boolean(false).fromDERNode(nodeFalse));
    }

    @Test
    public void test_integer_roundtrip() throws ASN1Exception {
        ASN1Integer intVal = new ASN1Integer(42);
        DERWriter writer = new DERWriter();
        intVal.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x02, 0x01, 0x2A}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(intVal, new ASN1Integer(0).fromDERNode(node));

        ASN1Integer largeInt = new ASN1Integer(new BigInteger("12345678901234567890"));
        writer = new DERWriter();
        largeInt.serialize(writer);
        encoded = writer.toByteArray();
        
        node = DERParser.parse(encoded);
        assertEquals(largeInt, new ASN1Integer(0).fromDERNode(node));
    }

    @Test
    public void test_null_roundtrip() throws ASN1Exception {
        ASN1Null nullVal = ASN1Null.INSTANCE;
        DERWriter writer = new DERWriter();
        nullVal.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x05, 0x00}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(nullVal, ASN1Null.INSTANCE.fromDERNode(node));
    }

    @Test
    public void test_octet_string_roundtrip() throws ASN1Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        ASN1OctetString os = new ASN1OctetString(data);
        DERWriter writer = new DERWriter();
        os.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x04, 0x05, 1, 2, 3, 4, 5}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(os, new ASN1OctetString(null).fromDERNode(node));
    }

    @Test
    public void test_bit_string_roundtrip() throws ASN1Exception {
        byte[] data = new byte[]{(byte) 0x80};
        ASN1BitString bs = new ASN1BitString(data, 7);
        DERWriter writer = new DERWriter();
        bs.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x03, 0x02, 0x07, (byte) 0x80}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(bs, new ASN1BitString(new byte[0], 0).fromDERNode(node));
    }

    @Test
    public void test_oid_roundtrip() throws ASN1Exception {
        long[] components = new long[]{1, 2, 840, 113549};
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(components);
        DERWriter writer = new DERWriter();
        oid.serialize(writer);
        byte[] encoded = writer.toByteArray();
        // 1.2.840.113549 -> 06 06 2A 86 48 86 F7 0D
        assertArrayEquals(new byte[]{0x06, 0x06, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(oid, new ASN1ObjectIdentifier(new long[]{0, 0}).fromDERNode(node));
    }

    @Test
    public void test_utf8_string_roundtrip() throws ASN1Exception {
        ASN1String.UTF8String s = new ASN1String.UTF8String("Hello");
        DERWriter writer = new DERWriter();
        s.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{12, 5, 'H', 'e', 'l', 'l', 'o'}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        assertEquals(s, new ASN1String.UTF8String("").fromDERNode(node));
    }

    @Test
    public void test_sequence_roundtrip() throws ASN1Exception {
        List<DERSerializable> comps = new ArrayList<>();
        comps.add(new ASN1Integer(1));
        comps.add(new ASN1Integer(2));
        ASN1Sequence seq = new ASN1Sequence(comps);

        DERWriter writer = new DERWriter();
        seq.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x30, 0x06, 0x02, 0x01, 0x01, 0x02, 0x01, 0x02}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        List<ASN1Integer> decoded = ASN1Sequence.decode(node, new ASN1Integer(0));
        assertEquals(2, decoded.size());
        assertEquals(new ASN1Integer(1), decoded.get(0));
        assertEquals(new ASN1Integer(2), decoded.get(1));
    }

    @Test
    public void test_set_roundtrip_canonical() throws ASN1Exception {
        List<DERSerializable> comps = new ArrayList<>();
        // Set: [Boolean(true), Integer(5)]
        // Boolean true: 01 01 FF
        // Integer 5: 02 01 05
        // Lexicographical ordering: 01 01 FF is before 02 01 05? 
        // 0x01 < 0x02. Yes.
        comps.add(new ASN1Integer(5));
        comps.add(new ASN1Boolean(true));
        ASN1Set set = new ASN1Set(comps);

        DERWriter writer = new DERWriter();
        set.serialize(writer);
        byte[] encoded = writer.toByteArray();
        // 0x31 0x06 01 01 FF 02 01 05
        assertArrayEquals(new byte[]{0x31, 0x06, 0x01, 0x01, (byte) 0xFF, 0x02, 0x01, 0x05}, encoded);
    }

    @Test
    public void test_real_roundtrip() throws ASN1Exception {
        ASN1Real real = new ASN1Real(2.5);
        DERWriter writer = new DERWriter();
        real.serialize(writer);
        byte[] encoded = writer.toByteArray();
        
        ASN1Node node = DERParser.parse(encoded);
        ASN1Real decoded = new ASN1Real(0).fromDERNode(node);
        assertEquals(2.5, decoded.value, 0.00001);
    }

    @Test
    public void test_generalized_time_roundtrip() throws ASN1Exception {
        ZonedDateTime dt = ZonedDateTime.of(2023, 10, 27, 12, 34, 56, 0, ZoneOffset.UTC);
        ASN1Time.GeneralizedTime gt = new ASN1Time.GeneralizedTime(dt);
        
        DERWriter writer = new DERWriter();
        gt.serialize(writer);
        byte[] encoded = writer.toByteArray();
        assertArrayEquals(new byte[]{0x18, 15, '2', '0', '2', '3', '1', '0', '2', '7', '1', '2', '3', '4', '5', '6', 'Z'}, encoded);

        ASN1Node node = DERParser.parse(encoded);
        ASN1Time.GeneralizedTime decoded = new ASN1Time.GeneralizedTime(null).fromDERNode(node);
        assertEquals(dt.toInstant(), decoded.value.toInstant());
    }

    @Test
    public void test_explicit_tagging() throws ASN1Exception {
        ASN1Integer val = new ASN1Integer(42);
        ASN1Identifier id = new ASN1Identifier(0, TagClass.ContextSpecific); // [0]
        ASN1Explicit explicit = new ASN1Explicit(id, val);
        
        DERWriter writer = new DERWriter();
        explicit.serialize(writer);
        byte[] encoded = writer.toByteArray();
        
        // Integer 42: 02 01 2A
        // Explicit [0]: A0 (Context Constructed 0) 03 (length) 02 01 2A
        assertArrayEquals(new byte[]{(byte) 0xA0, 0x03, 0x02, 0x01, 0x2A}, encoded);
        
        ASN1Node node = DERParser.parse(encoded);
        ASN1Integer decoded = ASN1Explicit.decode(node, id, new ASN1Integer(0));
        assertEquals(val, decoded);
    }

    @Test
    public void test_implicit_tagging() throws ASN1Exception {
        ASN1Integer val = new ASN1Integer(42);
        ASN1Identifier id = new ASN1Identifier(5, TagClass.Application); // [Application 5]
        ASN1Implicit implicit = new ASN1Implicit(id, val);
        
        DERWriter writer = new DERWriter();
        implicit.serialize(writer);
        byte[] encoded = writer.toByteArray();
        
        // Integer 42: 02 01 2A
        // Implicit [App 5]: 45 (Application Primitive 5) 01 (length) 2A (value)
        // Application=01, Primitive=00, Tag=00101 (5) -> 01000101 -> 0x45
        assertArrayEquals(new byte[]{0x45, 0x01, 0x2A}, encoded);
        
        // Decoding implicit is tricky as noted in implementation. 
        // We verify serialization correctness here.
    }

    @Test
    public void test_real_decimal_decoding() throws ASN1Exception {
        // Construct "123.45" decimal encoding
        // 09 (REAL) 07 (Length) 03 (Header: Decimal, NR3) '1' '2' '3' '.' '4' '5'
        // Header 03: bit 8=0 (Decimal). Bits 6..1: 000001 (NR1?) or ...
        // My implementation ignores bits 1..7 currently and just parses string.
        byte[] content = new byte[]{0x03, '1', '2', '3', '.', '4', '5'};
        // Full DER: 09 07 03 ...
        byte[] der = new byte[2 + content.length];
        der[0] = 0x09;
        der[1] = (byte) content.length;
        System.arraycopy(content, 0, der, 2, content.length);
        
        ASN1Node node = DERParser.parse(der);
        ASN1Real val = new ASN1Real(0).fromDERNode(node);
        assertEquals(123.45, val.value, 0.00001);
    }

    @Test
    public void test_additional_strings_roundtrip() throws ASN1Exception {
        // BMPString (UTF-16BE)
        ASN1String.BMPString bmp = new ASN1String.BMPString("Hello");
        DERWriter writer = new DERWriter();
        bmp.serialize(writer);
        byte[] encoded = writer.toByteArray();
        // Tag 30 (1E)
        // "Hello" in UTF-16BE: 00 48 00 65 00 6C 00 6C 00 6F (10 bytes)
        assertArrayEquals(new byte[]{0x1E, 0x0A, 0x00, 0x48, 0x00, 0x65, 0x00, 0x6C, 0x00, 0x6C, 0x00, 0x6F}, encoded);
        assertEquals(bmp, new ASN1String.BMPString("").fromDERNode(DERParser.parse(encoded)));

        // UniversalString (UTF-32BE)
        ASN1String.UniversalString univ = new ASN1String.UniversalString("Hi");
        writer = new DERWriter();
        univ.serialize(writer);
        encoded = writer.toByteArray();
        // Tag 28 (1C)
        // "Hi" in UTF-32BE: 00 00 00 48 00 00 00 69 (8 bytes)
        assertArrayEquals(new byte[]{0x1C, 0x08, 0, 0, 0, 0x48, 0, 0, 0, 0x69}, encoded);
        assertEquals(univ, new ASN1String.UniversalString("").fromDERNode(DERParser.parse(encoded)));

        // TeletexString (Latin1)
        ASN1String.TeletexString tlx = new ASN1String.TeletexString("Tlx");
        writer = new DERWriter();
        tlx.serialize(writer);
        encoded = writer.toByteArray();
        // Tag 20 (14)
        assertArrayEquals(new byte[]{0x14, 0x03, 'T', 'l', 'x'}, encoded);
        assertEquals(tlx, new ASN1String.TeletexString("").fromDERNode(DERParser.parse(encoded)));
        
        // VideotexString (Latin1)
        ASN1String.VideotexString vtx = new ASN1String.VideotexString("Vtx");
        writer = new DERWriter();
        vtx.serialize(writer);
        encoded = writer.toByteArray();
        // Tag 21 (15)
        assertArrayEquals(new byte[]{0x15, 0x03, 'V', 't', 'x'}, encoded);
        assertEquals(vtx, new ASN1String.VideotexString("").fromDERNode(DERParser.parse(encoded)));
        
        // GraphicString (Latin1)
        ASN1String.GraphicString gr = new ASN1String.GraphicString("Gr");
        writer = new DERWriter();
        gr.serialize(writer);
        encoded = writer.toByteArray();
        // Tag 25 (19)
        assertArrayEquals(new byte[]{0x19, 0x02, 'G', 'r'}, encoded);
        assertEquals(gr, new ASN1String.GraphicString("").fromDERNode(DERParser.parse(encoded)));
        
        // GeneralString (Latin1)
        ASN1String.GeneralString gen = new ASN1String.GeneralString("Gen");
        writer = new DERWriter();
        gen.serialize(writer);
        encoded = writer.toByteArray();
        // Tag 27 (1B)
        assertArrayEquals(new byte[]{0x1B, 0x03, 'G', 'e', 'n'}, encoded);
        assertEquals(gen, new ASN1String.GeneralString("").fromDERNode(DERParser.parse(encoded)));
    }
}
