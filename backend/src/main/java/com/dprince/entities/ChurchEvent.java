package com.dprince.entities;

import com.dprince.apis.events.models.validators.ChurchEventCreationValidator;
import com.dprince.apis.events.vos.EventAttendee;
import com.dprince.apis.utils.models.ChurchBelongingListQuery;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FieldNameConstants
@Data
@Entity
@Table(name="church_events",
        indexes={
            @Index(name = "index_institution_id", columnList = "institutionId"),
            @Index(name = "index_event_date", columnList = "eventDate"),
        })
public class ChurchEvent implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long institutionId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Institution childInstitution;



    @NotBlank(message = "Please provide the title of the event.",
            groups={
                ChurchEventCreationValidator.class
    })
    @Column(nullable = false)
    private String title;


    @NotBlank(message = "Please provide the description of the event.",
            groups={
                ChurchEventCreationValidator.class
    })
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "Please provide the place of the event.",
            groups={
                ChurchEventCreationValidator.class
    })
    private String place;
    // Can be entered
    private String remarks;

    @Min(value = 1, message="At least one attendee must come to an event.",
            groups={
                ChurchEventCreationValidator.class
    })
    @Column(nullable = false)
    private int expectedAttendees;

    @Transient
    private Long numberOfAttendees;

    @NotNull(message = "The date of the event should be provided.")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date eventDate;

    private boolean shared  = false;

    private boolean settled = false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Transient
    public Institution owner;


    public static Page<ChurchEvent> search(EntityManager entityManager,
                                    AppRepository appRepository,
                                    ChurchBelongingListQuery listQuery){
        Page<ChurchEvent> foundResults;
        try {
            String selectSsql = "SELECT ce FROM ChurchEvent ce",
                    countSql = "SELECT COUNT(*) FROM ChurchEvent ce",
                    whereQuery = " WHERE ce.institutionId IN :institutionsIds";

            if (!StringUtils.isEmpty(listQuery.getQuery())) whereQuery += " AND LOWER(ce.title) LIKE LOWER(:query)";

            if (listQuery.getPublished() != null) whereQuery += " AND ce.published=:published";
            if (listQuery.getFrom() != null) whereQuery += " AND ce.eventDate>=:fromDate";
            if (listQuery.getTo() != null) whereQuery += " AND ce.eventDate<=:toDate";


            TypedQuery<ChurchEvent> churchEventTypedQuery = entityManager.createQuery(selectSsql + whereQuery, ChurchEvent.class);
            TypedQuery<Long> countTypedQuery = entityManager.createQuery(countSql + whereQuery, Long.class);

            churchEventTypedQuery.setParameter("institutionsIds", listQuery.getInstitutionsIds());
            countTypedQuery.setParameter("institutionsIds", listQuery.getInstitutionsIds());

            if (!StringUtils.isEmpty(listQuery.getQuery())) {
                churchEventTypedQuery.setParameter("query", "%" + listQuery.getQuery() + "%");
                countTypedQuery.setParameter("query", "%" + listQuery.getQuery() + "%");
            }

            if (listQuery.getPublished() != null) {
                churchEventTypedQuery.setParameter("published", listQuery.getPublished());
                countTypedQuery.setParameter("published", listQuery.getPublished());
            }

            if (listQuery.getFrom() != null) {
                churchEventTypedQuery.setParameter("fromDate", listQuery.getFrom());
                countTypedQuery.setParameter("fromDate", listQuery.getFrom());
            }

            if (listQuery.getTo() != null) {
                churchEventTypedQuery.setParameter("toDate", listQuery.getTo());
                countTypedQuery.setParameter("toDate", listQuery.getTo());
            }

            int page = listQuery.getPage() - 1;
            if (page < 0) page = 0;
            churchEventTypedQuery.setMaxResults(listQuery.getSize());
            churchEventTypedQuery.setFirstResult(page * listQuery.getSize());
            List<ChurchEvent> foundEvents = churchEventTypedQuery.getResultList();
            foundEvents.parallelStream().forEach(singleEvent -> {
                appRepository.getInstitutionRepository()
                        .findById(singleEvent.getInstitutionId())
                        .ifPresent(singleEvent::setOwner);

                Long attendees = appRepository.getEventAttendanceRepository()
                        .countAllByEventId(singleEvent.getId());
                if(attendees==null) attendees = 0L;
                singleEvent.setNumberOfAttendees(attendees);
            });
            Long totalEvents = countTypedQuery.getSingleResult();

            foundResults = new PageImpl<>(foundEvents, PageRequest.of(page, listQuery.getSize()), totalEvents);
        } finally {
            entityManager.close();
        }
        return foundResults;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<EventAttendee> attendees;

    public void populate(AppRepository appRepository){
        Set<Long> attendeesIds = appRepository.getEventAttendanceRepository()
                .findAllByEventId(this.getId())
                .parallelStream()
                .map(EventAttendance::getMemberId)
                .collect(Collectors.toSet());
        Sort sort = Sort.by(Sort.Direction.ASC,
                InstitutionMember.Fields.firstName,
                InstitutionMember.Fields.lastName);
        List<EventAttendee> attendees = appRepository.getInstitutionMemberRepository()
                .getEventAttendees(attendeesIds, sort);
        this.setAttendees(attendees);
    }
}
