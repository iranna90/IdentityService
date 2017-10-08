package com.kmf.identity.services

import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserService

class UserServiceImpl : UserService {
  override fun createUser(user: User): User {
    println("user service is ${user}")
    return user
  }
}