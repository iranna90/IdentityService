package com.kmf.identity.database

import com.google.inject.Inject
import com.google.inject.Provider
import com.kmf.identity.domain.RefreshToken
import com.kmf.identity.domain.Repository
import com.kmf.identity.domain.User
import java.math.BigInteger
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

class EntityManagerProvider @Inject constructor(val entityManagerFactory: EntityManagerFactory) : Provider<EntityManager> {
  override fun get(): EntityManager {
    return entityManagerFactory.createEntityManager()
  }
}

open class TokenRepositoryImpl @Inject constructor(val entityManagerProvider: EntityManagerProvider) : Repository {

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

  override fun getRefreshTokenDetailsByUserId(id: BigInteger) = runWithTransaction { entityManager ->
    entityManager.createNamedQuery("retrieve_refresh_token_by_user_id", RefreshToken::class.java)
        .setParameter("id", id)
        .singleResult
  }

  override fun updateRefreshToken(refreshToken: RefreshToken) = runWithTransaction { entityManager ->
    entityManager.merge(refreshToken)
  }

  override fun getRefreshTokenDetails(refreshId: String) = runWithTransaction { entityManager ->
    entityManager.createNamedQuery("retrieve_refresh_token", RefreshToken::class.java)
        .setParameter("refreshId", refreshId)
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