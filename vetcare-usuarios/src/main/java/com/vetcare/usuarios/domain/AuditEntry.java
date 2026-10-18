package com.vetcare.usuarios.domain;

import java.time.Instant;

/**
 * Registro de auditoria de una cuenta.
 *
 * Se guarda en el propio documento del usuario y no en una coleccion aparte
 * porque la pregunta que responde es siempre la misma: quien cambio esta cuenta
 * y cuando. El autor no se puede deducir del token actual (pudo ser otro
 * administrador), por eso se persiste.
 */
public class AuditEntry {

    private String actor;
    private String action;
    private Instant occurredAt;

    public AuditEntry() {
    }

    public AuditEntry(String actor, String action, Instant occurredAt) {
        this.actor = actor;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    public static AuditEntry of(String actor, String action) {
        return new AuditEntry(actor, action, Instant.now());
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
