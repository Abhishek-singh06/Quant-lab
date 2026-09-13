package com.quantlab.marketdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ValidationResult(
    boolean isValid,
    MarketDataStatus status,
    List<String> errors,
    List<String> warnings
) {
    public static ValidationResult valid() {
        return new ValidationResult(true, MarketDataStatus.VALID, Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, MarketDataStatus.INVALID, errors, Collections.emptyList());
    }

    public static ValidationResult quarantined(List<String> errors, List<String> warnings) {
        return new ValidationResult(false, MarketDataStatus.QUARANTINED, errors, warnings);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public Builder addError(String error) {
            this.errors.add(error);
            return this;
        }

        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public ValidationResult build() {
            if (errors.isEmpty()) {
                return new ValidationResult(true, MarketDataStatus.VALID, Collections.emptyList(), warnings);
            }
            return new ValidationResult(false, MarketDataStatus.INVALID, errors, warnings);
        }
    }
}
