package com.aircall.notification.domain.pager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MonitoredService {

    public enum STATE {
        HEALTHY,
        UNHEALTHY,
    }

    private String id;
    private STATE state;
    private String externalId;
}
