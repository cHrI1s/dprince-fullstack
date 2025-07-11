package com.dprince.entities.repositories;


import com.dprince.entities.Template;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.enums.TemplateStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template , Long> {
    List<Template> findAllByInstitutionId(Long institutionId);

    Template findFirstByInstitutionIdAndTemplateStyleOrderByIdDesc(@NonNull Long institutionId,
                                @NonNull TemplateStyle style);

    List<Template> findAllByInstitutionIdIn(List<Long> institutionIds);


    @Query("SELECT t FROM Template t WHERE t.institutionId=:institutionId AND CONCAT(',', t.communicationWays, ',') LIKE CONCAT(',', CONVERT(VARCHAR, :templateStyle), ',')")
    Optional<Template> findUniqueTemplate(Long institutionId, TemplateStyle templateStyle);
}
