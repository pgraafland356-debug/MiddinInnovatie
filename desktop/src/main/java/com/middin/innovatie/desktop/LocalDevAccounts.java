package com.middin.innovatie.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Offline dev accounts from resources/dev-accounts.json (source: dev-accounts/dev-accounts.json). */
public final class LocalDevAccounts {
    private static final String RESOURCE_PATH = "/dev-accounts.json";
    private static final Pattern ACCOUNT_BLOCK = Pattern.compile(
            "\\{\\s*\"username\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*\"password\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*\\}");

    private static volatile List<Account> cached;

    private LocalDevAccounts() {}

    public static boolean matches(String username, String password) {
        if (username == null || password == null) return false;
        String u = username.trim();
        if (u.isEmpty() || password.isEmpty()) return false;
        for (Account account : all()) {
            if (account.username.equalsIgnoreCase(u) && account.password.equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static List<Account> all() {
        List<Account> list = cached;
        if (list != null) return list;
        synchronized (LocalDevAccounts.class) {
            if (cached != null) return cached;
            cached = Collections.unmodifiableList(loadAccounts());
            return cached;
        }
    }

    public static List<String> usernames() {
        List<String> names = new ArrayList<>();
        for (Account account : all()) {
            names.add(account.username);
        }
        return names;
    }

    private static List<Account> loadAccounts() {
        try (InputStream in = LocalDevAccounts.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) return List.of();
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (!json.isEmpty() && json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
            }
            return parseAccounts(json);
        } catch (IOException e) {
            return List.of();
        }
    }

    static List<Account> parseAccounts(String json) {
        List<Account> out = new ArrayList<>();
        Matcher m = ACCOUNT_BLOCK.matcher(json);
        while (m.find()) {
            String username = unescapeJson(m.group(1)).trim();
            String password = unescapeJson(m.group(2));
            if (!username.isEmpty() && !password.isEmpty()) {
                out.add(new Account(username, password));
            }
        }
        return out;
    }

    private static String unescapeJson(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public record Account(String username, String password) {
        public Account {
            username = username.toLowerCase(Locale.ROOT);
        }
    }
}
