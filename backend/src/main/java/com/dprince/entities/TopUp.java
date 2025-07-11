package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Table(name="top_ups", indexes = {
        @Index(name = "idx_institution_id", columnList = "institutionId")
})
@Entity
public class TopUp implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long sms;

    private Long email;

    private Long  whatsapp;

    private Long additionalUser;

    private Long backup;

    private Long families;
    private Long members;
    private Long churchBranches;

    @Column(nullable = false, unique = true)
    private Long institutionId;


    private void addTopup(TopUp topUp){
        if(topUp.getSms()!=null){
            if(this.sms==null) this.sms = 0L;
            this.sms += topUp.getSms();
        }
        if(topUp.getEmail()!=null){
            if(this.email==null) this.email = 0L;
            this.email += topUp.getEmail();
        }
        if(topUp.getWhatsapp()!=null){
            if(this.whatsapp==null) this.whatsapp = 0L;
            this.whatsapp += topUp.getWhatsapp();
        }
        if(topUp.getAdditionalUser()!=null) {
            if(this.additionalUser==null) this.additionalUser = 0L;
            this.additionalUser += topUp.getAdditionalUser();
        }
        if(topUp.getBackup()!=null) {
            if(this.backup==null) this.backup = 0L;
            this.backup += topUp.getBackup();
        }
        if(topUp.getMembers()!=null) {
            if(this.members==null) this.members = 0L;
            this.members += topUp.getMembers();
        }
        if(topUp.getFamilies()!=null) {
            if(this.families==null) this.families = 0L;
            this.families += topUp.getFamilies();
        }
        if(topUp.getChurchBranches()!=null) {
            if(this.churchBranches==null) this.churchBranches = 0L;
            this.churchBranches += topUp.getChurchBranches();
        }
    }


    public static void saveTopUp(@NonNull AppRepository appRepository,
                                  TopUp topUp){
        TopUp alreadySavedTopUp = appRepository.getTopUpRepository()
                .findByInstitutionId(topUp.getInstitutionId());
        if(alreadySavedTopUp!=null) alreadySavedTopUp.addTopup(topUp);
        else alreadySavedTopUp = topUp;
        appRepository.getTopUpRepository().save(alreadySavedTopUp);
    }
}
