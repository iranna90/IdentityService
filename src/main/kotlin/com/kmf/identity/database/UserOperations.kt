package com.kmf.identity.database

import com.google.inject.Inject
import com.google.inject.Provider
import com.kmf.identity.domain.User
import com.kmf.identity.domain.UserRepository
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

class EntityManagerProvider @Inject constructor(val entityManagerFactory: EntityManagerFactory) : Provider<EntityManager> {
  override fun get(): EntityManager {
    return entityManagerFactory.createEntityManager()
  }
}

open class UserDaoImpl @Inject constructor(val entityManagerProvider: EntityManagerProvider) : UserRepository {

  override
  fun createUser(user: User) = runWithTransaction { entityManager -> entityManager.persist(user) }

  override
  fun getUser(name: String, password: String, dairyId: String) = runWithTransaction { entityManager ->
    entityManager
        .createNamedQuery("retrieve_user_details", User::class.java)
        .setParameter("name", name)
        .setParameter("password", password)
        .setParameter("dairyId", dairyId)
        .singleResult
  }

  override
  fun getUser(name: String) = runWithTransaction { entityManager ->
    entityManager
        .createNamedQuery("retrieve_user_by_id_details", User::class.java)
        .setParameter("name", name)
        .singleResult
  }

  private fun <T> runWithTransaction(query: ((EntityManager) -> T)): T {
    val entityManager = entityManagerProvider.get()
    val transaction = entityManager.transaction
    transaction.begin()
    val value = query.invoke(entityManager)
    transaction.commit()
    return value
  }

}