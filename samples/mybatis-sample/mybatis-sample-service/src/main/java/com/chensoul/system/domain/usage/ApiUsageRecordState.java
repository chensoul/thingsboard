package com.chensoul.system.domain.usage;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiUsageRecordState implements Serializable {

    private final ApiFeature apiFeature;
    private final ApiUsageRecordKey key;
    private final long threshold;
    private final long value;

    public String getValueAsString() {
        return valueAsString(value);
    }

    public String getThresholdAsString() {
        return valueAsString(threshold);
    }

    private String valueAsString(long value) {
        if (value > 1_000_000 && value % 1_000_000 < 10_000) {
            return value / 1_000_000 + "M";
        } else if (value > 10_000) {
            return String.format("%.2fM", ((double) value) / 1_000_000);
        } else {
            return value + "";
        }
    }

}
