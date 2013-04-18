package models

/**
 * @author: mgibowski
 */
import play.api.mvc._
import org.joda.time.{Duration, DateTime}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import play.api.Logger

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

// Reactive Mongo plugin
import play.modules.reactivemongo._


import play.api.Play.current

object MongoPlaylistStore extends Controller with MongoController with PlaylistStore{
  val db = ReactiveMongoPlugin.db
  lazy val playListItems = db("PlaylistItems")

  implicit val reader = PlaylistItemBSONReader
  implicit val writer = PlaylistItemBSONWriter

  def findItems(playlistId: String) = {
    val query = QueryBuilder().query(BSONDocument(("playlistId" -> BSONObjectID(playlistId)))).sort(("startTime", SortOrder.Descending))
    playListItems.find[PlaylistItem](query).toList()
  }

  def addItem(item: PlaylistItem) = playListItems.insert[PlaylistItem](item).map{ lastError =>
    if (lastError.inError) {
      Logger.error("Problem with storing track in MongoDB", lastError.getCause)
    }
    lastError.ok
  }

}

object PlaylistItemBSONReader extends BSONReader[PlaylistItem]{
  def fromBSON(doc: BSONDocument): PlaylistItem = {
    val d = doc.toTraversable
    val playlistId = d.getAs[BSONObjectID]("playlistId").get.stringify
    val startTime = new DateTime(d.getAs[BSONDateTime]("startTime").get.value)
    val soundCloudId = d.getAs[BSONLong]("soundCloudId").get.toLong
    val soundCloudUsername = d.getAs[BSONString]("soundCloudUsername").get.value
    val soundCloudUsernameUrl = d.getAs[BSONString]("soundCloudUsernameUrl").get.value
    val title = d.getAs[BSONString]("title").get.value
    val permalinkUrl = d.getAs[BSONString]("permalinkUrl").get.value
    val artworkUrl = d.getAs[BSONString]("artworkUrl").get.value
    val durationMillis = d.getAs[BSONLong]("duration").get.toLong
    val track = Track(soundCloudId, title, soundCloudUsername, soundCloudUsernameUrl, permalinkUrl, artworkUrl, new Duration(durationMillis))
    PlaylistItem(playlistId, startTime, track)
  }
}

object PlaylistItemBSONWriter extends BSONWriter[PlaylistItem] {
  def toBSON(i: PlaylistItem) = BSONDocument(
    "playlistId" -> i.playlistId,
    "startTime" -> BSONDateTime(i.startTime.getMillis),
    "soundCloudId" -> BSONLong(i.track.soundCloudId),
    "soundCloudUsername" -> BSONString(i.track.soundCloudUsername),
    "soundCloudUsernameUrl" -> BSONString(i.track.soundCloudUsernameUrl),
    "title" -> BSONString(i.track.title),
    "permalinkUrl" -> BSONString(i.track.permalinkUrl),
    "artworkUrl" -> BSONString(i.track.artworkUrl),
    "duration" -> BSONLong(i.track.duration.getMillis)
  )
}