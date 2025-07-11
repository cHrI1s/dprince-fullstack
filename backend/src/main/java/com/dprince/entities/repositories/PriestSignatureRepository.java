package com.dprince.entities.repositories;

import com.dprince.entities.PriestSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PriestSignatureRepository extends JpaRepository<PriestSignature, Long> {
    List<PriestSignature> findAllByInstitutionId(Long institutionId);
    List<PriestSignature> findAllByMemberIdIn(List<Long> memberIds);
    PriestSignature findByInstitutionIdAndMemberId(Long institutionId, Long priestId);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}
