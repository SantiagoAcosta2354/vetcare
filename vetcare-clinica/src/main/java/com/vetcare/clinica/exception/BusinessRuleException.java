package com.vetcare.clinica.exception;

/** Una regla de negocio impide la operacion. Se traduce a HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
