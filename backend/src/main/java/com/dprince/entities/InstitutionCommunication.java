package com.dprince.entities;

import com.dprince.configuration.database.converters.ListOfLongConverter;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Table
@Entity(name="institution_communications")
public class InstitutionCommunication {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @Convert(converter = ListOfLongConverter.class)
    private List<Long> destination;

    private Long destinationGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CommunicationWay way;

    @Column(nullable = false)
    private String subject;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;

    @Column(nullable = false)
    private boolean sharedViaEmail;

    @Column(nullable = false)
    private boolean sharedViaWhatsApp;

    @Column(nullable = false)
    private boolean sharedSMS;

    private Long templateId;


    public void addPeopleInGroupToDestination(AppRepository appRepository){
        if(this.getDestinationGroup()!=null) {
            // find the group
            List<Long> groupMembersIds = appRepository
                    .getGroupMemberRepository()
                    .findAllByGroupId(this.getDestinationGroup())
                    .parallelStream()
                    .map(GroupMember::getMemberId)
                    .collect(Collectors.toList());
            if (this.getDestination() == null) this.destination = new ArrayList<>();
            this.destination.addAll(groupMembersIds);
        }
    }
}
