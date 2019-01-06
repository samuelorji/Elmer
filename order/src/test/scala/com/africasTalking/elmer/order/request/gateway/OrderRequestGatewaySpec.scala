package com.africasTalking.elmer.order
package request.gateway

import scala.concurrent.Future

import akka.actor.Props
import akka.http.scaladsl.model._

import spray.json._

import io.atlabs._

import horus.core.http.client._

import com.africasTalking._

import elmer.core.util.ElmerEnum._

import elmer.order.test._

import OrderRequestGateway._
import OrderRequestGatewayMarshalling._

class OrderRequestGatewaySpec extends TestHttpStringEndpointT
    with  OrderRequestGatewayJsonSupportT {

  val gateway = system.actorOf(Props(new OrderRequestGateway {
    override def sendHttpRequest(req: HttpRequest) =
      Future.successful(getStringHttpResponse(req))
  }))

  "The OrderRequestGateway" must {
    "process a valid JSON response from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        name     = FoodName.Ugali,
        quantity = 3
      )
      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Accepted,
        description = "Order accepted for processing"
      ))
    }
    "process a valid status code with invalid JSON from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        name     = FoodName.Rice,
        quantity = 3
      )
      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Failed,
        description = "Internal error while sending request to the gateway"
      ))
    }
    "process an invalid valid status code from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        name     = FoodName.BeefStew,
        quantity = 3
      )
      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Failed,
        description = "Internal error while sending request to the gateway"
      ))
    }

  }

  def getStringHttpResponseImpl(
    data: String,
    uri: Uri
  ) = {
    val request = data.parseJson.convertTo[EtherFoodOrderRequest]
    request.name match {
      case FoodName.Ugali =>
        ATHttpClientResponse(
          StatusCodes.OK,
          EtherFoodOrderResponse(
            status      = FoodOrderStatus.Accepted,
            description = "Order accepted for processing"
          ).toJson.compactPrint
        )
      case FoodName.Rice =>
        ATHttpClientResponse(
          StatusCodes.OK,
          "Invalid JSON"
        )
      case _ =>
        ATHttpClientResponse(
          StatusCodes.BadRequest,
          "Invalid request"
        )
    }
  }
}
