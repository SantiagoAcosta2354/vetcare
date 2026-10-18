package com.vetcare.facturacion.domain;

/**
 * Una factura solo pasa a PAGADA cuando el saldo llega a cero, y una pagada no
 * se edita: se anula con motivo.
 */
public enum InvoiceStatus {
    EMITIDA("Emitida"),
    PAGADA("Pagada"),
    ANULADA("Anulada");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
