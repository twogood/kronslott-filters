package se.activout.kronslott.filters

import io.dropwizard.ConfiguredBundle
import io.dropwizard.servlets.CacheBustingFilter
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.glassfish.jersey.server.filter.CsrfProtectionFilter
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.FilterRegistration

data class CrossOriginSettings(
        val enabled: Boolean = true,
        val allowedOrigins: String = "*",
        val allowedHeaders: String = "Content-Type,Accept,Origin",
        val allowedMethods: String = "OPTIONS,GET,PUT,POST,DELETE,HEAD"
)

data class FilterSettings(
        val cacheBusting: Boolean = true,
        val crossOrigin: CrossOriginSettings = CrossOriginSettings(),
        val csrfProtection: Boolean = true,
        val disableWwwAuthenticate: Boolean = true
)


interface FiltersConfig {
    var filterSettings: FilterSettings
}


private inline fun <reified T : Filter> Environment.addServletFilter(): FilterRegistration.Dynamic {
    val javaClass = T::class.java
    val filter = servlets().addFilter(javaClass.simpleName, javaClass)
    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, jersey().urlPattern)
    return filter
}

class FiltersBundle<C : FiltersConfig> : ConfiguredBundle<C> {
    override fun initialize(bootstrap: Bootstrap<*>) {
        // deliberately empty
    }

    override fun run(configuration: C, environment: Environment) {
        val filters = configuration.filterSettings

        if (filters.csrfProtection) {
            // This will require X-Requested-By on POST/PUT/DELETE requests or fail with 400 Bad Request!
            // In AngularJS: $httpProvider.defaults.headers.common['X-Requested-By'] = 'XHR';
            environment.jersey().register(CsrfProtectionFilter::class.java)
        }

        if (filters.cacheBusting) {
            environment.addServletFilter<CacheBustingFilter>()
        }

        if (filters.disableWwwAuthenticate) {
            environment.addServletFilter<DisableWwwAuthenticateFilter>()
        }

        val crossOrigin = filters.crossOrigin
        if (crossOrigin.enabled) {
            // To make authentication and cookies work cross-site, set withCredentials to true in XMLHttpRequest
            // See https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/withCredentials
            // In AngularJS: $httpProvider.defaults.withCredentials = true;
            environment.addServletFilter<CrossOriginFilter>().apply {
                setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, crossOrigin.allowedOrigins)
                setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, allowedHeaders(filters))
                setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, crossOrigin.allowedMethods)
            }
        }
    }

    private fun allowedHeaders(filters: FilterSettings): String {
        return filters.crossOrigin.allowedHeaders +
                if (filters.csrfProtection)
                    ",${CsrfProtectionFilter.HEADER_NAME}"
                else
                    ""
    }

}