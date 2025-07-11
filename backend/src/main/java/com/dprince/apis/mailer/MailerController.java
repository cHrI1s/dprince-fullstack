package com.dprince.apis.mailer;

import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.email.ApplicationMailSender;
import com.dprince.configuration.email.EmailService;
import com.dprince.entities.CustomMailSender;
import com.dprince.entities.Institution;
import com.dprince.entities.User;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/mailer")
public class MailerController {
    private final AppRepository appRepository;
    private final EmailService emailService;

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save-settings")
    public ApiWorkFeedback save(@RequestBody @Valid CustomMailSender customMailSender,
                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(customMailSender, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(customMailSender.getInstitutionId())
                .orElseThrow(()->{
                    // throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });

        CustomMailSender mailer = appRepository
                .getMailSenderRepository()
                .findByInstitutionId(customMailSender.getInstitutionId())
                .orElse(null);
        if(mailer!=null) customMailSender.setId(mailer.getId());
        customMailSender = this.appRepository.getMailSenderRepository()
                .save(customMailSender);
        // Add it to the senders
        ApplicationMailSender.addCustomMailSender(customMailSender);
        return ApiWorkFeedback.builder()
                .message("Mailer Settings saved!")
                .object(customMailSender)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/get/{institutionId}")
    public ApiWorkFeedback get(@PathVariable("institutionId") Long institutionId){
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(institutionId)
                .orElseThrow(()->{
                    // throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });

        CustomMailSender mailer = appRepository
                .getMailSenderRepository()
                .findByInstitutionId(institution.getId())
                .orElse(new CustomMailSender());
        return ApiWorkFeedback.builder()
                .message("Mailer Settings loaded!")
                .object(mailer)
                .build();
    }
}
