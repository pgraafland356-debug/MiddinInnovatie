package com.middin.innovatie.desktop.ui;

import com.middin.innovatie.desktop.AppVersion;
import com.middin.innovatie.desktop.DesktopAppUpdater;
import com.middin.innovatie.desktop.DesktopAppUpdater.WindowsRelease;
import com.middin.innovatie.desktop.DesktopPreferences;
import com.middin.innovatie.desktop.GeminiClient;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/** Meer-menu en subschermen (zelfde structuur als de Android-app). */
public final class MorePanel extends JPanel {
    private static final String MENU = "menu";
    private static final String SETTINGS = "settings";
    private static final String CHANGELOG = "changelog";
    private static final String UPDATES = "updates";
    private static final String INFO = "info";
    private static final String ABOUT = "about";
    private static final String CREDITS = "credits";
    private static final String GEMINI = "gemini";
    private static final String BLUETOOTH = "bluetooth";

    private final DesktopPreferences prefs;
    private final Runnable onLogoutRequest;
    private final Runnable onThemeChanged;
    private final File dataDir;
    private final Window owner;
    private final CardLayout cardLayout;
    private final JPanel stack;

    public MorePanel(DesktopPreferences prefs, Runnable onLogoutRequest, Window owner, Runnable onThemeChanged, File dataDir) {
        this.prefs = prefs;
        this.onLogoutRequest = onLogoutRequest;
        this.owner = owner;
        this.onThemeChanged = onThemeChanged;
        this.dataDir = dataDir;
        setLayout(new BorderLayout());
        setBackground(MiddinTheme.BACKGROUND);
        cardLayout = new CardLayout();
        stack = new JPanel(cardLayout);
        stack.setOpaque(false);
        stack.add(buildMenu(), MENU);
        stack.add(wrapScreen("Instellingen", buildSettings()), SETTINGS);
        stack.add(wrapScreen("Changelog", buildChangelog()), CHANGELOG);
        stack.add(wrapScreen("Updates", buildUpdates()), UPDATES);
        stack.add(wrapScreen("Info", buildInfo()), INFO);
        stack.add(wrapScreen("Over ons", buildAbout()), ABOUT);
        stack.add(wrapScreen("Credits", buildCredits()), CREDITS);
        stack.add(wrapScreen("Gemini-assistent", buildGemini()), GEMINI);
        stack.add(wrapScreen("Bluetooth", buildBluetooth()), BLUETOOTH);
        add(MiddinTheme.contentPad(stack), BorderLayout.CENTER);
        showMenu();
    }

    public void showMenu() {
        cardLayout.show(stack, MENU);
    }

    private JPanel buildMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.add(MiddinTheme.titleLabel("Meer"));
        menu.add(Box.createVerticalStrut(12));
        addMenuRow(menu, "Instellingen", SETTINGS);
        addMenuRow(menu, "Changelog", CHANGELOG);
        addMenuRow(menu, "Updates", UPDATES);
        addMenuRow(menu, "Info", INFO);
        addMenuRow(menu, "Over ons", ABOUT);
        addMenuRow(menu, "Credits", CREDITS);
        if (prefs.canConfigureEndpoints()) {
            addMenuRow(menu, "Gemini-assistent", GEMINI);
        }
        addMenuRow(menu, "Bluetooth", BLUETOOTH);
        return menu;
    }

    private void addMenuRow(JPanel menu, String title, String card) {
        JButton row = MiddinTheme.menuRowButton(title);
        row.addActionListener(e -> cardLayout.show(stack, card));
        menu.add(row);
        menu.add(Box.createVerticalStrut(10));
    }

    private JPanel wrapScreen(String title, JPanel content) {
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = MiddinTheme.textButton("← Terug");
        back.addActionListener(e -> showMenu());
        top.add(back, BorderLayout.WEST);
        JLabel t = MiddinTheme.titleLabel(title);
        t.setFont(MiddinTheme.FONT_TITLE.deriveFont(18f));
        top.add(t, BorderLayout.CENTER);
        wrap.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(MiddinTheme.BORDER));
        scroll.getViewport().setBackground(MiddinTheme.BACKGROUND);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildSettings() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 8, 24, 8));

        p.add(sectionTitle("Thema"));
        p.add(muted("Kies een weergave — het venster wordt direct vernieuwd."));
        p.add(Box.createVerticalStrut(8));
        DesktopThemeId activeTheme = DesktopThemeId.fromId(prefs.getTheme());
        for (DesktopThemeId theme : DesktopThemeId.values()) {
            JButton themeBtn = MiddinTheme.themePickButton(theme, theme == activeTheme);
            themeBtn.addActionListener(e -> {
                prefs.setTheme(theme.id());
                savePrefs();
                if (onThemeChanged != null) onThemeChanged.run();
            });
            p.add(themeBtn);
            p.add(Box.createVerticalStrut(6));
        }

        p.add(Box.createVerticalStrut(8));

        if (prefs.canConfigureEndpoints()) {
            p.add(Box.createVerticalStrut(16));
            p.add(sectionTitle("Gemini API-sleutel"));
            p.add(muted("Van Google AI Studio. Opgeslagen op alleen dit apparaat."));
            JPasswordField gemini = new JPasswordField(24);
            gemini.setText(prefs.getGeminiApiKey());
            gemini.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            p.add(gemini);
            JButton saveGemini = MiddinTheme.primaryButton("Sleutel opslaan");
            saveGemini.setAlignmentX(0f);
            saveGemini.setMaximumSize(new Dimension(220, 44));
            saveGemini.addActionListener(e -> {
                prefs.setGeminiApiKey(new String(gemini.getPassword()));
                savePrefs();
                JOptionPane.showMessageDialog(owner, "Gemini API-sleutel opgeslagen.");
            });
            p.add(Box.createVerticalStrut(8));
            p.add(saveGemini);
        }

        p.add(Box.createVerticalStrut(16));
        p.add(sectionTitle("Meldingen"));
        p.add(muted("Testmelding (desktop-toast)."));
        JButton notif = MiddinTheme.primaryButton("Testmelding versturen");
        notif.setEnabled(true);
        notif.setAlignmentX(0f);
        notif.setMaximumSize(new Dimension(260, 44));
        notif.addActionListener(e -> {
            try {
                java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                if (java.awt.SystemTray.isSupported()) {
                    java.awt.TrayIcon icon = new java.awt.TrayIcon(
                        new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB),
                        "Middin Innovatie"
                    );
                    tray.add(icon);
                    icon.displayMessage("Middin Innovatie", "Dit is een testmelding.", java.awt.TrayIcon.MessageType.INFO);
                    tray.remove(icon);
                } else {
                    JOptionPane.showMessageDialog(owner, "Dit is een testmelding.", "Middin Innovatie", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Dit is een testmelding.", "Middin Innovatie", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        p.add(Box.createVerticalStrut(8));
        p.add(notif);

        if (prefs.canConfigureEndpoints()) {
            p.add(Box.createVerticalStrut(16));
            p.add(sectionTitle("API-server"));
            p.add(muted("Build-standaard: " + prefs.getEffectiveApiBaseUrl()));
            JTextField api = MiddinTheme.outlinedField(30);
            api.setText(prefs.getApiBaseUrlOverride());
            api.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            p.add(api);
            p.add(muted("Actief: " + prefs.getEffectiveApiBaseUrl()));
            JPanel apiBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            apiBtns.setOpaque(false);
            JButton saveApi = MiddinTheme.primaryButton("Server-URL opslaan");
            saveApi.addActionListener(e -> {
                prefs.setApiBaseUrlOverride(api.getText());
                savePrefs();
            });
            JButton defaultApi = MiddinTheme.textButton("Build-standaard gebruiken");
            defaultApi.addActionListener(e -> {
                api.setText("");
                prefs.setApiBaseUrlOverride("");
                savePrefs();
            });
            apiBtns.add(saveApi);
            apiBtns.add(defaultApi);
            p.add(apiBtns);
            p.add(muted("Laat leeg en tik op Build-standaard gebruiken, of vul schema, host en poort in."));
        }

        p.add(Box.createVerticalStrut(16));
        p.add(sectionTitle("App-updates"));
        p.add(muted("Huidige versie: " + AppVersion.NAME + " (" + AppVersion.CODE + ")"));
        p.add(muted("Feed: " + prefs.getEffectiveUpdateFeedUrl()));
        JLabel updateStatus = muted("");
        JButton check = MiddinTheme.primaryButton("Controleren op update");
        check.setAlignmentX(0f);
        check.setMaximumSize(new Dimension(280, 44));
        JButton installUpdate = MiddinTheme.textButton("Downloaden en installeren");
        installUpdate.setAlignmentX(0f);
        installUpdate.setMaximumSize(new Dimension(280, 44));
        installUpdate.setEnabled(false);
        final WindowsRelease[] pendingRelease = new WindowsRelease[1];
        check.addActionListener(e -> runUpdateCheck(updateStatus, installUpdate, pendingRelease, false));
        installUpdate.addActionListener(e -> runUpdateInstall(updateStatus, pendingRelease[0]));
        p.add(Box.createVerticalStrut(8));
        p.add(check);
        p.add(Box.createVerticalStrut(8));
        p.add(installUpdate);
        p.add(updateStatus);

        if (prefs.canConfigureEndpoints()) {
            p.add(Box.createVerticalStrut(12));
            p.add(muted("Optioneel: eigen GitHub-feed URL (leeg = standaard)."));
            JTextField feed = MiddinTheme.outlinedField(30);
            feed.setText(prefs.getUpdateFeedOverride());
            feed.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            p.add(feed);
            JButton saveFeed = MiddinTheme.textButton("Feed-URL opslaan");
            saveFeed.addActionListener(e -> {
                prefs.setUpdateFeedOverride(feed.getText());
                savePrefs();
            });
            p.add(saveFeed);
        }

        p.add(Box.createVerticalStrut(16));
        p.add(sectionTitle("Taal"));
        ButtonGroup langGroup = new ButtonGroup();
        JRadioButton en = langRadio("English", "en", langGroup);
        JRadioButton nl = langRadio("Nederlands", "nl", langGroup);
        if ("en".equals(prefs.getLocale())) en.setSelected(true);
        else nl.setSelected(true);
        p.add(en);
        p.add(nl);
        Runnable saveLang = () -> {
            prefs.setLocale(en.isSelected() ? "en" : "nl");
            savePrefs();
        };
        en.addActionListener(e -> saveLang.run());
        nl.addActionListener(e -> saveLang.run());

        p.add(Box.createVerticalStrut(16));
        p.add(muted("Release-builds gebruiken minificatie. Geen productiegeheimen in de client; gebruik HTTPS en sleutels op de server."));

        JButton logout = MiddinTheme.primaryButton("Uitloggen");
        logout.setAlignmentX(0f);
        logout.setMaximumSize(new Dimension(220, 48));
        logout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(owner, "Weet je zeker dat je wilt uitloggen?", "Uitloggen", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) onLogoutRequest.run();
        });
        p.add(Box.createVerticalStrut(24));
        p.add(logout);
        return p;
    }

    private JPanel buildChangelog() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        addChangelogCard(p, "8 · 2026-05-08", "Changelog: de \"deze APK\"-kaart gebruikte alleen versionName; versionCode bleef in Gradle voor update-volgorde.");
        addChangelogCard(p, "7 · 2026-05-07", "Locale: MainActivity extends AppCompatActivity zodat EN/NL uit instellingen geldt voor Compose stringResource.");
        addChangelogCard(p, "6 · 2026-05-06", "Taal EN/NL app-breed; layout: adaptieve padding; Gradle checkAppLinks voor RSS-URL's.");
        addChangelogCard(p, "5 · 2026-04-22", "Welkomstscherm bij uitloggen; offline productassistent «wie ben je»-vragen; CIV-demo account.");
        addChangelogCard(p, "4 · 2026-04-22", "Collectief geheugen bewerken; Qtronix Libra 90; private updates; targetSdk 36.");
        addChangelogCard(p, "3 · 2026-03-26", "Muse product; credits bijgewerkt; changelog build-metadata.");
        addChangelogCard(p, "2 · 2026-03-24", "Roadmap: home, thema, producten, CameraX, Bluetooth, Gemini, updates.");
        addChangelogCard(p, "1 · 2026-03-24", "Fase 1: collectief geheugen, EN/NL, Ktor API login + chat, DataStore sessie.");
        return p;
    }

    private void addChangelogCard(JPanel parent, String header, String bullet) {
        JPanel card = MiddinTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(0f);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        JLabel h = MiddinTheme.bodyLabel(header);
        h.setFont(MiddinTheme.FONT_BODY.deriveFont(java.awt.Font.BOLD));
        card.add(h);
        card.add(Box.createVerticalStrut(6));
        card.add(MiddinTheme.bodyLabel("• " + bullet));
        parent.add(card);
        parent.add(Box.createVerticalStrut(10));
    }

    private JPanel buildUpdates() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(MiddinTheme.bodyLabel("Geplande uitrol en release-opmerkingen voor het team."));
        p.add(Box.createVerticalStrut(12));
        p.add(MiddinTheme.bodyLabel("Desktopversie " + AppVersion.NAME + " — Windows."));
        p.add(Box.createVerticalStrut(8));
        p.add(muted("Voor app-updates op Android: Meer → Instellingen → App-updates."));
        return p;
    }

    private JPanel buildInfo() {
        JTextArea area = MiddinTheme.bodyArea();
        area.setText(
            "Deze app verbindt met jullie eigen backend voor login en chat.\n\n"
                + "Beveiliging: gebruik HTTPS in productie. Gemini API-sleutels staan op het apparaat voor gemak—gebruik liever server-side AI voor vertrouwelijke data.\n\n"
                + "Functies: collectief geheugen (lokale database), REST-chat, producten met CameraX, ML Kit-labels als hint, gekoppelde Bluetooth-apparaten, Google Gemini (optioneel), testmeldingen, EN/NL UI."
        );
        area.setEditable(false);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(area, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildAbout() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JTextArea area = MiddinTheme.bodyArea();
        area.setText(
            "Middin Innovatie is gevestigd in Den Haag. We werken aan innovatie, digitale producten en samenwerking met partners en collega’s.\n\n"
                + "Deze applicatie bundelt interne tools: gedeeld geheugen, communicatie, productvastlegging en koppelingen (API, Gemini, Bluetooth) op één plek.\n\n"
                + "Voor huisstijl, logo’s en officiële teksten: volg jullie interne brandrichtlijnen."
        );
        area.setEditable(false);
        p.add(area);
        p.add(Box.createVerticalStrut(16));
        JLabel copy = MiddinTheme.mutedLabel("© " + Year.now().getValue() + " Middin Innovatie");
        copy.setAlignmentX(0.5f);
        p.add(copy);
        return p;
    }

    private JPanel buildCredits() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(MiddinTheme.bodyLabel("Mensen en teams achter deze build. Pas de lijst in code aan (CreditsRepository) naar jullie organisatie."));
        p.add(Box.createVerticalStrut(12));
        addCreditCard(p, "Middin Innovatie team", "Product & innovation — Den Haag");
        addCreditCard(p, "Platform engineering", "Android, API integration, security");
        addCreditCard(p, "You", "Pieter - Bas Graafland\nAndré de Winter\nSafeer Khan");
        return p;
    }

    private void addCreditCard(JPanel parent, String name, String role) {
        JPanel card = MiddinTheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(0f);
        JLabel n = MiddinTheme.bodyLabel(name);
        n.setFont(MiddinTheme.FONT_BODY.deriveFont(java.awt.Font.BOLD));
        card.add(n);
        card.add(MiddinTheme.bodyLabel(role.replace("\n", " · ")));
        parent.add(card);
        parent.add(Box.createVerticalStrut(10));
    }

    private JPanel buildGemini() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(MiddinTheme.bodyLabel("Gebruikt Google Gemini (gemini-1.5-flash). Plak een API-sleutel van Google AI Studio onder Meer → Instellingen. Sleutels blijven op dit apparaat."));
        JTextArea prompt = MiddinTheme.bodyArea();
        prompt.setRows(4);
        p.add(Box.createVerticalStrut(12));
        p.add(MiddinTheme.mutedLabel("Prompt"));
        p.add(prompt);
        JTextArea output = MiddinTheme.bodyArea();
        output.setEditable(false);
        JLabel status = muted("");
        JButton run = MiddinTheme.primaryButton("Uitvoeren");
        run.setAlignmentX(0f);
        run.setMaximumSize(new Dimension(200, 44));
        run.addActionListener(e -> {
            String text = prompt.getText().trim();
            if (text.isEmpty()) return;
            run.setEnabled(false);
            status.setText("Bezig…");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return GeminiClient.generate(prefs.getGeminiApiKey(), text);
                }

                @Override
                protected void done() {
                    run.setEnabled(true);
                    try {
                        output.setText(get());
                        status.setText("");
                    } catch (Exception ex) {
                        status.setText(ex.getMessage());
                    }
                }
            }.execute();
        });
        p.add(Box.createVerticalStrut(12));
        p.add(run);
        p.add(status);
        p.add(Box.createVerticalStrut(12));
        p.add(MiddinTheme.scroll(output));
        return p;
    }

    private JPanel buildBluetooth() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(MiddinTheme.bodyLabel(
            "Gekoppelde Bluetooth-apparaten op deze computer (zoals in de Android-app)."));
        p.add(Box.createVerticalStrut(12));
        p.add(MiddinTheme.mutedLabel("Gekoppeld"));
        List<com.middin.innovatie.desktop.BluetoothDeviceInfo> devices = com.middin.innovatie.desktop.BluetoothHelper.listPairedDevices();
        if (devices.isEmpty()) {
            p.add(MiddinTheme.bodyLabel("Geen gekoppelde apparaten gevonden."));
        } else {
            for (var d : devices) {
                JPanel card = MiddinTheme.cardPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setAlignmentX(0f);
                card.add(MiddinTheme.bodyLabel(d.name()));
                if (d.address() != null && !d.address().isBlank()) {
                    card.add(MiddinTheme.mutedLabel(d.address()));
                }
                p.add(card);
                p.add(Box.createVerticalStrut(8));
            }
        }
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = MiddinTheme.bodyLabel(text);
        l.setFont(MiddinTheme.FONT_BODY.deriveFont(java.awt.Font.BOLD));
        l.setAlignmentX(0f);
        return l;
    }

    private JLabel muted(String text) {
        JLabel l = MiddinTheme.mutedLabel(text);
        l.setAlignmentX(0f);
        return l;
    }

    private JRadioButton themeRadio(String label, String value, ButtonGroup group) {
        JRadioButton r = new JRadioButton(label);
        r.setFont(MiddinTheme.FONT_BODY);
        r.setForeground(MiddinTheme.TEXT);
        r.setOpaque(false);
        r.setAlignmentX(0f);
        group.add(r);
        return r;
    }

    private JRadioButton langRadio(String label, String value, ButtonGroup group) {
        return themeRadio(label, value, group);
    }

    private void savePrefs() {
        try {
            prefs.save();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner, "Kon instellingen niet opslaan: " + ex.getMessage());
        }
    }

    /** Melding bij opstarten als er een nieuwere versie op GitHub staat. */
    public void checkForUpdateOnStartup() {
        String feed = prefs.getEffectiveUpdateFeedUrl().trim();
        if (feed.isEmpty() || feed.contains("YOUR_GITHUB_USERNAME")) return;
        new SwingWorker<WindowsRelease, Void>() {
            @Override
            protected WindowsRelease doInBackground() throws Exception {
                DesktopAppUpdater updater = new DesktopAppUpdater();
                WindowsRelease release = updater.fetchLatestRelease(feed);
                if (release == null) return null;
                if (release.versionCode <= prefs.getUpdateNoticeDismissedCode()) return null;
                return release;
            }

            @Override
            protected void done() {
                try {
                    WindowsRelease release = get();
                    if (release == null) return;
                    String version = release.versionName.isEmpty()
                            ? Integer.toString(release.versionCode) : release.versionName;
                    int choice = JOptionPane.showOptionDialog(
                            owner,
                            "Er is een update beschikbaar: versie " + version + " (" + release.versionCode + ").\n"
                                    + "Wil je nu de volledige setup downloaden en installeren?",
                            "Update beschikbaar",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[] { "Nu bijwerken", "Later" },
                            "Later");
                    if (choice == JOptionPane.YES_OPTION) {
                        final WindowsRelease[] holder = new WindowsRelease[] { release };
                        JLabel status = muted("");
                        runUpdateInstall(status, holder[0]);
                    } else {
                        prefs.setUpdateNoticeDismissedCode(release.versionCode);
                        savePrefs();
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void runUpdateCheck(JLabel updateStatus, JButton installBtn, WindowsRelease[] holder, boolean quiet) {
        String feed = prefs.getEffectiveUpdateFeedUrl().trim();
        if (feed.isEmpty() || feed.contains("YOUR_GITHUB_USERNAME")) {
            updateStatus.setText("Stel eerst je GitHub-gebruikersnaam in (gradle.properties / feed-URL).");
            installBtn.setEnabled(false);
            holder[0] = null;
            return;
        }
        updateStatus.setText("Controleren op updates…");
        installBtn.setEnabled(false);
        holder[0] = null;
        new SwingWorker<WindowsRelease, Void>() {
            @Override
            protected WindowsRelease doInBackground() throws Exception {
                return new DesktopAppUpdater().fetchLatestRelease(feed);
            }

            @Override
            protected void done() {
                try {
                    WindowsRelease release = get();
                    if (release == null) {
                        updateStatus.setText("Geen update gevonden — je hebt de nieuwste versie.");
                        return;
                    }
                    holder[0] = release;
                    installBtn.setEnabled(true);
                    String version = release.versionName.isEmpty()
                            ? Integer.toString(release.versionCode) : release.versionName;
                    updateStatus.setText("Update gevonden: " + version + " (" + release.versionCode + ").");
                } catch (Exception ex) {
                    updateStatus.setText("Controleren op updates is mislukt.");
                }
            }
        }.execute();
    }

    private void runUpdateInstall(JLabel updateStatus, WindowsRelease release) {
        if (release == null) {
            updateStatus.setText("Controleer eerst op een update.");
            return;
        }
        updateStatus.setText("Setup wordt gedownload…");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                DesktopAppUpdater updater = new DesktopAppUpdater();
                File cache = new File(dataDir, "updates");
                File setup = updater.downloadSetup(release, cache);
                if (!updater.verifySha256(setup, release.sha256)) {
                    setup.delete();
                    throw new IOException("Integriteitscontrole mislukt (SHA-256).");
                }
                updater.launchInstaller(setup);
                prefs.setUpdateNoticeDismissedCode(release.versionCode);
                savePrefs();
                return setup.getAbsolutePath();
            }

            @Override
            protected void done() {
                try {
                    String path = get();
                    updateStatus.setText("Installatie gestart. Sluit deze app en volg de setup-wizard.\n" + path);
                    JOptionPane.showMessageDialog(owner,
                            "De Windows-setup is gestart.\nSluit Middin Innovatie en volg de installatiewizard.",
                            "Update",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    updateStatus.setText("Downloaden of installeren is mislukt.");
                }
            }
        }.execute();
    }
}
