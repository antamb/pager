package com.aircall.notification.domain.pager.ports.incoming;

import com.aircall.notification.domain.pager.ports.incoming.command.AckCommand;

public interface AckTimeoutUseCase {
    void handleAckTimeout(AckCommand command);
}
