package com.aircall.notification.domain.pager.ports.incoming.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertCommand {
    private final String message;
    private final String serviceId;
}
