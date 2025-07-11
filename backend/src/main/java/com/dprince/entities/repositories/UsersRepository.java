package com.dprince.entities.repositories;

import com.dprince.entities.User;
import com.dprince.entities.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {
    @NotNull
    List<User> findAll();
    User findByUsername(String username);
    User findByStaffId(String staffId);
    Page<User> findByPhone(Long phone, PageRequest pageRequest);

    List<User> findAllByUserType(@NonNull UserType userType);
    List<User> findAllByUserTypeIn(@NonNull List<UserType> userTypes);

    Page<User> findAllByUserTypeIn(@NonNull List<UserType> userTypes,
                                   @NonNull PageRequest pageRequest);


    @Query("SELECT u FROM User u WHERE u.userType IN :userTypes AND " +
            "(" +
                "LOWER(u.firstName) LIKE %:query% OR " +
                "LOWER(u.lastName) LIKE %:query% OR " +
                "CONCAT(LOWER(u.firstName), LOWER(u.lastName)) LIKE %:query% OR " +
                "CONCAT(LOWER(u.firstName),' ', LOWER(u.lastName)) LIKE %:query% OR " +
                "CONCAT(LOWER(u.lastName), LOWER(u.firstName)) LIKE %:query% OR " +
                "CONCAT(LOWER(u.lastName), ' ', LOWER(u.firstName)) LIKE %:query%" +
            ")")
    Page<User> findSimilar(@NonNull List<UserType> userTypes,
                           @NonNull String query,
                           @NonNull PageRequest pageRequest);

    long countAllByInstitutionId(@NonNull Long id);

    @Query("SELECT COUNT(*) FROM User u WHERE u.institutionId=:institutionId AND u.userType IN (:userTypes)")
    long countAllAdmins(@NonNull Long institutionId, List<UserType> userTypes);

    long countAllByUserTypeInAndInstitutionId(List<UserType> userTypes, Long institutionId);
    long countAllByUserTypeAndInstitutionIdIn(UserType userTypes, List<Long> institutionIds);

    Page<User> findAllByInstitutionId(@NonNull Long id,
                                      @NonNull PageRequest pageRequest);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);
}
