package com.dprince.entities.repositories;

import com.dprince.entities.InstitutionReceipt;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionReceiptsRepository extends CrudRepository<InstitutionReceipt, Long> {
    @Override
    List<InstitutionReceipt> findAll();

    Long countAllByInstitutionId(Long institutionId);
    Long countAllByCreditAccountId(Long creditAccountId);

    InstitutionReceipt findFirstByInstitutionIdOrderByIdDesc(Long institutionId);

    Optional<InstitutionReceipt> findByReceiptNo(String receiptNo);
    Optional<InstitutionReceipt> findByReceiptNoAndInstitutionId(String receiptNo, Long institutionId);

    @Query("SELECT ir from InstitutionReceipt ir" +
            " WHERE CONVERT(DATE, ir.entryDate)>=CONVERT(DATE, :startingDate)" +
            " AND ir.institutionId=:institutionId")
    List<InstitutionReceipt> getReceiptFromDate(Date startingDate, Long institutionId);

    @Query("SELECT COUNT(ir.id) from InstitutionReceipt ir" +
            " WHERE CONVERT(DATE, ir.entryDate)>=CONVERT(DATE, :startingDate)" +
            " AND CONVERT(DATE, ir.entryDate)<=CONVERT(DATE, :endingDate)" +
            " AND ir.institutionId=:institutionId")
    Long countReceiptsFromDate(@NonNull Date startingDate,
                               @NonNull Date endingDate,
                               @NonNull Long institutionId);

    @Query("SELECT COUNT(ir.id) from InstitutionReceipt ir" +
            " WHERE CONVERT(DATE, ir.entryDate)>=CONVERT(DATE, :startingDate)" +
            " AND CONVERT(DATE, ir.entryDate)<CONVERT(DATE, :endingDate)" +
            " AND ir.institutionId=:institutionId")
    Long countInBetween(@NonNull Date startingDate,
                        @NonNull Date endingDate,
                        @NonNull Long institutionId);


    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}
