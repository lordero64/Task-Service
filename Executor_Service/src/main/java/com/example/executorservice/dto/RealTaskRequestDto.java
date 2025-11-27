package com.example.executorservice.dto;

public class RealTaskRequestDto extends TaskRequestDto {
    private String data;
    private int retryCount = 3;

    public RealTaskRequestDto() {
        setType("REAL");
    }

    public RealTaskRequestDto(String data) {
        this.data = data;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}