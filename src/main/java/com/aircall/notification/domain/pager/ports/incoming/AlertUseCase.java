package com.aircall.notification.domain.pager.ports.incoming;

import com.aircall.notification.domain.pager.ports.incoming.command.AlertCommand;

public interface AlertUseCase {
    void handleAlert(AlertCommand command);
}
