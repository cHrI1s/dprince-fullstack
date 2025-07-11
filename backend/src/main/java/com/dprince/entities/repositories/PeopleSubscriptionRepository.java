package com.dprince.entities.repositories;

import com.dprince.entities.PeopleSubscription;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeopleSubscriptionRepository extends JpaRepository<PeopleSubscription , Long> {
    @Override
    @Transactional
    void deleteAll();

    Optional<PeopleSubscription> findFirstByMemberIdAndCategoryIdOrderByIdDesc(Long memberId, Long categoryId);
    Optional<PeopleSubscription> findFirstByMemberIdAndContributionIdOrderByIdDesc(Long memberId, Long contributionId);

    @Query(value = "SELECT p FROM PeopleSubscription p " +
            " WHERE p.memberId=:memberId" +
            " AND p.institutionId=:institutionId" +
            " AND p.categoryId=:categoryId")
    List<PeopleSubscription> getLastDonation(Long memberId,
                                             Long institutionId,
                                             Long categoryId,
                                             PageRequest pageRequest);

    @Query(value = "SELECT p FROM PeopleSubscription p WHERE p.memberId=:memberId AND p.institutionId=:institutionId AND p.contributionId=:contributionId")
    List<PeopleSubscription> getLastContribution(Long memberId,
                                                 Long institutionId,
                                                 Long contributionId,
                                                 PageRequest pageRequest);
}
