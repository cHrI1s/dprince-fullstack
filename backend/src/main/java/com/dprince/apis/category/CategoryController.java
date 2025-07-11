package com.dprince.apis.category;

import com.dprince.apis.misc.category.models.CategoryCreator;
import com.dprince.apis.misc.category.models.InstitutionCategoryModel;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.models.SimpleGetModel;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.Institution;
import com.dprince.entities.InstitutionCategory;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/category/")
public class CategoryController {
    private final AppRepository appRepository;


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save")
    @Operation(summary = "Create a new category.", responses = {
            @ApiResponse(description = "Successful Save",
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiWorkFeedback.class))),
            @ApiResponse(responseCode = "417", description = "Institution does not exist.")
    })
    public ApiWorkFeedback save(@RequestBody @Valid CategoryCreator creator,
                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        InstitutionType institutionType;
        Institution institution = null;
        if(creator.getInstitutionId()!=null) {
            institution = this.appRepository.getInstitutionRepository()
                    .findById(creator.getInstitutionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution does not exist."));
            institutionType = institution.getInstitutionType();
        } else {
            institutionType = InstitutionType.CHURCH;
        }

        List<InstitutionCategory> allCategories = new ArrayList<>();
        List<InstitutionCategoryModel> allVOCategories = creator.getCategories();
        allVOCategories.parallelStream().forEach(voCategory ->{
            InstitutionCategory category = new InstitutionCategory();
            category.setName(voCategory.getName());
            if(institutionType.equals(InstitutionType.GENERAL)) {
                category.setInstitutionId(creator.getInstitutionId());
            }
            allCategories.add(category);
        });
        List<InstitutionCategory> savedInstitutionCategory =
                this.appRepository.getCategoryRepository().saveAll(allCategories);

        final Institution theInstitution = institution;
        CompletableFuture.runAsync(()->{
            List<String> categoriesNames = savedInstitutionCategory
                    .stream().map(InstitutionCategory::getName).collect(Collectors.toList());
            Notification notification = Notification.createNotification(
                    loggedInUser,
                    theInstitution==null ? "Church Type(s) created" : "Category(ies) Creation",
                    String.join(", ", categoriesNames),
                    NotificationActionType.CREATE,
                    theInstitution==null ? null : theInstitution.getId()
            );
            this.appRepository.getNotificationsRepository().save(notification);
        });
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Saved.")
                .objects(savedInstitutionCategory)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update")
    @Operation(summary = "Update Category", responses = {
            @ApiResponse(description = "Successful Update",
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiWorkFeedback.class))),
            @ApiResponse(responseCode = "417", description = "An error occurred due to some restrictions not met.")
    })
    public ApiWorkFeedback update(@RequestBody @Valid InstitutionCategory institutionCategory,
                                  @NonNull HttpSession session){
        InstitutionCategory savedCategory = this.appRepository
                .getCategoryRepository()
                .findById(institutionCategory.getId())
                .orElse(null);

        if(savedCategory==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The Category you are trying to update does not exist.");
        }
        savedCategory.setName(institutionCategory.getName());
        savedCategory = this.appRepository.getCategoryRepository().save(savedCategory);

        List<InstitutionCategory> categoriesList = Collections.singletonList(savedCategory);
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Update.")
                .objects(categoriesList)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get")
    @Operation(summary = "Get all the categories", responses = {
            @ApiResponse(description = "List of Categories returned",
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "417", description = "An error occurred due to some restrictions not met.")
    })
    public List<InstitutionCategory> getAllCategory(@RequestBody @Valid  SimpleGetModel model,
                                                    @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.setWithInstitution(false);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);

        if(model.getInstitutionId()!=null){
            // Aha turi kuri general member
            Institution institution = this.appRepository.getInstitutionRepository()
                    .findById(model.getInstitutionId())
                    .orElseThrow(() -> {
                        // Return an error
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Failed to load Institution's data.");
                    });

            return this.appRepository.getCategoryRepository()
                    .findAllByInstitutionId(model.getInstitutionId());
        }

        return this.appRepository.getCategoryRepository()
                .findAllByInstitutionId(null);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete category by Id", responses = {
            @ApiResponse(description = "Successful Deletion",
                    responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiWorkFeedback.class))),
            @ApiResponse(responseCode = "417", description = "An error occurred due to some restrictions not met.")
    })
    public ApiWorkFeedback deleteCategory(@PathVariable("id") Long id){
        long institutions = this.appRepository
                .getInstitutionRepository()
                .countAllByCategoryId(id);
        if(institutions>0){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You can not delete this category.");
        }
        Long membersUsingCategory = this.appRepository
                .getInstitutionMemberRepository()
                .countWhereCategoriesUsed(id.toString());
        membersUsingCategory = (membersUsingCategory==null) ? 0 : membersUsingCategory;

        long institutionsUsingCategories = this.appRepository
                .getInstitutionRepository()
                .countAllByCategoryIdIs(id);

        long receipts = this.appRepository
                .getInstitutionDonationRepository()
                .countAllByCategoryIdIs(id);

        if(membersUsingCategory>0  || institutionsUsingCategories>0 || receipts>0) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "You can not delete this category as it used in some places");
        }
        this.appRepository.getCategoryRepository().deleteById(id);
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted")
                .build();
    }
}
