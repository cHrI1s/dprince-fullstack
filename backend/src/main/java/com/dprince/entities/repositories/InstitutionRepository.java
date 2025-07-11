package com.dprince.entities.repositories;

import com.dprince.entities.Institution;
import com.dprince.entities.enums.InstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByEmail(String email);
    List<Institution> findAllByParentInstitutionId(Long parentId);

    Optional<Institution> findByBaseCode(String baseCode);
    Optional<Institution> findByPhone(Long phone);

    long countAllByCategoryId(Long categoryId);

    List<Institution> findAllByInstitutionType(InstitutionType institutionType);

    @Query("SELECT  COUNT(*) from Institution  i WHERE i.blocked=true")

    Institution findInstitutionBySubscriptionPlan(Long subscriptionPlan);
    Optional<Institution> findByParentInstitutionIdAndId(Long parentInstitutionId, Long branchId);


    long countAllByCategoryIdIs(Long categoryId);

    Institution findByLogo(String logo);

    Institution findByDeletionToken(String deletionToken);
}
