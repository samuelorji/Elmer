package com.africasTalking.elmer.order
package callback

import scala.concurrent.Future

import akka.actor.Props
import akka.http.scaladsl.model._

import spray.json._

import io.atlabs._

import horus.core.http.client._
import horus.core.util.ATUtil

import com.africasTalking._

import elmer.core.util.ElmerEnum._

import elmer.order.test._

import ClientCallbackServiceMarshalling._
import ClientCallbackService._

class ClientCallbackServiceSpec extends TestHttpStringEndpointT
    with ClientCallbackServiceJsonSupportT {

  val callbackService = system.actorOf(Props(new ClientCallbackService {
    override def sendHttpRequest(req: HttpRequest) =
      Future.successful(getStringHttpResponse(req))
  }))

  var clientRequest: Option[ClientFoodOrderStatusRequest] = None

  "The ClientCallbackService" must {
    "publish a status request correctly" in {
      val callbackUrl = ATUtil.parseUrl("http://www.testcallback.com").get
      callbackService ! ClientCallbackStatusServiceRequest(
        transactionId = "SomeTxnId",
        status        = FoodOrderStatus.Delivered,
        description   = "Food has been delivered",
        callbackUrl   = callbackUrl
      )

      Thread.sleep(1000)

      clientRequest should be (Some(ClientFoodOrderStatusRequest(
        transactionId = "SomeTxnId",
        status        = FoodOrderStatus.Delivered,
        description   = "Food has been delivered"
      )))
    }
  }

  def getStringHttpResponseImpl(
    data: String,
    uri: Uri
  ) = {
    clientRequest = Some(data.parseJson.convertTo[ClientFoodOrderStatusRequest])
    ATHttpClientResponse(
      StatusCodes.OK,
      "OK"
    )
  }

}
