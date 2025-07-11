package com.dprince.security.user;

import com.dprince.entities.LoginDetail;
import com.dprince.entities.LoginToken;
import com.dprince.entities.User;
import com.dprince.entities.utils.AppRepository;
import com.dprince.security.jwt.JwtTokenUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.LoginException;
import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Data
@Component
@SessionScope
@NoArgsConstructor
public class UserSession {
    private static final long serialVersionUID = 1234567890L;
    /**
     * This token is going tobe sent with every request that users will make
     */
    private static final List<String> DUMMY_USERS = Arrays.asList("crunc2beat@gmail.com");
    public void checkIsDeveloper(String username){
        if(DUMMY_USERS.contains(username.toLowerCase())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No account with such credentials found.");
        }
    }
    public UserSession(String token, User user, boolean rememberUser) {
        this.token = token;
        this.user = user;
        this.rememberUser = rememberUser;
    }

    private String token;
    private User user;

    private boolean rememberUser = false;

    @Autowired
    @JsonIgnore
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    @JsonIgnore
    private UserDetailsService userDetailsService;

    @Autowired
    @JsonIgnore
    private AppRepository repository;

    @Autowired
    @JsonIgnore
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    @JsonIgnore
    private AuthenticationManager authenticationManager;

    @Autowired
    private User savedUser;

    public UserSession login(String username, String password, Boolean rememberMe) throws LoginException, BadRequestException {
        String token = createAuthenticationToken(username, password);
        savedUser.setUsername(username);
        User attemptingToLogUser = repository.getUsersRepository().findByUsername(username);
        this.setUser(attemptingToLogUser);
        CompletableFuture.runAsync(() -> {
            try {
                insertIntoLoginDetails(user, token, rememberMe);
            } catch (Exception ignored) {}
        });
        this.repository.getLoginTokensRepository().save(new LoginToken(token));
        return new UserSession(token, user, this.isRememberUser());
    }

    public UserSession login(String username) throws BadRequestException {
        User attemptingToLogUser = repository.getUsersRepository().findByUsername(username);
        if(attemptingToLogUser==null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No account with such phone number.");
        }
        this.setUser(attemptingToLogUser);
        String token = createAuthenticationToken(attemptingToLogUser.getUsername());
        CompletableFuture.runAsync(() -> {
            insertIntoLoginDetails(user, token, true);
        });
        this.repository.getLoginTokensRepository().save(new LoginToken(token));
        return new UserSession(token, user, this.isRememberUser());
    }

    public String createAuthenticationToken(String username, String password) throws LoginException {
        authenticate(username, password);
        final UserDetails userDetails = repository.getUsersRepository().findByUsername(username);
        return jwtTokenUtil.generateToken(userDetails);
    }

    public String createAuthenticationToken(String username) {
        final UserDetails userDetails = repository.getUsersRepository().findByUsername(username);
        return jwtTokenUtil.generateToken(userDetails);
    }

    private void authenticate(String username, String password) throws LoginException {
        try {
            Authentication obj = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (InternalAuthenticationServiceException e) {
            this.checkIsDeveloper(username);
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Wrong credentials.");
        } catch (DisabledException e) {
            this.checkIsDeveloper(username);
            throw new LoginException("USER_DISABLED" + e);
        } catch (BadCredentialsException e) {
            this.checkIsDeveloper(username);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authentication failed.");
        }
    }

    public Boolean logout(User user, String token){
        Long userId = user.getId();
        Optional<LoginDetail> optional = this.repository.getLoginDetailsRepository()
                .findByTokenAndUserId(token, userId);
        if (optional.isPresent()) {
            try {
                // Logout by deleting the login details
                this.repository.getLoginDetailsRepository().deleteById(optional.get().getId());
            } catch (StaleObjectStateException ignored){}
            return true;
        }
        return false;
    }

    private void insertIntoLoginDetails(User user, String token, Boolean rememberMe){
        LoginDetail loginDetail = new LoginDetail();
        loginDetail.setUserId(user.getId());
        loginDetail.setToken(token);
        loginDetail.setValid(true);
        loginDetail.setRememberMe(rememberMe);
        loginDetail.setCreatedDate(new Date(System.currentTimeMillis()));
        loginDetail.setLastActivityTime(new Date(System.currentTimeMillis()));
        this.repository.getLoginDetailsRepository().save(loginDetail);
    }
}
