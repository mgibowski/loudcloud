package models

import akka.actor._
import scala.concurrent.duration._

import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random

object RoomStore{
  val rooms = scala.collection.concurrent.TrieMap[String, ActorRef]()

  def generateId() = Random.nextInt().toString

  def createRoom() = {
    val roomId = generateId()
    val room = Akka.system.actorOf(Props[Room])
    rooms.put(roomId, room)
    roomId
  }

  def findRoomBy(roomId: String) = rooms.get(roomId)
}

object Room {
  implicit val timeout = Timeout(1 second)

  def join(room: ActorRef): scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (room ? Join("some user")).map {
      case Connected(enumerator) =>
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          room ! SendTrack("some user", event)
        }.mapDone { _ =>
          room ! Quit("some user")
        }
        (iteratee,enumerator)
      case CannotConnect(error) => Utils.closedSocketWithError(error)
    }
  }
}

class Room extends Actor {

  var members = Set.empty[String]
  val (roomEnumerator, roomChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case Join(username) => {
      members = members + username
      sender ! Connected(roomEnumerator)
      self ! NotifyJoin(username)
    }

    case NotifyJoin(username) => {
      notifyAll(username, Json.obj("msg" -> s"$username has entered the room"))
    }

    case SendTrack(username, track) => {
      notifyAll(username, track)
    }

    case Quit(username) => {
      members = members - username
      notifyAll(username, Json.obj("msg" -> s"$username has left the room"))
    }

  }

  def notifyAll(user: String, msg: JsValue) { roomChannel.push(msg) }

}

case class Join(username: String)
case class Quit(username: String)
case class SendTrack(username: String, track: JsValue)
case class NotifyJoin(username: String)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)

object Utils{
  def closedSocketWithError(errorMsg: String) = {
    // A finished Iteratee sending EOF
    val iteratee = Done[JsValue, Unit]((), Input.EOF)
    // Send an error and close the socket
    val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(errorMsg)))).andThen(Enumerator.enumInput(Input.EOF))
    (iteratee, enumerator)
  }
}
