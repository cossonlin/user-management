package com.cosson.usermanagement.repo;

import com.cosson.usermanagement.entity.Role;
import com.cosson.usermanagement.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}
