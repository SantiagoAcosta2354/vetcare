package com.vetcare.facturacion.domain;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Tarifa administrable. Los precios no se escriben en el codigo: el
 * administrador los ajusta y la factura toma el valor vigente al emitir.
 */
@Document(collection = "rates")
public class Rate {

    @Id
    private String id;

    private String code;
    private String name;
    private ItemType type = ItemType.SERVICIO;
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
