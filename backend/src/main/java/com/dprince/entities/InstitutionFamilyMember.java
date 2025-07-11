package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "institution_families_members",
        indexes = {
            @Index(name = "idx_family_id", columnList = "familyId"),
            @Index(name = "idx_institution_id", columnList = "institutionId"),
        })
public class InstitutionFamilyMember implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long familyId;

    @Column(nullable = false)
    private Long institutionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberOfFamilyTitle title;

    @Temporal(TemporalType.TIMESTAMP)
    private Date memberSince;


    @JsonIgnore
    @Transient
    public boolean isAlreadySaved(@NonNull AppRepository appRepository){
        Long count = appRepository
                .getInstitutionFamilyMemberRepository()
                .countSimilarMember(this.getInstitutionId(), this.getFamilyId(), this.getMemberId());
        if(count==null) count = 0L;
        return count>0;
    }

    @Override
    public boolean equals(Object o){
        if(o==null || this.getClass() != o.getClass()) return false;
        InstitutionFamilyMember theMember = (InstitutionFamilyMember) o;
        List<Boolean> equalities = new ArrayList<>();
        equalities.add(Objects.equals(this.getMemberId(), theMember.getMemberId()));
        equalities.add(Objects.equals(this.getInstitutionId(), theMember.getInstitutionId()));
        equalities.add(Objects.equals(this.getInstitutionId(), theMember.getInstitutionId()));
        equalities.add(Objects.equals(this.getFamilyId(), theMember.getFamilyId()));
        equalities.add(Objects.equals(this.getTitle(), theMember.getTitle()));
        return !equalities.contains(false);
    }

    @Override
    public int hashCode(){
        return Objects.hash(
                this.getMemberId(),
                this.getFamilyId(),
                this.getInstitutionId(),
                this.getTitle());
    }
}
