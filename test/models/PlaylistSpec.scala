package models

import org.specs2.mutable.Specification
import org.joda.time.{DateTimeUtils, DateTime}

import TrackFactory._
import scala.concurrent.{duration, Await}
import org.specs2.matcher.MatchResult

/**
 * @author: mgibowski
 */
class PlaylistSpec extends Specification{

  object Given{
    val longTimeAgo = 90
    val currentMoment = 90909090
    val playlistId = "516eecbb4fdc459e3c9b2c72"
    val track = createTrack()
    val atMostDuration = duration.Duration(200, duration.MILLISECONDS)
  }

  import Given._

  object Helpers{
    def endOf(p: Playlist)(implicit store: PlaylistStore) = Await.result(p.endTime, atMostDuration)
    def tracksFrom(p: Playlist)(implicit store: PlaylistStore) = Await.result(p.tracks, atMostDuration)
    def addTrack(p: Playlist, track: Track)(implicit store: PlaylistStore) = Await.result(p.addTrack(track), atMostDuration)
  }

  import Helpers._

  DateTimeUtils.setCurrentMillisFixed(currentMoment)

  def test(playlist: Playlist)(check: Playlist => MatchResult[_])(implicit store: PlaylistStore){
    addTrack(playlist, track)
    check.apply(playlist)
  }

  "Adding track to an empty playlist" should {
    def emptyPlaylist = new Playlist(playlistId)
    "make the playlist end at current moment + track duration" in {
      implicit val store = new SimplePlaylistStore
      test(emptyPlaylist){
        endOf(_) must beSome(new DateTime(currentMoment + defaultTrackLength))
      }
    }
    "make the playlist have a track starting at current moment" in {
      implicit val store = new SimplePlaylistStore
      test(emptyPlaylist){
        tracksFrom(_).headOption.map(_.startTime) must beSome(new DateTime(currentMoment))
      }
    }
  }

  "Adding track to non-empty playlist" should {
    def nonEmptyPlaylist(implicit store: PlaylistStore) = {val playlist = new Playlist(playlistId); addTrack(playlist, track); playlist}
    "make the playlist end at current moment + track duration" in {
      implicit val store = new SimplePlaylistStore
      val sut = nonEmptyPlaylist
      val previousEndTime = endOf(sut).get
      test(sut){
        endOf(_) must beSome(previousEndTime.plus(defaultTrackLength))
      }
    }
    "make the last track start at previous playlist end time" in {
      implicit val store = new SimplePlaylistStore
      val sut = nonEmptyPlaylist
      val previousEndTime = endOf(sut).get
      test(sut){
        tracksFrom(_).headOption.map(_.startTime) must beSome(previousEndTime)
      }
    }
  }

  "Adding track to a finished playlist" should {
    def finishedPlaylist(implicit store: PlaylistStore) = {
      DateTimeUtils.setCurrentMillisFixed(longTimeAgo)
      val playlist = new Playlist(playlistId)
      addTrack(playlist, track)
      DateTimeUtils.setCurrentMillisFixed(currentMoment)
      playlist
    }
    "make the playlist end at current moment + track duration" in {
      implicit val store = new SimplePlaylistStore
      test(finishedPlaylist){
        endOf(_) must beSome(new DateTime(currentMoment + defaultTrackLength))
      }
    }
    "make the playlist have a track starting at current moment" in {
      implicit val store = new SimplePlaylistStore
      test(finishedPlaylist){
        tracksFrom(_).headOption.map(_.startTime) must beSome(new DateTime(currentMoment))
      }
    }
  }

}
