package com.iho.asn1;

public class ASN1Exception extends Exception {
    private final ErrorCode code;

    public ASN1Exception(ErrorCode code, String reason) {
        super(String.format("ASN1Error.%s: %s", code, reason));
        this.code = code;
    }

    public ASN1Exception(ErrorCode code, String reason, String file, int line) {
        super(String.format("ASN1Error.%s: %s %s:%d", code, reason, file, line));
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
