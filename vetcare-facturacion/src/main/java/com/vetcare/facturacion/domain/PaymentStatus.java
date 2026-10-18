package com.vetcare.facturacion.domain;

/** Un pago no se edita: se anula con motivo y el saldo se recalcula. */
public enum PaymentStatus {
    APLICADO,
    ANULADO
}
