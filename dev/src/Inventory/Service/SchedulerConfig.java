package Inventory.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads scheduler settings from {@code scheduler.properties} on the classpath
 * or working directory. Falls back to built-in defaults if the file is absent.
 *
 * Properties:
 *   intervalSeconds  - how often the scheduler polls (default 86400 = once per day)
 *   leadDays         - place order when delivery is within this many days (default 2)
 */
public class SchedulerConfig {

    private static final long DEFAULT_INTERVAL = 86400;
    private static final int  DEFAULT_LEAD     = 2;

    private final long intervalSeconds;
    private final int  leadDays;

    /** Load from scheduler.properties (working directory or classpath). */
    public SchedulerConfig() {
        Properties props = new Properties();

        try (InputStream in = SchedulerConfig.class
                .getClassLoader().getResourceAsStream("Inventory/Service/scheduler.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}

        this.intervalSeconds = parseLong(props, "intervalSeconds", DEFAULT_INTERVAL);
        this.leadDays        = parseInt (props, "leadDays",        DEFAULT_LEAD);
    }

    /** Override constructor for tests - skips file loading. */
    public SchedulerConfig(long intervalSeconds, int leadDays) {
        if (intervalSeconds <= 0) throw new IllegalArgumentException("intervalSeconds must be > 0");
        if (leadDays < 0)         throw new IllegalArgumentException("leadDays cannot be negative");
        this.intervalSeconds = intervalSeconds;
        this.leadDays        = leadDays;
    }

    public long getIntervalSeconds() { return intervalSeconds; }
    public int  getLeadDays()        { return leadDays; }

    private static long parseLong(Properties p, String key, long fallback) {
        String v = p.getProperty(key);
        if (v == null) return fallback;
        try { return Long.parseLong(v.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static int parseInt(Properties p, String key, int fallback) {
        String v = p.getProperty(key);
        if (v == null) return fallback;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }
}
