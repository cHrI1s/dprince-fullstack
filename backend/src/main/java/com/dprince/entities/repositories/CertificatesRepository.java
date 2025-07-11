package com.dprince.entities.repositories;

import com.dprince.entities.Certificate;
import com.dprince.entities.enums.CertificateType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificatesRepository extends JpaRepository<Certificate, Long> {
    Long countAllByCertificateTypeAndOwnerId(@NonNull CertificateType certificateType,
                                             @NonNull Long ownerId);

    @Query("SELECT c FROM Certificate c " +
            "WHERE c.certificateType=:certificateType " +
            "AND c.ownerId=:ownerId")
    List<Certificate> getCertificates(@NonNull CertificateType certificateType,
                                      @NonNull Long ownerId,
                                      @NonNull PageRequest pageRequest);
}
