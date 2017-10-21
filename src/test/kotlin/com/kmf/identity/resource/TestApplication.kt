package com.kmf.identity.resource

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.name.Names
import com.kmf.identity.application.getProperties
import com.kmf.identity.domain.RefreshToken
import com.kmf.identity.domain.Repository
import com.kmf.identity.domain.User
import com.kmf.identity.services.TokenUtil
import com.kmf.identity.services.UserService
import com.kmf.identity.services.UserServiceImpl
import java.math.BigInteger

class TestModule constructor(val user: User) : AbstractModule() {
  override fun configure() {
    val userRepository = TokenRepositoryTestImpl(user)
    bind(TokenUtil::class.java).`in`(Singleton::class.java)
    bind(UserService::class.java).to(UserServiceImpl::class.java)
    bind(Repository::class.java).toInstance(userRepository)
    Names.bindProperties(binder(), getProperties("kmf.application"))
  }
}

internal val readWriteMap = hashMapOf("foo" to 1, "bar" to 2)

class TokenRepositoryTestImpl constructor(val user: User) : Repository {
  val refreshTokenMap = hashMapOf("valid" to RefreshToken(1.BI, user, "valid"))
  override fun createUser(user: User) = println("Creation successfully")
  override fun getUser(name: String, password: String, dairyId: String) = user
  override fun updateRefreshToken(refreshToken: RefreshToken): RefreshToken {
    refreshTokenMap.put(refreshToken.refreshId, refreshToken)
    return refreshToken
  }

  override fun getRefreshTokenDetails(refreshId: String) = refreshTokenMap.get(refreshId)!!
  override fun getRefreshTokenDetailsByUserId(id: BigInteger) = refreshTokenMap.get("valid")!!
}

internal val Int.BI: BigInteger?
  get() = BigInteger.valueOf(this.toLong())


