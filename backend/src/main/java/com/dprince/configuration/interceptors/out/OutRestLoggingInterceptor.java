package com.dprince.configuration.interceptors.out;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;


@Service
public class OutRestLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LogManager.getLogger(OutRestLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequestDetails(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponseDetails(response);
        return response;
    }

    private void logRequestDetails(HttpRequest request, byte[] body) {
        logger.info("URI: {}", request.getURI());
        logger.info("Method: {}", request.getMethod());
        logger.info("Headers: {}", request.getHeaders());
        logger.info("Request Body: {}", new String(body));
    }

    private void logResponseDetails(ClientHttpResponse response) throws IOException {
        logger.info("Response Status Code: {}", response.getStatusCode());
        logger.info("Response Headers: {}", response.getHeaders());
    }
}