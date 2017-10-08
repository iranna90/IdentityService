package com.kmf.identity.application


import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.persist.PersistService
import com.google.inject.persist.jpa.JpaPersistModule
import com.google.inject.servlet.GuiceFilter
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.servlet.ServletModule
import com.kmf.identity.database.UserDaoImpl
import com.kmf.identity.domain.UserRepository
import com.kmf.identity.resource.Resource
import com.kmf.identity.services.UserService
import com.kmf.identity.services.UserServiceImpl
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler

fun main(args: Array<String>) {
  startServer()
}

fun startServer() {
  // Create the server.
  val server = Server(8080)

  // Create a servlet context and add the jersey servlet.
  val sch = ServletContextHandler(server, "/")

  // Add our Guice listener that includes our bindings
  sch.addEventListener(ApplicationConfig())

  // Then add GuiceFilter and configure the server to
  // reroute all requests through this filter.
  sch.addFilter(GuiceFilter::class.java, "/*", null)

  // Must add DefaultServlet for embedded Jetty.
  // Failing to do this will cause 404 errors.
  // This is not needed if web.xml is used instead.
  sch.addServlet(DefaultServlet::class.java, "/")

  // Start the server
  server.start()
  server.join()
}

class ApplicationConfig : GuiceServletContextListener() {
  override fun getInjector(): Injector {
    val injector = Guice.createInjector(ApplicationModule(), JpaPersistModule("identity-service-db"))
    injector.getInstance(DatabaseInitializer::class.java)
    return injector
  }
}


class ApplicationModule : ServletModule() {
  override fun configureServlets() {
    // register the resources
    bind(Resource::class.java)
    bind(UserService::class.java).to(UserServiceImpl::class.java)
    bind(UserRepository::class.java).to(UserDaoImpl::class.java)
    // serve all requests from guice container
    serve("/*").with(GuiceContainer::class.java)
  }
}

class DatabaseInitializer @Inject constructor(val persistService: PersistService) {
  init {
    println("starting the data base unit")
    persistService.start()
  }
}

