package com.kmf.identity.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import com.google.inject.name.Named
import com.kmf.identity.domain.TokenRequest
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZonedDateTime
import java.util.*
import java.util.stream.Collectors.joining

interface UserService {
  fun createUser(user: User): User
  fun getUser(userId: String): User
  fun generateToken(tokenRequest: TokenRequest): Token
}

class UserServiceImpl @Inject constructor(val userRepository: UserRepository, val tokenUtil: TokenUtil) : UserService {

  override fun generateToken(tokenRequest: TokenRequest) = tokenUtil.generateToken(userRepository.getUser(tokenRequest.name, tokenRequest.password, tokenRequest.dairyId))

  override fun getUser(userId: String) = userRepository.getUser(userId)

  override fun createUser(user: User): User {
    userRepository.createUser(user)
    return user
  }
}

class TokenUtil @Inject constructor(
    @Named("token.issuer") val issuer: String,
    @Named("token.audience") val audience: String,
    @Named("token.expiration.period.minute") val expirationPeriodInMinute: Long,
    @Named("token.jwt.key.id") val keyId: String) {

  var privateKey: PrivateKey

  init {
    val fileContent = BufferedReader(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("certificate/auth-private.key")))
        .lines()
        .collect(joining(System.getProperty("line.separator")))

    val certificateString = fileContent
        .replace("-----BEGIN PRIVATE KEY-----" + System.getProperty("line.separator"), "")
        .replace("-----END PRIVATE KEY-----", "");

    val certificateBytes = Base64.getMimeDecoder().decode(certificateString)
    val spec = PKCS8EncodedKeySpec(certificateBytes)
    val kf = KeyFactory.getInstance("RSA")
    privateKey = kf.generatePrivate(spec)
  }

  internal fun generateToken(user: User): Token {
    val expirationPeriod = ZonedDateTime.now().plusMinutes(expirationPeriodInMinute)
    val jwtClaimsSet = JWTClaimsSet.Builder()
        .audience(audience)
        .issuer(issuer)
        .expirationTime(Date.from(expirationPeriod.toInstant()))
        .subject(user.name)
        .issueTime(Date())
        .claim("role", user.role)
        .claim("dairyId", user.dairyId)
        .build()
    return signTheToken(jwtClaimsSet)
  }

  private fun signTheToken(claimsSet: JWTClaimsSet): Token {
    val signer = RSASSASigner(privateKey)
    val jwsHeader = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build()
    val signedJwt = SignedJWT(jwsHeader, claimsSet)
    signedJwt.sign(signer)
    return Token(signedJwt.serialize())
  }
}

data class Token(@JsonProperty("token") val token: String)