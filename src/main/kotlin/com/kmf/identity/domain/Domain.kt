package com.kmf.identity.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

private val Int.BI: BigInteger
  get() = BigInteger.valueOf(this.toLong())

data class User @JsonCreator constructor(
    @JsonIgnore val id: BigInteger? = null,
    @JsonProperty("dairyId") val dairyId: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("role") val role: String) {

  // FOR JPA
  protected constructor() : this(dairyId = "", name = "", password = "", role = "")

}

// TODO : Need to remove user nullable, It is added now just to avoid calling from JPA constructor
data class RefreshToken(val id: BigInteger? = null, val user: User?, val refreshId: String) {
  //FOR JPA
  protected constructor() : this(user = null, refreshId = "")
}

data class TokenRequestDto @JsonCreator constructor(@JsonProperty("dairyId") val dairyId: String, @JsonProperty("name") val name: String, @JsonProperty("password") val password: String)

interface Repository {
  fun createUser(user: User)
  fun getUser(name: String, password: String, dairyId: String): User
  fun updateRefreshToken(refreshToken: RefreshToken): RefreshToken
  fun getRefreshTokenDetails(refreshId: String): RefreshToken
  fun getRefreshTokenDetailsByUserId(id: BigInteger): RefreshToken

}
