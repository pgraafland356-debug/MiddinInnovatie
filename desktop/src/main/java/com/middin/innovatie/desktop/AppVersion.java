package com.middin.innovatie.desktop;

/** Keep in sync with app/build.gradle.kts (versionName / versionCode). */
public final class AppVersion {
    public static final String NAME = "0.9.2";
    public static final int CODE = 11;

    /**
     * GitHub raw manifest — replace YOUR_GITHUB_USERNAME or set override in Instellingen.
     * Generated URL: raw.githubusercontent.com/{owner}/{repo}/main/releases/latest.json
     */
    public static final String UPDATE_FEED_DEFAULT =
            "https://raw.githubusercontent.com/pgraafland356-debug/MiddinInnovatie/main/releases/latest.json";

    private AppVersion() {}
}
