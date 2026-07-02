package com.middin.innovatie.desktop;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record MemoryEntry(String id, String content, String authorName, long createdAtEpochMs) {
    public String formattedTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("nl-NL"));
        return Instant.ofEpochMilli(createdAtEpochMs).atZone(ZoneId.systemDefault()).format(fmt);
    }
}
