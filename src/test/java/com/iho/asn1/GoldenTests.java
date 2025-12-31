package com.iho.asn1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;

public class GoldenTests {

    private byte[] readGolden(String name) throws IOException {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path path = projectDir.resolve("tests/golden/").resolve(name);
        return Files.readAllBytes(path);
    }

    @Test
    public void test_true() throws IOException, ASN1Exception {
        byte[] der = readGolden("true.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Boolean val = new ASN1Boolean(false).fromDERNode(node);
        assertTrue(val.value);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_false() throws IOException, ASN1Exception {
        byte[] der = readGolden("false.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Boolean val = new ASN1Boolean(true).fromDERNode(node);
        assertFalse(val.value);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_int_42() throws IOException, ASN1Exception {
        byte[] der = readGolden("int_42.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Integer val = new ASN1Integer(0).fromDERNode(node);
        assertEquals(42, val.value.intValue());
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_int_large() throws IOException, ASN1Exception {
        byte[] der = readGolden("int_large.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Integer val = new ASN1Integer(0).fromDERNode(node);
        // 02 08 01 02 03 04 05 06 07 08 -> 72623859790382856
        assertEquals(new BigInteger("72623859790382856"), val.value);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_null() throws IOException, ASN1Exception {
        byte[] der = readGolden("null.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Null val = ASN1Null.INSTANCE.fromDERNode(node);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_octet_string() throws IOException, ASN1Exception {
        byte[] der = readGolden("octet_string.der");
        ASN1Node node = DERParser.parse(der);
        ASN1OctetString val = new ASN1OctetString(null).fromDERNode(node);
        // "Hello World"
        assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), val.value);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_bit_string() throws IOException, ASN1Exception {
        byte[] der = readGolden("bit_string.der");
        ASN1Node node = DERParser.parse(der);
        ASN1BitString val = new ASN1BitString(new byte[0], 0).fromDERNode(node);
        // 03 0C 00 30 41 33 42 35 46 32 39 31 43 44
        // Content: "0A3B5F291CD"
        byte[] expected = "0A3B5F291CD".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, val.value);
        assertEquals(0, val.paddingBits);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_oid() throws IOException, ASN1Exception {
        byte[] der = readGolden("oid.der");
        ASN1Node node = DERParser.parse(der);
        ASN1ObjectIdentifier val = new ASN1ObjectIdentifier(new long[]{0, 0}).fromDERNode(node);
        long[] comps = val.getComponents();
        // 1.2.840.113549.1.1.11
        assertArrayEquals(new long[]{1, 2, 840, 113549, 1, 1, 11}, comps);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_utf8_string() throws IOException, ASN1Exception {
        byte[] der = readGolden("utf8_string.der");
        ASN1Node node = DERParser.parse(der);
        ASN1String.UTF8String val = new ASN1String.UTF8String("").fromDERNode(node);
        // "Hello UTF8"
        assertEquals("Hello UTF8", val.value);
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_generalized_time() throws IOException, ASN1Exception {
        byte[] der = readGolden("generalized_time.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Time.GeneralizedTime val = new ASN1Time.GeneralizedTime(null).fromDERNode(node);
        // 20230101120000Z
        ZonedDateTime expected = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(expected.toInstant(), val.value.toInstant());
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }

    @Test
    public void test_utc_time() throws IOException, ASN1Exception {
        byte[] der = readGolden("utc_time.der");
        ASN1Node node = DERParser.parse(der);
        ASN1Time.UTCTime val = new ASN1Time.UTCTime(null).fromDERNode(node);
        // 230101120000Z -> 2023...
        ZonedDateTime expected = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(expected.toInstant(), val.value.toInstant());
        
        DERWriter writer = new DERWriter();
        val.serialize(writer);
        assertArrayEquals(der, writer.toByteArray());
    }
}
