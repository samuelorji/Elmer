package com.africasTalking.elmer.web
package service

import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server._
import directives._
import Directives._
import AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }

import com.africasTalking._

import elmer.core.db.mysql.cache.AuthenticationDbCache

private[service] trait UserAuthenticationDirectiveT {

  private val challenge = HttpChallenges.basic("api")

  def authenticateUser(username: String): AuthenticationDirective[Int] =
    optionalHeaderValueByName("apikey").flatMap(apikey =>
      apikey match {
      case None    => reject(AuthenticationFailedRejection(CredentialsMissing, challenge)): Directive1[Int]
      case Some(x) =>
        AuthenticationDbCache.authenticate(
          username = username,
          apikey   = x
        ) match {
          case None       => reject(AuthenticationFailedRejection(CredentialsRejected, challenge)): Directive1[Int]
          case Some(user) => provide(user.id)
        }
      }
    )
}
