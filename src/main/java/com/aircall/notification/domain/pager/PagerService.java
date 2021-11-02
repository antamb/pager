package com.aircall.notification.domain.pager;

import com.aircall.notification.domain.pager.exception.DomainEntityNotFoundException;
import com.aircall.notification.domain.pager.exception.InvalidEscalationPolicyLevelException;
import com.aircall.notification.domain.pager.model.*;
import com.aircall.notification.domain.pager.ports.incoming.AckAlertUseCase;
import com.aircall.notification.domain.pager.ports.incoming.AckTimeoutUseCase;
import com.aircall.notification.domain.pager.ports.incoming.AlertUseCase;
import com.aircall.notification.domain.pager.ports.incoming.ServiceHealthyUseCase;
import com.aircall.notification.domain.pager.ports.incoming.command.AckCommand;
import com.aircall.notification.domain.pager.ports.incoming.command.AlertCommand;
import com.aircall.notification.domain.pager.ports.outcoming.EscalationPolicyPort;
import com.aircall.notification.domain.pager.ports.outcoming.ExternalTimerPort;
import com.aircall.notification.domain.pager.ports.outcoming.NotificationPort;
import com.aircall.notification.domain.pager.ports.outcoming.PagerRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@AllArgsConstructor
public class PagerService implements AlertUseCase, AckAlertUseCase, AckTimeoutUseCase, ServiceHealthyUseCase {

    private final int TIMEOUT = 15;

    private final PagerRepository repository;
    private final NotificationPort notificationPort;
    private final ExternalTimerPort externalTimerPort;
    private final EscalationPolicyPort escalationPolicyPort;

    /**
     * Acknowledge an existing Alert
     *
     * @param command
     * @return Alert
     */
    @SneakyThrows
    @Override
    public Alert ackAlert(AckCommand command) {
        if (!repository.alertExists(command.getAlertId())) {
            throw new DomainEntityNotFoundException("Alert with id " + command.getAlertId() + " not found");
        }
        return repository.acknowledgeAlert(command.getAlertId());
    }

    /**
     * Handle acknowledgement timeout related to an alert
     *
     * @param command
     */
    @SneakyThrows
    @Override
    public void handleAckTimeout(AckCommand command) {
        Alert alert = repository.findAlertById(command.getAlertId())
                .orElseThrow(() -> new DomainEntityNotFoundException("Alert with id " + command.getAlertId() + " not found"));
        MonitoredService monitoredService = repository.findMonitoredServiceByExternalId(alert.getServiceId())
                .orElseThrow(() -> new DomainEntityNotFoundException("Monitored service with id " + alert.getServiceId() + " not found"));
        if (!alert.isAck() && monitoredService.getState() == MonitoredService.STATE.UNHEALTHY) {
            Level level = escalate(alert);
            sendNotificationsAndSetTimer(level, alert);
        }
    }

    /**
     * Handle an alert
     *
     * @param command
     */
    @Override
    @SneakyThrows
    public void handleAlert(AlertCommand command) {
        String serviceId = command.getServiceId();
        MonitoredService existingMonitoredService =
                repository.findMonitoredServiceByExternalId(serviceId).orElse(null);
        // Monitored service exist in domain pager and is in an UNHEALTHY state
        if (existingMonitoredService != null
                && existingMonitoredService.getState() == MonitoredService.STATE.UNHEALTHY) {
            return;
        }

        MonitoredService monitoredService = repository.saveMonitoredService(
                MonitoredService.builder()
                        .externalId(serviceId)
                        .state(MonitoredService.STATE.UNHEALTHY)
                        .build());
        // Find escalation policy corresponding to the monitored service
        EscalationPolicy escalationPolicy = escalationPolicyPort.loadEscalationPolicy(serviceId)
                .orElseThrow(() ->
                        new DomainEntityNotFoundException("Escalation policy not found for service id: " + serviceId));

        Alert alert = repository.saveAlert(Alert.builder().serviceId(monitoredService.getExternalId())
                .message(command.getMessage()).ack(false).build());

        // Notify all targets and set external timer
        sendNotificationsAndSetTimer(escalationPolicy.getLevel(alert.getCurrentLevel()), alert);
    }

    /**
     * Flag a monitored service as healthy
     *
     * @param serviceId
     * @return MonitoredService
     */
    @SneakyThrows
    @Override
    public MonitoredService flagServiceHealthy(String serviceId) {
        MonitoredService monitoredService = repository.findMonitoredServiceByExternalId(serviceId)
                .orElseThrow(() ->
                        new DomainEntityNotFoundException("Monitored service with external id " + serviceId + " not found"));
        return repository.updateMonitoredServiceState(monitoredService.getId(), MonitoredService.STATE.HEALTHY);
    }

    /**
     * Escalate an alert to the next escalation policy level
     *
     * @param alert
     * @return Level
     */
    @SneakyThrows
    private Level escalate(Alert alert) {
        String serviceId = alert.getServiceId();
        // load escalation policy as it could have changed
        EscalationPolicy escalationPolicy = escalationPolicyPort.loadEscalationPolicy(serviceId)
                .orElseThrow(() ->
                        new DomainEntityNotFoundException("Escalation policy not found for service id: " + serviceId));
        // escalate alert to the next level
        int currentLevel = alert.escalate();
        if (currentLevel >= escalationPolicy.getLevels().size()) {
            throw new InvalidEscalationPolicyLevelException("Escalation forbidden due to invalid escalation level");
        }
        repository.saveAlert(alert);
        return escalationPolicy.getLevel(currentLevel);
    }

    private void sendNotificationsAndSetTimer(Level level, Alert alert) {
        List<Target> targets = level.getTargets();
        notificationPort.notify(targets, alert);
        externalTimerPort.setAckTimout(TIMEOUT, alert);
    }
}
