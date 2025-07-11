package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.OTPType;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

@Data
@Entity
@Table(name = "otps", indexes = {
        @Index(name="idx_user_id", columnList = "userId"),
        @Index(name="idx_otp_type", columnList = "type"),
})
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 7)
    private Integer otp;

    @Column(nullable = false)
    private Date creationDate = DateUtils.getNowDateTime();

    @Column(nullable = false)
    private OTPType type;

    public void generateOTP(){
        Random random = new Random();
        int sixDigitNumber = 100000 + random.nextInt(900000);
        this.setOtp(sixDigitNumber);
    }

    public void verifyOTP(){
        long difference = Duration.between(
                LocalDateTime.now(),
                LocalDateTime.ofInstant(this.getCreationDate().toInstant(), ZoneId.systemDefault()))
                .toMinutes();

        if(difference>5L) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "OTP expired.");
    }

    public String getLoginOTPMessage(){
        return "Dear User, Your OTP for login is "+this.getOtp()+". Never Share your OTP with anyone. dnote.ai";
    }
}
