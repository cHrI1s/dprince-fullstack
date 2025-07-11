package com.dprince.configuration.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;

@Service
public class LoggingInterceptor implements HandlerInterceptor {

	@Autowired
    private LoggingService loggingService;

	@Autowired
	private ApplicationContext appContext;

	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response,
							 Object handler) throws Exception {
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);
		logHeader(request);
		logParameter(request);
		loggingService.logRequest(request, null);
		return true;
	}

	private void saveAppRequest(@NotNull HttpServletRequest request){

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// Then do what is needed
		CompletableFuture.runAsync(()-> this.saveAppRequest(request));
	}

	@Override
	public void afterCompletion(@NotNull HttpServletRequest request,
								HttpServletResponse response,
								Object handler,
								Exception exception){
		/*
		if(request.getAttribute("startTime")!=null) {
			long startTime = (Long) request.getAttribute("startTime");
			long endTime = System.currentTimeMillis();
		}
		 */
	}

	private void logParameter(HttpServletRequest request) {
		/*
		Enumeration<String> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = enumeration.nextElement();
			LOGGER.info(String.format("Parameter Name and Value %s:%s", paramName, request.getParameter(paramName)));
		}
		 */
	}

	private void logHeader(HttpServletRequest request) throws IOException {
		Enumeration<String> enumeration = request.getHeaderNames();
		while (enumeration.hasMoreElements()) {
			String headerName = enumeration.nextElement(),
					requestHeader = request.getHeader(headerName);
			if(requestHeader.toLowerCase().startsWith("bearer")){
				// Perform some actions
				// If the bearer token is known
				if(requestHeader.length()>7) {
					String token = requestHeader.substring(7);
					BearerTokenWrapper bearerTokenWrapper = (BearerTokenWrapper) appContext.getBean("bearerTokenWrapper");
					bearerTokenWrapper.setToken(token);
				}
			}
		}
	}
}