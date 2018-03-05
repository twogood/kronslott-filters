package se.activout.kronslott.filters

import com.example.mockito.MockitoExtension
import com.nhaarman.mockitokotlin2.*
import io.dropwizard.Configuration
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.jetty.setup.ServletEnvironment
import io.dropwizard.servlets.CacheBustingFilter
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.glassfish.jersey.server.filter.CsrfProtectionFilter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import javax.servlet.Filter
import javax.servlet.FilterRegistration

class TestConfiguration() : Configuration(), FiltersConfig {
    override var filterSettings: FilterSettings = FilterSettings()
}

@ExtendWith(MockitoExtension::class)
class FiltersBundleTest {

    @Mock
    lateinit var jerseyEnvironment: JerseyEnvironment

    @Mock
    lateinit var servletEnvironment: ServletEnvironment

    @Mock
    lateinit var dynamicFilterRegistration: FilterRegistration.Dynamic

    @Mock
    lateinit var environment: Environment

    private lateinit var bundle: FiltersBundle<TestConfiguration>

    private lateinit var config: TestConfiguration

    private val allFiltersDisabled = FilterSettings(
            csrfProtection = false,
            cacheBusting = false,
            crossOrigin = CrossOriginSettings(enabled = false),
            disableWwwAuthenticate = false
    )

    @BeforeEach
    fun before() {
        whenever(environment.jersey()).thenReturn(jerseyEnvironment)
        whenever(environment.servlets()).thenReturn(servletEnvironment)
        whenever(servletEnvironment.addFilter(any(), any<Class<out Filter>>())).thenReturn(dynamicFilterRegistration)

        bundle = FiltersBundle()
        config = TestConfiguration()
    }

    @Test
    fun testAllFiltersDisabled() {
        // given
        config.filterSettings = allFiltersDisabled

        // when
        bundle.run(config, environment)

        // then
        verifyNoJerseyFilters()
        verifyNoServletFilters()
    }

    @Test
    fun testCsrfProtection() {
        // given
        config.filterSettings = allFiltersDisabled.copy(csrfProtection = true)

        // when
        bundle.run(config, environment)

        // then
        verify(environment.jersey(), times(1)).register(CsrfProtectionFilter::class.java)
        verifyNoServletFilters()
    }

    @Test
    fun testCacheBusting() {
        // given
        config.filterSettings = allFiltersDisabled.copy(cacheBusting = true)

        // when
        bundle.run(config, environment)

        // then
        verifyNoJerseyFilters()
        verify(environment.servlets(), times(1)).addFilter(CacheBustingFilter::class.java.simpleName, CacheBustingFilter::class.java)
    }

    @Test
    fun testDisableWwwAuthenticate() {
        // given
        config.filterSettings = allFiltersDisabled.copy(disableWwwAuthenticate = true)

        // when
        bundle.run(config, environment)

        // then
        verifyNoJerseyFilters()
        verify(environment.servlets(), times(1)).addFilter(DisableWwwAuthenticateFilter::class.java.simpleName, DisableWwwAuthenticateFilter::class.java)
    }

    @Test
    fun testCrossOrigin() {
        // given
        config.filterSettings = allFiltersDisabled.copy(crossOrigin = CrossOriginSettings(enabled = true))

        // when
        bundle.run(config, environment)

        // then
        verifyNoJerseyFilters()
        verify(environment.servlets(), times(1)).addFilter(CrossOriginFilter::class.java.simpleName, CrossOriginFilter::class.java)
    }


    private fun verifyNoJerseyFilters() {
        verify(environment.jersey(), never()).register(any())
    }

    private fun verifyNoServletFilters() {
        verify(environment.servlets(), never()).addFilter(any(), any<Class<out Filter>>())
        verify(dynamicFilterRegistration, never()).addMappingForUrlPatterns(any(), any(), any())
    }
}