package com.aircall.notification.domain;

import com.aircall.notification.domain.pager.PagerService;
import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.model.EscalationPolicy;
import com.aircall.notification.domain.pager.model.MonitoredService;
import com.aircall.notification.domain.pager.model.Target;
import com.aircall.notification.domain.pager.ports.incoming.command.AckCommand;
import com.aircall.notification.domain.pager.ports.outcoming.EscalationPolicyPort;
import com.aircall.notification.domain.pager.ports.outcoming.ExternalTimerPort;
import com.aircall.notification.domain.pager.ports.outcoming.NotificationPort;
import com.aircall.notification.domain.pager.ports.outcoming.PagerRepository;
import com.aircall.notification.domain.utils.EmailTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static com.aircall.notification.domain.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ReceiveAckTimeout {

    private PagerService pagerService;
    @Mock
    private PagerRepository repository;
    @Mock
    private NotificationPort notificationPort;
    @Mock
    private ExternalTimerPort externalTimerPort;
    @Mock
    private EscalationPolicyPort escalationPolicyPort;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;
    @Captor
    private ArgumentCaptor<List<Target>> targetCaptor;
    @Captor
    private ArgumentCaptor<MonitoredService> monitoredServiceCaptor;

    private AckCommand command;
    private EscalationPolicy escalationPolicy;

    @BeforeEach
    void beforeEach() {
        pagerService = new PagerService(repository, notificationPort,
                externalTimerPort, escalationPolicyPort);

        command = givenAckCommand();
        escalationPolicy = givenEscalationPolicy();
    }

    @Test
    void GivenUnhealthyService_WhenReceiveAckTimeout_ThenNotifyAll() {
        // Given
        // Alert is not acknowledge
        Alert alert = givenAlert(false, 1);
        MonitoredService monitoredService = givenMonitoredService(MonitoredService.STATE.UNHEALTHY);

        given(repository.findAlertById(alert.getId())).willReturn(Optional.of(alert));
        given(repository.findMonitoredServiceByExternalId(alert.getServiceId())).willReturn(Optional.of(monitoredService));
        given(escalationPolicyPort.loadEscalationPolicy(monitoredServiceId)).willReturn(Optional.of(escalationPolicy));

        // When
        // receive ack timeout
        pagerService.handleAckTimeout(command);

        // Then
        then(notificationPort).should().notify(targetCaptor.capture(), alertCaptor.capture());
        then(externalTimerPort).should().setAckTimout(eq(15), eq(alert));

        Alert actualAlert = alertCaptor.getValue();
        Target actualTarget = targetCaptor.getValue().get(0);
        Target expectedTarget = escalationPolicy.getLevel(2).getTargets().get(0);
        assertAll(
                () -> assertEquals(actualAlert.getId(), alert.getId()),
                // Make sure the next escalation policy level is called
                () -> assertEquals(((EmailTarget) expectedTarget).getEmail(),
                        ((EmailTarget) actualTarget).getEmail())
        );
    }

    @Test
    void GivenUnhealthyService_WhenReceiveAckAndAckTimeout_ThenDoNotNotify() {
        // Given
        Alert alertNotAck = givenAlert(false, 1);
        Alert alertAck = givenAlert(true, 1);
        MonitoredService monitoredService = givenMonitoredService(MonitoredService.STATE.UNHEALTHY);
        given(repository.acknowledgeAlert(alertNotAck.getId())).willReturn(alertAck);
        given(repository.findAlertById(eq(alertAck.getId()))).willReturn(Optional.of(alertAck));
        given(repository.findMonitoredServiceByExternalId(monitoredService.getExternalId()))
                .willReturn(Optional.of(monitoredService));
        given(repository.alertExists(alertNotAck.getId())).willReturn(true);

        // When
        // acknowledge alert
        Alert actualAlert = pagerService.ackAlert(command);
        pagerService.handleAckTimeout(command);

        // Then
        // notifications should not be sent to targets and timer should not be set
        assertTrue(actualAlert.isAck());
        then(notificationPort).should(never()).notify(anyList(), any(Alert.class));
        then(externalTimerPort).should(never()).setAckTimout(eq(15), eq(alertAck));
    }

    @Test
    void GivenUnhealthyService_WhenReceiveHealthyAndAckTimeout_ThenDoNotNotify() {
        // Given
        Alert alert = givenAlert(false, 1);
        MonitoredService healthyService = givenMonitoredService(MonitoredService.STATE.HEALTHY);
        MonitoredService unhealthyService = givenMonitoredService(MonitoredService.STATE.HEALTHY);

        given(repository.findMonitoredServiceByExternalId(monitoredServiceId))
                .willReturn(Optional.of(unhealthyService));
        given(repository.updateMonitoredServiceState(healthyService.getId(),
                MonitoredService.STATE.HEALTHY)).willReturn(healthyService);
        given(repository.findAlertById(eq(alert.getId()))).willReturn(Optional.of(alert));
        given(repository.findMonitoredServiceByExternalId(healthyService.getExternalId()))
                .willReturn(Optional.of(healthyService));

        // When
        // receive healthy event
        MonitoredService actualService = pagerService.flagServiceHealthy(unhealthyService.getExternalId());
        pagerService.handleAckTimeout(command);

        // Then
        // notifications should not be sent to targets and timer should not be set
        assertEquals(actualService.getState(), MonitoredService.STATE.HEALTHY);
        then(notificationPort).should(never()).notify(anyList(), any(Alert.class));
        then(externalTimerPort).should(never()).setAckTimout(eq(15), eq(alert));
    }
}
