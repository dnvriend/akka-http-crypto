/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ ActorMaterializer, Materializer }
import com.github.dnvriend.SecurityService.Crypto
import org.apache.shiro.codec.{ Base64, CodecSupport }
import org.apache.shiro.crypto.AesCipherService
import org.apache.shiro.util.ByteSource
import org.mindrot.jbcrypt.BCrypt
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ ExecutionContext, Future }

object SecurityService {

  // an enumeration
  object Crypto extends Enumeration {
    type Crypto = Value
    val AES = Value("AES")
    val BCRYPT = Value("BCRYPT")
    val UNKNOWN = Value("UNKNOWN")
  }

  // factory method
  def apply()(implicit ec: ExecutionContext, log: LoggingAdapter): SecurityService =
    new SecurityServiceImpl
}

trait SecurityService {
  def decryptAes(base64Encrypted: String): Future[DecryptResponse]

  def encryptAes(plainText: String): Future[EncryptResponse]

  def hashBcrypt(plainText: String): Future[EncryptResponse]

  def validateBcrypt(candidate: String, hashed: String): Future[DecryptResponse]
}

class SecurityServiceImpl(implicit ec: ExecutionContext, log: LoggingAdapter) extends SecurityService {

  object AES {
    val passPhrase = "j68KkRjq21ykRGAQ"
    val cipher = new AesCipherService
  }

  override def encryptAes(plainText: String): Future[EncryptResponse] = Future {
    val result = AES.cipher.encrypt(plainText.getBytes, AES.passPhrase.getBytes).toBase64
    log.debug(s"[EncryptAES]: plainText: $plainText, result: $result")
    EncryptResponse(Crypto.AES.toString, result)
  }

  override def decryptAes(base64Encrypted: String): Future[DecryptResponse] = Future {
    val byteSource: ByteSource = ByteSource.Util.bytes(base64Encrypted)
    val decryptedToken = AES.cipher.decrypt(Base64.decode(byteSource.getBytes), AES.passPhrase.getBytes)
    val result = CodecSupport.toString(decryptedToken.getBytes)
    log.debug(s"[DecryptAES]: base64Encrypted: $base64Encrypted")
    DecryptResponse(Crypto.AES.toString, result)
  }

  override def hashBcrypt(plainText: String): Future[EncryptResponse] = Future {
    val hashed = BCrypt.hashpw(plainText, BCrypt.gensalt(15))
    log.debug(s"[HashBCrypt]: plainText: $plainText")
    EncryptResponse(Crypto.BCRYPT.toString, hashed)
  }

  override def validateBcrypt(candidate: String, hashed: String): Future[DecryptResponse] = Future {
    if (BCrypt.checkpw(candidate, hashed)) {
      log.debug(s"[ValidateBCrypt]: candidate: $candidate, hashed: $hashed, result: Valid")
      DecryptResponse(Crypto.BCRYPT.toString, "Valid")
    } else {
      log.debug(s"[ValidateBCrypt]: candidate: $candidate, hashed: $hashed, result: Invalid")
      DecryptResponse(Crypto.BCRYPT.toString, "Invalid")
    }
  }
}

case class EncryptResponse(crypto: String, response: String)

case class DecryptResponse(crypto: String, response: String)

object Main extends App with DefaultJsonProtocol with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, this.getClass)

  // the security service encrypts and decrypts using AES
  val securityService = SecurityService()

  implicit val encryptResponseFormat = jsonFormat2(EncryptResponse)
  implicit val decryptResponseFormat = jsonFormat2(DecryptResponse)

  val routes: Route =
    pathPrefix("crypto") {
      pathPrefix("aes") {
        path("encrypt") {
          post {
            parameter('text.as[String]) { text ⇒
              complete(securityService.encryptAes(text))
            }
          }
        } ~
          path("decrypt") {
            post {
              parameter('encrypted.as[String]) { encrypted ⇒
                complete(securityService.decryptAes(encrypted))
              }
            }
          }
      } ~
        pathPrefix("bcrypt") {
          path("hash") {
            post {
              parameter('text.as[String]) { text ⇒
                complete(securityService.hashBcrypt(text))
              }
            }
          } ~
            path("validate") {
              post {
                parameter('candidate.as[String], 'hash.as[String]) { (candidate: String, hash: String) ⇒
                  complete(securityService.validateBcrypt(candidate, hash))
                }
              }
            }
        }
    }

  // launch the akka-http service
  Http().bindAndHandle(routes, "0.0.0.0", 8080)
}
