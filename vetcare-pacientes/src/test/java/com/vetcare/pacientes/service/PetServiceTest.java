package com.vetcare.pacientes.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.Species;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.repository.PetRepository;
import com.vetcare.pacientes.web.dto.PetRequest;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private OwnerService ownerService;

    private PetService petService;

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, ownerService);
    }

    private Owner owner(boolean active) {
        Owner owner = new Owner();
        owner.setId("o1");
        owner.setFullName("Ana Perez");
        owner.setActive(active);
        return owner;
    }

    private PetRequest request(String microchip) {
        return new PetRequest("o1", "Luna", Species.CANINO, "Criolla", "HEMBRA", null, 8.5, microchip, null);
    }

    @Test
    @DisplayName("El microchip no puede repetirse entre pacientes")
    void microchipDuplicadoSeRechaza() {
        when(ownerService.requireOwner("o1")).thenReturn(owner(true));
        Pet other = new Pet();
        other.setId("p9");
        when(petRepository.findByMicrochip("900123456789012")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> petService.create(request("900123456789012"), "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("microchip");

        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    @DisplayName("Un dueno inactivo no puede registrar pacientes")
    void duenoInactivoNoRegistra() {
        when(ownerService.requireOwner("o1")).thenReturn(owner(false));

        assertThatThrownBy(() -> petService.create(request(null), "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactivo");
    }
}
