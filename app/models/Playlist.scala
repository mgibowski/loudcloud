package models

import org.joda.time.{DateTime, Duration}

/**
 * @author: mgibowski
 */
class Playlist{
  def addTrack(track: Track)(implicit store: PlaylistStore) {
    val now = DateTime.now()
    val trackStart = this.endTime match {
      case None => now
      case Some(end) if(end.isBefore(now)) => now
      case Some(end) => end
    }
    store.addItem(PlaylistItem(trackStart, track.duration))
  }
  def tracks(implicit store: PlaylistStore) = store.getItems
  def endTime(implicit store: PlaylistStore) = store.getItems.headOption.map(_.endTime)
}

case class Track(duration: Duration)
case class PlaylistItem(startTime: DateTime, duration: Duration){
  def endTime = startTime.plus(duration)
}

trait PlaylistStore{
  def addItem(item: PlaylistItem)
  def getItems: Seq[PlaylistItem]
}

class SimplePlaylistStore extends PlaylistStore{
  var items = Seq[PlaylistItem]()
  def addItem(item: PlaylistItem) { items +:= item }
  def getItems = items
}