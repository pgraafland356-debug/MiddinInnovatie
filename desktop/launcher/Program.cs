using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

internal static class Program
{
    [STAThread]
    private static int Main()
    {
        var dir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar);
        var jar = Path.Combine(dir, "app", "MiddinInnovatie.jar");
        if (!File.Exists(jar))
        {
            MessageBox.Show(
                "Kan MiddinInnovatie.jar niet vinden in:\n" + jar,
                "Middin Innovatie",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
            return 1;
        }

        var javaw = FindJavaw(dir);
        if (javaw == null)
        {
            MessageBox.Show(
                "Java niet gevonden.\n\n"
                    + "Herinstalleer Middin Innovatie (Java hoort bij de installatie te zitten),\n"
                    + "of installeer JDK 17+ en zet JAVA_HOME.",
                "Middin Innovatie",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
            return 1;
        }

        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = javaw,
                Arguments = "-jar \"" + jar + "\"",
                WorkingDirectory = dir,
                UseShellExecute = false,
            });
            return 0;
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                "Starten mislukt: " + ex.Message,
                "Middin Innovatie",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
            return 1;
        }
    }

    private static string FindJavaw(string appDir)
    {
        var bundled = Path.Combine(appDir, "runtime", "bin", "javaw.exe");
        if (File.Exists(bundled)) return bundled;

        var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            var candidate = Path.Combine(javaHome, "bin", "javaw.exe");
            if (File.Exists(candidate)) return candidate;
        }

        string[] known =
        {
            @"C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin\javaw.exe",
            @"C:\Program Files\Java\jdk-17\bin\javaw.exe",
            @"C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot\bin\javaw.exe",
        };
        foreach (var path in known)
        {
            if (File.Exists(path)) return path;
        }

        var pathEnv = Environment.GetEnvironmentVariable("PATH");
        if (string.IsNullOrWhiteSpace(pathEnv)) return null;
        foreach (var part in pathEnv.Split(';'))
        {
            if (string.IsNullOrWhiteSpace(part)) continue;
            var candidate = Path.Combine(part.Trim(), "javaw.exe");
            if (File.Exists(candidate)) return candidate;
        }
        return null;
    }
}
