package com.dprince.entities.repositories;

import com.dprince.entities.TopUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopUpRepository extends JpaRepository<TopUp, Long> {
    TopUp findByInstitutionId(Long institutionId);
}
