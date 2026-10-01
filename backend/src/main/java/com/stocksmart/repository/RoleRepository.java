package com.stocksmart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocksmart.entity.Role;
import com.stocksmart.entity.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
