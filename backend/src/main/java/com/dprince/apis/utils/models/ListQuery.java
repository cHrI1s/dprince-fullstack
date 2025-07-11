package com.dprince.apis.utils.models;

import com.dprince.apis.utils.models.capabilities.Internationalizable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class ListQuery implements Internationalizable {
    @Min(value=1, message="The minimum value of page must be 1.")
    private int page = 1;

    private Date from;

    private Date to;

    @Min(value=1, message="The minimum value of page size must be 1.")
    @Max(value=1000000, message = "The maximum value for listing if 1000000.")
    private int size = 10;

    private String query;
    public void setQuery(String query) {
        this.query = StringUtils.isEmpty(query) ? null : query.trim();
    }

    @NotEmpty(message = "We could not locate your device. Please make sure your device is properly set.")
    private String clientTimezone = "Asia/Calcutta";
}
