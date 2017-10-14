package com.kmf.identity.resource

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.name.Names
import com.kmf.identity.application.getProperties
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository
import com.kmf.identity.services.TokenUtil
import com.kmf.identity.services.UserService
import com.kmf.identity.services.UserServiceImpl

class TestModule constructor(val user: User) : AbstractModule() {
  override fun configure() {
    val userRepository = UserDaoTestImp(user)
    bind(TokenUtil::class.java).`in`(Singleton::class.java)
    bind(UserService::class.java).to(UserServiceImpl::class.java)
    bind(UserRepository::class.java).toInstance(userRepository)
    Names.bindProperties(binder(), getProperties("kmf.application"))
  }
}

class UserDaoTestImp constructor(val user: User) : UserRepository {
  override fun createUser(user: User) = println("Creation successfully")
  override fun getUser(name: String, password: String, dairyId: String) = user
  override fun getUser(name: String) = user
}

