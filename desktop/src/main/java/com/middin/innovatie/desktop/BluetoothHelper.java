package com.middin.innovatie.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Lists paired Bluetooth devices on Windows (like Android bonded devices). */
public final class BluetoothHelper {
    public static List<BluetoothDeviceInfo> listPairedDevices() {
        List<BluetoothDeviceInfo> out = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-Command",
                "Get-PnpDevice -Class Bluetooth -ErrorAction SilentlyContinue | "
                    + "Where-Object { $_.Status -eq 'OK' -and $_.FriendlyName } | "
                    + "Select-Object -ExpandProperty FriendlyName"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String name = line.trim();
                    if (!name.isEmpty()) out.add(new BluetoothDeviceInfo(name, ""));
                }
            }
            p.waitFor();
        } catch (Exception ignored) {
        }
        return out;
    }

    private BluetoothHelper() {}
}
