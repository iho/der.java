package com.iho.asn1;

public enum ErrorCode {
    UnexpectedFieldType,
    InvalidASN1Object,
    InvalidASN1IntegerEncoding,
    TruncatedASN1Field,
    UnsupportedFieldLength,
    InvalidPEMDocument,
    InvalidStringRepresentation,
    TooFewOIDComponents,
    ValueOutOfRange,
    UnknownError
}
