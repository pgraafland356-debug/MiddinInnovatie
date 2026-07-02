package com.middin.innovatie.app.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseManifestTest {

  private val sample = """
    {
      "versionName": "0.9.3",
      "versionCode": 12,
      "changelog": "Test",
      "android": {
        "apkUrl": "https://example.com/app.apk",
        "sha256": "abc"
      },
      "windows": {
        "setupUrl": "https://example.com/setup.exe",
        "sha256": "def"
      }
    }
  """.trimIndent()

  @Test
  fun parseAndroidRelease_whenNewer_returnsRelease() {
    val r = ReleaseManifest.parseAndroidRelease(sample, 11)
    assertNotNull(r)
    assertEquals(12, r!!.versionCode)
    assertEquals("https://example.com/app.apk", r.apkUrl)
  }

  @Test
  fun parseAndroidRelease_whenCurrent_returnsNull() {
    assertNull(ReleaseManifest.parseAndroidRelease(sample, 12))
  }
}
