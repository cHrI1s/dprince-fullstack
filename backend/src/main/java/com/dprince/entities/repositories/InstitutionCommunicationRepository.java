package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionCommunication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InstitutionCommunicationRepository extends CrudRepository<InstitutionCommunication, Long> {

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}
