package com.vetcare.facturacion.domain;

/** El IVA se aplica solo a productos; los servicios de la clinica no lo llevan. */
public enum ItemType {
    SERVICIO,
    PRODUCTO
}
