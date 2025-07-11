package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import lombok.Data;

import javax.persistence.*;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "file_tokens",
        indexes = {
                @Index(name = "index_token", columnList = "token"),
                @Index(name = "index_file_name", columnList = "fileName"),
        })
public class FileToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String token;
    public void generateToken(){
        this.token = UUID.randomUUID().toString();
    }

    @Column(nullable = false)
    private String fileName;

    @Transient
    private File file;

    @Column(nullable = false)
    private Date expirationDate = DateUtils.getTomorrowEndDateTime();

    @Transient
    private Map<String, File> attachments = new HashMap<>();
    public void addAttachment(String fileName, File file){
        this.attachments.put(fileName, file);
    }
}
