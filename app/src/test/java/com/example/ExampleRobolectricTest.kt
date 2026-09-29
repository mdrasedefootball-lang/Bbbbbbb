package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProtectionState
import com.example.data.repository.ShieldRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DF Shield", appName)
    }

    @Test
    fun `repository initializes default filters and allowlist`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShieldRepository(context)

        val filters = repo.filterRules.first()
        assertTrue(filters.isNotEmpty())
        assertTrue(filters.any { it.nameBn.contains("বিজ্ঞাপন") })

        val allowlist = repo.allowlistEntries.first()
        assertTrue(allowlist.isNotEmpty())
    }

    @Test
    fun `repository updates protection state properly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShieldRepository(context)

        repo.setProtectionState(ProtectionState.ENABLED)
        assertEquals(ProtectionState.ENABLED, repo.protectionState.first())

        repo.setProtectionState(ProtectionState.DISABLED)
        assertEquals(ProtectionState.DISABLED, repo.protectionState.first())
    }
}
