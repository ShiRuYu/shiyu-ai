package com.shiyu.ai.dal.retention;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Retention windows for operational records that are safe to purge. */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.retention.data")
public class DataRetentionProperties {

    private boolean enabled = true;
    private int executionDays = 30;
    private int auditDays = 180;
    private int usageDays = 365;
    private int taskDays = 30;
}
