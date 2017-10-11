package com.kmf.identity.application


import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Names
import com.google.inject.servlet.GuiceFilter
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.servlet.ServletModule
import com.kmf.identity.database.EntityManagerProvider
import com.kmf.identity.database.UserDaoImpl
import com.kmf.identity.domain.UserRepository
import com.kmf.identity.resource.Resource
import com.kmf.identity.resource.UserDetailsReader
import com.kmf.identity.resource.VersionResource
import com.kmf.identity.services.TokenUtil
import com.kmf.identity.services.UserService
import com.kmf.identity.services.UserServiceImpl
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.ConfigurationConverter
import org.apache.commons.configuration2.EnvironmentConfiguration
import org.apache.commons.configuration2.SubsetConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import java.io.File
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.Persistence


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
    val injector = Guice.createInjector(ApplicationModule())
    return injector
  }
}


class ApplicationModule : ServletModule() {
  override fun configureServlets() {
    // register the resources
    bind(Resource::class.java)
    bind(VersionResource::class.java)
    bind(UserDetailsReader::class.java).`in`(Singleton::class.java)
    bind(TokenUtil::class.java).`in`(Singleton::class.java)
    bind(UserService::class.java).to(UserServiceImpl::class.java)
    bind(UserRepository::class.java).to(UserDaoImpl::class.java)
    bind(EntityManager::class.java).toProvider(EntityManagerProvider::class.java)
    Names.bindProperties(binder(), getProperties("kmf.application"))
    // serve all requests from guice container
    serve("/*").with(GuiceContainer::class.java)
  }

  @Provides
  @Singleton
  private fun entityManagerFactory() = Persistence.createEntityManagerFactory("identity-service-db", getDatabaseProperties())

}

private fun getDatabaseProperties(): HashMap<String, String> {
  val properties = HashMap<String, String>()
  val envProps = getProperties("DB")
  properties.put("hibernate.connection.url", "jdbc:postgresql://${envProps.getProperty("HOST")}:${envProps.getProperty("PORT")}/${envProps.getProperty("NAME")}")
  properties.put("hibernate.connection.username", envProps.getProperty("USERNAME"))
  properties.put("hibernate.connection.password", envProps.getProperty("PASSWORD"))
  properties.put("hibernate.connection.pool_size", envProps.getProperty("CONNECTION_POOL"))
  return properties
}

private fun getProperties(prefix: String): Properties {
  val compositeConfiguration = CompositeConfiguration()
  val environmentConfiguration = EnvironmentConfiguration()
  val configurations = Configurations()
  val propertiesConfiguration = configurations.properties(File("application.properties"))

  // add env config to composite config
  compositeConfiguration.addConfiguration(environmentConfiguration)
  // add file config
  compositeConfiguration.addConfiguration(propertiesConfiguration)
  val subsetConfiguration = SubsetConfiguration(compositeConfiguration, prefix, ".")
  return ConfigurationConverter.getProperties(subsetConfiguration)
}

