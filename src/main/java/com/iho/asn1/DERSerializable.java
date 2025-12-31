package com.iho.asn1;

public interface DERSerializable {
    void serialize(DERWriter writer) throws ASN1Exception;
}
