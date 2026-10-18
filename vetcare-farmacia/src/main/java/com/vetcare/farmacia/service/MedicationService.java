package com.vetcare.farmacia.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.Medication;
import com.vetcare.farmacia.exception.BusinessRuleException;
import com.vetcare.farmacia.exception.ResourceNotFoundException;
import com.vetcare.farmacia.repository.BatchRepository;
import com.vetcare.farmacia.repository.MedicationRepository;
import com.vetcare.farmacia.web.dto.AlertResponse;
import com.vetcare.farmacia.web.dto.BatchResponse;
import com.vetcare.farmacia.web.dto.MedicationRequest;
import com.vetcare.farmacia.web.dto.MedicationResponse;
import com.vetcare.farmacia.web.dto.PurchaseRequest;

/**
 * Catalogo e inventario.
 *
 * La existencia de un medicamento es la suma de sus lotes vigentes. Por eso el
 * stock no se edita a mano: entra por compras (con lote y vencimiento) y sale por
 * despachos.
 */
@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final BatchRepository batchRepository;

    @Value("${vetcare.inventory.expiry-alert-days:30}")
    private int alertDays;

    public MedicationService(MedicationRepository medicationRepository, BatchRepository batchRepository) {
        this.medicationRepository = medicationRepository;
        this.batchRepository = batchRepository;
    }

    public List<MedicationResponse> findAll(String query) {
        List<Medication> found = (query == null || query.isBlank())
                ? medicationRepository.findByActiveTrueOrderByNameAsc()
                : medicationRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        return found.stream().map(medication -> MedicationResponse.from(medication, available(medication.getId()))).toList();
    }

    public MedicationResponse findById(String id) {
        Medication medication = requireMedication(id);
        return MedicationResponse.from(medication, available(id));
    }

    public MedicationResponse create(MedicationRequest request, String actor) {
        medicationRepository.findByNameIgnoreCase(request.name().trim()).stream().findFirst().ifPresent(existing -> {
            throw new BusinessRuleException("Ya existe un medicamento con el nombre " + existing.getName());
        });
        Medication medication = new Medication();
        apply(medication, request);
        medication.setCreatedBy(actor);
        medication.setActive(true);
        return MedicationResponse.from(medicationRepository.save(medication), 0);
    }

    public MedicationResponse update(String id, MedicationRequest request) {
        Medication medication = requireMedication(id);
        apply(medication, request);
        return MedicationResponse.from(medicationRepository.save(medication), available(id));
    }

    public MedicationResponse changeStatus(String id, boolean active) {
        Medication medication = requireMedication(id);
        medication.setActive(active);
        return MedicationResponse.from(medicationRepository.save(medication), available(id));
    }

    /** Existencia real: solo cuentan los lotes que no estan vencidos. */
    public int available(String medicationId) {
        return batchRepository
                .findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                        medicationId, LocalDate.now(), 0)
                .stream().mapToInt(Batch::getQuantity).sum();
    }

    public List<BatchResponse> batchesOf(String medicationId) {
        LocalDate today = LocalDate.now();
        return batchRepository.findByMedicationIdOrderByExpiresAtAsc(medicationId).stream()
                .map(batch -> BatchResponse.from(batch, today, alertDays))
                .toList();
    }

    /** Entrada de mercancia. Exige lote y vencimiento futuro. */
    public BatchResponse receive(PurchaseRequest request, String actor) {
        Medication medication = requireMedication(request.medicationId());
        String batchNumber = request.batchNumber().trim();

        Batch batch = batchRepository.findByMedicationIdAndBatchNumber(medication.getId(), batchNumber)
                .orElseGet(() -> {
                    Batch created = new Batch();
                    created.setMedicationId(medication.getId());
                    created.setMedicationName(medication.getName());
                    created.setBatchNumber(batchNumber);
                    created.setExpiresAt(request.expiresAt());
                    created.setSupplier(request.supplier());
                    created.setRegisteredBy(actor);
                    return created;
                });

        if (!batch.getExpiresAt().equals(request.expiresAt())) {
            throw new BusinessRuleException("El lote " + batchNumber + " ya existe con vencimiento "
                    + batch.getExpiresAt() + "; no puede recibirse con otra fecha");
        }

        batch.setQuantity(batch.getQuantity() + request.quantity());
        return BatchResponse.from(batchRepository.save(batch), LocalDate.now(), alertDays);
    }

    public List<AlertResponse> alerts() {
        LocalDate today = LocalDate.now();
        List<AlertResponse> alerts = new ArrayList<>();

        for (Medication medication : medicationRepository.findByActiveTrueOrderByNameAsc()) {
            int available = available(medication.getId());
            if (available < medication.getStockMin()) {
                alerts.add(new AlertResponse("STOCK_BAJO", medication.getId(), medication.getName(),
                        "Existencia " + available + " por debajo del minimo " + medication.getStockMin(),
                        available, null));
            }
        }

        for (Batch batch : batchRepository.findByQuantityGreaterThanAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
                0, today.plusDays(alertDays))) {
            boolean expired = batch.getExpiresAt().isBefore(today);
            alerts.add(new AlertResponse(expired ? "VENCIDO" : "POR_VENCER", batch.getMedicationId(),
                    batch.getMedicationName(),
                    (expired ? "Lote vencido: " : "Lote proximo a vencer: ") + batch.getBatchNumber(),
                    batch.getQuantity(), batch.getExpiresAt()));
        }
        return alerts;
    }

    public Medication requireMedication(String id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un medicamento con el id " + id));
    }

    private void apply(Medication medication, MedicationRequest request) {
        medication.setName(request.name().trim());
        medication.setActiveIngredient(request.activeIngredient());
        medication.setPresentation(request.presentation());
        medication.setUnit(request.unit());
        medication.setStockMin(request.stockMin() == null ? 0 : request.stockMin());
    }
}
