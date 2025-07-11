package com.dprince.entities.repositories;

import com.dprince.apis.dashboard.vos.TopMember;
import com.dprince.entities.InstitutionDonation;
import com.dprince.entities.parts.TopDonatedAmount;
import com.dprince.entities.parts.UniqueDonation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface InstitutionDonationRepository extends CrudRepository<InstitutionDonation, Long> {

    @Transactional
    void deleteAllByReceiptNo(String receiptNo);

    List<InstitutionDonation> findAllByReceiptNoAndInstitutionId(String receiptNo,
                                                                 @NonNull Long institutionId);


    @Transactional
    void deleteAllByInstitutionId(Long institutionId);

    @Transactional
    void deleteAllByMemberId(Long memberId);


    @Query("SELECT new com.dprince.entities.parts.UniqueDonation(" +
            "isd.amount as amount, isd.receiptNo as receiptNo, isd.entryDate as entryDate)" +
            " FROM InstitutionDonation isd " +
            " INNER JOIN InstitutionMember im ON isd.memberId=im.id" +
            " WHERE isd.churchContributionId=:churchContributionId" +
            " AND CONVERT(DATE, isd.entryDate)>=CONVERT(DATE, :startDate)" +
            " AND CONVERT(DATE, isd.entryDate)<=CONVERT(DATE, :endDate)" +
            " GROUP BY isd.amount, isd.receiptNo, isd.entryDate")
    List<UniqueDonation> getChurchUniqueDonations(@NonNull Long churchContributionId,
                                                  @NonNull Date startDate,
                                                  @NonNull Date endDate);


    @Query("SELECT new com.dprince.entities.parts.UniqueDonation(" +
            "isd.amount as amount, isd.receiptNo as receiptNo, isd.entryDate as entryDate)" +
            " FROM InstitutionDonation isd " +
            " INNER JOIN InstitutionMember im ON isd.memberId=im.id" +
            " WHERE isd.categoryId=:categoryId" +
            " AND CONVERT(DATE, isd.entryDate)>=CONVERT(DATE, :startDate)" +
            " AND CONVERT(DATE, isd.entryDate)<=CONVERT(DATE, :endDate)" +
            " GROUP BY isd.amount, isd.receiptNo, isd.entryDate")
    List<UniqueDonation> getGOUniqueDonations(@NonNull Long categoryId,
                                              @NonNull Date startDate,
                                              @NonNull Date endDate);

    @Query("SELECT COUNT(*) FROM InstitutionDonation insd " +
            "WHERE insd.institutionId=:institutionId" +
            " AND insd.memberId=:memberId" +
            " AND insd.receiptNo=:receiptNo" +
            " AND insd.amount=:amount" +
            " AND insd.churchContributionId=:churchContributionId" +
            " AND insd.categoryId=:categoryId" +
            " AND insd.entryDate=:entryDate")
    Long countSimilar(@NonNull Long institutionId,
                      @NonNull Long memberId,
                      @NonNull String receiptNo,
                      @NonNull Long amount,
                      @Nullable Long churchContributionId,
                      @Nullable Long categoryId,
                      @NonNull Date entryDate);


    @Query("SELECT new com.dprince.apis.dashboard.vos.TopMember(" +
            " im.code, " +
            " CONCAT(im.firstName, ' ', im.lastName) as full_name, " +
            " cat.name as category, " +
            " SUM(don.amount) as amount, " +
            " im.district as district" +
            ")" +
            " FROM InstitutionDonation don" +
            "    INNER JOIN InstitutionMember im ON im.id=don.memberId" +
            "    INNER JOIN InstitutionCategory cat on cat.id = don.categoryId" +
            " WHERE im.institutionId=:institutionId " +
            " AND (CONVERT(DATE, don.entryDate) BETWEEN CONVERT(DATE, :startDate) AND CONVERT(DATE, :endDate))" +
            " GROUP BY don.memberId, im.firstName, im.lastName, im.code, im.district, cat.name")
    List<TopMember> getTopDonationsInGeneral(@NonNull Long institutionId,
                                             @NonNull Date startDate,
                                             @NonNull Date endDate,
                                             @NonNull PageRequest pageRequest);


    @Query("SELECT new com.dprince.apis.dashboard.vos.TopMember(" +
            " im.code, " +
            " CONCAT(im.firstName, ' ', im.lastName) as full_name, " +
            " cc.name as category, " +
            " SUM(don.amount) as amount, " +
            " im.district as district" +
            ")" +
            " FROM InstitutionDonation don" +
            "    INNER JOIN InstitutionMember im ON im.id=don.memberId" +
            "    INNER JOIN ChurchContribution cc on cc.id = don.churchContributionId" +
            " WHERE im.institutionId=:institutionId " +
            " AND (CONVERT(DATE, don.entryDate) BETWEEN CONVERT(DATE, :startDate) AND CONVERT(DATE, :endDate))" +
            " GROUP BY don.memberId, im.firstName, im.lastName, im.code, im.district, cc.name")
    List<TopMember> getTopDonationsInChurch(@NonNull Long institutionId,
                                             @NonNull Date startDate,
                                             @NonNull Date endDate,
                                             @NonNull PageRequest pageRequest);


    @Query("SELECT new com.dprince.entities.parts.TopDonatedAmount(" +
            " SUM(isd.amount) as amount," +
            " isd.memberId as memberId" +
            ") FROM InstitutionDonation isd" +
            " WHERE isd.institutionId=:institutionId" +
            " AND CONVERT(DATE, isd.entryDate) BETWEEN CONVERT(DATE, :startDate) AND CONVERT(DATE, :endDate) GROUP BY isd.memberId")
    List<TopDonatedAmount> getTopAmounts(@NonNull Long institutionId,
                                         @NonNull Date startDate,
                                         @NonNull Date endDate,
                                         @NonNull PageRequest pageRequest);


    long countAllByCategoryIdIs(Long categoryId);


    @Query("SELECT new com.dprince.entities.parts.UniqueDonation(" +
            "isd.amount as amount, isd.receiptNo as receiptNo, isd.entryDate as entryDate)" +
            " FROM InstitutionDonation isd " +
            " INNER JOIN InstitutionCategory cat ON isd.categoryId=cat.id " +
            " INNER JOIN InstitutionMember im ON isd.memberId=im.id" +
            " WHERE isd.categoryId=:categoryId" +
            " AND CONVERT(DATE, isd.entryDate)>=CONVERT(DATE, :startDate)" +
            " AND CONVERT(DATE, isd.entryDate)<=CONVERT(DATE, :endDate)" +
            " GROUP BY isd.amount, isd.receiptNo, isd.entryDate")
    List<UniqueDonation> sumAllDonationsByCategory(@NonNull Long categoryId,
                                   @NonNull Date startDate,
                                   @NonNull Date endDate);

    @Query("SELECT new com.dprince.entities.parts.UniqueDonation(" +
            "isd.amount as amount, isd.receiptNo as receiptNo, isd.entryDate as entryDate)" +
            " FROM InstitutionDonation isd " +
            " INNER JOIN ChurchContribution cc ON isd.churchContributionId=cc.id " +
            " INNER JOIN InstitutionMember im ON isd.memberId=im.id" +
            " WHERE isd.churchContributionId=:contributionId" +
            " AND CONVERT(DATE, isd.entryDate)>=CONVERT(DATE, :startDate)" +
            " AND CONVERT(DATE, isd.entryDate)<=CONVERT(DATE, :endDate)" +
            " GROUP BY isd.amount, isd.receiptNo, isd.entryDate")
    List<UniqueDonation> sumAllDonationsByContribution(Long contributionId,
                                       @NonNull Date startDate,
                                       @NonNull Date endDate);
}
