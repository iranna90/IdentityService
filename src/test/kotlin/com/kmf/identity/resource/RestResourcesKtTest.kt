/*
package com.kmf.identity.resource

import com.kmf.identity.services.UserService
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.mockito.Mockito.mock
import org.testng.annotations.Test
import javax.ws.rs.core.Application

class RestResourcesKtTest : JerseyTest() {

  val BASE_URL = "identity"

  lateinit var userservice: UserService

  lateinit var resource: Resource

  override fun configure(): Application {
    userservice = mock(UserService::class.java)
    resource = Resource(userservice)
    return ResourceConfig().register(resource).register(UserDetailsReader::class.java)
  }

  @Test
  fun version() {
    val response = target("$BASE_URL/version").request().get()
    println(response)
  }

  @Test
  fun tokenBinaryTest() {

  }
}*/
