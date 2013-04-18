package models

import org.specs2.mutable.Specification

import JsonImplicits._
import play.api.libs.json.Json
import org.joda.time.{DateTime, Duration}
import reactivemongo.bson.BSONObjectID

/**
 * @author: mgibowski
 */
class JsonSpec extends Specification{

  val trackStr =
    """
      |{
      | "soundCloudId": 18589213,
      | "title": "Ritual Union (Maya Jane Coles Remix)",
      | "soundCloudUsername": "Peacefrog Records",
      | "permalinkUrl" : "http://soundcloud.com/peacefrog-records/ritual-union-maya-jane-coles",
      | "artworkUrl": "http://i1.sndcdn.com/artworks-000009042687-dpsn6u-large.jpg?e48997d",
      | "duration" : 248989
      |}
    """.stripMargin

  "Track" should {
    "be read from Json" in {
      val track = Json.parse(trackStr).as[Track]
      track.soundCloudId must beEqualTo(18589213)
      track.title must beEqualTo("Ritual Union (Maya Jane Coles Remix)")
      track.permalinkUrl must beEqualTo("http://soundcloud.com/peacefrog-records/ritual-union-maya-jane-coles")
      track.artworkUrl must beEqualTo("http://i1.sndcdn.com/artworks-000009042687-dpsn6u-large.jpg?e48997d")
      track.duration must beEqualTo(new Duration(248989))
    }
  }

  "Playlist Item" should {
    "be written to Json" in {
      // Given
      val trackJson = Json.parse(trackStr)
      val playlistItem = new PlaylistItem(
        playlistId = BSONObjectID.generate,
        startTime = new DateTime(1231123123),
        status = None,
        track = trackJson.as[Track]
      )
      // When
      val json = Json.toJson(playlistItem)
      // Then
      (json \ "startTime").as[Long] must beEqualTo(1231123123)
      (json \ "track") must beEqualTo(trackJson)
    }
  }

}
