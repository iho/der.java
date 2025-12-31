package com.iho.asn1;

public interface DERParseable<T> {
    T fromDERNode(ASN1Node node) throws ASN1Exception;
}
