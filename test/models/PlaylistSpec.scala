package models

import org.specs2.mutable.Specification
import org.joda.time.{DateTimeUtils, DateTime}

import TrackFactory._
import scala.concurrent.{duration, Await}
import org.specs2.matcher.MatchResult
import reactivemongo.bson.BSONObjectID
import PlaylistItemStatus._


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
    def endOf(p: Playlist) = Await.result(p.endTime, atMostDuration)
    def tracksFrom(p: Playlist) = Await.result(p.tracks, atMostDuration)
    def addTrack(p: Playlist, track: Track) = Await.result(p.addTrack(track), atMostDuration)
    def emptyPlaylist = new Playlist(playlistId)(new SimplePlaylistStore)
  }

  import Helpers._

  DateTimeUtils.setCurrentMillisFixed(currentMoment)

  def testAddingTrackTo(playlist: Playlist)(check: Playlist => MatchResult[_]){
    addTrack(playlist, track)
    check.apply(playlist)
  }

  "Adding track to an empty playlist" should {
    "make the playlist end at current moment + track duration" in {
      testAddingTrackTo(emptyPlaylist){
        endOf(_) must beSome(new DateTime(currentMoment + defaultTrackLength))
      }
    }
    "make the playlist have a track starting at current moment" in {
      testAddingTrackTo(emptyPlaylist){
        tracksFrom(_).headOption.map(_.startTime) must beSome(new DateTime(currentMoment))
      }
    }
  }

  "Adding track to non-empty playlist" should {
    def nonEmptyPlaylist = {val playlist = emptyPlaylist; addTrack(playlist, track); playlist}
    "make the playlist end at current moment + track duration" in {
      val sut = nonEmptyPlaylist
      val previousEndTime = endOf(sut).get
      testAddingTrackTo(sut){
        endOf(_) must beSome(previousEndTime.plus(defaultTrackLength))
      }
    }
    "make the last track start at previous playlist end time" in {
      val sut = nonEmptyPlaylist
      val previousEndTime = endOf(sut).get
      testAddingTrackTo(sut){
        tracksFrom(_).headOption.map(_.startTime) must beSome(previousEndTime)
      }
    }
  }

  "Adding track to a finished playlist" should {
    def finishedPlaylist = {
      DateTimeUtils.setCurrentMillisFixed(longTimeAgo)
      val playlist = emptyPlaylist
      addTrack(playlist, track)
      DateTimeUtils.setCurrentMillisFixed(currentMoment)
      playlist
    }
    "make the playlist end at current moment + track duration" in {
      testAddingTrackTo(finishedPlaylist){
        endOf(_) must beSome(new DateTime(currentMoment + defaultTrackLength))
      }
    }
    "make the playlist have a track starting at current moment" in {
      testAddingTrackTo(finishedPlaylist){
        tracksFrom(_).headOption.map(_.startTime) must beSome(new DateTime(currentMoment))
      }
    }
  }

  "Playlist Item Status" should {
    def createItemsStartingAfter(start: Int) = {
      def item(num: Int) = new PlaylistItem(BSONObjectID(playlistId), new DateTime(start + num * defaultTrackLength), None, track)
      Seq(item(1),item(2),item(3),item(4)).reverse
    }
    "be PLAYED for all items with end time < now " in {
      // Given
      val items = createItemsStartingAfter(longTimeAgo)
      // When
      val result = Playlist.withStatuses(items)
      // Then
      forall(result)(_.status must beSome(PLAYED))
    }
    "be QUEUED for all items with start time > now except the oldest" in {
      // Given
      val items = createItemsStartingAfter(currentMoment)
      // When
      val result = Playlist.withStatuses(items)
      // Then
      forall(result.dropRight(1))(_.status must beSome(QUEUED))
      result.last.status must beSome(PLAYING)
    }
  }

}
