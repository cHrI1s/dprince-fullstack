package com.dprince.entities.repositories;

import com.dprince.entities.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {
    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);
    Long countAllByEventId(Long eventId);

    List<EventAttendance> findAllByEventId(@NonNull Long eventId);
}
