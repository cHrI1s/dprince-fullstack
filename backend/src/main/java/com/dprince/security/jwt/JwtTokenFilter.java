package com.dprince.security.jwt;

import com.dprince.security.OmitAuthList;
import com.dprince.security.constants.SecurityConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * * @author Chris Ndayishimiye
 * * @created 5/22/2021
 */
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private Environment environment;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.HEADER_STRING);
        String username = null,
                authToken = null;
        // LOGGER.error("requested URI "+ request.getRequestURI());

        List<String> antMatchedUrls = OmitAuthList.getOmitList();
        List<String> devAntMatchedUrls = Arrays.asList(environment.getActiveProfiles());
        devAntMatchedUrls
                .parallelStream()
                .forEach(singleActiveProfile->{
            if(singleActiveProfile.equalsIgnoreCase("dev")) antMatchedUrls.addAll(OmitAuthList.getDevOmitList());
        });

        boolean matchesOmitList = OmitAuthList.isUrlMatched(request.getRequestURI(), antMatchedUrls);
        if(!matchesOmitList) {
            if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                authToken = header.replace(SecurityConstants.TOKEN_PREFIX, "");
                try {
                    username = jwtTokenUtil.getUsernameFromToken(authToken);
                } catch (IllegalArgumentException e) {
                    // LOGGER.error("An error occurred during getting username from token", e);
                } catch (ExpiredJwtException e) {
                    // LOGGER.error("The token is expired and not valid anymore", e);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Your session has been terminated. Please login to continue.");
                } catch (SignatureException e) {
                    // LOGGER.error("Authentication Failed. Username or Password not valid.");
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Authentication failed. Username or password is invalid");
                }
            } else {
                // LOGGER.error("Couldn't find bearer string, will ignore the header");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication failed.");
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // LOGGER.info("Authenticated user " + username + ", setting security context");
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }

}