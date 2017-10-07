package com.kmf.identity.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class User @JsonCreator constructor(@JsonProperty("name") val name: String, @JsonProperty("password") val password: String, @JsonProperty("role") val role: String)

interface UserService {
  fun createUser(user: User): User
}