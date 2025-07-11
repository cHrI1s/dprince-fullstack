package com.dprince.entities.repositories;

import com.dprince.entities.GuestPriest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestPriestsRepository extends JpaRepository<GuestPriest, Long> {
    List<GuestPriest> findAllByInstitutionId(Long institutionId);
}
