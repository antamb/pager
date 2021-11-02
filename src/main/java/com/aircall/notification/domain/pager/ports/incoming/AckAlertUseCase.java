package com.aircall.notification.domain.pager.ports.incoming;

import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.ports.incoming.command.AckCommand;

public interface AckAlertUseCase {
    Alert ackAlert(AckCommand command);
}
