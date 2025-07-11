package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupRepository  extends JpaRepository<InstitutionGroup, Long> {
    List<InstitutionGroup> findAllByInstitutionId(Long institutionId);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}
