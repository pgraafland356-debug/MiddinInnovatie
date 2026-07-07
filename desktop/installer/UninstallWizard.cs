using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

internal sealed class UninstallWizardForm : Form
{
    private readonly string version = "0.9.3";
    private readonly Panel content;
    private readonly Button backBtn;
    private readonly Button nextBtn;
    private readonly Button cancelBtn;
    private int step;

    private string installRoot;
    private string dataDir;
    private CheckBox keepUserData;
    private Label statusLabel;
    private ProgressBar progressBar;

    public UninstallWizardForm()
    {
        installRoot = DetectInstallRoot();
        dataDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "MiddinInnovatie");

        Text = "Middin Innovatie verwijderen";
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

        if (!Directory.Exists(installRoot))
        {
            MessageBox.Show(
                "Middin Innovatie is niet gevonden in:\n" + installRoot + "\n\n"
                    + "Mogelijk is de app al verwijderd.",
                Text,
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
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

    private static string DetectInstallRoot()
    {
        string exeDir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(
            Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar);
        if (File.Exists(Path.Combine(exeDir, "MiddinInnovatie.exe"))
            || File.Exists(Path.Combine(exeDir, "app", "MiddinInnovatie.jar")))
        {
            return exeDir;
        }
        return Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Programs", "MiddinInnovatie");
    }

    private void GoBack()
    {
        if (step > 0 && step < 3) ShowStep(step - 1);
    }

    private void GoNext()
    {
        if (step == 0) { ShowStep(1); return; }
        if (step == 1) { ShowStep(2); return; }
        if (step == 2)
        {
            var confirm = MessageBox.Show(
                "Weet u zeker dat u Middin Innovatie wilt verwijderen?",
                Text,
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Warning);
            if (confirm != DialogResult.Yes) return;
            ShowStep(3);
            RunUninstall();
            return;
        }
        if (step == 4) Close();
    }

    private void ShowStep(int s)
    {
        step = s;
        content.Controls.Clear();
        backBtn.Enabled = s > 0 && s < 3;
        nextBtn.Text = s == 4 ? "Sluiten" : s == 2 ? "Verwijderen" : "Volgende >";
        nextBtn.Enabled = s != 3;

        if (s == 0) BuildWelcome();
        else if (s == 1) BuildDetails();
        else if (s == 2) BuildOptions();
        else if (s == 3) BuildProgress();
        else if (s == 4) BuildFinish();
    }

    private void BuildWelcome()
    {
        AddTitle("Middin Innovatie verwijderen");
        AddBody(
            "Deze wizard verwijdert Middin Innovatie van uw computer.\n\n"
                + "Versie: " + version + "\n"
                + "Installatiemap:\n" + installRoot);
    }

    private void BuildDetails()
    {
        AddTitle("Wat wordt verwijderd");
        AddBody(
            "Programmabestanden:\n" + installRoot + "\n\n"
                + "Snelkoppelingen:\n"
                + "- Bureaublad: Middin Innovatie\n"
                + "- Start-menu: Middin Innovatie\n\n"
                + "Gebruikersdata (optioneel):\n" + dataDir);
    }

    private void BuildOptions()
    {
        AddTitle("Opties");
        AddSubtitle("Kies wat u wilt bewaren:");
        keepUserData = new CheckBox
        {
            Text = "Gebruikersdata bewaren (chat, geheugen, instellingen)",
            Checked = true,
            Location = new Point(20, 88),
            AutoSize = true
        };
        content.Controls.Add(keepUserData);
        keepUserData.BringToFront();
        AddBodyAt(
            "Als u gebruikersdata bewaart, blijven uw instellingen en opgeslagen berichten staan.\n"
                + "Alleen het programma en snelkoppelingen worden verwijderd.",
            120, 140);
    }

    private void BuildProgress()
    {
        AddTitle("Verwijderen...");
        statusLabel = new Label
        {
            Location = new Point(20, 100),
            Size = new Size(480, 60),
            Text = "Bezig met verwijderen..."
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
        AddTitle("Verwijdering voltooid");
        string dataNote = keepUserData != null && keepUserData.Checked
            ? "Uw gebruikersdata is bewaard in:\n" + dataDir
            : "Programma en gebruikersdata zijn verwijderd.";
        string releases = InstallerUrls.ReleasesPageUrl;
        string extra = releases.Length > 0
            ? "\n\nLater opnieuw installeren?\n" + releases
            : "";
        AddBody("Middin Innovatie is verwijderd.\n\n" + dataNote + extra);
    }

    private void RunUninstall()
    {
        try
        {
            SetProgress(10, "App afsluiten...");
            StopAppProcesses();

            SetProgress(30, "Snelkoppelingen verwijderen...");
            RemoveShortcuts();

            if (keepUserData == null || !keepUserData.Checked)
            {
                SetProgress(55, "Gebruikersdata verwijderen...");
                TryDeleteDirectory(dataDir);
            }
            else
            {
                SetProgress(55, "Gebruikersdata bewaren...");
            }

            SetProgress(80, "Programmabestanden verwijderen...");
            ScheduleInstallFolderDelete(installRoot);

            SetProgress(100, "Klaar.");
            ShowStep(4);
        }
        catch (Exception ex)
        {
            MessageBox.Show("Verwijderen mislukt:\n" + ex.Message, Text, MessageBoxButtons.OK, MessageBoxIcon.Error);
            ShowStep(2);
        }
    }

    private static void StopAppProcesses()
    {
        foreach (Process p in Process.GetProcessesByName("javaw"))
        {
            try
            {
                string path = "";
                try { path = p.MainModule.FileName; } catch { }
                if (path.IndexOf("MiddinInnovatie", StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    p.Kill();
                    p.WaitForExit(3000);
                }
            }
            catch { }
        }
        foreach (Process p in Process.GetProcessesByName("MiddinInnovatie"))
        {
            try
            {
                p.Kill();
                p.WaitForExit(3000);
            }
            catch { }
        }
    }

    private void RemoveShortcuts()
    {
        string menuDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.Programs),
            "Middin Innovatie");
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        TryDeleteFile(Path.Combine(desktop, "Middin Innovatie.lnk"));
        TryDeleteFile(Path.Combine(menuDir, "Middin Innovatie.lnk"));
        TryDeleteFile(Path.Combine(menuDir, "Middin Innovatie verwijderen.lnk"));
        TryDeleteDirectory(menuDir);
    }

    private static void ScheduleInstallFolderDelete(string folder)
    {
        string bat = Path.Combine(Path.GetTempPath(), "middin-uninstall-" + Guid.NewGuid().ToString("N") + ".cmd");
        string script =
            "@echo off\r\n"
            + "timeout /t 2 /nobreak >nul\r\n"
            + "rd /s /q \"" + folder + "\"\r\n"
            + "del \"%~f0\"\r\n";
        File.WriteAllText(bat, script);
        Process.Start(new ProcessStartInfo
        {
            FileName = bat,
            CreateNoWindow = true,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Hidden,
        });
    }

    private static void TryDeleteDirectory(string path)
    {
        if (!Directory.Exists(path)) return;
        try { Directory.Delete(path, true); }
        catch { }
    }

    private static void TryDeleteFile(string path)
    {
        if (!File.Exists(path)) return;
        try { File.Delete(path); }
        catch { }
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
        AddBodyAt(text, 55, 120);
    }

    private void AddBodyAt(string text, int y, int height)
    {
        content.Controls.Add(new Label
        {
            Text = text,
            AutoSize = false,
            Size = new Size(480, height),
            Location = new Point(20, y)
        });
    }
}

internal static class UninstallWizardProgram
{
    [STAThread]
    private static void Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new UninstallWizardForm());
    }
}
