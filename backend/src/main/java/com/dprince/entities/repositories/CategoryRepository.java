package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<InstitutionCategory, Long> {
    List<InstitutionCategory> findAllByNameInAndInstitutionId(List<String> names, Long institutionId);
    List<InstitutionCategory> findAllByInstitutionId(@Nullable Long institutionId);

    List<InstitutionCategory> findAllByIdIn(List<Long> categoriesIds);

    InstitutionCategory findByNameAndInstitutionId(String name, Long institutionId);
}
