package com.steamthreat.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes victim identifiers and neutralizes live infrastructure before data is persisted or displayed.
 */
public final class ThreatIntelSanitizer {

    public static final String DEFANGED_C2_HOST = "nexustechsolution[.]top";
    public static final String DEFANGED_C2_BASE_URL = "hxxps://" + DEFANGED_C2_HOST;

    private static final String RAW_C2_HOST = String.join("", "nexus", "tech", "solution", ".top");
    private static final Pattern RAW_C2 = Pattern.compile(Pattern.quote(RAW_C2_HOST), Pattern.CASE_INSENSITIVE);
    private static final Pattern STEAM_ID_64 = Pattern.compile("\\b7656\\d{13}\\b");
    private static final Pattern ACCOUNT_FIELD = Pattern.compile(
            "(?i)((?:account\\s*[:=]\\s*|[?&]u=))[0-9a-z]{8,20}");
    private static final Pattern SHA1_LIKE = Pattern.compile("(?i)\\b[0-9a-f]{40}\\b");
    private static final Pattern PRIVATE_HOME = Pattern.compile(
            "(?i)[a-z]:[\\\\/](?:users|用户)[\\\\/][^\\\\/\\s]+(?:[\\\\/][^\\s,;]*)?");

    private ThreatIntelSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String sanitized = RAW_C2.matcher(value).replaceAll(Matcher.quoteReplacement(DEFANGED_C2_HOST));
        sanitized = sanitized.replace("https://" + DEFANGED_C2_HOST, DEFANGED_C2_BASE_URL)
                .replace("http://" + DEFANGED_C2_HOST, "hxxp://" + DEFANGED_C2_HOST);
        sanitized = STEAM_ID_64.matcher(sanitized).replaceAll("[REDACTED_STEAMID64]");
        sanitized = ACCOUNT_FIELD.matcher(sanitized).replaceAll("$1[REDACTED_ACCOUNT]");
        sanitized = SHA1_LIKE.matcher(sanitized).replaceAll("[REDACTED_40_HEX]");
        return PRIVATE_HOME.matcher(sanitized).replaceAll("[PRIVATE_HOME]");
    }
}
