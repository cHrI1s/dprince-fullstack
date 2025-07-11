package com.dprince.apis.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * * @author Chris Ndayishimiye
 * * @created 11/14/23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@Data
public class ApiWorkFeedback {
    public boolean successful;
    public String message;
    public Object object;
    public List<?> objects;
}
