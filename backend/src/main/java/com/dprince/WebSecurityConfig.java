package com.dprince;

import com.dprince.security.CustomAccessDeniedHandler;
import com.dprince.security.CustomUnAuthorizationHandler;
import com.dprince.security.OmitAuthList;
import com.dprince.security.jwt.JwtAuthenticationEntryPoint;
import com.dprince.security.jwt.JwtTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * * @author Chris Ndayishimiye
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);
    @Resource(name = "userService")
    private UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomUnAuthorizationHandler customUnAuthorizationHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final Environment environment;

    @Autowired
    WebSecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                      CustomUnAuthorizationHandler customUnAuthorizationHandler,
                      CustomAccessDeniedHandler customAccessDeniedHandler,
                      Environment environment){
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customUnAuthorizationHandler = customUnAuthorizationHandler;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.environment = environment;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean
    public JwtTokenFilter authenticationTokenFilterBean() throws Exception {
        return new JwtTokenFilter();
    }

    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        List<String> antMatchedUrls = OmitAuthList.getOmitList();
        List<String> devAntMatchedUrls = Arrays.asList(environment.getActiveProfiles());
        devAntMatchedUrls
                .parallelStream()
                .forEach(singleActiveProfile->{
            if(singleActiveProfile.equalsIgnoreCase("dev")) antMatchedUrls.addAll(OmitAuthList.getDevOmitList());
        });
        String[] antMatchingUrls = antMatchedUrls.toArray(new String[0]);
        // We don't need CSRF for this example
        httpSecurity.cors().and().csrf().disable()
                .authorizeRequests()
                    // dont authenticate this particular request
                    .antMatchers(antMatchingUrls).permitAll()
                // all other requests need to be authenticated
                .anyRequest().authenticated().and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
                        exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and().exceptionHandling()
                    .authenticationEntryPoint(customUnAuthorizationHandler)
                .and().exceptionHandling()
                    .accessDeniedHandler(customAccessDeniedHandler)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public BCryptPasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*"));
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.setAllowCredentials(true);

        // Useful for the websockets
        corsConfiguration.addAllowedHeader(HttpHeaders.CONTENT_TYPE);
        corsConfiguration.addAllowedHeader(HttpHeaders.ORIGIN);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCEPT);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCEPT_ENCODING);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        corsConfiguration.addAllowedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        corsConfiguration.addAllowedHeader(HttpHeaders.AUTHORIZATION);
        corsConfiguration.addAllowedHeader("X-Requested-With");

        corsConfiguration.addExposedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        corsConfiguration.addExposedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        corsConfiguration.addExposedHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        corsConfiguration.addExposedHeader(HttpHeaders.CONTENT_TYPE);

        corsConfiguration.addExposedHeader(HttpHeaders.ORIGIN);
        corsConfiguration.addExposedHeader(HttpHeaders.ACCEPT);


        corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.HEAD.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        corsConfiguration.setMaxAge(1800L);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}