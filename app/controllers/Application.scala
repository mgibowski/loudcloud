package controllers

import play.api._
import mvc._
import Play._
import http.ContentTypes

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index())
  }

  def room = Action {
    Ok(views.html.room())
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