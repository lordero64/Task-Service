package com.example.executorservice.dto;

public class DummyTaskRequestDto extends TaskRequestDto {
    private String message;
    private int priority = 1;

    public DummyTaskRequestDto() {
        setType("DUMMY");
    }

    public DummyTaskRequestDto(String message) {
        this();
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}