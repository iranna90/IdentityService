package com.kmf.identity.database

import com.google.inject.Inject
import com.google.inject.Provider
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository
import javax.persistence.EntityManager
import javax.transaction.Transactional

class UserDaoImpl @Inject constructor(val entityManagerProvider: Provider<EntityManager>) : UserRepository {

  override
  fun  createUser(user: User) {
    val entityManager = entityManagerProvider.get()
    val transaction = entityManager.transaction
    transaction.begin()
    entityManager.persist(user)
    transaction.commit()
  }

  override @Transactional fun  getUser(name: String, password: String) = entityManagerProvider.get()
      .createNamedQuery("retrieve_user_details", User::class.java)
      .setParameter("name", name)
      .setParameter("password", password)
      .singleResult

}