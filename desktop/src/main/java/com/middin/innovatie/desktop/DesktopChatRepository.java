package com.middin.innovatie.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Desktop port of LocalProductChatRepository. */
public final class DesktopChatRepository {
    private static final String MARKER = "---MESSAGE---";
    private static final String END = "---END---";

    private final File storeFile;
    private final List<Catalog.Product> products;

    public DesktopChatRepository(File storeFile, List<Catalog.Product> products) {
        this.storeFile = storeFile;
        this.products = products;
        migrateLegacyIfNeeded();
    }

    public List<ChatMessage> listMessages() throws IOException {
        List<ChatMessage> messages = readAll();
        if (messages.isEmpty()) {
            messages.add(welcomeRow());
            writeAll(messages);
        }
        return messages;
    }

    public void sendMessage(String text) throws IOException {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Leeg bericht.");
        }
        long t0 = System.currentTimeMillis();
        List<ChatMessage> messages = readAll();
        messages.add(new ChatMessage(UUID.randomUUID().toString(), trimmed, ProductChatKnowledge.AUTHOR_USER, t0));
        String reply = ProductChatKnowledge.answer(trimmed, products);
        messages.add(new ChatMessage(UUID.randomUUID().toString(), reply, ProductChatKnowledge.AUTHOR_BOT, t0 + 1));
        writeAll(messages);
    }

    public void clearHistory() throws IOException {
        writeAll(new ArrayList<>());
    }

    private ChatMessage welcomeRow() {
        return new ChatMessage(
            "welcome-local-assistant",
            ProductChatKnowledge.welcomeMessage(),
            ProductChatKnowledge.AUTHOR_BOT,
            System.currentTimeMillis()
        );
    }

    private void migrateLegacyIfNeeded() {
        File legacy = new File(storeFile.getParentFile(), "chat.txt");
        if (!legacy.exists() || storeFile.exists()) return;
        try {
            String raw = Files.readString(legacy.toPath(), StandardCharsets.UTF_8);
            if (raw.isBlank()) return;
            List<ChatMessage> messages = new ArrayList<>();
            long t = System.currentTimeMillis();
            for (String block : raw.split("\n\n")) {
                if (block.isBlank()) continue;
                int nl = block.indexOf('\n');
                if (nl <= 0) continue;
                String who = block.substring(0, nl).replace(":", "").trim();
                String text = block.substring(nl + 1);
                messages.add(new ChatMessage(UUID.randomUUID().toString(), text, who, t));
                t += 1;
            }
            if (!messages.isEmpty()) {
                writeAll(messages);
                legacy.delete();
            }
        } catch (IOException ignored) {
        }
    }

    private List<ChatMessage> readAll() throws IOException {
        if (!storeFile.exists()) return new ArrayList<>();
        String raw = Files.readString(storeFile.toPath(), StandardCharsets.UTF_8);
        if (raw.isBlank()) return new ArrayList<>();
        List<ChatMessage> out = new ArrayList<>();
        String[] blocks = raw.split(MARKER);
        for (String block : blocks) {
            if (block.isBlank()) continue;
            String body = block.trim();
            if (body.startsWith(END)) body = body.substring(END.length()).trim();
            int endIdx = body.indexOf(END);
            if (endIdx >= 0) body = body.substring(0, endIdx).trim();
            String id = null;
            String author = null;
            long time = 0;
            StringBuilder text = new StringBuilder();
            boolean inText = false;
            for (String line : body.split("\n", -1)) {
                if (!inText) {
                    if (line.startsWith("id=")) id = line.substring(3);
                    else if (line.startsWith("author=")) author = line.substring(7);
                    else if (line.startsWith("time=")) time = Long.parseLong(line.substring(5));
                    else if (line.equals("text=")) inText = true;
                } else {
                    if (text.length() > 0) text.append('\n');
                    text.append(line);
                }
            }
            if (id != null && author != null) {
                out.add(new ChatMessage(id, text.toString(), author, time));
            }
        }
        return out;
    }

    private void writeAll(List<ChatMessage> messages) throws IOException {
        storeFile.getParentFile().mkdirs();
        StringBuilder sb = new StringBuilder();
        for (ChatMessage m : messages) {
            sb.append(MARKER).append('\n');
            sb.append("id=").append(m.id()).append('\n');
            sb.append("author=").append(m.authorName()).append('\n');
            sb.append("time=").append(m.createdAtEpochMs()).append('\n');
            sb.append("text=\n");
            sb.append(m.text()).append('\n');
            sb.append(END).append('\n');
        }
        Files.writeString(storeFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}
