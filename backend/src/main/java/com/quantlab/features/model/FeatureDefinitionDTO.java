package com.quantlab.features.model;

public class FeatureDefinitionDTO {

    private String featureName;
    private FeatureCategory category;
    private String description;
    private int defaultLookback;
    private String timeframe;
    private String featureVersion;
    private String formulaVersion;
    private PriceSeriesType priceSeriesType;
    private boolean isEnabled;

    public FeatureDefinitionDTO() {}

    public FeatureDefinitionDTO(String featureName, FeatureCategory category, String description,
                                int defaultLookback, String timeframe, String featureVersion,
                                String formulaVersion, PriceSeriesType priceSeriesType, boolean isEnabled) {
        this.featureName = featureName;
        this.category = category;
        this.description = description;
        this.defaultLookback = defaultLookback;
        this.timeframe = timeframe;
        this.featureVersion = featureVersion;
        this.formulaVersion = formulaVersion;
        this.priceSeriesType = priceSeriesType;
        this.isEnabled = isEnabled;
    }

    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public FeatureCategory getCategory() { return category; }
    public void setCategory(FeatureCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDefaultLookback() { return defaultLookback; }
    public void setDefaultLookback(int defaultLookback) { this.defaultLookback = defaultLookback; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getFormulaVersion() { return formulaVersion; }
    public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }
    public PriceSeriesType getPriceSeriesType() { return priceSeriesType; }
    public void setPriceSeriesType(PriceSeriesType priceSeriesType) { this.priceSeriesType = priceSeriesType; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}
