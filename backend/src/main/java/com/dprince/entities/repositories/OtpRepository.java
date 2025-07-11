package com.dprince.entities.repositories;

import com.dprince.entities.OTP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends CrudRepository<OTP, Long> {
    Optional<OTP> findByOtpAndUserId(@NonNull Integer otp,
                                     @NonNull Long userId);
}
