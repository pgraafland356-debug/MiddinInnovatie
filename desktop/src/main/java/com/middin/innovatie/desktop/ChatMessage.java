package com.middin.innovatie.desktop;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record ChatMessage(String id, String text, String authorName, long createdAtEpochMs) {
    public boolean isAssistant() {
        return ProductChatKnowledge.AUTHOR_BOT.equals(authorName);
    }

    public String formattedTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("nl-NL"));
        return Instant.ofEpochMilli(createdAtEpochMs).atZone(ZoneId.systemDefault()).format(fmt);
    }
}
