package com.example.executorservice.controller;

import com.example.executorservice.dto.ExecutorStats;
import com.example.executorservice.dto.TaskRequestDto;
import com.example.executorservice.dto.TaskResponseDto;
import com.example.executorservice.service.TaskExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/executor")
public class TaskExecutorController {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorController.class);

    @Autowired
    private TaskExecutorService taskExecutorService;

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponseDto> submitTask(@RequestBody TaskRequestDto taskRequestDto) {
        try {
            logger.info("Received task request: {}", taskRequestDto.getTaskId());

            boolean accepted = taskExecutorService.submitTask(taskRequestDto);

            if (accepted) {
                return ResponseEntity.accepted().body(
                        new TaskResponseDto("TASK_ACCEPTED",
                                "Task " + taskRequestDto.getTaskId() + " accepted for processing")
                );
            } else {
                return ResponseEntity.status(429).body(
                        new TaskResponseDto("QUEUE_FULL",
                                "Task queue is full. Please try again later")
                );
            }

        } catch (Exception e) {
            logger.error("Error processing task request: {}", taskRequestDto.getTaskId(), e);
            return ResponseEntity.badRequest().body(
                    new TaskResponseDto("ERROR",
                            "Failed to process task: " + e.getMessage())
            );
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ExecutorStats> getStats() {
        return ResponseEntity.ok(taskExecutorService.getStats());
    }

    @GetMapping("/health")
    public ResponseEntity<TaskResponseDto> health() {
        return ResponseEntity.ok(new TaskResponseDto("UP", "ExecutorService is running"));
    }
}