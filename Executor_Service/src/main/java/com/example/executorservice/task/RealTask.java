package com.example.executorservice.task;

import com.example.executorservice.dto.MessageServiceRequest;
import com.example.executorservice.dto.RealTaskRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class RealTask implements Task {
    private static final Logger logger = LoggerFactory.getLogger(RealTask.class);
    private final RealTaskRequestDto request;
    private final RestTemplate restTemplate;
    private final String targetServiceUrl;

    public RealTask(RealTaskRequestDto request, RestTemplate restTemplate, String targetServiceUrl) {
        this.request = request;
        this.restTemplate = restTemplate;
        this.targetServiceUrl = targetServiceUrl;
    }

    @Override
    public void execute() {
        int attempt = 0;
        boolean success = false;

        while (attempt < request.getRetryCount() && !success) {
            attempt++;
            try {
                logger.info("Executing REAL task ID: {}, Attempt: {}, Target URL: {}", request.getTaskId(), attempt,
                        targetServiceUrl);

                // Подготовка запроса к message-service
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // Создаем DTO для message-service
                MessageServiceRequest messageRequest = new MessageServiceRequest(request.getData());

                HttpEntity<MessageServiceRequest> entity = new HttpEntity<>(messageRequest, headers);

                // Отправка POST запроса в message-service
                ResponseEntity<String> response = restTemplate.exchange(
                        targetServiceUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("REAL task ID: {} completed successfully. Response: {}",
                            request.getTaskId(), response.getBody());
                    success = true;
                } else {
                    logger.warn("REAL task ID: {} failed with status: {}",
                            request.getTaskId(), response.getStatusCode());
                }

            } catch (Exception e) {
                logger.error("Error executing REAL task ID: {} on attempt {}",
                        request.getTaskId(), attempt, e);

                if (attempt < request.getRetryCount()) {
                    try {
                        Thread.sleep(2000 * attempt); // Увеличивающаяся задержка
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            logger.error("REAL task ID: {} failed after {} attempts",
                    request.getTaskId(), request.getRetryCount());
        }
    }

    @Override
    public String getTaskId() {
        return request.getTaskId();
    }

    @Override
    public int getPriority() {
        return 2; // Real tasks have higher priority than dummy tasks
    }
}