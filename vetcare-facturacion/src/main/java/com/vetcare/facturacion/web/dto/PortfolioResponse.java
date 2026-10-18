package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;

/** Cartera: lo facturado, lo cobrado y lo que sigue pendiente. */
public record PortfolioResponse(long invoices, BigDecimal billed, BigDecimal collected, BigDecimal pending) {
}
