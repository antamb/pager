package com.aircall.notification.domain.pager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Alert {

    public Alert(String id, String message, String serviceId, Instant createdAt) {
        this.id = id;
        this.ack = false;
        this.message = message;
        this.serviceId = serviceId;
        this.createdAt = createdAt;
    }

    public Alert(Alert alert) {

    }

    private String id;
    private boolean ack;
    private String message;
    private String serviceId;
    private int currentLevel;
    private Instant createdAt;

    public int escalate() {
        return ++currentLevel;
    }
}
