package com.dprince.entities.repositories;

import com.dprince.apis.events.vos.EventAttendee;
import com.dprince.entities.InstitutionMember;
import com.dprince.entities.enums.BaptismStatus;
import com.dprince.entities.enums.ChurchFunction;
import com.dprince.entities.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface InstitutionMemberRepository extends JpaRepository<InstitutionMember, Long> {
    Optional<InstitutionMember> findByIdAndInstitutionId(Long id, Long institutionId);
    long countAllByInstitutionId(Long id);
    long countAllByGender(Gender gender);
    long countAllByGenderAndInstitutionId(Gender gender, Long institutionId);
    long countAllByInstitutionIdAndActive(Long institutionId, boolean active);

    long countAllByCountryIsAndInstitutionIdIs(String countryCode, Long institutionId);
    long countAllByCountryIsNotAndInstitutionIdIs(String countryCode, Long institutionId);

    @Query("SELECT COUNT(*) FROM InstitutionMember m " +
            "WHERE CONCAT(',', m.categoriesIds, ',') " +
            "LIKE CONCAT(',', :categoryId, ',')")
    long countAllCategoryWise(String categoryId);
    

    @Query("SELECT m FROM InstitutionMember  m " +
            "WHERE (" +
            "m.firstName LIKE %:query% " +
            "OR m.lastName LIKE %:query%" +
            "OR CONCAT(m.firstName, ' ', m.lastName) LIKE %:query%" +
            "OR CONCAT(m.lastName, ' ', m.firstName) LIKE %:query%" +
            ") AND m.institutionId=:institutionId")
    List<InstitutionMember> searchUsingQuery(@NotBlank String query,
                                             @NotNull Long institutionId);


    @Query("SELECT m FROM InstitutionMember  m " +
            "WHERE m.institutionId=:institutionId AND m.churchFunction=:churchFunction")
    List<InstitutionMember> getPriests(Long institutionId,
                                       ChurchFunction churchFunction);

    Page<InstitutionMember> findAllByInstitutionId(@NonNull Long id,
                                                   @NonNull PageRequest pageRequest);

    List<InstitutionMember> findAllByInstitutionId(@NonNull Long id);


    List<InstitutionMember> findAllByInstitutionIdIn(@NonNull List<Long> ids);

    List<InstitutionMember> findAllByIdIn(@NonNull List<Long> familyMembersIds);
    List<InstitutionMember> findAllByCodeIn(@NonNull List<String> memberCodes);
    List<InstitutionMember> findAllByIdIn(@NonNull Set<Long> familyMembersIds);

    @Query("SELECT m FROM InstitutionMember m " +
            "WHERE (" +
            "LOWER(m.firstName) LIKE LOWER(:query) OR " +
            "LOWER(m.lastName) LIKE LOWER(:query) OR " +
            "CONCAT(LOWER(m.firstName), ' ',LOWER(m.lastName))  LIKE LOWER(:query) OR " +
            "CONCAT(LOWER(m.firstName), LOWER(m.lastName))  LIKE LOWER(:query) OR " +
            "CONCAT(LOWER(m.lastName), ' ',LOWER(m.firstName))  LIKE LOWER(:query) OR " +
            "CONCAT(LOWER(m.lastName),LOWER(m.firstName))  LIKE LOWER(:query)" +
            ") OR LOWER(m.code) LIKE %:searchCode%" +
            " AND m.institutionId=:institutionId"
    )
    List<InstitutionMember> searchMemberByNameOrSearchCode(String query,
                                                           String searchCode,
                                                           @NonNull Long institutionId);

    @Transactional
    void deleteAllByInstitutionId(Long institutionId);

    @Query("SELECT COUNT(*) FROM InstitutionMember m WHERE CONCAT(',', m.categoriesIds, ',') LIKE CONCAT('%,', :categoryId, ',%')")
    Long countWhereCategoriesUsed(String categoryId);

    @Query("SELECT COUNT(*) FROM InstitutionMember m WHERE CONCAT(',', m.categoriesIds, ',') LIKE CONCAT('%,', :categoryId, ',%') AND CONVERT(DATE, m.creationDate) BETWEEN CONVERT(DATE, :from) AND CONVERT(DATE, :to)")
    Long countWhereCategoriesUsed(String categoryId,
                                  @NonNull Date from,
                                  @NonNull Date to);

    @Query("SELECT COUNT(*) FROM InstitutionMember m WHERE CONCAT(',', m.contributionsIds, ',') LIKE CONCAT('%,', :categoryId, ',%') AND CONVERT(DATE, m.creationDate) BETWEEN CONVERT(DATE, :from) AND CONVERT(DATE, :to)")
    Long countWhereContributionUsed(String categoryId,
                                    @NonNull Date from,
                                    @NonNull Date to);

    @Query("SELECT im FROM InstitutionMember im" +
            " WHERE lower(im.code)=lower(:code)" +
            " AND im.institutionId=:institutionId")
    Optional<InstitutionMember> findByCodeAndInstitutionId(@NonNull String code,
                                                           @NonNull Long institutionId);


    @Query("SELECT COUNT(im.id) FROM InstitutionMember im" +
            " WHERE lower(im.code)=lower(:code)" +
            " AND im.institutionId=:institutionId")
    Long countByCodeAndInstitutionId(String code, Long institutionId);

    
    // New methods to check if phone number and email are unique
    Optional<InstitutionMember> findByPhoneCodeAndPhone(Integer phoneCode, Long phone);

    Optional<InstitutionMember> findByEmail(String email);

    // உங்களுடைய பழைய getWithSimilarData மெத்தட்
    // இது போன் அல்லது ஈமெயில் இரண்டில் ஏதேனும் ஒன்று இருந்தால் ரிட்டர்ன் செய்யும்.
    // தனித்தனியாக சரிபார்க்க மேலே உள்ள findByPhoneCodeAndPhone மற்றும் findByEmail-ஐப் பயன்படுத்துவது நல்லது.
 // InstitutionMemberRepository.java file-ல் இந்த method-ஐக் கண்டறிந்து, இதனுடன் REPLACE செய்யவும்:
 // (இது பொதுவாக தோராயமாக Line 100-110-ல் இருக்கலாம், உங்கள் கோட் அமைப்பு பொறுத்து மாறுபடலாம்)

 @Query("SELECT m FROM InstitutionMember m WHERE " +
        "(:memberIdToExclude IS NULL OR m.id != :memberIdToExclude) AND " + // <-- இந்த லைன் சேர்க்கப்பட்டுள்ளது
        "(m.phone = :phone OR m.email = :email) AND " +
        "m.institutionId = :institutionId")
 List<InstitutionMember> getWithSimilarData(Long phone, String email, Long institutionId, Long memberIdToExclude); // <-- parameter சேர்க்கப்பட்டுள்ளது

    InstitutionMember findTopByInstitutionIdOrderByIdDesc(Long institutionId);


    long countAllByActiveAndInstitutionId(boolean active, Long institutionId);

    long countAllByBaptizedAndGenderAndInstitutionId(BaptismStatus baptized,
                                                     Gender gender,
                                                     Long institutionId);

    long countAllByCommunionAndGenderAndInstitutionId(boolean communion,
                                                     Gender gender,
                                                     Long institutionId);




    @Query("SELECT COUNT(*) FROM InstitutionMember m " +
            " WHERE CONVERT(DATE, m.dob)>CONVERT(DATE, :dateStart)" +
            " AND CONVERT(DATE, m.dob)<=CONVERT(DATE, :dateEnd)" +
            " AND m.gender=:gender" +
            " AND m.institutionId=:institutionId")
    long getGenderBasedDobs(Date dateStart, Date dateEnd,
                            Gender gender,
                            Long institutionId);


    @Query("SELECT COUNT(*) FROM InstitutionMember m" +
            " WHERE (CONVERT(DATE, FORMAT(m.dob, 'MMdd'))>=CONVERT(DATE, FORMAT(:from, 'MMdd')) " +
            " AND CONVERT(DATE,FORMAT(m.dob, 'MMdd'))<=CONVERT(DATE, FORMAT(:end, 'MMdd')))" +
            " AND m.institutionId=:institutionId")
    Long getBirthdaysBetween(@NonNull Date from,
                             @NonNull Date end,
                             @NonNull Long institutionId);

    @Modifying
    @Transactional
    @Query("UPDATE InstitutionMember SET churchFunction=null WHERE institutionId=:institutionId")
    void unsetPriests(Long institutionId);


    @Query("SELECT new com.dprince.apis.events.vos.EventAttendee(" +
            "im.firstName as firstName," +
            "im.lastName as lastName," +
            "im.code as code" +
            ") FROM InstitutionMember im WHERE im.id IN :attendeesIds")
    List<EventAttendee> getEventAttendees(Set<Long> attendeesIds,
                                          Sort sort);
}