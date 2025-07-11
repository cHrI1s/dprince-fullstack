package com.dprince.entities.repositories;

import com.dprince.entities.FileToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTokensRepository extends JpaRepository<FileToken, Long> {
    boolean existsByToken(String token);
}
