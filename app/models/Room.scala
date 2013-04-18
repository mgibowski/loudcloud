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
import reactivemongo.bson.BSONObjectID

import JsonImplicits._

object RoomStore{
  val rooms = scala.collection.concurrent.TrieMap[String, ActorRef]()

  def createNewRoom() = {
    val roomId = BSONObjectID.generate.stringify
    createRoomWith(roomId)
    roomId
  }

  def createRoomWith(roomId: String) {
    val props = Props(new Room(roomId, new Playlist(roomId)(MongoPlaylistStore)))
    val room = Akka.system.actorOf(props)
    rooms.put(roomId, room)
  }

  def findRoomBy(roomId: String) = rooms.get(roomId)
}

object Room {
  implicit val timeout = Timeout(1 second)

  def join(room: ActorRef): scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (room ? Join).map {
      case Connected(enumerator) =>
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          room ! SendTrack(event)
        }.mapDone { _ =>
          room ! Quit
        }
        (iteratee,enumerator)
      case CannotConnect(error) => Utils.closedSocketWithError(error)
    }
  }
}

class Room(id: String, playlist: Playlist) extends Actor {

  var membersCount = 0
  val (roomEnumerator, roomChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case Join => {
      membersCount+=1
      sender ! Connected(roomEnumerator)
      self ! NotifyJoin
    }

    case NotifyJoin => {
      notifyAll(Json.obj("membersCount" -> membersCount))
    }

    case SendTrack(trackJson) => {
      val track = trackJson.as[Track]
      for {
        ok <- playlist.addTrack(track)
        if ok
        maybeLastItem <- playlist.lastAdded
        lastItem <- maybeLastItem
      } {
        val item = Json.toJson(lastItem)
        notifyAll(item)
      }
    }

    case Quit => {
      membersCount-=1
      notifyAll(Json.obj("membersCount" -> membersCount))
    }

  }

  def notifyAll(msg: JsValue) { roomChannel.push(msg) }

}

object Join
object Quit
case class SendTrack(track: JsValue)
object NotifyJoin

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
