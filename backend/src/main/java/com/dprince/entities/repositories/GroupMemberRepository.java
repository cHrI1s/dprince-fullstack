package com.dprince.entities.repositories;

import com.dprince.entities.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends CrudRepository<GroupMember, Long> {
    List<GroupMember> findAllByGroupId(Long groupId);
    Optional<GroupMember> findAllByGroupIdAndMemberId(Long groupId, Long memberId);

    @Transactional
    void deleteAllByMemberId(Long id);

    @Transactional
    void deleteAllByGroupIdIn(List<Long> groupsIds);


    @Transactional
    void deleteByMemberIdAndGroupId(Long memberId, Long groupId);


    long countAllByGroupId(Long groupId);
}
