package com.dprince.entities.repositories;

import com.dprince.entities.enums.CertificateType;
import com.dprince.entities.CertificateBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificatesBackgroundsRepository extends JpaRepository<CertificateBackground, Long> {
    Optional<CertificateBackground> findByInstitutionIdAndCertificateType(Long institutionId, CertificateType type);
    List<CertificateBackground> findAllByInstitutionId(Long institutionId);
}
