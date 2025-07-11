package com.dprince.entities.repositories;

import com.dprince.entities.SubscriptionPlan;
import com.dprince.entities.enums.InstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllByInstitutionType(InstitutionType institutionType);
}
