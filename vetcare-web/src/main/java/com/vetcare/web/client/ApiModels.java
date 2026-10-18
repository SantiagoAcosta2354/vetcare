package com.vetcare.web.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Modelos que devuelve la API. Se declaran como records anidados para no repetir
 * un archivo por clase; el servicio es la fuente de verdad y aqui solo se toma
 * lo que la vista necesita.
 */
public final class ApiModels {

    private ApiModels() {
    }

    public record User(String id, String fullName, String email, String phone, List<String> roles,
            List<String> roleLabels, boolean active) {
    }

    public record LoginResponse(String accessToken, String tokenType, long expiresIn, User user) {
    }

    public record Owner(String id, String documentType, String document, String fullName, String phone, String email,
            String address, String userId, boolean active) {
    }

    public record Pet(String id, String ownerId, String ownerName, String name, String species, String breed,
            String sex, LocalDate birthDate, Double weightKg, String microchip, String status, String notes) {
    }

    public record Clinic(String id, String name, String address, String phone, LocalTime openingTime,
            LocalTime closingTime, int slotMinutes) {
    }

    public record Appointment(String id, String clinicId, String clinicName, String petId, String petName,
            String petSpecies, String ownerId, String ownerName, String professionalId, String professionalName,
            LocalDate date, LocalTime startTime, LocalTime endTime, String reason, String status, String statusLabel,
            String notes) {
    }

    public record Slot(LocalTime start, LocalTime end, boolean available) {
    }

    public record Availability(LocalDate date, String professionalId, List<Slot> slots) {
    }

    public record Record(String id, String appointmentId, String petId, String petName, String petSpecies,
            String ownerId, String ownerName, String professionalId, String professionalName, String reason,
            String status, String statusLabel, Double weightKg, Double temperatureC, Integer heartRate,
            Integer respiratoryRate, String diagnosis, String treatmentDescription, Integer treatmentDurationDays,
            String treatmentIndications, String notes, String closureNote, Instant openedAt, Instant closedAt) {
    }

    public record PrescriptionItem(String medicationId, String medicationName, String dose, String frequency,
            Integer durationDays, int quantity) {
    }

    public record Prescription(String id, String recordId, String appointmentId, String petId, String petName,
            String ownerId, List<PrescriptionItem> items, String status, String statusLabel, String observations,
            Instant issuedAt) {
    }

    public record LabOrder(String id, String recordId, String appointmentId, String petId, String petName,
            String examName, String notes, String status, String statusLabel, Instant orderedAt,
            String resultValues, String resultObservations, Instant resultAt) {
    }

    public record Medication(String id, String name, String activeIngredient, String presentation, String unit,
            int stockMin, int available, boolean lowStock, boolean active) {
    }

    public record Alert(String type, String medicationId, String medicationName, String message, int quantity,
            LocalDate expiresAt) {
    }

    public record PrescriptionLine(String medicationId, int quantity) {
    }

    public record BatchAllocation(String batchId, String batchNumber, LocalDate expiresAt, int quantity) {
    }

    public record DispensingItem(String medicationId, String medicationName, int quantity,
            List<BatchAllocation> allocations) {
    }

    public record Dispensing(String id, String prescriptionId, String petId, String petName, String status,
            String statusLabel, List<DispensingItem> items, Instant dispensedAt, String notes) {
    }

    public record InvoiceItem(String code, String description, String type, String referenceId, int quantity,
            BigDecimal unitPrice, BigDecimal taxRate, BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public record Invoice(String id, String number, String ownerId, String ownerName, String petId, String petName,
            String appointmentId, LocalDate consultationDate, List<InvoiceItem> items, BigDecimal subtotal,
            BigDecimal taxTotal, BigDecimal total, BigDecimal paid, BigDecimal balance, String status,
            String statusLabel, Instant issuedAt, String cancelReason) {
    }

    public record Payment(String id, String invoiceId, BigDecimal amount, String method, String reference,
            Instant paidAt, String status) {
    }

    public record Portfolio(long invoices, BigDecimal billed, BigDecimal collected, BigDecimal pending) {
    }

    public record Vaccination(String id, String recordId, String petId, String petName, String vaccineId,
            String vaccineName, String batch, LocalDate appliedAt, LocalDate nextDoseAt) {
    }

    public record Vaccine(String id, String name, String species, int nextDoseDays, boolean active) {
    }

    public record AppointmentSummary(LocalDate date, long total, long attended, long pending, long cancelled) {
    }

    public record Rate(String id, String code, String name, String type, BigDecimal price, BigDecimal taxRate,
            boolean active) {
    }
}
