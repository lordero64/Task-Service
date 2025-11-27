package com.example.executorservice.task;

import com.example.executorservice.dto.DummyTaskRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyTask implements Task {
    private static final Logger logger = LoggerFactory.getLogger(DummyTask.class);
    private final DummyTaskRequestDto request;

    public DummyTask(DummyTaskRequestDto request) {
        this.request = request;
    }

    @Override
    public void execute() {
        try {
            logger.info("Executing DUMMY task ID: {}, Message: {}", request.getTaskId(), request.getMessage());

            // Имитация обработки
            Thread.sleep(1000);

            logger.info("DUMMY task ID: {} completed successfully", request.getTaskId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("DUMMY task ID: {} was interrupted", request.getTaskId());
        } catch (Exception e) {
            logger.error("Error executing DUMMY task ID: {}", request.getTaskId(), e);
        }
    }

    @Override
    public String getTaskId() {
        return request.getTaskId();
    }

    @Override
    public int getPriority() {
        return request.getPriority();
    }
}