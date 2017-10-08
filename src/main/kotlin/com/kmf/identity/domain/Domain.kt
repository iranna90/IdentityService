package com.kmf.identity.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

private val Int.BI: BigInteger
  get() = BigInteger.valueOf(this.toLong())

data class User @JsonCreator constructor(
    @JsonProperty("dairyId") val dairyId: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("role") val role: String
) {
  @JsonIgnore
  var id: BigInteger = 0.BI
}

interface UserService {
  fun createUser(user: User): User
}