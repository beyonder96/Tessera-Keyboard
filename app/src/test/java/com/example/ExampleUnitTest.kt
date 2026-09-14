package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun groqErrorMessageExtraction_worksProperly() {
    val jsonError = """{"error":{"message":"Unknown request URL: POST /v1/chat/completions. Please check the URL","type":"invalid_request_error","code":"unknown_url"}}"""
    val match = """"message"\s*:\s*"([^"]+)"""".toRegex().find(jsonError)
    assertNotNull(match)
    assertEquals("Unknown request URL: POST /v1/chat/completions. Please check the URL", match?.groupValues?.get(1))

    val invalidKeyJson = """{"error":{"message":"Invalid API Key","type":"invalid_request_error","code":"invalid_api_key"}}"""
    val keyMatch = """"message"\s*:\s*"([^"]+)"""".toRegex().find(invalidKeyJson)
    assertNotNull(keyMatch)
    assertEquals("Invalid API Key", keyMatch?.groupValues?.get(1))
  }

  @Test
  fun screenshotHeuristic_identifiesScreenshotsAccurately() {
    val paths = listOf(
      "Pictures/Screenshots/Screenshot_20260914_012345.png",
      "/storage/emulated/0/DCIM/Screenshots/Screenshot_1.jpg",
      "Pictures/Capturas de tela/captura_tela_01.png",
      "print_whatsapp.png"
    )

    paths.forEach { path ->
      val isScreenshot = path.contains("screenshot", ignoreCase = true) ||
                         path.contains("captura", ignoreCase = true) ||
                         path.contains("print", ignoreCase = true)
      assertTrue("Path '$path' should be detected as screenshot", isScreenshot)
    }

    val regularPhotos = listOf(
      "DCIM/Camera/IMG_20260914_123456.jpg",
      "Download/document.pdf",
      "DCIM/100ANDRO/DSC_0001.JPG"
    )

    regularPhotos.forEach { path ->
      val isScreenshot = path.contains("screenshot", ignoreCase = true) ||
                         path.contains("captura", ignoreCase = true) ||
                         path.contains("print", ignoreCase = true)
      assertFalse("Path '$path' should NOT be detected as screenshot", isScreenshot)
    }
  }
}
