package com.dprince.apis.institution.models.donations;

import com.dprince.apis.institution.models.donations.parts.SingleDonation;
import com.dprince.apis.institution.validators.ChurchContributionValidator;
import com.dprince.apis.institution.validators.GeneralDonationValidation;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.Subscriptionable;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.InstitutionDonation;
import com.dprince.entities.enums.PaymentMode;
import com.dprince.entities.enums.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class DonationContribution implements Institutionable, Subscriptionable {
    private Long institutionId;

    @NotNull(message = "Member not specified!", groups = {ChurchContributionValidator.class})
    @NotNull(message = "Partner not specified!", groups = {GeneralDonationValidation.class})
    private Long memberId;

    @NotNull(message = "Please specify the payment mode.",
            groups = {
                    ChurchContributionValidator.class,
                    GeneralDonationValidation.class
            })
    private PaymentMode paymentMode;

    @Valid
    @NotNull(message = "No donations were made",
        groups = {
            ChurchContributionValidator.class,
            GeneralDonationValidation.class
    })
    @Size(min = 1, message = "At least one donation should be made",
        groups = {
            ChurchContributionValidator.class,
            GeneralDonationValidation.class
    })
    private List<SingleDonation> donations;
    public void addDonation(@NonNull List<InstitutionDonation> donations,
                             @Nullable Subscription subscription){
        donations.parallelStream()
                .forEach(donation->{
                    SingleDonation theDonation = new SingleDonation();
                    theDonation.setContributionId(donation.getChurchContributionId());
                    theDonation.setAmount(donation.getAmount());
                    theDonation.setCategoryId(donation.getCategoryId());
                    theDonation.setSubscription(subscription==null ? Subscription.ANNUAL : subscription);
                    if(this.getDonations()==null) this.donations = new ArrayList<>();
                    this.donations.add(theDonation);
                });
    }

    private String bankReference;
    private String referenceNo;
    private String referenceAccount;
    private String remarks;
    private Long creditAccount;

    @PastOrPresent(message = "Entry date must be a date in present orin the past.",
            groups = {
                    ChurchContributionValidator.class,
                    GeneralDonationValidation.class
            })
    private Date entryDate = DateUtils.getNowDateTime();

    @JsonIgnore
    private Date deadline;

    @JsonIgnore
    private Date start = DateUtils.getNowDateTime();




}
