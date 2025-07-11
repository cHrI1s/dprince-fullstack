package com.dprince.entities.repositories;

import com.dprince.entities.PinCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PincodeRepository extends JpaRepository<PinCode, Long> {
    Optional<PinCode> findByCode(Integer code);
}
