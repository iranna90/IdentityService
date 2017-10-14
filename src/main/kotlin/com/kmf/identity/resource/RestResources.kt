package com.kmf.identity.resource

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.kmf.identity.domain.TokenRequestDto
import com.kmf.identity.domain.User
import com.kmf.identity.services.UserService
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider

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
  @Consumes(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun generateToken(tokenRequest: TokenRequestDto) = userService.generateToken(tokenRequest)
}

data class VersionDto @JsonCreator constructor(@JsonProperty("name") val name: String, @JsonProperty("version") val version: String)

@Path("/identity/version")
class VersionResource @Inject constructor(objectMapper: ObjectMapper) {

  val versionDto: VersionDto = objectMapper.readValue(this.javaClass.classLoader.getResourceAsStream("version.json"), VersionDto::class.java)

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  fun getVersion() = versionDto
}

@Provider
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
class UserDetailsReader : MessageBodyReader<TokenRequestDto> {

  override fun isReadable(clazz: Class<*>?, p1: Type?, p2: Array<out Annotation>?, p3: MediaType?) = clazz?.equals(TokenRequestDto::class.java)!!

  override fun readFrom(clazz: Class<TokenRequestDto>?, p1: Type?, p2: Array<out Annotation>?, p3: MediaType?, headers: MultivaluedMap<String, String>?, dataStream: InputStream?): TokenRequestDto {
    val contentLength = headers?.getFirst(HttpHeaders.CONTENT_LENGTH)?.toInt()
    val byteArray = getBytesFromStream(dataStream, contentLength)
    return readTokenRequestFromBytes(byteArray)
  }
}

private fun getBytesFromStream(dataStream: InputStream?, contentLength: Int?): ByteArray {
  val byteArrayOutputStream = ByteArrayOutputStream()
  val byteArray = ByteArray(1024)
  var read: Int?

  while (true) {
    read = dataStream?.read(byteArray, 0, byteArray.size)
    if (read === null || read === -1) {
      break
    }
    byteArrayOutputStream.write(byteArray, 0, read)
  }

  if (byteArrayOutputStream.size() != contentLength) {
    throw RuntimeException("Bad request")
  }
  return byteArrayOutputStream.toByteArray()
}

private fun readTokenRequestFromBytes(byteArray: ByteArray): TokenRequestDto {
  // dairyId:userName:base64(password)
  val stringValue = String(byteArray)
  val values = stringValue.split(":")
  if (values.size != 3) {
    throw RuntimeException("Bad request")
  }

  return TokenRequestDto(values.get(0), values.get(1), decode.invoke(values.get(2)))
}

private val decode: (String) -> String = { encodedValue -> String(Base64.getDecoder().decode(encodedValue)) }
private val encode: (String) -> String = { encodedValue -> Base64.getEncoder().encodeToString(encodedValue.toByteArray()) }
