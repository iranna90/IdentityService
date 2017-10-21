package com.kmf.identity.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import com.google.inject.name.Named
import com.kmf.identity.DomainEntityNotFoundException
import com.kmf.identity.domain.RefreshToken
import com.kmf.identity.domain.Repository
import com.kmf.identity.domain.TokenRequestDto
import com.kmf.identity.domain.User
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.eclipse.jetty.http.HttpStatus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZonedDateTime
import java.util.*
import java.util.stream.Collectors.joining
import javax.persistence.NoResultException

interface UserService {
  fun createUser(user: User): User
  fun generateToken(tokenRequest: TokenRequestDto): Token
  fun refreshToken(refreshId: String): Token
}

class UserServiceImpl @Inject constructor(val repository: Repository, val tokenUtil: TokenUtil) : UserService {

  override fun refreshToken(refreshId: String): Token {
    var refreshTokenDetails: RefreshToken

    // TODO : Better option ?
    try {
      refreshTokenDetails = repository.getRefreshTokenDetails(refreshId)
    } catch (exp: NoResultException) {
      throw DomainEntityNotFoundException(HttpStatus.NOT_FOUND_404, "Refresh token with id: ${refreshId} not found")
    }

    val user = refreshTokenDetails.user!!
    val token = tokenUtil.generateToken(user)
    val refreshToken = refreshTokenDetails.copy(refreshId = token.refreshToken)
    repository.updateRefreshToken(refreshToken)
    return token
  }

  override fun generateToken(tokenRequest: TokenRequestDto): Token {
    val user = repository.getUser(tokenRequest.name, tokenRequest.password, tokenRequest.dairyId)
    val token = tokenUtil.generateToken(user)
    var existingRefreshToken: RefreshToken?

    //TODO : better option ?
    try {
      existingRefreshToken = repository.getRefreshTokenDetailsByUserId(user.id!!)
    } catch (exp: NoResultException) {
      existingRefreshToken = null
    }
    val refreshToken = existingRefreshToken?.copy(refreshId = token.refreshToken) ?: RefreshToken(user = user, refreshId = token.refreshToken)
    repository.updateRefreshToken(refreshToken)
    return token
  }

  override fun createUser(user: User): User {
    repository.createUser(user)
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
    return signTheToken(jwtClaimsSet, UUID.randomUUID().toString())
  }

  private fun signTheToken(claimsSet: JWTClaimsSet, refreshToken: String): Token {
    val signer = RSASSASigner(privateKey)
    val jwsHeader = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build()
    val signedJwt = SignedJWT(jwsHeader, claimsSet)
    signedJwt.sign(signer)
    return Token(signedJwt.serialize(), refreshToken)
  }
}

data class Token(@JsonProperty("token") val token: String, @JsonProperty("refreshToken") val refreshToken: String)