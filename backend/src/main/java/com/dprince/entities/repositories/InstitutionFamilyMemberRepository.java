
package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionFamilyMember;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface InstitutionFamilyMemberRepository extends CrudRepository<InstitutionFamilyMember, Long> {
	
	
	
	
    @Transactional
    void deleteAllByFamilyId(@NonNull Long familyId);

    @Transactional
    @Modifying
    @Query("DELETE FROM InstitutionFamilyMember ifm " +
            "WHERE ifm.institutionId=:institutionId " +
            "AND ifm.familyId=:familyId "+
            "AND ifm.memberId NOT IN(:membersIds) ")
    void deleteNonExistingMembers(@NonNull Long institutionId,
                                  @NonNull Long familyId,
                                  @NonNull List<Long> membersIds);

    List<InstitutionFamilyMember> findAllByFamilyId(@NonNull Long familyId);
    List<InstitutionFamilyMember> findAllByFamilyIdIn(@NonNull Set<Long> familyId);

    @Query("SELECT DISTINCT ifm FROM InstitutionFamilyMember ifm " +
            "WHERE ifm.memberId IN :membersIds")
    List<InstitutionFamilyMember> getAllFamiliesOfMembers(Set<Long> membersIds);

    InstitutionFamilyMember findByMemberIdAndFamilyId(Long memberId, Long familyId);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);


    @Query("SELECT COUNT(*) FROM InstitutionFamilyMember ifm WHERE ifm.institutionId=:institutionId " +
            "AND ifm.familyId=:familyId AND ifm.memberId=:memberId")
    Long countSimilarMember(long institutionId, long familyId, long memberId);
}
