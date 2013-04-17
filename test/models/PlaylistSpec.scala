package models

import org.specs2.mutable.Specification
import org.joda.time.{DateTimeUtils, DateTime, Duration}

/**
 * @author: mgibowski
 */
class PlaylistSpec extends Specification{

  val LONG_TIME_AGO = 90
  val CURRENT_MOMENT = 90909090
  val TRACK_LENGTH = 3000
  DateTimeUtils.setCurrentMillisFixed(CURRENT_MOMENT)

  "Adding track to an empty playlist" should {
    // Given
    implicit val store: PlaylistStore = new SimplePlaylistStore
    val emptyPlaylist = new Playlist()
    val track = Track(duration = new Duration(TRACK_LENGTH))
    // When
    emptyPlaylist.addTrack(track)
    // Then
    "make the playlist end at current moment + track duration" in {
      emptyPlaylist.endTime must beSome(new DateTime(CURRENT_MOMENT + TRACK_LENGTH))
    }
    "make the playlist have a track starting at current moment" in {
      emptyPlaylist.tracks.headOption.map(_.startTime) must beSome(new DateTime(CURRENT_MOMENT))
    }
  }

  "Adding track to non-empty playlist" should {
    // Given
    implicit val store: PlaylistStore = new SimplePlaylistStore
    val playlist = new Playlist()
    val track = Track(duration = new Duration(TRACK_LENGTH))
    playlist.addTrack(track)
    val endBefore = playlist.endTime.get
    // When
    playlist.addTrack(track)
    // Then
    "make the playlist end at current moment + track duration" in {
      playlist.endTime must beSome(endBefore.plus(TRACK_LENGTH))
    }
    "make the last track start at previous playlist end time" in {
      playlist.tracks.headOption.map(_.startTime) must beSome(endBefore)
    }
  }

  "Adding track to a finished playlist" should {
    // Given
    DateTimeUtils.setCurrentMillisFixed(LONG_TIME_AGO)
    implicit val store: PlaylistStore = new SimplePlaylistStore
    val playlist = new Playlist()
    val track = Track(duration = new Duration(TRACK_LENGTH))
    playlist.addTrack(track)
    DateTimeUtils.setCurrentMillisFixed(CURRENT_MOMENT)
    // When
    playlist.addTrack(track)
    // Then
    "make the playlist end at current moment + track duration" in {
      playlist.endTime must beSome(new DateTime(CURRENT_MOMENT + TRACK_LENGTH))
    }
    "make the playlist have a track starting at current moment" in {
      playlist.tracks.headOption.map(_.startTime) must beSome(new DateTime(CURRENT_MOMENT))
    }
  }

}
