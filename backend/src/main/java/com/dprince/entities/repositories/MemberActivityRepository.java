package com.dprince.entities.repositories;

import com.dprince.entities.MemberActivity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberActivityRepository extends CrudRepository<MemberActivity, Long> {
    Optional<MemberActivity> findByMemberId(Long aLong);
}
