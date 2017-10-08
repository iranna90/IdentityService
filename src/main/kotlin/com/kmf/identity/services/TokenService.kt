package com.kmf.identity.services

import com.google.inject.Inject
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository

interface UserService {
  fun createUser(user: User): User
}

class UserServiceImpl @Inject constructor(val userRepository: UserRepository) : UserService {
  override fun createUser(user: User): User {
    userRepository.createUser(user)
    return user
  }
}