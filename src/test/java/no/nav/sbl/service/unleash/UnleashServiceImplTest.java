package no.nav.sbl.service.unleash;

import io.getunleash.Unleash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class UnleashServiceImplTest {
    @Mock
    private Unleash unleash;
    private UnleashService unleashService;

    @BeforeEach
    void init() throws Exception {
        openMocks(this).close();
        unleashService = new UnleashServiceImpl(unleash);
    }

    @Test
    void isEnabled() {
        when(unleash.isEnabled(Feature.SAMPLE_FEATURE.getPropertyKey())).thenReturn(true);

        boolean enabled = unleashService.isEnabled(Feature.SAMPLE_FEATURE);

        verify(unleash, times(1)).isEnabled(any());
        assertTrue(enabled);
    }

    @Test
    void isDisabled() {
        when(unleash.isEnabled(Feature.SAMPLE_FEATURE.getPropertyKey())).thenReturn(false);

        boolean enabled = unleashService.isEnabled(Feature.SAMPLE_FEATURE);

        verify(unleash, times(1)).isEnabled(any());
        assertFalse(enabled);
    }
}
