package com.quantlab.features.model;

public enum Timeframe {
    ONE_MINUTE("1M"),
    FIVE_MINUTES("5M"),
    FIFTEEN_MINUTES("15M"),
    THIRTY_MINUTES("30M"),
    ONE_HOUR("1H"),
    ONE_DAY("1D"),
    ONE_WEEK("1W");

    private final String code;

    Timeframe(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Timeframe fromCode(String code) {
        for (Timeframe tf : values()) {
            if (tf.code.equalsIgnoreCase(code) || tf.name().equalsIgnoreCase(code)) {
                return tf;
            }
        }
        return ONE_DAY;
    }
}
