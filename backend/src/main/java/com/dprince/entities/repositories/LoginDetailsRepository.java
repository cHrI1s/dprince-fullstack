package com.dprince.entities.repositories;

import com.dprince.entities.LoginDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginDetailsRepository extends CrudRepository<LoginDetail, Long> {
    Optional<LoginDetail> findByTokenAndUserId(String token, Long userId);
}
