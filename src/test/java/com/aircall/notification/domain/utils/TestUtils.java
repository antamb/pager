package com.aircall.notification.domain.utils;

import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.model.EscalationPolicy;
import com.aircall.notification.domain.pager.model.Level;
import com.aircall.notification.domain.pager.model.MonitoredService;
import com.aircall.notification.domain.pager.ports.incoming.command.AckCommand;
import com.aircall.notification.domain.pager.ports.incoming.command.AlertCommand;

import java.util.Arrays;

import static java.util.Collections.singletonList;

public class TestUtils {

    public static final String alertId = "123";
    public static final String monitoredServiceId = "12345";
    public static final String alertMessage = "alert message";

    public static MonitoredService givenMonitoredService(MonitoredService.STATE state) {
        return MonitoredService.builder().externalId(TestUtils.monitoredServiceId)
                .state(state).build();
    }

    public static EscalationPolicy givenEscalationPolicy() {
        return EscalationPolicy.builder()
                .monitoredServiceId(monitoredServiceId)
                .levels(Arrays.asList(
                        Level.builder().targets(singletonList(new SmsTarget("0000000000"))).build(),
                        Level.builder().targets(singletonList(new SmsTarget("0000000001"))).build(),
                        Level.builder().targets(singletonList(new EmailTarget("test@test.com"))).build()
                )).build();
    }

    public static AlertCommand givenAlertCommand() {
        return AlertCommand.builder().serviceId(monitoredServiceId).message(alertMessage).build();
    }

    public static Alert givenAlert(boolean ack, int currentLevel) {
        return Alert.builder().id(alertId).ack(ack).currentLevel(currentLevel)
                .message(alertMessage).serviceId(monitoredServiceId).build();
    }

    public static AckCommand givenAckCommand() {
        return AckCommand.builder().alertId(alertId).build();
    }
}
