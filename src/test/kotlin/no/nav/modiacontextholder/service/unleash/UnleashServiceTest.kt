package no.nav.modiacontextholder.service.unleash

import io.getunleash.Unleash
import io.getunleash.UnleashContextProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnleashServiceTest {
    private val unleashContext: UnleashContextProvider = mockk()
    private val toggleableFeatureService: ToggleableFeatureService = UnleashService(unleashContext)
    private val unleash: Unleash = mockk()

    private object SampleFeature : ToggleableFeature {
        override val featureName: String = "sample-feature"
    }

    @Test
    fun `isEnabled should return true when feature is enabled`() {
        every { unleash.isEnabled(SampleFeature.featureName) } returns true

        val enabled: Boolean = toggleableFeatureService.isEnabled(SampleFeature.featureName)

        verify(exactly = 1) { unleash.isEnabled(any()) }
        assertTrue(enabled)
    }

    @Test
    fun `isEnabled should return false when feature is disabled`() {
        every { unleash.isEnabled(SampleFeature.featureName) } returns false

        val enabled: Boolean = toggleableFeatureService.isEnabled(SampleFeature)

        verify(exactly = 1) { unleash.isEnabled(any()) }
        assertFalse(enabled)
    }
}
