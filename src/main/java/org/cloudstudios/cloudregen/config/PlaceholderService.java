package org.cloudstudios.cloudregen.config;

import org.cloudstudios.cloudregen.storage.RegionRepository;

import java.util.Locale;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderService {
    private static final Pattern REGION_TIME_PATTERN = Pattern.compile("%cr_time_([^%]+)%", Pattern.CASE_INSENSITIVE);

    private final RegionRepository repository;
    private final java.util.function.ToLongFunction<String> secondsProvider;

    public PlaceholderService(RegionRepository repository, java.util.function.ToLongFunction<String> secondsProvider) {
        this.repository = repository;
        this.secondsProvider = secondsProvider;
    }

    public String apply(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Matcher matcher = REGION_TIME_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer(input.length() + 16);
        while (matcher.find()) {
        String regionName = normalizeRegionName(matcher.group(1));
            long value = resolve(regionName);
            String replacement = value < 0 ? "N/A" : String.valueOf(value);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private long resolve(String regionName) {
        if (regionName.isEmpty()) {
            return -1;
        }
        if (repository.get(regionName) == null) {
            return -1;
        }
        long seconds = secondsProvider.applyAsLong(regionName.toLowerCase(Locale.ROOT));
        return Math.max(0L, seconds);
    }

    private String normalizeRegionName(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.startsWith("<")) {
            value = value.substring(1);
        }
        if (value.endsWith(">")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.trim();
    }
}
