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
    private readonly bool silentMode;
    private readonly string defaultInstallDir;
    private string installDir;
    private int installedVersionCode;
    private string installedVersionName;

    private Label titleLabel;
    private Label bodyLabel;
    private ProgressBar progressBar;
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
        ClientSize = new Size(560, 420);
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        StartPosition = FormStartPosition.CenterScreen;

        titleLabel = new Label
        {
            Location = new Point(20, 16),
            Size = new Size(520, 28),
            Font = new Font(Font.FontFamily, 12f, FontStyle.Bold),
            Text = "Middin Innovatie Update"
        };

        bodyLabel = new Label
        {
            Location = new Point(20, 52),
            Size = new Size(520, 220),
            Text = BuildStatusText("Klik op Controleren om te zien of er een nieuwere versie is.")
        };

        progressBar = new ProgressBar
        {
            Location = new Point(20, 280),
            Size = new Size(520, 22),
            Style = ProgressBarStyle.Continuous,
            Visible = false
        };

        checkBtn = new Button { Text = "Controleren op update", Location = new Point(20, 320), Width = 160, Height = 30 };
        installBtn = new Button { Text = "Downloaden en installeren", Location = new Point(190, 320), Width = 180, Height = 30, Enabled = false };
        releasesBtn = new Button { Text = "GitHub releases", Location = new Point(380, 320), Width = 120, Height = 30 };
        closeBtn = new Button { Text = "Sluiten", Location = new Point(460, 360), Width = 80, Height = 30 };

        checkBtn.Click += delegate { BeginCheck(false); };
        installBtn.Click += delegate { BeginInstall(); };
        releasesBtn.Click += delegate { OpenReleasesPage(); };
        closeBtn.Click += delegate { Close(); };

        Controls.Add(titleLabel);
        Controls.Add(bodyLabel);
        Controls.Add(progressBar);
        Controls.Add(checkBtn);
        Controls.Add(installBtn);
        Controls.Add(releasesBtn);
        Controls.Add(closeBtn);

        Shown += delegate
        {
            if (silentMode) BeginCheck(true);
            else if (!string.IsNullOrEmpty(InstallerUrls.UpdateFeedUrl)) BeginCheck(false);
        };
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
                installedVersionName = "geïnstalleerd (versie onbekend)";
            }
            else
            {
                installedVersionName = "niet geïnstalleerd";
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

    private string BuildStatusText(string message)
    {
        string feed = string.IsNullOrEmpty(InstallerUrls.UpdateFeedUrl)
            ? "(feed niet geconfigureerd — zet middin.github.owner in gradle.properties)"
            : InstallerUrls.UpdateFeedUrl;
        return "Installatiemap:\n  " + installDir
            + "\n\nGeïnstalleerde versie: " + installedVersionName
            + " (code " + installedVersionCode + ")"
            + "\n\nUpdate-feed:\n  " + feed
            + "\n\n" + message;
    }

    private void SetStatus(string message)
    {
        bodyLabel.Text = BuildStatusText(message);
    }

    private void BeginCheck(bool autoInstall)
    {
        string feed = InstallerUrls.UpdateFeedUrl != null ? InstallerUrls.UpdateFeedUrl.Trim() : "";
        if (string.IsNullOrEmpty(feed) || feed.Contains("YOUR_"))
        {
            SetStatus("Update-feed is niet geconfigureerd.");
            if (silentMode) Environment.Exit(2);
            return;
        }

        checkBtn.Enabled = false;
        installBtn.Enabled = false;
        pendingRelease = null;
        SetStatus("Controleren op update...");

        ThreadPool.QueueUserWorkItem(delegate
        {
            string error = null;
            ReleaseInfo release = null;
            int remoteVersionCode = 0;
            string remoteVersionName = "";
            try
            {
                string body = DownloadFeed(feed);
                remoteVersionCode = ExtractInt(body, "versionCode");
                remoteVersionName = ExtractString(body, "versionName");
                if (remoteVersionName == null) remoteVersionName = "";
                release = ParseWindowsRelease(body, installedVersionCode);
            }
            catch (Exception ex)
            {
                error = ex.Message;
            }

            BeginInvoke(new Action(delegate
            {
                checkBtn.Enabled = true;
                if (error != null)
                {
                    SetStatus("Fout bij controleren:\n" + error);
                    if (silentMode) Environment.Exit(2);
                    return;
                }

                if (release == null)
                {
                    if (remoteVersionCode > 0 && remoteVersionCode < installedVersionCode)
                    {
                        SetStatus(
                            "Geen update op GitHub (versie "
                            + remoteVersionName + " / code " + remoteVersionCode + ").\n\n"
                            + "Jouw installatie is nieuwer (code " + installedVersionCode + "). "
                            + "Lokale codewijzigingen gaan niet automatisch via de updater.\n\n"
                            + "• Lokaal testen: .\\scripts\\release-checklist.ps1 -DeployLocal\n"
                            + "• Uitrollen: -BumpPatch -Publish (en push releases/latest.json)");
                    }
                    else
                    {
                        SetStatus(
                            "U gebruikt al de nieuwste versie op GitHub ("
                            + remoteVersionName + " / code " + remoteVersionCode + ").\n\n"
                            + "Nieuwe codewijzigingen vereisen een hogere versionCode, rebuild en publicatie.");
                    }
                    if (silentMode) Environment.Exit(0);
                    return;
                }

                pendingRelease = release;
                string version = string.IsNullOrEmpty(release.VersionName)
                    ? release.VersionCode.ToString() : release.VersionName;
                string changelog = string.IsNullOrEmpty(release.Changelog)
                    ? "" : "\n\nWijzigingen:\n" + release.Changelog;
                SetStatus("Update beschikbaar: versie " + version + " (code " + release.VersionCode + ")." + changelog);
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
        progressBar.Value = 0;
        SetStatus("Downloaden van setup...");

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
                        SetStatus("Downloaden... " + e.ProgressPercentage + "%");
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
            SetStatus("Download mislukt:\n" + e.Error.Message);
            installBtn.Enabled = pendingRelease != null;
            if (silentMode) Environment.Exit(2);
            return;
        }

        SetStatus("Controleren van bestand (SHA-256)...");
        try
        {
            if (!VerifySha256(setupPath, pendingRelease.Sha256))
            {
                progressBar.Visible = false;
                File.Delete(setupPath);
                SetStatus("SHA-256 komt niet overeen. Download geannuleerd.");
                installBtn.Enabled = true;
                if (silentMode) Environment.Exit(2);
                return;
            }

            SetStatus("Setup wordt gestart. Sluit Middin Innovatie als die nog open staat.");
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
            SetStatus("Installatie starten mislukt:\n" + ex.Message);
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

    private static ReleaseInfo FetchLatestRelease(string feedUrl, int currentVersionCode)
    {
        string body = DownloadFeed(feedUrl);
        return ParseWindowsRelease(body.Trim(), currentVersionCode);
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
