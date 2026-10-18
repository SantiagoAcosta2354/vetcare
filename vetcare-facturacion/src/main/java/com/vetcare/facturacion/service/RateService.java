package com.vetcare.facturacion.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.facturacion.domain.ItemType;
import com.vetcare.facturacion.domain.Rate;
import com.vetcare.facturacion.exception.BusinessRuleException;
import com.vetcare.facturacion.exception.ResourceNotFoundException;
import com.vetcare.facturacion.repository.RateRepository;
import com.vetcare.facturacion.web.dto.RateRequest;
import com.vetcare.facturacion.web.dto.RateResponse;

@Service
public class RateService {

    private final RateRepository rateRepository;

    public RateService(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    public List<RateResponse> findAll() {
        return rateRepository.findByActiveTrueOrderByNameAsc().stream().map(RateResponse::from).toList();
    }

    public RateResponse create(RateRequest request) {
        rateRepository.findByCode(request.code().trim()).ifPresent(existing -> {
            throw new BusinessRuleException("Ya existe una tarifa con el codigo " + existing.getCode());
        });
        Rate rate = new Rate();
        apply(rate, request);
        return RateResponse.from(rateRepository.save(rate));
    }

    public RateResponse update(String id, RateRequest request) {
        Rate rate = requireRate(id);
        apply(rate, request);
        return RateResponse.from(rateRepository.save(rate));
    }

    public RateResponse changeStatus(String id, boolean active) {
        Rate rate = requireRate(id);
        rate.setActive(active);
        return RateResponse.from(rateRepository.save(rate));
    }

    public Rate requireRate(String id) {
        return rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una tarifa con el id " + id));
    }

    private void apply(Rate rate, RateRequest request) {
        rate.setCode(request.code().trim());
        rate.setName(request.name().trim());
        rate.setType(request.type());
        rate.setPrice(request.price());
        // El IVA solo aplica a productos: un servicio con IVA seria un cobro indebido.
        rate.setTaxRate(request.type() == ItemType.PRODUCTO
                ? (request.taxRate() == null ? new BigDecimal("0.19") : request.taxRate())
                : BigDecimal.ZERO);
    }
}
