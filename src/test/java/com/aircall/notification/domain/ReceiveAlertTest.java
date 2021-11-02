package com.aircall.notification.domain;

import com.aircall.notification.domain.pager.PagerService;
import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.model.EscalationPolicy;
import com.aircall.notification.domain.pager.model.MonitoredService;
import com.aircall.notification.domain.pager.model.Target;
import com.aircall.notification.domain.pager.ports.incoming.command.AlertCommand;
import com.aircall.notification.domain.pager.ports.outcoming.EscalationPolicyPort;
import com.aircall.notification.domain.pager.ports.outcoming.ExternalTimerPort;
import com.aircall.notification.domain.pager.ports.outcoming.NotificationPort;
import com.aircall.notification.domain.pager.ports.outcoming.PagerRepository;
import com.aircall.notification.domain.utils.SmsTarget;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ReceiveAlertTest {

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

    private AlertCommand command;
    private EscalationPolicy escalationPolicy;

    @BeforeEach
    void beforeEach() {
        pagerService = new PagerService(repository, notificationPort,
                externalTimerPort, escalationPolicyPort);

        command = givenAlertCommand();
        escalationPolicy = givenEscalationPolicy();
    }

    @Test
    void GivenHealthyService_WhenReceiveAlert_ThenNotifyAll() {
        // Given
        Alert alert = givenAlert(false, 0);
        MonitoredService monitoredService = givenMonitoredService(MonitoredService.STATE.HEALTHY);
        given(repository.saveAlert(any(Alert.class))).willReturn(alert);
        given(repository.saveMonitoredService(any(MonitoredService.class))).willReturn(monitoredService);
        given(escalationPolicyPort.loadEscalationPolicy(monitoredServiceId)).willReturn(Optional.of(escalationPolicy));

        // When
        // receive a new alert
        pagerService.handleAlert(command);

        // Then
        then(escalationPolicyPort).should().loadEscalationPolicy(command.getServiceId());
        then(repository).should().saveAlert(alertCaptor.capture());

        then(repository).should().saveMonitoredService(monitoredServiceCaptor.capture());
        then(notificationPort).should().notify(targetCaptor.capture(), alertCaptor.capture());
        then(externalTimerPort).should().setAckTimout(eq(15), eq(alert));

        Alert actualAlert = alertCaptor.getValue();
        Target actualTarget = targetCaptor.getValue().get(0);
        MonitoredService actualService = monitoredServiceCaptor.getValue();
        Target expectedTarget = escalationPolicy.getLevel(0).getTargets().get(0);
        assertAll(
                // make sure alert received is persisted into the pager database
                () -> assertEquals(actualAlert.getId(), alert.getId()),
                () -> assertEquals(actualAlert.getServiceId(), command.getServiceId()),
                () -> assertEquals(actualAlert.getMessage(), command.getMessage()),
                // make sure new state of monitored service is UNHEALTHY
                () -> assertEquals(actualService.getState(), MonitoredService.STATE.UNHEALTHY),
                // verify that Pager notifies all targets of the FIRST level of the escalation policy
                () -> assertEquals(((SmsTarget) expectedTarget).getPhoneNumber(),
                        ((SmsTarget) actualTarget).getPhoneNumber())
        );
    }

    @Test
    void GivenUnhealthyService_WhenReceiveAlert_ThenDoNotNotify() {
        // Given
        Alert alert = givenAlert(false, 0);
        MonitoredService monitoredService = givenMonitoredService(MonitoredService.STATE.UNHEALTHY);
        given(repository.findMonitoredServiceByExternalId(alert.getServiceId())).willReturn(Optional.of(monitoredService));

        // When
        pagerService.handleAlert(command);

        // Then
        then(notificationPort).should(never()).notify(anyList(), any(Alert.class));
        then(externalTimerPort).should(never()).setAckTimout(eq(15), eq(alert));
    }
}
