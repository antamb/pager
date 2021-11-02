package com.aircall.notification.domain.pager.ports.outcoming;

import com.aircall.notification.domain.pager.model.Alert;
import com.aircall.notification.domain.pager.model.Target;

import java.util.List;

public interface NotificationPort {
    void notify(List<Target> targets, Alert alert);
}
