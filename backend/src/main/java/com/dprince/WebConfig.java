package com.dprince;

import com.dprince.configuration.interceptors.BearerTokenWrapper;
import com.dprince.configuration.interceptors.LoggingInterceptor;
import com.dprince.configuration.interceptors.out.OutRestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@RequiredArgsConstructor
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	private final LoggingInterceptor loggingInterceptor;
	private final OutRestLoggingInterceptor restLoggingInterceptor;


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loggingInterceptor);
	}

	@Bean(name="bearerTokenWrapper")
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BearerTokenWrapper bearerTokenWrapper() {
		return new BearerTokenWrapper();
	}


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.additionalInterceptors(restLoggingInterceptor)
				.build();
	}
}