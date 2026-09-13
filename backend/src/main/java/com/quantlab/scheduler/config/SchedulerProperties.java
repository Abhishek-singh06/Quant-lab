package com.quantlab.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "quantlab.scheduler")
public class SchedulerProperties {

    /**
     * Master switch for background automation scheduler.
     * Default: false in test, configurable via QUANTLAB_SCHEDULER_ENABLED.
     */
    private boolean enabled = false;

    /**
     * Individual job configurations.
     */
    private Map<String, JobConfig> jobs = new HashMap<>();

    public static class JobConfig {
        private boolean enabled = true;
        private boolean marketHoursOnly = false;
        private int maxRetries = 2;
        private long fixedRateMs = 60000;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isMarketHoursOnly() { return marketHoursOnly; }
        public void setMarketHoursOnly(boolean marketHoursOnly) { this.marketHoursOnly = marketHoursOnly; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public long getFixedRateMs() { return fixedRateMs; }
        public void setFixedRateMs(long fixedRateMs) { this.fixedRateMs = fixedRateMs; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Map<String, JobConfig> getJobs() { return jobs; }
    public void setJobs(Map<String, JobConfig> jobs) { this.jobs = jobs; }

    public JobConfig getJobConfig(String jobName) {
        return jobs.computeIfAbsent(jobName, k -> new JobConfig());
    }
}
