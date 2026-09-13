package com.quantlab.features.service;

import com.quantlab.features.calculator.*;
import com.quantlab.features.entity.FeatureDefinition;
import com.quantlab.features.model.FeatureDefinitionDTO;
import com.quantlab.features.model.PriceSeriesType;
import com.quantlab.features.repository.FeatureDefinitionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TechnicalFeatureRegistry {

    private static final Logger log = LoggerFactory.getLogger(TechnicalFeatureRegistry.class);

    private final FeatureDefinitionRepository definitionRepository;
    private final Map<String, TechnicalFeatureCalculator> calculators = new ConcurrentHashMap<>();

    public TechnicalFeatureRegistry(FeatureDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @PostConstruct
    public void init() {
        // Register all standard calculators

        // 1. Returns
        registerCalculator(new ReturnsCalculator("RETURN_1D", 1, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_5D", 5, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_10D", 10, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_20D", 20, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_21D", 21, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_63D", 63, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_126D", 126, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("RETURN_252D", 252, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new ReturnsCalculator("LOG_RETURN_1D", 1, true, PriceSeriesType.SPLIT_ADJUSTED));

        // 2. SMAs
        registerCalculator(new SmaCalculator("SMA_5", 5, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new SmaCalculator("SMA_10", 10, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new SmaCalculator("SMA_20", 20, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new SmaCalculator("SMA_50", 50, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new SmaCalculator("SMA_100", 100, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new SmaCalculator("SMA_200", 200, PriceSeriesType.SPLIT_ADJUSTED));

        // 3. EMAs
        registerCalculator(new EmaCalculator("EMA_5", 5, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new EmaCalculator("EMA_10", 10, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new EmaCalculator("EMA_20", 20, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new EmaCalculator("EMA_50", 50, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new EmaCalculator("EMA_100", 100, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new EmaCalculator("EMA_200", 200, PriceSeriesType.SPLIT_ADJUSTED));

        // 4. RSI
        registerCalculator(new RsiCalculator("RSI_14", 14, PriceSeriesType.SPLIT_ADJUSTED));

        // 5. MACD
        registerCalculator(new MacdCalculator("MACD_LINE_12_26", 12, 26, 9, MacdCalculator.Component.MACD_LINE, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MacdCalculator("MACD_SIGNAL_9", 12, 26, 9, MacdCalculator.Component.SIGNAL_LINE, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MacdCalculator("MACD_HISTOGRAM_12_26_9", 12, 26, 9, MacdCalculator.Component.HISTOGRAM, PriceSeriesType.SPLIT_ADJUSTED));

        // 6. ATR
        registerCalculator(new AtrCalculator("ATR_14", 14, false, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new AtrCalculator("ATR_PERCENT_14", 14, true, PriceSeriesType.SPLIT_ADJUSTED));

        // 7. Volatility
        registerCalculator(new VolatilityCalculator("VOLATILITY_10D", 10, true, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new VolatilityCalculator("VOLATILITY_20D", 20, true, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new VolatilityCalculator("VOLATILITY_21D", 21, true, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new VolatilityCalculator("VOLATILITY_63D", 63, true, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new VolatilityCalculator("VOLATILITY_252D", 252, true, PriceSeriesType.SPLIT_ADJUSTED));

        // 8. Momentum
        registerCalculator(new MomentumCalculator("MOMENTUM_5", 5, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MomentumCalculator("MOMENTUM_10", 10, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MomentumCalculator("MOMENTUM_20", 20, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MomentumCalculator("MOMENTUM_63", 63, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MomentumCalculator("MOMENTUM_126", 126, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new MomentumCalculator("MOMENTUM_252", 252, PriceSeriesType.SPLIT_ADJUSTED));

        // 9. Volume Ratios
        registerCalculator(new VolumeRatioCalculator("VOLUME_RATIO_5", 5));
        registerCalculator(new VolumeRatioCalculator("VOLUME_RATIO_10", 10));
        registerCalculator(new VolumeRatioCalculator("VOLUME_RATIO_20", 20));
        registerCalculator(new VolumeRatioCalculator("VOLUME_RATIO_50", 50));

        // 10. 52-Week High / Low / Position
        registerCalculator(new Week52PositionCalculator("WEEK_52_HIGH", 252, Week52PositionCalculator.Mode.HIGH, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new Week52PositionCalculator("WEEK_52_LOW", 252, Week52PositionCalculator.Mode.LOW, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new Week52PositionCalculator("WEEK_52_POSITION", 252, Week52PositionCalculator.Mode.POSITION, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new Week52PositionCalculator("DISTANCE_FROM_52W_HIGH", 252, Week52PositionCalculator.Mode.DISTANCE_FROM_HIGH, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new Week52PositionCalculator("DISTANCE_FROM_52W_LOW", 252, Week52PositionCalculator.Mode.DISTANCE_FROM_LOW, PriceSeriesType.SPLIT_ADJUSTED));

        // 11. Drawdowns
        registerCalculator(new DrawdownCalculator("DRAWDOWN", 252, DrawdownCalculator.Mode.CURRENT, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new DrawdownCalculator("MAX_DRAWDOWN_20", 20, DrawdownCalculator.Mode.MAX_ROLLING, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new DrawdownCalculator("MAX_DRAWDOWN_63", 63, DrawdownCalculator.Mode.MAX_ROLLING, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new DrawdownCalculator("MAX_DRAWDOWN_126", 126, DrawdownCalculator.Mode.MAX_ROLLING, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new DrawdownCalculator("MAX_DRAWDOWN_252", 252, DrawdownCalculator.Mode.MAX_ROLLING, PriceSeriesType.SPLIT_ADJUSTED));

        // 12. Relative Strength vs Benchmark
        registerCalculator(new RelativeStrengthCalculator("RS_NIFTY_20", "NIFTY 50", 20, PriceSeriesType.SPLIT_ADJUSTED));
        registerCalculator(new RelativeStrengthCalculator("RS_NIFTY_63", "NIFTY 50", 63, PriceSeriesType.SPLIT_ADJUSTED));

        log.info("Initialized TechnicalFeatureRegistry with {} calculators", calculators.size());
    }

    public void registerCalculator(TechnicalFeatureCalculator calculator) {
        calculators.put(calculator.getFeatureName(), calculator);
    }

    public Optional<TechnicalFeatureCalculator> getCalculator(String featureName) {
        return Optional.ofNullable(calculators.get(featureName));
    }

    public Collection<TechnicalFeatureCalculator> getAllCalculators() {
        return Collections.unmodifiableCollection(calculators.values());
    }

    public List<FeatureDefinitionDTO> getRegisteredFeatureDefinitions() {
        List<FeatureDefinitionDTO> list = new ArrayList<>();
        for (TechnicalFeatureCalculator calc : calculators.values()) {
            list.add(new FeatureDefinitionDTO(
                    calc.getFeatureName(),
                    calc.getCategory(),
                    "Technical indicator " + calc.getFeatureName(),
                    calc.getRequiredLookback(),
                    "1D",
                    calc.getFeatureVersion(),
                    calc.getFormulaVersion(),
                    calc.getRequiredPriceSeriesType(),
                    true
            ));
        }
        return list;
    }
}
