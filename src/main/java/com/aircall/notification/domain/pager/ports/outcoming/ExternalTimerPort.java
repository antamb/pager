package com.aircall.notification.domain.pager.ports.outcoming;

import com.aircall.notification.domain.pager.model.Alert;

public interface ExternalTimerPort {
    void setAckTimout(int timout, Alert alert);
}
