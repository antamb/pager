package com.aircall.notification.domain.pager.ports.incoming;

import com.aircall.notification.domain.pager.model.MonitoredService;

public interface ServiceHealthyUseCase {
    MonitoredService flagServiceHealthy(String serviceId);
}
