package com.kmf.identity.services

import com.google.inject.Inject
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository

interface UserService {
  fun createUser(user: User): User
  fun getUser(userId: String): User
}

class UserServiceImpl @Inject constructor(val userRepository: UserRepository) : UserService {

  override fun getUser(userId: String) = userRepository.getUser(userId)

  override fun createUser(user: User): User {
    userRepository.createUser(user)
    return user
  }


}