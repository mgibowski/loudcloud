package models

import org.joda.time.{DateTime, Duration}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * @author: mgibowski
 */
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
  def endTime = store.findItems(id).map(_.headOption.map(_.endTime))
}

case class Track(soundCloudId: Long, title: String, soundCloudUsername: String, permalinkUrl: String, artworkUrl: String, duration: Duration)
case class PlaylistItem(playlistId: BSONObjectID, startTime: DateTime, track: Track){
  def endTime = startTime.plus(track.duration)
}
object PlaylistItem{
  def apply(playlistId: String, startTime: DateTime, track: Track): PlaylistItem =
    new PlaylistItem(BSONObjectID(playlistId), startTime, track)
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