package com.vetcare.farmacia.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vetcare.farmacia.client.ClinicaClient;
import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.BatchAllocation;
import com.vetcare.farmacia.domain.Dispensing;
import com.vetcare.farmacia.domain.DispensingItem;
import com.vetcare.farmacia.domain.DispensingStatus;
import com.vetcare.farmacia.exception.BusinessRuleException;
import com.vetcare.farmacia.exception.ResourceNotFoundException;
import com.vetcare.farmacia.repository.BatchRepository;
import com.vetcare.farmacia.repository.DispensingRepository;
import com.vetcare.farmacia.web.dto.DispensingRequest;
import com.vetcare.farmacia.web.dto.DispensingResponse;

/**
 * Despacho de formulas.
 *
 * Reglas:
 *   1. El descuento ocurre al despachar, nunca al formular.
 *   2. Un lote vencido no se entrega aunque tenga existencia; se busca el
 *      siguiente lote vigente (primero el que vence antes).
 *   3. Si falta existencia de cualquier item, el despacho queda
 *      PENDIENTE_POR_STOCK y no se descuenta nada: un despacho parcial haria que
 *      la factura cobrara producto que el paciente no recibio.
 */
@Service
public class DispensingService {

    private static final Logger log = LoggerFactory.getLogger(DispensingService.class);

    private final DispensingRepository dispensingRepository;
    private final BatchRepository batchRepository;
    private final MedicationService medicationService;
    private final ClinicaClient clinicaClient;

    public DispensingService(DispensingRepository dispensingRepository, BatchRepository batchRepository,
            MedicationService medicationService, ClinicaClient clinicaClient) {
        this.dispensingRepository = dispensingRepository;
        this.batchRepository = batchRepository;
        this.medicationService = medicationService;
        this.clinicaClient = clinicaClient;
    }

    public DispensingResponse dispense(DispensingRequest request, String actor) {
        dispensingRepository.findByPrescriptionId(request.prescriptionId()).ifPresent(existing -> {
            throw new BusinessRuleException("La formula ya tiene un despacho registrado ("
                    + existing.getStatus().getLabel() + ")");
        });

        Dispensing dispensing = new Dispensing();
        dispensing.setPrescriptionId(request.prescriptionId());
        dispensing.setRecordId(request.recordId());
        dispensing.setPetId(request.petId());
        dispensing.setPetName(request.petName());
        dispensing.setOwnerId(request.ownerId());
        dispensing.setNotes(request.notes());
        dispensing.setItems(buildItems(request));
        dispensing.setDispensedBy(actor);

        boolean complete = allocate(dispensing);
        dispensing.setStatus(complete ? DispensingStatus.DESPACHADA : DispensingStatus.PENDIENTE_POR_STOCK);
        if (complete) {
            dispensing.setDispensedAt(Instant.now());
        }
        Dispensing saved = dispensingRepository.save(dispensing);
        clinicaClient.notifyPrescription(saved.getPrescriptionId(), saved.getStatus());
        return DispensingResponse.from(saved);
    }

    /** Reintento cuando la formula quedo pendiente y ya llego la mercancia. */
    public DispensingResponse retry(String id, String actor) {
        Dispensing dispensing = requireDispensing(id);
        if (dispensing.getStatus() != DispensingStatus.PENDIENTE_POR_STOCK) {
            throw new BusinessRuleException("Solo se reintenta un despacho pendiente por stock");
        }
        boolean complete = allocate(dispensing);
        dispensing.setDispensedBy(actor);
        if (complete) {
            dispensing.setStatus(DispensingStatus.DESPACHADA);
            dispensing.setDispensedAt(Instant.now());
        }
        Dispensing saved = dispensingRepository.save(dispensing);
        clinicaClient.notifyPrescription(saved.getPrescriptionId(), saved.getStatus());
        return DispensingResponse.from(saved);
    }

    public DispensingResponse findById(String id) {
        return DispensingResponse.from(requireDispensing(id));
    }

    public List<DispensingResponse> findByStatus(DispensingStatus status) {
        return dispensingRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(DispensingResponse::from).toList();
    }

    public List<DispensingResponse> findByPet(String petId) {
        return dispensingRepository.findByPetIdOrderByCreatedAtDesc(petId).stream()
                .map(DispensingResponse::from).toList();
    }

    public List<DispensingResponse> findAll() {
        return dispensingRepository.findAll().stream().map(DispensingResponse::from).toList();
    }

    // --------------------------------------------------------------- apoyo

    private List<DispensingItem> buildItems(DispensingRequest request) {
        List<DispensingItem> items = new ArrayList<>();
        for (DispensingRequest.Item requested : request.items()) {
            var medication = medicationService.requireMedication(requested.medicationId());
            if (!medication.isActive()) {
                throw new BusinessRuleException("El medicamento " + medication.getName() + " esta inactivo");
            }
            items.add(new DispensingItem(medication.getId(), medication.getName(), requested.quantity()));
        }
        return items;
    }

    /**
     * Asigna lotes vigentes a cada item. Si falta existencia, deja las
     * asignaciones vacias y devuelve false sin tocar el inventario.
     */
    private boolean allocate(Dispensing dispensing) {
        LocalDate today = LocalDate.now();
        boolean complete = true;

        for (DispensingItem item : dispensing.getItems()) {
            List<BatchAllocation> allocations = new ArrayList<>();
            int pending = item.getQuantity();

            List<Batch> validBatches = batchRepository
                    .findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                            item.getMedicationId(), today, 0);

            for (Batch batch : validBatches) {
                if (pending == 0) {
                    break;
                }
                int taken = Math.min(pending, batch.getQuantity());
                if (taken > 0) {
                    allocations.add(new BatchAllocation(batch.getId(), batch.getBatchNumber(),
                            batch.getExpiresAt(), taken));
                    pending -= taken;
                }
            }

            if (pending > 0) {
                item.setAllocations(List.of());
                complete = false;
                log.info("Sin existencia suficiente de {}: faltan {} unidades", item.getMedicationName(), pending);
            } else {
                item.setAllocations(allocations);
            }
        }

        if (!complete) {
            return false;
        }

        // Solo se descuenta cuando todos los items quedaron cubiertos.
        for (DispensingItem item : dispensing.getItems()) {
            for (BatchAllocation allocation : item.getAllocations()) {
                Batch batch = batchRepository.findById(allocation.getBatchId()).orElseThrow();
                batch.setQuantity(batch.getQuantity() - allocation.getQuantity());
                batchRepository.save(batch);
            }
        }
        return true;
    }

    private Dispensing requireDispensing(String id) {
        return dispensingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un despacho con el id " + id));
    }
}
