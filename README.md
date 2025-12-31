# der.java

A robust ASN.1 DER (Distinguished Encoding Rules) parser and serializer for Java.

This project is a port of the `rust-asn1` library's DER implementation to Java, aiming for strict adherence to encoding rules, type safety, and ease of use.

## Features

*   **Strict DER Parsing**: Validates strict DER rules (e.g., minimal integer encoding, sorting of Set components).
*   **Comprehensive Type Support**: Supports a wide range of ASN.1 types including primitive, constructed, string, and time types.
*   **Tagging Support**: Full support for `EXPLICIT` and `IMPLICIT` tagging.
*   **Zero-Copy Parsing (where possible)**: Efficient architecture mirroring the Rust implementation.
*   **BigInteger Support**: Handling of arbitrary precision integers.
*   **Java 17+**: Built for modern Java environments.

## Supported ASN.1 Types

*   **Primitive Types**:
    *   `BOOLEAN`
    *   `INTEGER` (arbitrary precision)
    *   `NULL`
    *   `REAL` (Binary IEEE 754 and Decimal decoding)
    *   `ENUMERATED` (mapped to Integer/Long)
*   **String Types**:
    *   `OCTET STRING`
    *   `BIT STRING` (with padding bits)
    *   `UTF8String`
    *   `IA5String`
    *   `PrintableString`
    *   `NumericString`
    *   `VisibleString`
    *   `TeletexString` (T61)
    *   `VideotexString`
    *   `GraphicString`
    *   `GeneralString`
    *   `UniversalString` (UTF-32BE)
    *   `BMPString` (UTF-16BE)
*   **Time Types**:
    *   `GeneralizedTime`
    *   `UTCTime`
*   **Structure Types**:
    *   `SEQUENCE`
    *   `SET` (Canonical ordering supported)
    *   `OBJECT IDENTIFIER` (OID)
*   **Tagging**:
    *   `Explicit` Wrapper
    *   `Implicit` Wrapper

## Usage

### Parsing

```java
import com.iho.asn1.*;

byte[] encoded = ...;
ASN1Node node = DERParser.parse(encoded);

// Decode specific types
ASN1Integer intVal = new ASN1Integer(0).fromDERNode(node);
System.out.println("Integer: " + intVal.value);

// Or for constructed types
if (node.identifier.equals(ASN1Identifier.SEQUENCE)) {
    // ... iterate over children
}
```

### Serialization

```java
import com.iho.asn1.*;

DERWriter writer = new DERWriter();

// Serialize a Sequence
List<DERSerializable> sequenceValues = new ArrayList<>();
sequenceValues.add(new ASN1Integer(42));
sequenceValues.add(new ASN1String.UTF8String("Hello"));

ASN1Sequence seq = new ASN1Sequence(sequenceValues);
seq.serialize(writer);

byte[] result = writer.toByteArray();
```

### Explicit/Implicit Tagging

```java
// Explicit Tag [0]
ASN1Explicit explicit = new ASN1Explicit(
    new ASN1Identifier(0, TagClass.ContextSpecific), 
    new ASN1Integer(100)
);
explicit.serialize(writer);

// Implicit Tag [Application 5]
ASN1Implicit implicit = new ASN1Implicit(
    new ASN1Identifier(5, TagClass.Application), 
    new ASN1Integer(100)
);
implicit.serialize(writer);
```

## Build and Test

The project uses Gradle.

**Build:**
```bash
./gradlew build
```

**Run All Tests:**
```bash
./gradlew test
```

**Run Golden Tests:**
(Validates against golden samples from `rust-asn1`)
```bash
./gradlew test --tests "com.iho.asn1.GoldenTests"
```

## Project Structure

*   `src/main/java/com/iho/asn1`: Source code.
*   `src/test/java/com/iho/asn1`: Unit tests and Golden tests.
*   `tests/golden`: Golden DER files used for verification.

## License

MIT