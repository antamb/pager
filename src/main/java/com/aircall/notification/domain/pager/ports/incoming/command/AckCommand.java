package com.aircall.notification.domain.pager.ports.incoming.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AckCommand {
    private final String alertId;
}
