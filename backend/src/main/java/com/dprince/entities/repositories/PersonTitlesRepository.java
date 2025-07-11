package com.dprince.entities.repositories;

import com.dprince.entities.PersonTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonTitlesRepository extends JpaRepository<PersonTitle, Long> {
    Optional<PersonTitle> findByShortName(String name);
}
