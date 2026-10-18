package com.vetcare.farmacia.seed;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.Medication;
import com.vetcare.farmacia.repository.BatchRepository;
import com.vetcare.farmacia.repository.MedicationRepository;

/** Catalogo minimo con existencia para poder formular y despachar en la demostracion. */
@Configuration
@ConditionalOnProperty(name = "vetcare.seed.enabled", havingValue = "true", matchIfMissing = true)
public class InventorySeeder {

    private static final Logger log = LoggerFactory.getLogger(InventorySeeder.class);

    private final MedicationRepository medicationRepository;
    private final BatchRepository batchRepository;

    public InventorySeeder(MedicationRepository medicationRepository, BatchRepository batchRepository) {
        this.medicationRepository = medicationRepository;
        this.batchRepository = batchRepository;
    }

    @Bean
    @Order(3)
    ApplicationRunner seedInventory() {
        return args -> {
            seed("Amoxicilina 250 mg", "Amoxicilina", "Tableta", "unidad", 10, "L-2026-01", 60);
            seed("Meloxicam 2 mg/ml", "Meloxicam", "Frasco 10 ml", "ml", 5, "L-2026-02", 30);
            seed("Albendazol 300 mg", "Albendazol", "Tableta", "unidad", 5, "L-2026-03", 40);
            seed("Suero oral veterinario", "Electrolitos", "Sobre", "sobre", 8, "L-2026-04", 25);
            log.info("Inventario de demostracion verificado");
        };
    }

    private void seed(String name, String ingredient, String presentation, String unit, int stockMin,
            String batchNumber, int quantity) {
        if (medicationRepository.findByNameIgnoreCase(name).stream().findFirst().isPresent()) {
            return;
        }
        Medication medication = new Medication();
        medication.setName(name);
        medication.setActiveIngredient(ingredient);
        medication.setPresentation(presentation);
        medication.setUnit(unit);
        medication.setStockMin(stockMin);
        medication.setActive(true);
        medication.setCreatedBy("sistema");
        Medication saved = medicationRepository.save(medication);

        Batch batch = new Batch();
        batch.setMedicationId(saved.getId());
        batch.setMedicationName(saved.getName());
        batch.setBatchNumber(batchNumber);
        batch.setExpiresAt(LocalDate.now().plusMonths(8));
        batch.setQuantity(quantity);
        batch.setSupplier("Distribuidora Veterinaria");
        batch.setRegisteredBy("sistema");
        batchRepository.save(batch);
    }
}
