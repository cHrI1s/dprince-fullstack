package com.dprince.entities;

import com.dprince.apis.institution.models.AdminListingQuery;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.*;
import java.util.List;



@Data
@Entity
@Table(name = "groups_members",
        indexes = {
            @Index(name = "index_member_id", columnList = "memberId"),
        })
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long memberId;


    public static Page<InstitutionMember> listMembers(EntityManager entityManager,
                                                      AppRepository appRepository,
                                                      AdminListingQuery listingQuery){
        Page<InstitutionMember> foundResults;
        try {
            String selectQueryString = "SELECT im FROM InstitutionMember im",
                    countQueryString = "SELECT COUNT(*) FROM InstitutionMember im",
                    whereString = " INNER JOIN GroupMember gm ON im.id=gm.memberId" +
                            " WHERE im.institutionId=:institutionId AND gm.groupId=:groupId";

            if (!StringUtils.isEmpty(listingQuery.getQuery())) {
                whereString += " AND LOWER(CONCAT(im.firstName, ' ', im.lastName)) LIKE LOWER(:query) OR LOWER(CONCAT(im.lastName, ' ', im.firstName)) LIKE LOWER(:query)";
            }

            TypedQuery<InstitutionMember> selectTypeQuery = entityManager.createQuery(selectQueryString + whereString, InstitutionMember.class);
            TypedQuery<Long> countTypeQuery = entityManager.createQuery(countQueryString + whereString, Long.class);

            selectTypeQuery.setParameter("institutionId", listingQuery.getInstitutionId());
            countTypeQuery.setParameter("institutionId", listingQuery.getInstitutionId());

            selectTypeQuery.setParameter("groupId", listingQuery.getGroupId());
            countTypeQuery.setParameter("groupId", listingQuery.getGroupId());

            if (!StringUtils.isEmpty(listingQuery.getQuery())) {
                selectTypeQuery.setParameter("query", "%" + listingQuery.getQuery() + "%");
                countTypeQuery.setParameter("query", "%" + listingQuery.getQuery() + "%");
            }

            int page = listingQuery.getPage() - 1;
            if (page < 0) page = 0;
            selectTypeQuery.setFirstResult(page * listingQuery.getSize());
            selectTypeQuery.setMaxResults(listingQuery.getSize());

            List<InstitutionMember> foundMembers = selectTypeQuery.getResultList();
            Long total = countTypeQuery.getSingleResult();

            foundResults = new PageImpl<>(foundMembers,
                    PageRequest.of(page, listingQuery.getSize()),
                    total);
        } finally {
            entityManager.close();
        }
        return foundResults;
    }

}
