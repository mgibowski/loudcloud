package models

import org.specs2.mutable.Specification

import JsonImplicits._
import play.api.libs.json.Json
import org.joda.time.Duration

/**
 * @author: mgibowski
 */
class JsonSpec extends Specification{

  val str =
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
      val track = Json.parse(str).as[Track]
      track.soundCloudId must beEqualTo(18589213)
      track.title must beEqualTo("Ritual Union (Maya Jane Coles Remix)")
      track.permalinkUrl must beEqualTo("http://soundcloud.com/peacefrog-records/ritual-union-maya-jane-coles")
      track.artworkUrl must beEqualTo("http://i1.sndcdn.com/artworks-000009042687-dpsn6u-large.jpg?e48997d")
      track.duration must beEqualTo(new Duration(248989))
    }
  }

}
