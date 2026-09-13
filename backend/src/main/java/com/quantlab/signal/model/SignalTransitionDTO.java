package com.quantlab.signal.model;

import java.time.Instant;
import java.util.UUID;

public record SignalTransitionDTO(
    UUID id,
    Long instrumentId,
    String symbol,
    SignalType previousSignal,
    SignalType newSignal,
    Double previousScore,
    Double newScore,
    Instant transitionTimestamp,
    String transitionReason,
    UUID signalId
) {}
