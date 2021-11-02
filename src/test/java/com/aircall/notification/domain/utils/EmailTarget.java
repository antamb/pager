package com.aircall.notification.domain.utils;

import com.aircall.notification.domain.pager.model.Target;
import lombok.Getter;

public class EmailTarget implements Target {
    private final String email;

    public EmailTarget(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
