package com.iho.asn1;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

public class ASN1Real implements DERParseable<ASN1Real>, DERSerializable {
    public final double value;

    public ASN1Real(double value) {
        this.value = value;
    }

    @Override
    public ASN1Real fromDERNode(ASN1Node node) throws ASN1Exception {
        if (!node.identifier.equals(ASN1Identifier.REAL)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "Expected REAL, got " + node.identifier);
        }
        if (!(node.content instanceof ASN1Node.Primitive)) {
            throw new ASN1Exception(ErrorCode.UnexpectedFieldType, "REAL must be primitive");
        }
        byte[] data = ((ASN1Node.Primitive) node.content).data;
        if (data.length == 0) return new ASN1Real(0.0);

        byte first = data[0];
        if (first == 0x40) return new ASN1Real(Double.POSITIVE_INFINITY);
        if (first == 0x41) return new ASN1Real(Double.NEGATIVE_INFINITY);

        if ((first & 0x80) != 0) {
            // Binary encoding
            double sign = (first & 0x40) != 0 ? -1.0 : 1.0;
            int expLen = (first & 0x03) + 1;
            
            if (data.length < 1 + expLen) {
                throw new ASN1Exception(ErrorCode.InvalidASN1Object, "REAL encoding too short");
            }

            long exponent = 0;
            for (int i = 0; i < expLen; i++) {
                exponent = (exponent << 8) | (data[1 + i] & 0xFF);
            }
            // Sign extend
            if ((data[1] & 0x80) != 0) {
                exponent |= (~0L << (expLen * 8));
            }

            long mantissa = 0;
            for (int i = 1 + expLen; i < data.length; i++) {
                mantissa = (mantissa << 8) | (data[i] & 0xFF);
            }

            // Note: This logic assumes base-2 and might need scaling for the mantissa alignment.
            // Simplified: value = S * M * 2^E
            return new ASN1Real(sign * mantissa * Math.pow(2.0, exponent));
        } else {
            // Decimal encoding (bit 8 is 0)
            // Bit 7 reserved (0) or specialized?
            // "If bit 8 is 0, then the encoding ... is decimal"
            // Content starts at byte 1 (0-indexed byte 0 is header).
            
            if (data.length < 2) {
                // If length is 1, it's just the header byte. Empty string? "value is 0"? 
                // No, value 0 is length 0.
                throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "Decimal REAL too short");
            }
            
            String s = new String(data, 1, data.length - 1, StandardCharsets.US_ASCII);
            try {
                // Handle NR1/NR2/NR3. Java's parseDouble handles most: "123", "123.45", "1.2E3"
                // Replace comma with dot if present (ISO 6093 allows comma)
                s = s.replace(',', '.');
                return new ASN1Real(Double.parseDouble(s));
            } catch (NumberFormatException e) {
                 throw new ASN1Exception(ErrorCode.InvalidStringRepresentation, "Invalid Decimal REAL format: " + s);
            }
        }
    }

    @Override
    public void serialize(DERWriter writer) throws ASN1Exception {
        // Zero
        if (value == 0.0) {
            // Check for negative zero?
            if (Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(-0.0)) {
                // DER doesn't strictly distinguish -0.0 from 0.0 usually, but let's encode as 0-length (0.0).
                // "The real value zero is encoded ... as no octets."
            }
            writer.writePrimitive(ASN1Identifier.REAL, new byte[0]);
            return;
        }

        if (Double.isInfinite(value)) {
            writer.writePrimitive(ASN1Identifier.REAL, new byte[]{value > 0 ? (byte) 0x40 : (byte) 0x41});
            return;
        }

        if (Double.isNaN(value)) {
            throw new ASN1Exception(ErrorCode.InvalidASN1Object, "NaN cannot be encoded in DER REAL");
        }

        // Binary encoding (base 2)
        long bits = Double.doubleToRawLongBits(value);
        boolean negative = (bits & (1L << 63)) != 0;
        int exponent = (int) ((bits >> 52) & 0x7FFL) - 1023;
        long mantissa = bits & 0x000FFFFFFFFFFFFFL;
        
        // Add implicit leading 1 unless denormalized
        if (exponent == -1023) {
            exponent = -1022; // denormal
        } else {
            mantissa |= 0x0010000000000000L;
        }
        
        // Mantissa is now an integer M. Value was (1.F) * 2^E.
        // M = (1.F) * 2^52.
        // So Value = M * 2^(E - 52).
        exponent -= 52;

        // Remove trailing zeros from mantissa to be minimal
        while (mantissa != 0 && (mantissa & 1) == 0) {
            mantissa >>= 1;
            exponent++;
        }

        byte firstByte = (byte) (0x80 | (negative ? 0x40 : 0x00));
        
        // Exponent length selection
        // We support 1 or 2 bytes for exponent (sufficient for double)
        byte[] expBytes;
        if (exponent >= -128 && exponent <= 127) {
            expBytes = new byte[]{(byte) exponent};
            firstByte |= 0x00; // expLen = 1
        } else {
            expBytes = new byte[]{(byte) (exponent >> 8), (byte) exponent};
            firstByte |= 0x01; // expLen = 2
        }

        byte[] mantissaBytes = longToBytes(mantissa);
        
        byte[] result = new byte[1 + expBytes.length + mantissaBytes.length];
        result[0] = firstByte;
        System.arraycopy(expBytes, 0, result, 1, expBytes.length);
        System.arraycopy(mantissaBytes, 0, result, 1 + expBytes.length, mantissaBytes.length);
        
        writer.writePrimitive(ASN1Identifier.REAL, result);
    }

    private byte[] longToBytes(long value) {
        int len = (64 - Long.numberOfLeadingZeros(value) + 7) / 8;
        if (len == 0) return new byte[]{0};
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[len - 1 - i] = (byte) (value >> (i * 8));
        }
        return bytes;
    }

    @Override
    public String toString() {
        return "ASN1Real(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASN1Real asn1Real = (ASN1Real) o;
        return Double.compare(asn1Real.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
