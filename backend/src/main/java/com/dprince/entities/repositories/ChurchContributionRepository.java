package com.dprince.entities.repositories;

import com.dprince.entities.ChurchContribution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChurchContributionRepository extends CrudRepository<ChurchContribution, Long> {
    List<ChurchContribution> findAllByInstitutionId(Long institutionId);
    @Transactional
    void deleteAllByInstitutionId(Long institutionId);

    List<ChurchContribution> findAllByIdIn(List<Long> contributionIds);

    ChurchContribution findByNameAndInstitutionId(String name, Long institutionId);
}
