package com.aircall.notification.domain.pager.ports.outcoming;

import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.model.MonitoredService;

import java.util.Optional;

public interface PagerRepository {
   Alert saveAlert(Alert alert);
   Alert acknowledgeAlert(String alertId);
   boolean alertExists(String alertId);
   Optional<Alert> findAlertById(String serviceId);

   MonitoredService saveMonitoredService(MonitoredService service);
   Optional<MonitoredService> findMonitoredServiceByExternalId(String serviceId);
   MonitoredService updateMonitoredServiceState(String serviceId, MonitoredService.STATE state);
}

