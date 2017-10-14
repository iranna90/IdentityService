package com.kmf.identity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonMappingException
import org.eclipse.jetty.http.HttpStatus.*
import javax.inject.Singleton
import javax.persistence.NoResultException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

data class ErrorResponse @JsonCreator constructor(@JsonProperty("code") val code: Int, @JsonProperty("message") val message: String)

@Provider
@Singleton
class EntityNotFoundMapper : ExceptionMapper<NoResultException> {
  override fun toResponse(ex: javax.persistence.NoResultException): Response {
    ex.printStackTrace()
    return Response.status(NOT_FOUND_404).entity(ErrorResponse(NOT_FOUND_404, ex.message!!)).type(MediaType.APPLICATION_JSON).build()
  }
}

@Provider
@Singleton
class BadRequest : ExceptionMapper<JsonMappingException> {
  override fun toResponse(ex: JsonMappingException): Response {
    ex.printStackTrace()
    val originalMessage = ex.originalMessage
    val message = originalMessage.substring(originalMessage.indexOf("problem: ") + "problem: ".length, originalMessage.indexOf(": method"))
    val parameter = originalMessage.subSequence(originalMessage.indexOf(", parameter"), originalMessage.length)
    return Response.status(BAD_REQUEST_400).entity(ErrorResponse(BAD_REQUEST_400, "${message}${parameter}")).type(MediaType.APPLICATION_JSON).build()
  }
}

@Provider
@Singleton
class InternalError : ExceptionMapper<Throwable> {
  override fun toResponse(ex: Throwable): Response {
    ex.printStackTrace()
    return Response.status(INTERNAL_SERVER_ERROR_500).entity(ErrorResponse(INTERNAL_SERVER_ERROR_500, "Error accured while processing request")).type(MediaType.APPLICATION_JSON).build()
  }
}
