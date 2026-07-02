# Middin Innovatie – Windows desktop

Desktop companion for the Android app: **product catalog** and **offline product assistant**.

**Runs on:** Windows 10/11 (64-bit). Java is bundled — no separate install needed.

## Installer for colleagues (recommended)

```powershell
.\scripts\create-windows-installer.ps1
```

Share: **`MiddinInnovatie-Windows-Setup-0.9.2.exe`** (one file, Java included)

Colleagues:
1. Double-click **`MiddinInnovatie-Windows-Setup-0.9.2.exe`**
2. Follow the setup wizard
3. App installs to `%LOCALAPPDATA%\Programs\MiddinInnovatie` with Start menu + desktop shortcut

No separate Java install needed.

## Portable (no install)

```powershell
.\scripts\build-desktop.ps1 -InstallerType app-image
```

Run: `desktop\build\dist\MiddinInnovatie\MiddinInnovatie.exe`

## Development

```powershell
.\gradlew.bat :desktop:run
```

## What is included

| Feature | Desktop | Android app |
|--------|---------|-------------|
| Product catalog (17 items) | Yes | Yes |
| Offline product chat | Yes | Yes |
| Login | Local (any credentials) | API / local |
| Bluetooth, camera, Gemini | No | Yes |
| News RSS, full settings | No | Yes |

Data is stored under `%LOCALAPPDATA%\MiddinInnovatie` on Windows.
