package models

import org.joda.time.{DateTime, Duration}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scala.Some

/**
 * @author: mgibowski
 */
object PlaylistItemStatus extends Enumeration{
  type PlaylistItemStatus = Value
  val PLAYED, QUEUED, PLAYING = Value
}
import PlaylistItemStatus._

case class Playlist(id: String)(store: PlaylistStore){
  def addTrack(track: Track) = {
    for {
      maybeEnd <- endTime
      now = DateTime.now()
      trackStart = maybeEnd match {
        case None => now
        case Some(end) if (end.isBefore(now)) => now
        case Some(end) => end
      }
      ok <- store.addItem(PlaylistItem(this.id, trackStart, track))
    } yield ok
  }
  def tracks = store.findItems(id)
  def lastAdded = store.findItems(id).map(_.headOption)
  def endTime = store.findItems(id).map(_.headOption.map(_.endTime))
}

object Playlist{
  def withStatuses(items: Seq[PlaylistItem]) = {
    val playedOrQueued = items.map(_.withStatus)
    val playingIdx = playedOrQueued.lastIndexWhere(_.status == Some(QUEUED))
    if (playingIdx > -1){
      val playing = playedOrQueued(playingIdx).copy(status = Some(PLAYING))
      playedOrQueued.patch(playingIdx, Seq(playing), 1)
    } else
      playedOrQueued
  }
}

case class Track(soundCloudId: Long, title: String, soundCloudUsername: String, soundCloudUsernameUrl: String, permalinkUrl: String, artworkUrl: String, duration: Duration)
case class PlaylistItem(playlistId: BSONObjectID, startTime: DateTime, status: Option[PlaylistItemStatus], track: Track){
  def endTime = startTime.plus(track.duration)
  def withStatus = {
    val newStatus = if (endTime.isBeforeNow) Some(PLAYED) else Some(QUEUED)
    this.copy(status = newStatus)
  }
}
object PlaylistItem{
  def apply(playlistId: String, startTime: DateTime, track: Track): PlaylistItem =
    new PlaylistItem(BSONObjectID(playlistId), startTime, None, track)
}

trait PlaylistStore{
  def addItem(item: PlaylistItem): Future[Boolean]
  def findItems(playlistId: String): Future[Seq[PlaylistItem]]
}

class SimplePlaylistStore extends PlaylistStore{
  var items = Seq[PlaylistItem]()
  def addItem(item: PlaylistItem) = { items +:= item; Future(true) }
  def findItems(playlistId: String) = Future(items)
}