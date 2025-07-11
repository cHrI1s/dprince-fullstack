package com.dprince.configuration.email.util;

import com.dprince.entities.Institution;
import com.dprince.entities.User;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EmailToSend {
    public void setEmail(String email) {
        if(!StringUtils.isEmpty(email)){
            this.emails.add(email);
        }
    }

    private List<String> emails = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private List<String> cc;
    private String subject;
    private String body;
    private Map<String, File> attachments = new HashMap<>();
    private Map<String, byte[]> byteAttachments = new HashMap<>();

    private Institution institution;
    private User loggedInUser;
    private boolean systemMail = false;
}
