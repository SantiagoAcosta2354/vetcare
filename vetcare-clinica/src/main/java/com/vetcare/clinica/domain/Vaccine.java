package com.vetcare.clinica.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Vacuna del catalogo. `nextDoseDays` es el esquema: a los cuantos dias
 * corresponde el refuerzo para esa especie.
 */
@Document(collection = "vaccines")
public class Vaccine {

    @Id
    private String id;

    private String name;
    private Species species = Species.AMBAS;
    private int nextDoseDays;
    private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public int getNextDoseDays() {
        return nextDoseDays;
    }

    public void setNextDoseDays(int nextDoseDays) {
        this.nextDoseDays = nextDoseDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
