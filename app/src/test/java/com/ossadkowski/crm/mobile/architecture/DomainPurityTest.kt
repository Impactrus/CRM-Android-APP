package com.ossadkowski.crm.mobile.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Enforces the Clean Architecture dependency rule for new-feature code:
 * the domain layer must not depend on Android, Retrofit, or Gson DTO annotations.
 *
 * Walks the source tree at compile time (pure JVM, no extra deps) and fails
 * the build if a forbidden import is found anywhere under `domain/`.
 *
 * If this test fails, the offending file belongs in `data/` or `ui/`, not `domain/`.
 */
class DomainPurityTest {

    private val forbiddenSubstrings = listOf(
        "import android.",
        "import androidx.",
        "import retrofit2.",
        "import okhttp3.",
        "import com.google.gson.annotations.SerializedName",
        "@SerializedName"
    )

    @Test
    fun `domain layer has no Android, Retrofit or DTO-annotation dependencies`() {
        val domainRoot = locateDomainRoot()
            ?: error("Could not locate domain source root from working dir ${File("").absolutePath}")

        val violations = mutableListOf<String>()
        domainRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val text = file.readText()
                forbiddenSubstrings.forEach { needle ->
                    if (text.contains(needle)) {
                        violations += "${file.relativeTo(domainRoot)} contains forbidden token: $needle"
                    }
                }
            }

        assertTrue(
            "Domain layer purity violations:\n" + violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    private fun locateDomainRoot(): File? {
        // Try a few likely working directories so the test is robust to how Gradle invokes it.
        val candidates = listOf(
            "src/main/java/com/ossadkowski/crm/mobile/domain",
            "app/src/main/java/com/ossadkowski/crm/mobile/domain",
            "../app/src/main/java/com/ossadkowski/crm/mobile/domain"
        )
        return candidates.map { File(it) }.firstOrNull { it.isDirectory }
    }
}
