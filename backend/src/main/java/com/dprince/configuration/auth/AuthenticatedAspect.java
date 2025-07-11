package com.dprince.configuration.auth;

import com.dprince.apis.utils.NetworkUtils;
import com.dprince.entities.Institution;
import com.dprince.entities.LoginToken;
import com.dprince.entities.User;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.security.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * * @author Chris Ndayishimiye
 * * @created 11/13/23
 */
@Slf4j
@Aspect
@Component
public class AuthenticatedAspect {
    private final AppRepository appRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final User userService;

    @Autowired
    public AuthenticatedAspect(AppRepository appRepository,
                               JwtTokenUtil jwtTokenUtil,
                               User userService){
        this.appRepository = appRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    private static AppRepository APP_REPOSITORY;
    private static JwtTokenUtil JWT_TOKEN_UTIL;
    private static User USER_SERVICE;


    @PostConstruct
    public void init() {
        APP_REPOSITORY = this.appRepository;
        JWT_TOKEN_UTIL = this.jwtTokenUtil;
        USER_SERVICE = this.userService;
        USER_SERVICE.setRepository(APP_REPOSITORY);
    }

    @Before("@annotation(com.dprince.configuration.auth.Authenticated)")
    public static void before(JoinPoint jointPoint){
        boolean authAllowUser = false;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null) return;

        MethodSignature signature = (MethodSignature) jointPoint.getSignature();
        Method method = signature.getMethod();
        Authenticated loggedInAnnotation = method.getAnnotation(Authenticated.class);

        HttpServletRequest httprequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        HttpServletResponse httpresponse = ((ServletRequestAttributes) requestAttributes).getResponse();
        HttpSession session = httprequest.getSession();
        //Session


        // get username from token
        String bearerToken = JwtTokenUtil.getBearerToken(httprequest);
        if(bearerToken!=null) {
            String username = JWT_TOKEN_UTIL.getUsernameFromToken(bearerToken);
            USER_SERVICE.loadClientFromRequest(username, bearerToken, httprequest);
            String otherUsername;


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    otherUsername = ((UserDetails) principal).getUsername();
                    if (JWT_TOKEN_UTIL.validateToken(bearerToken, (UserDetails) principal)) {
                        //verify the token is in use;
                        User loggedInUser = APP_REPOSITORY.getUsersRepository().findByUsername(otherUsername);
                        if (loggedInUser != null) {
                            session.setAttribute("loggedInUser",
                                    loggedInUser);

                            // Check the user has the right to perform the operation;
                            UserType[] allowedUserTypes = loggedInAnnotation.userTypes();
                            if (allowedUserTypes.length > 0) {
                                List<UserType> userTypeList = Arrays.asList(allowedUserTypes);
                                if (!userTypeList.contains(loggedInUser.getUserType())) {
                                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                            "You are not authorized to perform this operation.");
                                }
                            }

                            if(loggedInUser.getInstitutionId()!=null){
                                Institution institution = APP_REPOSITORY.getInstitutionRepository()
                                        .findById(loggedInUser.getInstitutionId())
                                        .orElse(null);
                                if(institution==null) {
                                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                            "Institution not recognized!");
                                } else {
                                    String institutionIpAddress = institution.getIpAddress();
                                    if(!StringUtils.isEmpty(institutionIpAddress)){
                                        String requestIpAddress = NetworkUtils.getUserIp(httprequest);
                                        if(!institutionIpAddress.equalsIgnoreCase(requestIpAddress)){
                                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                    "You are not allowed to perform any operation if not from your institution premises.");
                                        }
                                    }
                                }
                            }
                            // This means the user is logged in
                            Optional<LoginToken> savedToken = APP_REPOSITORY.getLoginTokensRepository().findFirstByJwtTokenIs(bearerToken);
                            if (savedToken.isPresent()) {
                                authAllowUser = true;
                            }
                        }
                    }
                }
            }
        }

        if (!authAllowUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not recognized by the system. Your session may have ended. Please try to login.");
        }
    }
}
