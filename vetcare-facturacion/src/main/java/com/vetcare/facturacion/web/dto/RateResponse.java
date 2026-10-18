package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;

import com.vetcare.facturacion.domain.ItemType;
import com.vetcare.facturacion.domain.Rate;

public record RateResponse(String id, String code, String name, ItemType type, BigDecimal price,
        BigDecimal taxRate, boolean active) {

    public static RateResponse from(Rate rate) {
        return new RateResponse(rate.getId(), rate.getCode(), rate.getName(), rate.getType(), rate.getPrice(),
                rate.getTaxRate(), rate.isActive());
    }
}
