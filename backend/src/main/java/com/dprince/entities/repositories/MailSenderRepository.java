package com.dprince.entities.repositories;

import com.dprince.entities.CustomMailSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailSenderRepository extends JpaRepository<CustomMailSender, Long> {
    Optional<CustomMailSender> findByInstitutionId(Long institutionId);
}
