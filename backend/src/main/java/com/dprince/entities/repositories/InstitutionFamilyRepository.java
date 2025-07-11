package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionFamily;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface InstitutionFamilyRepository extends CrudRepository<InstitutionFamily, Long> {
    @Query("SELECT COUNT(DISTINCT inf.id) FROM InstitutionFamily inf " +
            " INNER JOIN InstitutionFamilyMember ifm" +
            " ON inf.id=ifm.familyId" +
            " WHERE inf.institutionId=:id")
    long countAllByInstitutionId(@NonNull Long id);

    @Query("SELECT COUNT(DISTINCT inf.id) FROM InstitutionFamily inf " +
            " INNER JOIN InstitutionFamilyMember ifm" +
            " ON inf.id=ifm.familyId")
    Long countFamilies();

    Page<InstitutionFamily> findAllByInstitutionId(@NonNull Long institutionId,
                                                   @NonNull PageRequest pageRequest);


    InstitutionFamily findByFamilyHead(@NonNull Long memberId);

    InstitutionFamily findInstitutionFamilyByInstitutionId(Long institutionId);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);


    /*
    @Query("SELECT COUNT(*) FROM InstitutionFamily f" +
            " WHERE (CONVERT(DATE, FORMAT(f.dob, 'MMdd'))>=CONVERT(DATE, FORMAT(:from, 'MMdd')) " +
            " AND CONVERT(DATE, FORMAT(f.dob, 'MMdd'))<=CONVERT(DATE, FORMAT(:end, 'MMdd')))" +
            " AND f.institutionId=:institutionId")
    Long getDomsBetween(@NonNull Date from,
                        @NonNull Date end,
                        @NonNull Long institutionId);
     */

    @Query("SELECT COUNT(*) FROM InstitutionMember im" +
            " WHERE (CONVERT(DATE, FORMAT(im.dom, 'MMdd'))>=CONVERT(DATE, FORMAT(:from, 'MMdd')) " +
            " AND CONVERT(DATE, FORMAT(im.dom, 'MMdd'))<=CONVERT(DATE, FORMAT(:end, 'MMdd')))" +
            " AND im.institutionId=:institutionId")
    Long getDomsBetween(@NonNull Date from,
                        @NonNull Date end,
                        @NonNull Long institutionId);
}
