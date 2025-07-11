package com.dprince.entities;

import com.dprince.apis.institution.models.BirthdaysModel;
import com.dprince.apis.institution.models.FamilyListQuery;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.embeded.FamilyAddress;
import com.dprince.entities.enums.Gender;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.FamilyMemberDBO;
import com.dprince.entities.vos.PageVO;
import com.dprince.startup.GeneralValues;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "institution_families",
        indexes = {
            @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class InstitutionFamily extends FamilyAddress implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dob;

    private String photo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, length = 50)
    private String name;


    @Column(nullable = false)
    private Long familyHead;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String familyCode;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String hofCode;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Transient
    private List<InstitutionMember> members = new ArrayList<>();
    public void addMember(InstitutionMember member){
        this.members.add(member);
    }

    /**
     * Method that populates the family with members.
     * Each Member is of type InstitutionMember
      */
    public void populateMembers(AppRepository appRepository){
        appRepository
                .getInstitutionFamilyMemberRepository()
                .findAllByFamilyId(this.getId())
                        .parallelStream()
                                .collect(Collectors.toMap(
                                        obj->obj,
                                        obj->obj,
                                        (existing, replacement)->existing
                                )).values()
                        .parallelStream()
                                .forEach(familyMember->{
                                    appRepository.getInstitutionMemberRepository()
                                            .findById(familyMember.getMemberId())
                                            .ifPresent(member ->{
                                                MemberOfFamilyTitle title = familyMember.getTitle();
                                                PersonTitle personTitle = GeneralValues.PERSON_TITLES.get(member.getTitleId());
                                                if(personTitle!=null) member.setTitle(personTitle.getShortName());
                                                member.setFamilyRole(title);
                                                member.setMemberSince(familyMember.getMemberSince());
                                                this.addMember(member);
                                            });
                                });
    }

    public void delete(AppRepository appRepository){
        appRepository.getInstitutionFamilyMemberRepository().deleteAllByFamilyId(this.getId());
        appRepository.getInstitutionFamilyRepository().deleteById(this.getId());
    }

    public static Page<InstitutionFamily> findFamilies(EntityManager entityManager,
                                                      AppRepository appRepository,
                                                      FamilyListQuery listQuery){
        Page<InstitutionFamily> foundResults;
        try {
            String selectSql = "SELECT DISTINCT f FROM InstitutionFamily f",
                    joinString = " INNER JOIN InstitutionFamilyMember ifm ON f.id=ifm.familyId" +
                            " JOIN InstitutionMember im ON im.id=ifm.memberId",
                    countSql = "SELECT COUNT(DISTINCT(f)) FROM InstitutionFamily f",
                    conditionString = " WHERE f.institutionId=:institutionId";

            if (!StringUtils.isEmpty(listQuery.getQuery())) {
                conditionString += " AND (LOWER(im.firstName) LIKE LOWER(:query) OR LOWER(im.lastName) LIKE LOWER(:query) OR LOWER(CONCAT(im.firstName, ' ', im.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(im.lastName, ' ', im.firstName)) LIKE LOWER(:query) OR LOWER(im.email)=LOWER(:query))";
            }

            if (listQuery.getMemberCodes() != null && !listQuery.getMemberCodes().isEmpty()) {
                conditionString += " AND LOWER(im.code) IN (:memberCodes)";
            }

            if (listQuery.getFrom() != null) conditionString += " AND CONVERT(DATE, FORMAT(f.dob, 'MMdd'))>=CONVERT(DATE, FORMAT(f.dob, :fromDate))";
            if (listQuery.getTo() != null) conditionString += " AND CONVERT(DATE, FORMAT(f.dob, 'MMdd'))<=CONVERT(DATE, FORMAT(f.dob, :toDate))";

            TypedQuery<InstitutionFamily> selectQuery = entityManager.createQuery(selectSql + joinString + conditionString, InstitutionFamily.class);
            TypedQuery<Long> countQuery = entityManager.createQuery(countSql + joinString + conditionString, Long.class);

            selectQuery.setParameter("institutionId", listQuery.getInstitutionId());
            countQuery.setParameter("institutionId", listQuery.getInstitutionId());

            if (!StringUtils.isEmpty(listQuery.getQuery())) {
                selectQuery.setParameter("query", "%" + listQuery.getQuery() + "%");
                countQuery.setParameter("query", "%" + listQuery.getQuery() + "%");
            }
            if (listQuery.getMemberCodes() != null && !listQuery.getMemberCodes().isEmpty()) {
                List<String> upperCaseCodes = listQuery.getMemberCodes()
                        .parallelStream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
                selectQuery.setParameter("memberCodes", upperCaseCodes);
                countQuery.setParameter("memberCodes", upperCaseCodes);
            }

            if (listQuery.getFrom() != null) {
                selectQuery.setParameter("fromDate", listQuery.getFrom());
                countQuery.setParameter("fromDate", listQuery.getFrom());
            }
            if (listQuery.getTo() != null) {
                selectQuery.setParameter("toDate", listQuery.getTo());
                countQuery.setParameter("toDate", listQuery.getTo());
            }
            // Set pagination parameters
            int page = listQuery.getPage() - 1;
            if (page < 0) page = 0;
            selectQuery.setFirstResult(page * listQuery.getSize());
            selectQuery.setMaxResults(listQuery.getSize());

            List<InstitutionFamily> results = selectQuery.getResultList();
            results.parallelStream().forEach(singleFamily -> singleFamily.populateMembers(appRepository));
            Long total = countQuery.getSingleResult();
            if (total == null) total = 0L;
            foundResults = new PageImpl<>(results, PageRequest.of(page, listQuery.getSize()), total);
        } finally {
            entityManager.close();
        }
        return foundResults;
    }


    @Transient
    @JsonIgnore
    public static Long countFamiliesInInstitutionType(EntityManager entityManager,
                                                      InstitutionType institutionType){
        String sql = "SELECT COUNT(if) FROM InstitutionFamily if" +
                " INNER JOIN Institution i ON if.institutionId=i.id" +
                " WHERE i.institutionType=:institutionType";
        TypedQuery<Long> query = entityManager.createQuery(sql, Long.class);
        query.setParameter("institutionType", institutionType);
        Long foundResult = query.getSingleResult();
        return foundResult == null ? 0 : foundResult;
    }


    public static PageVO getMarriages(@NonNull EntityManager entityManager,
                                    @NonNull Institution institution,
                                    @NonNull BirthdaysModel model){
        PageVO pageVO;
        try {
            String selectQuery = "SELECT f FROM InstitutionFamily f",
                    joinQuery = " INNER JOIN InstitutionFamilyMember fm ON fm.familyId=f.id",
                    whereQuery = " WHERE fm.institutionId=:institutionId",
                    groupQuery = " GROUP By f.id, f.institutionId, f.dob, f.name, f.familyHead," +
                            " f.addressLine1, f.addressLine2, f.addressLine3, f.pincode, f.district, f.state, f.phone, f.phoneCode, f.country, f.familyCode, f.lastUpdate, f.photo" +
                            " HAVING COUNT(DISTINCT fm.title)>=1" +
                            " AND SUM(CASE WHEN fm.title=:maleTitle THEN 1 ELSE 0 END)=1" +
                            " AND SUM(CASE WHEN fm.title=:femaleTitle THEN 1 ELSE 0 END)=1",
                    countSqlQuery = "SELECT COUNT(f) FROM InstitutionFamily f";

            if(model.getEnd()==null){
                whereQuery += " AND CONVERT(DATE, FORMAT(f.dob, 'MMdd'))=CONVERT(DATE, FORMAT(:startDate, 'MMdd'))";
            } else {
                whereQuery += " AND (CONVERT(DATE, FORMAT(f.dob, 'MMdd'))>=CONVERT(DATE, FORMAT(:startDate, 'MMdd'))";
                whereQuery += " AND CONVERT(DATE, FORMAT(f.dob, 'MMdd'))<=CONVERT(DATE, FORMAT(:endDate, 'MMdd')))";
            }

            TypedQuery<InstitutionFamily> selectionQuery = entityManager.createQuery(selectQuery + joinQuery + whereQuery + groupQuery,
                    InstitutionFamily.class);

            TypedQuery<Long> countQuery = entityManager.createQuery(countSqlQuery + joinQuery + whereQuery + groupQuery,
                    Long.class);

            selectionQuery.setParameter("institutionId", institution.getId());
            countQuery.setParameter("institutionId", institution.getId());

            selectionQuery.setParameter("maleTitle", MemberOfFamilyTitle.HUSBAND);
            countQuery.setParameter("maleTitle", MemberOfFamilyTitle.HUSBAND);

            selectionQuery.setParameter("femaleTitle", MemberOfFamilyTitle.WIFE);
            countQuery.setParameter("femaleTitle", MemberOfFamilyTitle.WIFE);

            // Add dates to the the search
            selectionQuery.setParameter("startDate", model.getStart());
            countQuery.setParameter("startDate", model.getStart());
            if(model.getEnd()!=null){
                selectionQuery.setParameter("endDate", model.getEnd());
                countQuery.setParameter("endDate", model.getEnd());
            }

            int page = model.getPage() - 1;
            if (page < 0) page = 0;
            selectionQuery.setFirstResult(page * model.getSize());
            selectionQuery.setMaxResults(model.getSize());

            List<InstitutionFamily> results = selectionQuery.getResultList();
            InstitutionFamily.populateFamilies(entityManager, results);
            Long count = 0L;
            try {
                count = countQuery.getSingleResult();
            } catch (Exception ignored){}

            Page<InstitutionFamily> pagedResults = new PageImpl<>(results,
                    PageRequest.of(page, model.getSize()), count);
            pageVO = PageVO.getPageVO(pagedResults);
        } catch (Exception exception){
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Failed to load Families");
        } finally {
            entityManager.close();
        }
        return pageVO;
    }

    public static void populateFamilies(@NonNull EntityManager entityManager,
                                        @NonNull List<InstitutionFamily> results){
        try {
            List<Long> familiesIds = results.parallelStream()
                    .map(InstitutionFamily::getId)
                    .collect(Collectors.toList());

          List<MemberOfFamilyTitle> familyOwners = Arrays.asList(
                   MemberOfFamilyTitle.HUSBAND,
                    MemberOfFamilyTitle.WIFE
            );

            // Kora map ya member na

            String sql = "SELECT new com.dprince.entities.vos.FamilyMemberDBO(" +
                    "im, ifm.familyId, ifm.title) FROM InstitutionMember im INNER JOIN InstitutionFamilyMember ifm ON ifm.memberId=im.id WHERE ifm.familyId IN :families";
            TypedQuery<FamilyMemberDBO> membersSelection = entityManager.createQuery(sql,
                    FamilyMemberDBO.class);
            membersSelection.setParameter("families", familiesIds);
            List<FamilyMemberDBO> familyMembersList = membersSelection.getResultList()
                    .parallelStream()
                    .filter(member -> familyOwners.contains(member.getFamilyTitle()))
                    .collect(Collectors.toList());
            Map<Long, List<InstitutionMember>> membersMap = new HashMap<>();


            Map<Long, InstitutionMember> familyNames = new HashMap<>();
            familyMembersList.parallelStream()
                    .forEach(memberDBO -> {
                        if (!membersMap.containsKey(memberDBO.getFamilyId())) {
                            membersMap.put(memberDBO.getFamilyId(), new ArrayList<>());
                        }
                        InstitutionMember member = new InstitutionMember();
                        BeanUtils.copyProperties(memberDBO, member);
                        member.setFamilyRole(memberDBO.getFamilyTitle());
                        if (memberDBO.getFamilyTitle().equals(MemberOfFamilyTitle.HUSBAND)
                                || member.getGender().equals(Gender.MALE)) {
                            familyNames.put(memberDBO.getFamilyId(), member);
                        }
                        membersMap.get(memberDBO.getFamilyId()).add(member);
                    });


            results.parallelStream()
                    .forEach(family -> {
                        if (membersMap.containsKey(family.getId())) {
                            family.setMembers(membersMap.get(family.getId()));
                        }
                        if (familyNames.containsKey(family.getId())) {
                            family.setName(familyNames.get(family.getId()).getFullName());
                            family.setHofCode(familyNames.get(family.getId()).getCode());
                        }
                    });
        } finally {
            entityManager.close();
        }
    }
}
