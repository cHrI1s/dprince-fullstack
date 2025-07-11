package com.dprince.entities.repositories;

import com.dprince.entities.CronJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CronJobRepository extends JpaRepository<CronJob, Long> {
    CronJob findByInstitutionId(Long institutionId);
}