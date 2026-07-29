using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;

/// <summary>
/// Standalone Middin Innovatie update program for Windows.
/// Checks releases/latest.json, downloads the setup EXE, verifies SHA-256, launches installer.
/// </summary>
internal sealed class UpdateWizardForm : Form
{
    // Middin brand colors (aligned with desktop app)
    private static readonly Color BrandPrimary = Color.FromArgb(0x00, 0x1A, 0x9E);
    private static readonly Color BrandPrimaryLight = Color.FromArgb(0xF2, 0xF3, 0xFA);
    private static readonly Color BrandBorder = Color.FromArgb(0xCC, 0xCC, 0xCC);
    private static readonly Color BrandMuted = Color.FromArgb(0x77, 0x77, 0x77);
    private static readonly Color BrandOk = Color.FromArgb(0x1B, 0x7A, 0x4E);
    private static readonly Color BrandWarn = Color.FromArgb(0xB2, 0x6A, 0x00);
    private static readonly Color BrandErr = Color.FromArgb(0xB0, 0x00, 0x20);

    private readonly bool silentMode;
    private readonly string defaultInstallDir;
    private string installDir;
    private int installedVersionCode;
    private string installedVersionName;

    private Label headerTitle;
    private Label headerSub;
    private Label installedValue;
    private Label remoteValue;
    private Label statusBadge;
    private Label statusMessage;
    private TextBox changelogBox;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Button checkBtn;
    private Button installBtn;
    private Button releasesBtn;
    private Button closeBtn;

    private ReleaseInfo pendingRelease;

    private UpdateWizardForm(bool silent)
    {
        silentMode = silent;
        defaultInstallDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Programs", "MiddinInnovatie");
        installDir = defaultInstallDir;
        ReadInstalledVersion();

        Text = "Middin Innovatie bijwerken";
        AutoScaleMode = AutoScaleMode.Font;
        ClientSize = new Size(600, 520);
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        StartPosition = FormStartPosition.CenterScreen;
        BackColor = Color.White;
        Font = new Font("Segoe UI", 9f, FontStyle.Regular);

        BuildUi();

        Shown += delegate
        {
            if (silentMode) BeginCheck(true);
            else if (!string.IsNullOrEmpty(InstallerUrls.UpdateFeedUrl)) BeginCheck(false);
        };
    }

    private void BuildUi()
    {
        // Header band
        Panel header = new Panel
        {
            Location = new Point(0, 0),
            Size = new Size(600, 72),
            BackColor = BrandPrimary
        };
        headerTitle = new Label
        {
            Location = new Point(20, 14),
            Size = new Size(560, 28),
            Font = new Font("Segoe UI", 14f, FontStyle.Bold),
            ForeColor = Color.White,
            BackColor = BrandPrimary,
            Text = "Middin Innovatie Update"
        };
        headerSub = new Label
        {
            Location = new Point(20, 42),
            Size = new Size(560, 20),
            Font = new Font("Segoe UI", 8.5f, FontStyle.Regular),
            ForeColor = Color.FromArgb(220, 220, 255),
            BackColor = BrandPrimary,
            Text = "Controleert de GitHub-releasefeed en installeert de Windows-setup"
        };
        header.Controls.Add(headerTitle);
        header.Controls.Add(headerSub);

        // Version cards row
        Panel installedCard = MakeInfoCard(20, 88, 270, 78, "Geinstalleerd", out installedValue);
        Panel remoteCard = MakeInfoCard(310, 88, 270, 78, "Op GitHub", out remoteValue);
        installedValue.Text = FormatVersion(installedVersionName, installedVersionCode);
        remoteValue.Text = "Nog niet gecontroleerd";

        // Status badge + message
        statusBadge = new Label
        {
            Location = new Point(20, 180),
            Size = new Size(140, 26),
            Font = new Font("Segoe UI", 8.5f, FontStyle.Bold),
            TextAlign = ContentAlignment.MiddleCenter,
            Text = "KLAAR",
            ForeColor = Color.White,
            BackColor = BrandPrimary
        };
        statusMessage = new Label
        {
            Location = new Point(170, 180),
            Size = new Size(410, 48),
            Font = new Font("Segoe UI", 9f, FontStyle.Regular),
            ForeColor = BrandMuted,
            Text = "Klik op Controleren om te zien of er een nieuwere versie is."
        };

        Label changelogTitle = new Label
        {
            Location = new Point(20, 236),
            Size = new Size(200, 18),
            Font = new Font("Segoe UI", 8.5f, FontStyle.Bold),
            ForeColor = BrandPrimary,
            Text = "Wijzigingen"
        };
        changelogBox = new TextBox
        {
            Location = new Point(20, 256),
            Size = new Size(560, 110),
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            BorderStyle = BorderStyle.FixedSingle,
            BackColor = BrandPrimaryLight,
            ForeColor = BrandPrimary,
            Font = new Font("Segoe UI", 9f, FontStyle.Regular),
            Text = "Geen update-info geladen."
        };

        progressLabel = new Label
        {
            Location = new Point(20, 378),
            Size = new Size(560, 16),
            Font = new Font("Segoe UI", 8f, FontStyle.Regular),
            ForeColor = BrandMuted,
            Text = "",
            Visible = false
        };
        progressBar = new ProgressBar
        {
            Location = new Point(20, 396),
            Size = new Size(560, 16),
            Style = ProgressBarStyle.Continuous,
            Visible = false
        };

        checkBtn = MakeButton("Controleren", 20, 430, 150, true);
        installBtn = MakeButton("Downloaden & installeren", 180, 430, 200, true);
        installBtn.Enabled = false;
        releasesBtn = MakeButton("GitHub", 390, 430, 90, false);
        closeBtn = MakeButton("Sluiten", 490, 430, 90, false);

        checkBtn.Click += delegate { BeginCheck(false); };
        installBtn.Click += delegate { BeginInstall(); };
        releasesBtn.Click += delegate { OpenReleasesPage(); };
        closeBtn.Click += delegate { Close(); };

        Label footer = new Label
        {
            Location = new Point(20, 478),
            Size = new Size(560, 28),
            Font = new Font("Segoe UI", 7.5f, FontStyle.Regular),
            ForeColor = BrandMuted,
            Text = ShortPath(installDir)
                + (string.IsNullOrEmpty(InstallerUrls.UpdateFeedUrl)
                    ? "  |  feed niet geconfigureerd"
                    : "  |  feed: GitHub raw latest.json")
        };

        Controls.Add(header);
        Controls.Add(installedCard);
        Controls.Add(remoteCard);
        Controls.Add(statusBadge);
        Controls.Add(statusMessage);
        Controls.Add(changelogTitle);
        Controls.Add(changelogBox);
        Controls.Add(progressLabel);
        Controls.Add(progressBar);
        Controls.Add(checkBtn);
        Controls.Add(installBtn);
        Controls.Add(releasesBtn);
        Controls.Add(closeBtn);
        Controls.Add(footer);
    }

    private static Panel MakeInfoCard(int x, int y, int w, int h, string title, out Label valueLabel)
    {
        Panel card = new Panel
        {
            Location = new Point(x, y),
            Size = new Size(w, h),
            BackColor = BrandPrimaryLight,
            BorderStyle = BorderStyle.FixedSingle
        };
        Label titleLbl = new Label
        {
            Location = new Point(12, 8),
            Size = new Size(w - 24, 18),
            Font = new Font("Segoe UI", 8f, FontStyle.Bold),
            ForeColor = BrandMuted,
            BackColor = BrandPrimaryLight,
            Text = title.ToUpperInvariant()
        };
        valueLabel = new Label
        {
            Location = new Point(12, 30),
            Size = new Size(w - 24, 36),
            Font = new Font("Segoe UI", 11f, FontStyle.Bold),
            ForeColor = BrandPrimary,
            BackColor = BrandPrimaryLight,
            Text = "—"
        };
        card.Controls.Add(titleLbl);
        card.Controls.Add(valueLabel);
        return card;
    }

    private static Button MakeButton(string text, int x, int y, int width, bool primary)
    {
        Button b = new Button
        {
            Text = text,
            Location = new Point(x, y),
            Size = new Size(width, 36),
            FlatStyle = FlatStyle.Flat,
            Font = new Font("Segoe UI", 9f, primary ? FontStyle.Bold : FontStyle.Regular),
            Cursor = Cursors.Hand
        };
        if (primary)
        {
            b.BackColor = BrandPrimary;
            b.ForeColor = Color.White;
            b.FlatAppearance.BorderSize = 0;
        }
        else
        {
            b.BackColor = Color.White;
            b.ForeColor = BrandPrimary;
            b.FlatAppearance.BorderColor = BrandBorder;
            b.FlatAppearance.BorderSize = 1;
        }
        return b;
    }

    private static string FormatVersion(string name, int code)
    {
        if (string.IsNullOrEmpty(name) || name == "onbekend")
            return "code " + code;
        return name + "  ·  code " + code;
    }

    private static string ShortPath(string path)
    {
        if (string.IsNullOrEmpty(path)) return "";
        if (path.Length <= 70) return path;
        return "..." + path.Substring(path.Length - 67);
    }

    private void SetUiState(string badge, Color badgeColor, string message, string changelog)
    {
        statusBadge.Text = badge;
        statusBadge.BackColor = badgeColor;
        statusMessage.Text = message;
        if (changelog != null) changelogBox.Text = changelog;
    }

    private void ReadInstalledVersion()
    {
        installedVersionCode = 0;
        installedVersionName = "onbekend";
        string versionFile = Path.Combine(installDir, "version.json");
        if (!File.Exists(versionFile))
        {
            if (File.Exists(Path.Combine(installDir, "MiddinInnovatie.exe")))
            {
                installedVersionName = "geinstalleerd (versie onbekend)";
            }
            else
            {
                installedVersionName = "niet geinstalleerd";
            }
            return;
        }

        try
        {
            string json = File.ReadAllText(versionFile, Encoding.UTF8);
            if (json.Length > 0 && json[0] == '\uFEFF') json = json.Substring(1);
            installedVersionCode = ExtractInt(json, "versionCode");
            string name = ExtractString(json, "versionName");
            if (!string.IsNullOrEmpty(name)) installedVersionName = name;
        }
        catch
        {
            installedVersionName = "onbekend";
        }
    }

    private void BeginCheck(bool autoInstall)
    {
        string feed = InstallerUrls.UpdateFeedUrl != null ? InstallerUrls.UpdateFeedUrl.Trim() : "";
        if (string.IsNullOrEmpty(feed) || feed.Contains("YOUR_"))
        {
            SetUiState("FOUT", BrandErr, "Update-feed is niet geconfigureerd.", "Zet middin.github.owner in gradle.properties en bouw opnieuw.");
            if (silentMode) Environment.Exit(2);
            return;
        }

        checkBtn.Enabled = false;
        installBtn.Enabled = false;
        pendingRelease = null;
        SetUiState("BEZIG", BrandWarn, "Controleren op update...", changelogBox.Text);

        ThreadPool.QueueUserWorkItem(delegate
        {
            string error = null;
            ReleaseInfo release = null;
            int remoteVersionCode = 0;
            string remoteVersionName = "";
            string remoteChangelog = "";
            try
            {
                string body = DownloadFeed(feed);
                remoteVersionCode = ExtractInt(body, "versionCode");
                remoteVersionName = ExtractString(body, "versionName") ?? "";
                remoteChangelog = ExtractString(body, "changelog") ?? "";
                release = ParseWindowsRelease(body, installedVersionCode);
            }
            catch (Exception ex)
            {
                error = ex.Message;
            }

            BeginInvoke(new Action(delegate
            {
                checkBtn.Enabled = true;
                remoteValue.Text = remoteVersionCode > 0
                    ? FormatVersion(remoteVersionName, remoteVersionCode)
                    : "onbekend";

                if (error != null)
                {
                    SetUiState("FOUT", BrandErr, "Fout bij controleren: " + error, remoteChangelog);
                    if (silentMode) Environment.Exit(2);
                    return;
                }

                if (release == null)
                {
                    if (remoteVersionCode > 0 && remoteVersionCode < installedVersionCode)
                    {
                        SetUiState(
                            "LOKAAL NIEUWER",
                            BrandWarn,
                            "Geen update op GitHub — jouw installatie is nieuwer. Lokale codewijzigingen gaan niet via de updater.",
                            string.IsNullOrEmpty(remoteChangelog) ? "Geen changelog." : remoteChangelog);
                    }
                    else
                    {
                        SetUiState(
                            "ACTUEEL",
                            BrandOk,
                            "Je gebruikt al de nieuwste versie op GitHub.",
                            string.IsNullOrEmpty(remoteChangelog) ? "Geen changelog." : remoteChangelog);
                    }
                    if (silentMode) Environment.Exit(0);
                    return;
                }

                pendingRelease = release;
                string version = string.IsNullOrEmpty(release.VersionName)
                    ? release.VersionCode.ToString() : release.VersionName;
                remoteValue.Text = FormatVersion(release.VersionName, release.VersionCode);
                SetUiState(
                    "UPDATE",
                    BrandPrimary,
                    "Update beschikbaar: versie " + version + " (code " + release.VersionCode + "). Klik op Downloaden & installeren.",
                    string.IsNullOrEmpty(release.Changelog) ? "Geen changelog." : release.Changelog);
                installBtn.Enabled = true;
                if (autoInstall) BeginInstall();
            }));
        });
    }

    private void BeginInstall()
    {
        if (pendingRelease == null) return;

        checkBtn.Enabled = false;
        installBtn.Enabled = false;
        releasesBtn.Enabled = false;
        progressBar.Visible = true;
        progressLabel.Visible = true;
        progressBar.Value = 0;
        progressLabel.Text = "Downloaden van setup...";
        SetUiState("DOWNLOAD", BrandWarn, "Setup wordt gedownload...", null);

        string cacheDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "MiddinInnovatie", "updates");
        Directory.CreateDirectory(cacheDir);
        string setupPath = Path.Combine(cacheDir, "MiddinInnovatie-Setup-" + pendingRelease.VersionCode + ".exe");

        using (var wc = new WebClient())
        {
            wc.DownloadProgressChanged += delegate(object s, DownloadProgressChangedEventArgs e)
            {
                if (e.ProgressPercentage >= 0)
                {
                    BeginInvoke(new Action(delegate
                    {
                        progressBar.Value = Math.Min(100, e.ProgressPercentage);
                        progressLabel.Text = "Downloaden... " + e.ProgressPercentage + "%";
                        SetUiState("DOWNLOAD", BrandWarn, "Downloaden... " + e.ProgressPercentage + "%", null);
                    }));
                }
            };
            wc.DownloadFileCompleted += delegate(object s, AsyncCompletedEventArgs e)
            {
                BeginInvoke(new Action(delegate { OnDownloadComplete(e, setupPath); }));
            };
            try
            {
                wc.DownloadFileAsync(new Uri(pendingRelease.SetupUrl), setupPath);
            }
            catch (Exception ex)
            {
                OnDownloadComplete(new AsyncCompletedEventArgs(ex, false, null), setupPath);
            }
        }
    }

    private void OnDownloadComplete(AsyncCompletedEventArgs e, string setupPath)
    {
        releasesBtn.Enabled = true;
        checkBtn.Enabled = true;

        if (e.Error != null)
        {
            progressBar.Visible = false;
            progressLabel.Visible = false;
            SetUiState("FOUT", BrandErr, "Download mislukt: " + e.Error.Message, null);
            installBtn.Enabled = pendingRelease != null;
            if (silentMode) Environment.Exit(2);
            return;
        }

        progressLabel.Text = "Controleren van bestand (SHA-256)...";
        SetUiState("CHECK", BrandWarn, "SHA-256 controleren...", null);
        try
        {
            if (!VerifySha256(setupPath, pendingRelease.Sha256))
            {
                progressBar.Visible = false;
                progressLabel.Visible = false;
                File.Delete(setupPath);
                SetUiState("FOUT", BrandErr, "SHA-256 komt niet overeen. Download geannuleerd.", null);
                installBtn.Enabled = true;
                if (silentMode) Environment.Exit(2);
                return;
            }

            SetUiState("START", BrandOk, "Setup wordt gestart. Sluit Middin Innovatie als die nog open staat.", null);
            progressLabel.Text = "Installer starten...";
            StopRunningApp();
            Process.Start(new ProcessStartInfo
            {
                FileName = setupPath,
                WorkingDirectory = Path.GetDirectoryName(setupPath),
                UseShellExecute = true
            });
            if (silentMode) Environment.Exit(1);
            Close();
        }
        catch (Exception ex)
        {
            progressBar.Visible = false;
            progressLabel.Visible = false;
            SetUiState("FOUT", BrandErr, "Installatie starten mislukt: " + ex.Message, null);
            installBtn.Enabled = true;
            if (silentMode) Environment.Exit(2);
        }
    }

    private static void StopRunningApp()
    {
        foreach (string name in new[] { "javaw", "java" })
        {
            foreach (Process p in Process.GetProcessesByName(name))
            {
                try
                {
                    string path = p.MainModule != null ? p.MainModule.FileName : "";
                    if (path.IndexOf("MiddinInnovatie", StringComparison.OrdinalIgnoreCase) >= 0
                        || path.IndexOf("middin", StringComparison.OrdinalIgnoreCase) >= 0)
                    {
                        p.Kill();
                        p.WaitForExit(3000);
                    }
                }
                catch
                {
                    // ignore processes we cannot inspect
                }
            }
        }
    }

    private static void OpenReleasesPage()
    {
        string url = InstallerUrls.ReleasesPageUrl;
        if (string.IsNullOrEmpty(url)) return;
        Process.Start(new ProcessStartInfo { FileName = url, UseShellExecute = true });
    }

    private static string DownloadFeed(string feedUrl)
    {
        using (var wc = new WebClient())
        {
            wc.Headers.Add(HttpRequestHeader.UserAgent, "MiddinInnovatie-Updater/" + InstallerVersion.VERSION_NAME);
            string body = wc.DownloadString(feedUrl);
            if (body.Length > 0 && body[0] == '\uFEFF') body = body.Substring(1);
            return body.Trim();
        }
    }

    private static ReleaseInfo ParseWindowsRelease(string json, int currentVersionCode)
    {
        int versionCode = ExtractInt(json, "versionCode");
        if (versionCode <= currentVersionCode) return null;

        string windowsBlock = ExtractObject(json, "windows");
        string setupUrl;
        string sha256;
        if (windowsBlock != null)
        {
            setupUrl = ExtractString(windowsBlock, "setupUrl");
            sha256 = ExtractString(windowsBlock, "sha256");
        }
        else
        {
            setupUrl = ExtractString(json, "setupUrl");
            sha256 = ExtractString(json, "sha256");
        }

        if (string.IsNullOrEmpty(setupUrl) || string.IsNullOrEmpty(sha256))
        {
            throw new IOException("Manifest mist windows.setupUrl of sha256");
        }

        return new ReleaseInfo(
            versionCode,
            ExtractString(json, "versionName"),
            setupUrl,
            sha256,
            ExtractString(json, "changelog"));
    }

    private static bool VerifySha256(string filePath, string expectedSha)
    {
        using (var sha = SHA256.Create())
        using (var stream = File.OpenRead(filePath))
        {
            byte[] hash = sha.ComputeHash(stream);
            var sb = new StringBuilder();
            foreach (byte b in hash) sb.Append(b.ToString("x2"));
            return sb.ToString().Equals(expectedSha.Trim(), StringComparison.OrdinalIgnoreCase);
        }
    }

    private static int ExtractInt(string json, string key)
    {
        Match m = Regex.Match(json, "\"" + Regex.Escape(key) + "\"\\s*:\\s*(\\d+)");
        if (!m.Success) throw new IOException("Manifest mist " + key);
        return int.Parse(m.Groups[1].Value);
    }

    private static string ExtractString(string json, string key)
    {
        Match m = Regex.Match(json, "\"" + Regex.Escape(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        if (!m.Success) return null;
        return UnescapeJson(m.Groups[1].Value);
    }

    private static string ExtractObject(string json, string key)
    {
        Match m = Regex.Match(json, "\"" + Regex.Escape(key) + "\"\\s*:\\s*\\{");
        if (!m.Success) return null;
        int start = m.Index + m.Value.Length - 1;
        int depth = 0;
        for (int i = start; i < json.Length; i++)
        {
            char c = json[i];
            if (c == '{') depth++;
            else if (c == '}')
            {
                depth--;
                if (depth == 0) return json.Substring(start, i - start + 1);
            }
        }
        return null;
    }

    private static string UnescapeJson(string s)
    {
        return s.Replace("\\\"", "\"").Replace("\\\\", "\\").Replace("\\n", "\n").Replace("\\r", "\r");
    }

    private sealed class ReleaseInfo
    {
        internal readonly int VersionCode;
        internal readonly string VersionName;
        internal readonly string SetupUrl;
        internal readonly string Sha256;
        internal readonly string Changelog;

        internal ReleaseInfo(int versionCode, string versionName, string setupUrl, string sha256, string changelog)
        {
            VersionCode = versionCode;
            VersionName = versionName ?? "";
            SetupUrl = setupUrl;
            Sha256 = sha256;
            Changelog = changelog ?? "";
        }
    }

    [STAThread]
    private static void Main(string[] args)
    {
        bool silent = false;
        foreach (string arg in args)
        {
            if (arg.Equals("/silent", StringComparison.OrdinalIgnoreCase)
                || arg.Equals("--silent", StringComparison.OrdinalIgnoreCase))
            {
                silent = true;
            }
        }

        ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12 | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls;
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new UpdateWizardForm(silent));
    }
}
