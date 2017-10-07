package com.kmf.identity.resource

import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserService
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/identity")
class Resource @Inject constructor(val userService: UserService) {

  @Path("/users")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun createUser(user: User) = userService.createUser(user)

  @Path("/version")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  fun getVersion() = "1.0.0"
}