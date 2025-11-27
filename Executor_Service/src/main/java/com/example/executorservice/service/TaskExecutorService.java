package com.example.executorservice.service;

import com.example.executorservice.dto.*;
import com.example.executorservice.task.DummyTask;
import com.example.executorservice.task.RealTask;
import com.example.executorservice.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
public class TaskExecutorService {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorService.class);

    @Value("${app.executor.thread-pool.size:10}")
    private int threadPoolSize;

    @Value("${app.executor.queue.capacity:1000}")
    private int queueCapacity;

    @Value("${app.executor.target.service.url:http://localhost:8080/messages }")
    private String targetServiceUrl;

    private ExecutorService executorService;
    private PriorityBlockingQueue<Task> taskQueue;
    private volatile boolean running = true;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        logger.info("Initializing TaskExecutorService with target URL: {}", targetServiceUrl);

        // Создаем очередь с приоритетом
        taskQueue = new PriorityBlockingQueue<>(queueCapacity,
                (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));

        // Создаем FixedThreadPool
        executorService = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "task-executor-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        });

        // Запускаем обработчики очереди
        startQueueConsumers();

        logger.info("TaskExecutorService initialized with {} threads and queue capacity {}",
                threadPoolSize, queueCapacity);
    }

    private void startQueueConsumers() {
        for (int i = 0; i < threadPoolSize; i++) {
            executorService.submit(this::processTasks);
        }
    }

    private void processTasks() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.take();
                logger.debug("Processing task ID: {} in thread: {}",
                        task.getTaskId(), Thread.currentThread().getName());

                task.execute();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Task processor thread interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error in task processor thread", e);
            }
        }
    }

    /**
     * Добавление задачи в очередь
     */
    public boolean submitTask(TaskRequestDto taskRequestDto) {
        try {
            Task task = createTaskFromRequest(taskRequestDto);

            if (taskQueue.size() >= queueCapacity) {
                logger.warn("Task queue is full. Rejecting task ID: {}", taskRequestDto.getTaskId());
                return false;
            }

            boolean offered = taskQueue.offer(task);
            if (offered) {
                logger.info("Task ID: {} submitted to queue. Queue size: {}/{}",
                        taskRequestDto.getTaskId(), taskQueue.size(), queueCapacity);
            } else {
                logger.warn("Failed to submit task ID: {} to queue", taskRequestDto.getTaskId());
            }

            return offered;

        } catch (Exception e) {
            logger.error("Error submitting task ID: {}", taskRequestDto.getTaskId(), e);
            return false;
        }
    }

    private Task createTaskFromRequest(TaskRequestDto taskRequestDto) {
        if (taskRequestDto instanceof DummyTaskRequestDto) {
            return new DummyTask((DummyTaskRequestDto) taskRequestDto);
        } else if (taskRequestDto instanceof RealTaskRequestDto) {
            return new RealTask((RealTaskRequestDto) taskRequestDto, restTemplate, targetServiceUrl);
        } else {
            throw new IllegalArgumentException("Unknown task type: " + taskRequestDto.getType());
        }
    }

    /**
     * Получение статистики
     */
    public ExecutorStats getStats() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
        return new ExecutorStats(
                taskQueue.size(),
                queueCapacity,
                threadPoolSize,
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getCompletedTaskCount(),
                threadPoolExecutor.getTaskCount()
        );
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down TaskExecutorService...");
        running = false;

        executorService.shutdownNow();

        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Executor service did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Shutdown was interrupted");
        }

        logger.info("TaskExecutorService shutdown completed");
    }
}
