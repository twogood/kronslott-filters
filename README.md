*Project Kronslott*
# DropWizard useful filters
DropWizard bundle to enable and configure filters that are especially useful 
if you are writing a single-page application with DropWizard as backend.

All filters will apply to the current Jersey URL pattern
(`environment.jersey().urlPattern`), which defaults to `/*`.

The filters are:

### CacheBustingFilter
This servlet filter adds a no-cache header to all responses. Especially useful
for Internet Explorer clients.

Class name: `io.dropwizard.servlets.CacheBustingFilter`

### CrossOriginFilter
This will make your service support cross-origin requests. Especially useful 
in development environments.
 
If CsrfProtectionFilter is enabled, it will automatically add X-Requested-By
to the list of allowed HTTP headers.
 
Note: To make authentication and cookies work cross-site, set withCredentials
to true in XMLHttpRequest. Read more at
https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/withCredentials

Example client code for AngularJS:

```javascript
$httpProvider.defaults.withCredentials = true;
```

Class name: `org.eclipse.jetty.servlets.CrossOriginFilter`

### CsrfProtectionFilter
This Jersey filter will require X-Requested-By on POST/PUT/DELETE requests
or fail with 400 Bad Request!

Example client code for AngularJS:

```javascript
$httpProvider.defaults.headers.common['X-Requested-By'] = 'XHR';
```

Class name: `org.glassfish.jersey.server.filter.CsrfProtectionFilter`


### DisableWwwAuthenticateFilter
This servlet filter is implemented in this package and it removes the 
`WWW-Authenticate` header from HTTP 401 responses, so that the web browser
will not show any popup asking for user credentials.

## Usage

### example.yml

This example configuration shows the default values.

```yaml
filters:
  cacheBusting: true
  crossOrigin:
    enabled: true
    allowedOrigins: *
    allowedHeaders: Content-Type,Accept,Origin
    allowedMethods: OPTIONS,GET,PUT,POST,DELETE,HEAD
  csrfProtection: true
  disableWwwAuthenticate: true
```

### example.kt

```kotlin
class ExampleConfig : Configuration(), FiltersConfig {
    @Valid
    @JsonProperty("filters")
    override var filterSettings: FilterSettings = FilterSettings()
   
    // ...
}

class ExampleApp : Application<ExampleConfig>() {

    override fun initialize(bootstrap: Bootstrap<ExampleConfig>) {
        bootstrap.addBundle(FiltersBundle<ExampleConfig>())
        // ...
    }
    
    // ...
}    
```

## Adding this library to your project

This project is not yet available from Maven Central Repository, but it's 
available via
[JitPack.io](https://jitpack.io/#se.activout/kronslott-filters).

[![Release](https://jitpack.io/v/se.activout/kronslott-filters.svg)](https://jitpack.io/#se.activout/kronslott-filters)
