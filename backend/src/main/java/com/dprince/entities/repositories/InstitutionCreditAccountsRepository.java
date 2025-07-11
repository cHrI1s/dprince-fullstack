package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionCreditAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionCreditAccountsRepository extends CrudRepository<InstitutionCreditAccount, Long> {
    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
    InstitutionCreditAccount findByNameAndInstitutionId(String name, Long institutionId);
    List<InstitutionCreditAccount> findAllByInstitutionId(Long institutionId);
    List<InstitutionCreditAccount> findAllByInstitutionIdIn(List<Long> institutionsIds);

    Optional<InstitutionCreditAccount> findByInstitutionIdInAndId(List<Long> institutionsIds, Long accountId);
}
