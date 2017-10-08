package com.kmf.identity.database

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.persist.Transactional
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository
import javax.persistence.EntityManager

open class UserDaoImpl @Inject constructor(val entityManagerProvider: Provider<EntityManager>) : UserRepository {

  override @Transactional
  fun createUser(user: User) = entityManagerProvider.get().persist(user)

  override @Transactional
  fun getUser(name: String, password: String) = entityManagerProvider.get()
      .createNamedQuery("retrieve_user_details", User::class.java)
      .setParameter("name", name)
      .setParameter("password", password)
      .singleResult

}