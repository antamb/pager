package com.aircall.notification.domain.utils;

import com.aircall.notification.domain.pager.model.Target;
import lombok.Getter;

public class SmsTarget implements Target {
    private final String phoneNumber;

    public SmsTarget(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
