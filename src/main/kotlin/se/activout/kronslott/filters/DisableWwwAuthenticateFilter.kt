package se.activout.kronslott.filters

import org.glassfish.jersey.server.filter.CsrfProtectionFilter
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class DisableWwwAuthenticateFilter : Filter {
    override fun init(filterConfig: FilterConfig?) {
        // deliberately empty
    }

    override fun destroy() {
        // deliberately empty
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if ((request as HttpServletRequest).getHeader(CsrfProtectionFilter.HEADER_NAME) != null) {
            val responseWrapper = object : HttpServletResponseWrapper(response as HttpServletResponse) {
                override fun setHeader(name: String, value: String?) {
                    if (!isWwwAuthenticate(name)) {
                        super.setHeader(name, value)
                    }
                }

                override fun addHeader(name: String, value: String?) {
                    if (!isWwwAuthenticate(name)) {
                        super.addHeader(name, value)
                    }
                }

                private fun isWwwAuthenticate(name: String) = name.equals("WWW-Authenticate", true)
            }
            chain.doFilter(request, responseWrapper)
        } else {
            chain.doFilter(request, response)
        }
    }


}