package com.dprince.entities.repositories;

import com.dprince.entities.LoginToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * * @author Chris Ndayishimiye
 * * @created 11/13/23
 */
@Repository
public interface LoginTokensRepository extends CrudRepository<LoginToken, Long> {
    Optional<LoginToken> findFirstByJwtTokenIs(String jwtToken);
}
