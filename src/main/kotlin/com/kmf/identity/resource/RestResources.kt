package com.kmf.identity.resource

import com.kmf.identity.domain.User
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/identity")
class Resource {

  @Path("/users")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun createUser(user: User): User {
    println("user is ${user}")
    return user
  }

  @Path("/version")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  fun getVersion() = "1.0.0"
}