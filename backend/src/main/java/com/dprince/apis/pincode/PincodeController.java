package com.dprince.apis.pincode;

import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.PinCode;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/pincode/")
public class PincodeController {

    private final AppRepository appRepository;

    public PincodeController(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,

            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })

    @PostMapping("/save")
    public ApiWorkFeedback saveUnknownPincode(@Valid  @RequestBody PinCode pincode){
        PinCode savedPincode = this.appRepository.getPincodeRepository().findByCode(pincode.getCode())
                .orElse(null);
        if(savedPincode!=null){
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "The pincode already exist.");
        }
        PinCode save = this.appRepository.getPincodeRepository().save(pincode);
        return ApiWorkFeedback
                .builder()
                .message("The new pincode code has been saved.")
                .object(save)
                .build();
    }
}
