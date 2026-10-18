package com.vetcare.usuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrueOrderByFullNameAsc();

    List<User> findByRolesContaining(Role role);

    long countByRolesContainingAndActiveTrue(Role role);
}
