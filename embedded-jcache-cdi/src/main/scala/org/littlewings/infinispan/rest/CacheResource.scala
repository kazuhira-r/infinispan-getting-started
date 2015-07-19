package org.littlewings.infinispan.rest

import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, Path, Produces, QueryParam}

import org.littlewings.infinispan.service.MessageService

@Path("cache")
@RequestScoped
class CacheResource {
  @Inject
  private var messageService: MessageService = _

  @GET
  @Path("put")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Transactional
  def put(@QueryParam("key") key: String, @QueryParam("message") message: String): String = {
    messageService.putCache(key, message)
    s"Putted $key:$message."
  }

  @GET
  @Path("putFail")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Transactional
  def putFail(@QueryParam("key") key: String, @QueryParam("message") message: String): String = {
    messageService.putCache(key, message)
    throw new RuntimeException("Oops!!")
  }

  @GET
  @Path("get")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Transactional
  def get(@QueryParam("key") key: String): String =
    messageService.message(key)
}
