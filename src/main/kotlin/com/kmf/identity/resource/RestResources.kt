package com.kmf.identity.resource

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.kmf.identity.domain.TokenRequest
import com.kmf.identity.domain.User
import com.kmf.identity.services.UserService
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

  @Path("users/{userId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  fun getUser(@PathParam("userId") userId: String) = userService.getUser(userId)

  @Path("/token")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun generateToken(tokenRequest: TokenRequest) = userService.generateToken(tokenRequest)
}

data class VersionDto @JsonCreator constructor(@JsonProperty("name") val name: String, @JsonProperty("version") val version: String)

@Path("/identity/version")
class VersionResource @Inject constructor(objectMapper: ObjectMapper) {

  val versionDto: VersionDto = objectMapper.readValue(this.javaClass.classLoader.getResourceAsStream("version.json"), VersionDto::class.java)

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  fun getVersion() = versionDto
}