package com.kmf.identity.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Guice
import com.kmf.identity.domain.TokenRequestDto
import com.kmf.identity.domain.User
import com.kmf.identity.services.Token
import com.kmf.identity.services.UserService
import com.nimbusds.jwt.JWTParser
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTestNg
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.Test
import java.util.*
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

private val dairyId = "khajuri"
private val name = "iranna"
private val password = "password"
private val subscriberRole = "subscriber"
private val separator = ":"
private val dto = VersionDto("identity-service", "1.0-SNAPSHOT")
private val objectMapper = ObjectMapper()
private val versionResource = VersionResource(objectMapper)
private val tokenRequest = TokenRequestDto(dairyId, name, password)
private val subscriberUser = User(dairyId, name, password, subscriberRole)
private val injector = Guice.createInjector(TestModule(subscriberUser))
private val encode: (String) -> String = { data -> Base64.getEncoder().encodeToString(data.toByteArray()) }
private val binaryConversion: (TokenRequestDto) -> ByteArray = { dto -> "${dto.dairyId}${separator}${dto.name}${separator}${encode.invoke(dto.password)}".toByteArray() }
private fun <R> parseResponse(response: Response, clazz: Class<R>): R = objectMapper.readValue(response.readEntity(String::class.java), clazz)

private fun validateToken(tokenRequest: TokenRequestDto): (Token) -> Unit = { token ->
  val jwt = JWTParser.parse(token.token)
  assertThat(jwt.jwtClaimsSet.subject, CoreMatchers.`is`(tokenRequest.name))
  assertThat(jwt.jwtClaimsSet.getStringClaim("role"), CoreMatchers.`is`(subscriberRole))
  assertThat(jwt.jwtClaimsSet.getStringClaim("dairyId"), CoreMatchers.`is`(dairyId))
}


class RestResourcesKtTest : JerseyTestNg.ContainerPerClassTest() {

  val BASE_URL = "identity"

  override fun configure() = ResourceConfig().register(versionResource).register(Resource(injector.getInstance(UserService::class.java))).register(UserDetailsReader::class.java)

  @Test
  fun version() = assertThat(getOperation("${BASE_URL}/version", VersionDto::class.java), CoreMatchers.`is`(dto))

  @Test
  fun getToken() = validateToken(tokenRequest).invoke(postOperation("${BASE_URL}/token", Entity.entity(binaryConversion.invoke(tokenRequest), MediaType.APPLICATION_OCTET_STREAM), Token::class.java))

  @Test
  fun tokenBinaryTest() = validateToken(tokenRequest).invoke(postOperation("${BASE_URL}/token", Entity.entity(tokenRequest, MediaType.APPLICATION_JSON), Token::class.java))

  private fun <R> getOperation(url: String, clazz: Class<R>): R = parseResponse(target(url).request().get(), clazz)
  private fun <T, R> postOperation(url: String, entity: Entity<T>, clazz: Class<R>): R = parseResponse(target(url).request().post(entity), clazz)
}
