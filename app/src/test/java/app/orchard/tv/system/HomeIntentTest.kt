package app.orchard.tv.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.orchard.tv.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Queries the merged manifest for each flavor, without registering synthetic activities. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeIntentTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    private fun assertMatches(expected: Int, vararg categories: String, flags: Int = 0) {
        val intent = Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
        categories.forEach { intent.addCategory(it) }
        val matches = context.packageManager.queryIntentActivities(intent, flags)
        // Count raw query results, not distinct components: launcher listings must not duplicate us.
        assertEquals("${categories.toList()}, flags=$flags", expected, matches.size)
        matches.forEach { assertEquals("app.orchard.tv.MainActivity", it.activityInfo.name) }
    }

    @Test
    fun homeIntentsResolveOnlyInStandard() {
        val expected = if (BuildConfig.FLAVOR == "standard") 1 else 0
        for (flags in listOf(0, PackageManager.MATCH_DEFAULT_ONLY)) {
            assertMatches(expected, Intent.CATEGORY_HOME, flags = flags)
            assertMatches(expected, Intent.CATEGORY_HOME, Intent.CATEGORY_LAUNCHER, flags = flags)
            assertMatches(expected, Intent.CATEGORY_HOME, "android.intent.category.COMMON_HOME", flags = flags)
            assertMatches(expected, "android.intent.category.HOME.CC", flags = flags)
        }
    }

    @Test
    fun launcherQueriesReturnOneActivityInBothFlavors() {
        assertMatches(1, Intent.CATEGORY_LAUNCHER)
        assertMatches(1, Intent.CATEGORY_LEANBACK_LAUNCHER)
    }

    @Test
    fun unrelatedCategoryStillDoesNotMatch() {
        assertMatches(0, Intent.CATEGORY_HOME, "app.orchard.tv.UNKNOWN_CATEGORY")
    }
}
