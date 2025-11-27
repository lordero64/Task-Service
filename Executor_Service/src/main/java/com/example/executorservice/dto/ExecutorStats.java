package com.example.executorservice.dto;

public class ExecutorStats {
    private final int queueSize;
    private final int queueCapacity;
    private final int poolSize;
    private final int activeThreads;
    private final long completedTasks;
    private final long totalTasks;

    public ExecutorStats(int queueSize, int queueCapacity, int poolSize, int activeThreads, long completedTasks,
                         long totalTasks) {
        this.queueSize = queueSize;
        this.queueCapacity = queueCapacity;
        this.poolSize = poolSize;
        this.activeThreads = activeThreads;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
    }

    public int getQueueSize() { return queueSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public int getPoolSize() { return poolSize; }
    public int getActiveThreads() { return activeThreads; }
    public long getCompletedTasks() { return completedTasks; }
    public long getTotalTasks() { return totalTasks; }
    public double getQueueUsagePercent() {
        return (double) queueSize / queueCapacity * 100;
    }
}

