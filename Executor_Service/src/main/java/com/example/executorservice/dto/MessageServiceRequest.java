package com.example.executorservice.dto;

public class MessageServiceRequest {
    private String content;
    public MessageServiceRequest() {}

    public MessageServiceRequest(String content) {
        this.content = content;
    }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

