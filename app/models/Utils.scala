package models

import play.api.libs.iteratee.{Enumerator, Input, Done}
import play.api.libs.json._
import org.joda.time.{DateTime, Duration}
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
  implicit val jodaDurationFormat = new Format[Duration]{
    def reads(json: JsValue) = JsSuccess(new Duration(json.as[Long]))
    def writes(o: Duration) = JsNumber(o.getMillis)
  }
  implicit val jodaDateTimeFormat = new Format[DateTime]{
    def reads(json: JsValue) = JsSuccess(new DateTime(json.as[Long]))
    def writes(o: DateTime) = JsNumber(o.getMillis())
  }
  implicit val trackFormat = Json.format[Track]
  implicit val playListItemWriter = new Writes[PlaylistItem] {
    def writes(o: PlaylistItem) = Json.obj(
    "startTime" -> Json.toJson(o.startTime),
    "track" -> Json.toJson(o.track)
    )
  }

}

