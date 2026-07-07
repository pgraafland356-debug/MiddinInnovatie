package com.middin.innovatie.desktop;

import java.util.List;
import java.util.Locale;

/** Offline dev accounts — keep in sync with LoginViewModel.LocalDevAccounts (Android). */
public final class LocalDevAccounts {
    private static final List<Account> ACCOUNTS = List.of(
            new Account(DesktopPreferences.ENDPOINT_SETTINGS_USERNAME, "admin")
    );

    private LocalDevAccounts() {}

    public static boolean matches(String username, String password) {
        if (username == null || password == null) return false;
        String u = username.trim();
        if (u.isEmpty() || password.isEmpty()) return false;
        for (Account account : ACCOUNTS) {
            if (account.username.equalsIgnoreCase(u) && account.password.equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static List<Account> all() {
        return ACCOUNTS;
    }

    public record Account(String username, String password) {
        public Account {
            username = username.toLowerCase(Locale.ROOT);
        }
    }
}
