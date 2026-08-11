package com.steamthreat.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreatIntelSanitizerTest {

    @Test
    void removesVictimIdentifiersAndDefangsInfrastructure() {
        String steamId = "7656" + "1111111111111";
        String account = "12345" + "67890a";
        String avatar = "ab".repeat(20);
        String rawHost = String.join("", "nexus", "tech", "solution", ".top");
        String privatePath = "C:" + "\\Users\\analyst\\evidence\\sample.log";
        String input = "account: " + account + ", steam=" + steamId + ", avatar=" + avatar
                + ", url=https://" + rawHost + "/path, source=" + privatePath;

        String output = ThreatIntelSanitizer.sanitize(input);

        assertFalse(output.contains(steamId));
        assertFalse(output.contains(account));
        assertFalse(output.contains(avatar));
        assertFalse(output.contains(rawHost));
        assertFalse(output.contains("analyst"));
        assertTrue(output.contains("[REDACTED_STEAMID64]"));
        assertTrue(output.contains("[REDACTED_ACCOUNT]"));
        assertTrue(output.contains("[REDACTED_40_HEX]"));
        assertTrue(output.contains(ThreatIntelSanitizer.DEFANGED_C2_BASE_URL));
        assertTrue(output.contains("[PRIVATE_HOME]"));
    }

    @Test
    void preservesNullAndBlankValues() {
        assertTrue(ThreatIntelSanitizer.sanitize(null) == null);
        assertTrue(ThreatIntelSanitizer.sanitize("   ").isBlank());
    }
}
