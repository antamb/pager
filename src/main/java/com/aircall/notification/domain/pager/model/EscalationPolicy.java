package com.aircall.notification.domain.pager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EscalationPolicy {
    private String id;
    private List<Level> levels;
    private String monitoredServiceId;

    public Level getLevel(int index) {
        return levels.get(index);
    }
}
