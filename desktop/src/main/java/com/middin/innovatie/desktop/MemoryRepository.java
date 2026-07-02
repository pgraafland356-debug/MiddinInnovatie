package com.middin.innovatie.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Local collective memory (port of Android Room collective_memory). */
public final class MemoryRepository {
    private static final String MARKER = "---MEMORY---";
    private static final String END = "---END---";

    private final File storeFile;

    public MemoryRepository(File storeFile) {
        this.storeFile = storeFile;
    }

    public List<MemoryEntry> listAll() throws IOException {
        List<MemoryEntry> items = readAll();
        items.sort(Comparator.comparingLong(MemoryEntry::createdAtEpochMs).reversed());
        return items;
    }

    public void add(String content, String author) throws IOException {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Leeg bericht.");
        List<MemoryEntry> items = readAll();
        items.add(new MemoryEntry(UUID.randomUUID().toString(), text, author == null ? "" : author.trim(), System.currentTimeMillis()));
        writeAll(items);
    }

    public void update(String id, String content) throws IOException {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Leeg bericht.");
        List<MemoryEntry> items = readAll();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id().equals(id)) {
                MemoryEntry old = items.get(i);
                items.set(i, new MemoryEntry(old.id(), text, old.authorName(), old.createdAtEpochMs()));
                writeAll(items);
                return;
            }
        }
        throw new IOException("Notitie niet gevonden.");
    }

    public void delete(String id) throws IOException {
        List<MemoryEntry> items = readAll();
        items.removeIf(e -> e.id().equals(id));
        writeAll(items);
    }

    private List<MemoryEntry> readAll() throws IOException {
        if (!storeFile.exists()) return new ArrayList<>();
        String raw = Files.readString(storeFile.toPath(), StandardCharsets.UTF_8);
        if (raw.isBlank()) return new ArrayList<>();
        List<MemoryEntry> out = new ArrayList<>();
        for (String block : raw.split(MARKER)) {
            if (block.isBlank()) continue;
            String body = block.trim();
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
            if (id != null) out.add(new MemoryEntry(id, text.toString(), author == null ? "" : author, time));
        }
        return out;
    }

    private void writeAll(List<MemoryEntry> items) throws IOException {
        storeFile.getParentFile().mkdirs();
        StringBuilder sb = new StringBuilder();
        for (MemoryEntry e : items) {
            sb.append(MARKER).append('\n');
            sb.append("id=").append(e.id()).append('\n');
            sb.append("author=").append(e.authorName()).append('\n');
            sb.append("time=").append(e.createdAtEpochMs()).append('\n');
            sb.append("text=\n");
            sb.append(e.content()).append('\n');
            sb.append(END).append('\n');
        }
        Files.writeString(storeFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}
