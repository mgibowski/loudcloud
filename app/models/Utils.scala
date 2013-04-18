package models

import play.api.libs.iteratee.{Enumerator, Input, Done}
import play.api.libs.json._
import org.joda.time.Duration
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

/**
 * @author: mgibowski
 */
object Utils{
  def closedSocketWithError(errorMsg: String) = {
    // A finished Iteratee sending EOF
    val iteratee = Done[JsValue, Unit]((), Input.EOF)
    // Send an error and close the socket
    val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(errorMsg)))).andThen(Enumerator.enumInput(Input.EOF))
    (iteratee, enumerator)
  }
}

object JsonImplicits {
  implicit val jodaDurationReads = new Reads[Duration]{
    def reads(json: JsValue) = JsSuccess(new Duration(json.as[Long]))
  }
  implicit val trackReads = Json.reads[Track]
}

