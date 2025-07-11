package com.dprince.security.jwt;

import com.dprince.security.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.function.Function;


/**
 * * @author Chris Ndayishimiye
 * * @project iblink
 * * @created 5/22/2021
 */
@Component
public class JwtTokenUtil implements Serializable {

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return (claims==null) ? null : claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(SecurityConstants.SIGNING_KEY).parseClaimsJws(token).getBody();
        } catch(JwtException e){
            return null;
        }
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        return doGenerateToken(userDetails.getUsername());
    }

    private String doGenerateToken(String subject) {

        Claims claims = Jwts.claims().setSubject(subject);
        claims.put("scopes", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        Date now = new Date();
        claims.setNotBefore(now);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(SecurityConstants.TOKEN_ISSUER)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.ACCESS_TOKEN_VALIDITY_SECONDS*1000))
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.SIGNING_KEY)
                .setId(UUID.randomUUID().toString()).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        if(StringUtils.isEmpty(username)) return false;
        return (
                username.equals(userDetails.getUsername())
                        && !isTokenExpired(token));
    }

    /**
     * Returns the bearer token from the request made by the user
     * @param request: <p>Is a HttpServletRequest that is used to get bearertoken from</p>
     * @return String <p>A bearer token</p>
     */
    public static String getBearerToken(HttpServletRequest request){
        String token = null;
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement(),
                    requestHeader = request.getHeader(headerName);
            // LOGGER.info(String.format("Header Name and Value %s:%s", headerName, requestHeader));
            if (requestHeader.toLowerCase().startsWith("bearer")) {
                token = requestHeader.substring(7);
                // LOGGER.error("The bearer token is "+ token);
                break;
            }
        }
        return token;
    }

}