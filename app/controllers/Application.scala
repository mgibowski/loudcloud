package controllers

import play.api._
import mvc._
import Play._
import http.ContentTypes
import play.api.libs.json._
import models._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  
  def index = Action { implicit request: RequestHeader =>
    Ok(views.html.index())
  }

  def createRoom = Action {
    val roomId = RoomStore.createNewRoom()
    Ok(Json.obj("roomId" -> JsString(roomId))).as("application/json; charset=utf-8")
  }

  def enterRoom(id: String) = Action {
    Async {
      for {
        playlistItems <- MongoPlaylistStore.findItems(id).map(Playlist.withStatuses _)
        room = RoomStore.findRoomBy(id)
      } yield {
        (room.isDefined, !playlistItems.isEmpty) match {
          case (false, false) => NotFound(views.html.notFound())
          case (false, true) => { // room exists, but actor is dead
            RoomStore.createRoomWith(id)
            Ok(views.html.room(playlistItems))
          }
          case (_, _) => Ok(views.html.room(playlistItems))
        }
      }
    }
  }

  /**
   * Creates WebSocket for the user
   */
  def roomWS(roomId: String) = WebSocket.async[JsValue] { request  =>
    RoomStore.findRoomBy(roomId) match {
      case Some(room) => Room.join(room)
      case None => Future(Utils.closedSocketWithError("Room not found"))
    }
  }

  def soundCloudClientId = Action {
    current.configuration.getString("sc.client_id") match {
      case Some(clientId) => Ok("(function(){define([],function(){var e;return e=\"%s\"})}).call(this)".format(clientId)).as(ContentTypes.JAVASCRIPT)
      case None => {
        val message = "No SoundCloud client id found! Configure it in secrets.conf"
        Logger.error(message)
        NotFound(message)
      }
    }
  }
  
}