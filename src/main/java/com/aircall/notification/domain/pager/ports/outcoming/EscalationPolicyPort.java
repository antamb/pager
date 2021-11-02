package com.aircall.notification.domain.pager.ports.outcoming;

import com.aircall.notification.domain.pager.model.EscalationPolicy;

import java.util.Optional;

public interface EscalationPolicyPort {
    Optional<EscalationPolicy> loadEscalationPolicy(String serviceId);
}
