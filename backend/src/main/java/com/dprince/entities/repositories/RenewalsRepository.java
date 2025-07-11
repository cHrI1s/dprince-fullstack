package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionSubscriptionRenewal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RenewalsRepository extends JpaRepository<InstitutionSubscriptionRenewal, Long> {
}
