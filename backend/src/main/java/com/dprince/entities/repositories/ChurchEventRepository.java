package com.dprince.entities.repositories;

import java.util.List;
import com.dprince.entities.ChurchEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ChurchEventRepository extends JpaRepository<ChurchEvent, Long> {
    Page<ChurchEvent> findAllByInstitutionId(@NonNull Long institutionId,
                                        @NonNull PageRequest pageRequest);

    Page<ChurchEvent> findAllByInstitutionIdIn(@NonNull List<Long> institutionId,
                                        @NonNull PageRequest pageRequest);


    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}