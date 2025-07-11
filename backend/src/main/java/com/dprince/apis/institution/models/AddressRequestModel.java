package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.enums.DurationGap;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.entities.Institution;
import com.dprince.entities.enums.AppLanguage;
import com.dprince.entities.enums.PaymentMode;
import com.dprince.entities.enums.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AddressRequestModel extends ListQuery implements Institutionable {
    private Long institutionId;
    private List<String> country;
    private Long phone;
    private Long alternatePhone;
    private Long creditAccountId;

    private List<Long> categories;
    private Integer pincode;
    private List<String> states;
    private String state;
    public void setState(String state) {
        this.state = StringUtils.isEmpty(state) ? null : state.trim();
    }

    private String district;
    public void setDistrict(String district) {
        this.district = StringUtils.isEmpty(district) ? null : district.trim();
    }

    private List<Subscription> subscription;

    @NotNull(message = "Please select the Timing")
    private DurationGap duration = DurationGap.TODAY;
    private Long minAmount;
    private Long maxAmount;
    private List<PaymentMode> paymentMode;
    private List<String> memberCodes;
    private List<AppLanguage> languages;

    private Boolean withReceipt = null;
    private Boolean active = null;

    @JsonIgnore
    private Institution institution;
    public void setInstitution(Institution institution) {
        this.institution = institution;
        if(institution!=null) this.setInstitutionId(institution.getId());
    }

    public void adjust(){
        this.adjustDuration();
    }


    public void adjustDuration(){
        final Date today = DateUtils.getNowDateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        if(this.getDuration()!=null){
            Date from = DateUtils.convertToTimezone(today, this.getClientTimezone()),
                    to = calendar.getTime();
            switch (this.getDuration()){
                case YEAR:
                    calendar.add(Calendar.YEAR, -1);
                    from = calendar.getTime();
                    break;

                case LAST_SIX_MONTH:
                    calendar.add(Calendar.MONTH, -6);
                    from = calendar.getTime();
                    break;

                case LAST_THREE_MONTH:
                    calendar.add(Calendar.MONTH, -3);
                    from = calendar.getTime();
                    break;

                case LAST_MONTH:
                    calendar.add(Calendar.MONTH, -1);
                    from = calendar.getTime();
                    break;

                case LAST_SEVEN_DAYS:
                    calendar.add(Calendar.DAY_OF_WEEK, -7);
                    from = calendar.getTime();
                    break;

                case TODAY:
                    to = null;
                    break;

                case SINGLE_DATE:
                    if(this.getFrom()==null){
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Date(s) must not be empty");
                    }
                    from = DateUtils.convertToTimezone(this.getFrom(), this.getClientTimezone());
                    to = null;
                    break;

                case CUSTOM_DATE_GAP:
                    if(this.getTo()==null || this.getFrom()==null){
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Date(s) must not be empty");
                    }
                    if(this.getTo().before(this.getFrom())) {
                        throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Ending date must be greater than the starting date.");
                    }
                    from = DateUtils.convertToTimezone(this.getFrom(), this.getClientTimezone());
                    to = DateUtils.convertToTimezone(this.getTo(), this.getClientTimezone());
                    break;
            }
            this.setFrom(from);

            if(to!=null) this.setTo(to);
        }
    }
}
