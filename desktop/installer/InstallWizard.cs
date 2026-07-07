using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Windows.Forms;

internal sealed class InstallWizardForm : Form
{
    private const string EmbeddedPayloadName = "MiddinInnovatie.Payload.zip";
    private readonly string version = "0.9.3";
    private readonly string payloadDir;
    private readonly Panel content;
    private readonly Button backBtn;
    private readonly Button nextBtn;
    private readonly Button cancelBtn;
    private int step;

    private TextBox pathBox;
    private CheckBox desktopShortcut;
    private CheckBox startMenuShortcut;
    private CheckBox launchAfter;
    private Label statusLabel;
    private ProgressBar progressBar;
    private string installRoot;

    public InstallWizardForm()
    {
        payloadDir = ResolvePayloadDir();
        installRoot = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Programs", "MiddinInnovatie");

        Text = "Middin Innovatie Setup";
        AutoScaleMode = AutoScaleMode.Font;
        ClientSize = new Size(544, 400);
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(480, 360);

        var nav = new Panel { Dock = DockStyle.Bottom, Height = 56, Padding = new Padding(12, 10, 12, 10) };
        backBtn = new Button { Text = "< Terug", Width = 96, Height = 28, Anchor = AnchorStyles.Left | AnchorStyles.Top };
        nextBtn = new Button { Text = "Volgende >", Width = 104, Height = 28, Anchor = AnchorStyles.Right | AnchorStyles.Top };
        cancelBtn = new Button { Text = "Annuleren", Width = 96, Height = 28, Anchor = AnchorStyles.Right | AnchorStyles.Top };
        backBtn.Click += delegate { GoBack(); };
        nextBtn.Click += delegate { GoNext(); };
        cancelBtn.Click += delegate { Close(); };
        nav.Controls.Add(backBtn);
        nav.Controls.Add(cancelBtn);
        nav.Controls.Add(nextBtn);
        nav.Resize += delegate { LayoutNavButtons(nav); };

        content = new Panel { Dock = DockStyle.Fill, Padding = new Padding(20) };

        Controls.Add(nav);
        Controls.Add(content);
        LayoutNavButtons(nav);

        if (!HasPayload(payloadDir))
        {
            MessageBox.Show(
                "Installatiebestanden ontbreken.\nGebruik MiddinInnovatie-Windows-Setup.exe of pak het zip-bestand uit.",
                Text,
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
            Close();
            return;
        }

        ShowStep(0);
    }

    private void LayoutNavButtons(Panel nav)
    {
        int y = 12;
        int right = nav.ClientSize.Width - 12;
        nextBtn.Location = new Point(right - nextBtn.Width, y);
        cancelBtn.Location = new Point(nextBtn.Left - 8 - cancelBtn.Width, y);
        backBtn.Location = new Point(12, y);
    }

    private static string ResolvePayloadDir()
    {
        string local = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "payload");
        if (HasPayload(local)) return local;

        string embedded = ExtractEmbeddedPayload();
        if (embedded != null && HasPayload(embedded)) return embedded;

        return local;
    }

    private static bool HasPayload(string dir)
    {
        return Directory.Exists(dir)
            && File.Exists(Path.Combine(dir, "MiddinInnovatie.exe"))
            && File.Exists(Path.Combine(dir, "app", "MiddinInnovatie.jar"))
            && File.Exists(Path.Combine(dir, "runtime", "bin", "javaw.exe"));
    }

    private static string ExtractEmbeddedPayload()
    {
        try
        {
            Assembly asm = Assembly.GetExecutingAssembly();
            using (Stream stream = asm.GetManifestResourceStream(EmbeddedPayloadName))
            {
                if (stream == null) return null;
                string tempRoot = Path.Combine(
                    Path.GetTempPath(),
                    "MiddinInnovatieSetup-" + Guid.NewGuid().ToString("N"));
                string zipPath = Path.Combine(tempRoot, "payload.zip");
                Directory.CreateDirectory(tempRoot);
                using (FileStream file = File.Create(zipPath))
                {
                    stream.CopyTo(file);
                }
                string extractDir = Path.Combine(tempRoot, "payload");
                ZipFile.ExtractToDirectory(zipPath, extractDir);
                return extractDir;
            }
        }
        catch
        {
            return null;
        }
    }

    private void GoBack()
    {
        if (step > 0 && step < 4) ShowStep(step - 1);
    }

    private void GoNext()
    {
        if (step == 0) { ShowStep(1); return; }
        if (step == 1)
        {
            string bundled = Path.Combine(payloadDir, "runtime", "bin", "javaw.exe");
            if (!File.Exists(bundled) && FindJavaw() == null)
            {
                var r = MessageBox.Show(
                    "Java is niet gevonden en niet meegeleverd.\nDe app start mogelijk niet.\n\nToch doorgaan?",
                    Text,
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Warning);
                if (r != DialogResult.Yes) return;
            }
            ShowStep(2);
            return;
        }
        if (step == 2)
        {
            installRoot = pathBox.Text.Trim();
            if (installRoot.Length == 0)
            {
                MessageBox.Show("Kies een installatiemap.", Text, MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
            ShowStep(3);
            return;
        }
        if (step == 3)
        {
            ShowStep(4);
            RunInstall();
            return;
        }
        if (step == 5)
        {
            if (launchAfter.Checked)
            {
                string exe = Path.Combine(installRoot, "MiddinInnovatie.exe");
                if (File.Exists(exe)) Process.Start(exe);
            }
            Close();
        }
    }

    private void ShowStep(int s)
    {
        step = s;
        content.Controls.Clear();
        backBtn.Enabled = s > 0 && s < 4;
        nextBtn.Text = s == 5 ? "Voltooien" : s == 3 ? "Installeren" : "Volgende >";
        nextBtn.Enabled = s != 4;

        if (s == 0) BuildWelcome();
        else if (s == 1) BuildRequirements();
        else if (s == 2) BuildLocation();
        else if (s == 3) BuildOptions();
        else if (s == 4) BuildProgress();
        else if (s == 5) BuildFinish();
    }

    private void BuildWelcome()
    {
        AddTitle("Welkom bij Middin Innovatie Setup");
        AddBody(
            "Deze wizard installeert Middin Innovatie op uw computer.\n\n"
                + "Versie: " + version + "\n"
                + "Klik op Volgende om door te gaan.");
    }

    private void BuildRequirements()
    {
        AddTitle("Systeemvereisten");
        string bundled = Path.Combine(payloadDir, "runtime", "bin", "javaw.exe");
        string javaLine = File.Exists(bundled)
            ? "Java: meegeleverd met deze installatie (geen aparte installatie nodig)"
            : FindJavaw() != null
                ? "Java: gevonden op dit systeem\n  " + FindJavaw()
                : "Java: NIET gevonden — herinstalleer of installeer JDK/JRE 17+";
        AddBody(
            "Windows 10/11 (64-bit)\n"
                + javaLine + "\n\n"
                + "Schijfruimte: ca. 70 MB");
    }

    private void BuildLocation()
    {
        AddTitle("Installatiemap");
        AddSubtitle("Kies waar Middin Innovatie wordt geïnstalleerd:");
        pathBox = new TextBox
        {
            Text = installRoot,
            Width = 400,
            Location = new Point(20, 95),
            Anchor = AnchorStyles.Left | AnchorStyles.Right | AnchorStyles.Top
        };
        var browse = new Button { Text = "Bladeren...", Location = new Point(20, 130), Width = 100 };
        browse.Click += delegate
        {
            using (var dlg = new FolderBrowserDialog())
            {
                dlg.SelectedPath = pathBox.Text;
                dlg.Description = "Installatiemap kiezen";
                if (dlg.ShowDialog() == DialogResult.OK) pathBox.Text = dlg.SelectedPath;
            }
        };
        content.Controls.Add(pathBox);
        content.Controls.Add(browse);
    }

    private void BuildOptions()
    {
        AddTitle("Opties");
        AddSubtitle("Kies wat u wilt aanmaken:");
        desktopShortcut = new CheckBox
        {
            Text = "Snelkoppeling op het bureaublad",
            Checked = true,
            Location = new Point(20, 88),
            AutoSize = true
        };
        startMenuShortcut = new CheckBox
        {
            Text = "Snelkoppeling in het Start-menu",
            Checked = true,
            Location = new Point(20, 118),
            AutoSize = true
        };
        content.Controls.Add(desktopShortcut);
        content.Controls.Add(startMenuShortcut);
        desktopShortcut.BringToFront();
        startMenuShortcut.BringToFront();
    }

    private void BuildProgress()
    {
        AddTitle("Installeren...");
        statusLabel = new Label
        {
            Location = new Point(20, 100),
            Size = new Size(480, 60),
            Text = "Bezig met installeren..."
        };
        progressBar = new ProgressBar
        {
            Location = new Point(20, 170),
            Size = new Size(480, 24),
            Style = ProgressBarStyle.Continuous,
            Maximum = 100,
            Value = 0
        };
        content.Controls.Add(statusLabel);
        content.Controls.Add(progressBar);
    }

    private void BuildFinish()
    {
        AddTitle("Installatie voltooid");
        AddBody(
            "Middin Innovatie is geïnstalleerd in:\n"
                + installRoot + "\n\n"
                + "U kunt de app starten via het Start-menu of het bureaublad.\n\n"
                + "Updates: Meer > Instellingen > Controleren op update\n"
                + InstallerUrls.UpdateFeedUrl);
        launchAfter = new CheckBox
        {
            Text = "Middin Innovatie nu starten",
            Checked = true,
            Location = new Point(20, 200),
            AutoSize = true
        };
        content.Controls.Add(launchAfter);
    }

    private void RunInstall()
    {
        try
        {
            SetProgress(10, "Mappen aanmaken...");
            string appDir = Path.Combine(installRoot, "app");
            Directory.CreateDirectory(appDir);

            SetProgress(30, "Bestanden kopiëren...");
            CopyPayloadFile("MiddinInnovatie.exe", Path.Combine(installRoot, "MiddinInnovatie.exe"));
            CopyPayloadFile("Start Middin Innovatie.bat", Path.Combine(installRoot, "Start Middin Innovatie.bat"));
            CopyPayloadFile(Path.Combine("app", "MiddinInnovatie.jar"), Path.Combine(installRoot, "app", "MiddinInnovatie.jar"));
            CopyPayloadDirectory("runtime", Path.Combine(installRoot, "runtime"));
            CopyPayloadFile("MiddinInnovatie-Uninstall.exe", Path.Combine(installRoot, "MiddinInnovatie-Uninstall.exe"));

            SetProgress(60, "Snelkoppelingen aanmaken...");
            string target = Path.Combine(installRoot, "MiddinInnovatie.exe");
            if (startMenuShortcut.Checked) CreateStartMenuShortcut(target);
            if (desktopShortcut.Checked) CreateDesktopShortcut(target);

            SetProgress(80, "Verwijderen voorbereiden...");
            WriteUninstaller();

            SetProgress(100, "Klaar.");
            ShowStep(5);
        }
        catch (Exception ex)
        {
            MessageBox.Show("Installatie mislukt:\n" + ex.Message, Text, MessageBoxButtons.OK, MessageBoxIcon.Error);
            ShowStep(3);
        }
    }

    private void CopyPayloadFile(string relative, string destFile)
    {
        string src = Path.Combine(payloadDir, relative);
        if (!File.Exists(src)) throw new FileNotFoundException("Ontbreekt in payload: " + relative, src);
        string destDir = Path.GetDirectoryName(destFile);
        if (!string.IsNullOrEmpty(destDir)) Directory.CreateDirectory(destDir);
        File.Copy(src, destFile, true);
    }

    private void CopyPayloadDirectory(string relative, string destDir)
    {
        string src = Path.Combine(payloadDir, relative);
        if (!Directory.Exists(src)) throw new DirectoryNotFoundException("Ontbreekt in payload: " + relative);
        if (Directory.Exists(destDir)) Directory.Delete(destDir, true);
        CopyDirectoryRecursive(src, destDir);
    }

    private static void CopyDirectoryRecursive(string source, string dest)
    {
        Directory.CreateDirectory(dest);
        foreach (string file in Directory.GetFiles(source))
        {
            File.Copy(file, Path.Combine(dest, Path.GetFileName(file)), true);
        }
        foreach (string dir in Directory.GetDirectories(source))
        {
            CopyDirectoryRecursive(dir, Path.Combine(dest, Path.GetFileName(dir)));
        }
    }

    private void CreateStartMenuShortcut(string target)
    {
        string menuDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.Programs),
            "Middin Innovatie");
        Directory.CreateDirectory(menuDir);
        CreateShortcut(Path.Combine(menuDir, "Middin Innovatie.lnk"), target);
    }

    private void CreateDesktopShortcut(string target)
    {
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        CreateShortcut(Path.Combine(desktop, "Middin Innovatie.lnk"), target);
    }

    private static void CreateShortcut(string lnkPath, string target)
    {
        Type t = Type.GetTypeFromProgID("WScript.Shell");
        if (t == null) return;
        object shell = Activator.CreateInstance(t);
        object shortcut = t.InvokeMember("CreateShortcut", System.Reflection.BindingFlags.InvokeMethod, null, shell, new object[] { lnkPath });
        shortcut.GetType().InvokeMember("TargetPath", System.Reflection.BindingFlags.SetProperty, null, shortcut, new object[] { target });
        shortcut.GetType().InvokeMember("WorkingDirectory", System.Reflection.BindingFlags.SetProperty, null, shortcut, new object[] { Path.GetDirectoryName(target) });
        shortcut.GetType().InvokeMember("Description", System.Reflection.BindingFlags.SetProperty, null, shortcut, new object[] { "Middin Innovatie desktop" });
        shortcut.GetType().InvokeMember("Save", System.Reflection.BindingFlags.InvokeMethod, null, shortcut, null);
    }

    private void WriteUninstaller()
    {
        string uninstallExe = Path.Combine(installRoot, "MiddinInnovatie-Uninstall.exe");
        string menuDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.Programs),
            "Middin Innovatie");
        Directory.CreateDirectory(menuDir);
        CreateShortcut(Path.Combine(menuDir, "Middin Innovatie verwijderen.lnk"), uninstallExe);

        string uninstallPs1 = Path.Combine(installRoot, "uninstall.ps1");
        string script =
            "$ErrorActionPreference = 'Stop'\r\n"
            + "Start-Process -FilePath '" + uninstallExe.Replace("'", "''") + "' -Wait\r\n";
        File.WriteAllText(uninstallPs1, script);
    }

    private void SetProgress(int value, string message)
    {
        progressBar.Value = Math.Min(100, Math.Max(0, value));
        statusLabel.Text = message;
        Application.DoEvents();
    }

    private void AddTitle(string text)
    {
        content.Controls.Add(new Label
        {
            Text = text,
            Font = new Font(Font.FontFamily, 14, FontStyle.Bold),
            AutoSize = true,
            Location = new Point(20, 20)
        });
    }

    private void AddSubtitle(string text)
    {
        content.Controls.Add(new Label
        {
            Text = text,
            AutoSize = true,
            Location = new Point(20, 55)
        });
    }

    private void AddBody(string text)
    {
        content.Controls.Add(new Label
        {
            Text = text,
            AutoSize = false,
            Size = new Size(480, 120),
            Location = new Point(20, 55)
        });
    }

    private static string FindJavaw()
    {
        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            string c = Path.Combine(javaHome, "bin", "javaw.exe");
            if (File.Exists(c)) return c;
        }
        string[] known =
        {
            @"C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin\javaw.exe",
            @"C:\Program Files\Java\jdk-17\bin\javaw.exe",
            @"C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot\bin\javaw.exe",
        };
        foreach (string path in known)
        {
            if (File.Exists(path)) return path;
        }
        string pathEnv = Environment.GetEnvironmentVariable("PATH");
        if (string.IsNullOrWhiteSpace(pathEnv)) return null;
        foreach (string part in pathEnv.Split(';'))
        {
            if (string.IsNullOrWhiteSpace(part)) continue;
            string c = Path.Combine(part.Trim(), "javaw.exe");
            if (File.Exists(c)) return c;
        }
        return null;
    }
}

internal static class InstallWizardProgram
{
    [STAThread]
    private static void Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new InstallWizardForm());
    }
}
